#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8099}"
COURIER_ID="${COURIER_ID:-1}"
PING_DELAY="${PING_DELAY:-0.6}"

API="$BASE_URL/api/v1/couriers/$COURIER_ID"

# All coordinates sit due north of Ataşehir MMM Migros on the same meridian, which keeps the
# distances easy to read: 0.001 degrees of latitude is roughly 111 metres.
STORE_LNG=29.1244229
FAR=40.9968      # ~497 m from the store
OUTSIDE=40.9935  # ~130 m, just beyond the 100 m radius
INSIDE=40.9927   # ~41 m, inside the radius
AWAY=41.9000     # ~101 km, unreachable in ten seconds

format_json() {
  if command -v python3 >/dev/null 2>&1; then
    python3 -m json.tool
  else
    cat
  fi
}

section() {
  printf '\n%s\n' "$1"
  printf '%s\n' "------------------------------------------------------------------"
}

ping() {
  local lat=$1 recorded_at=$2 note=$3 response accepted

  response=$(curl -sS -X POST "$API/locations" \
    -H 'Content-Type: application/json' \
    -d "{\"lat\":$lat,\"lng\":$STORE_LNG,\"recordedAt\":\"$recorded_at\"}")

  accepted=$(printf '%s' "$response" | grep -o '"accepted":[a-z]*' | cut -d: -f2 || true)

  if [ -z "$accepted" ]; then
    printf '  ping   %s   lat=%s   %-34s [refused]\n' "${recorded_at:11:8}" "$lat" "$note"
    printf '\n  The application would not accept this ping:\n'
    printf '%s' "$response" | format_json | sed 's/^/    /'
    printf '\n  The demo cannot continue from here.\n'
    exit 1
  fi

  printf '  ping   %s   lat=%s   %-34s %s\n' \
    "${recorded_at:11:8}" "$lat" "$note" "[accepted=$accepted]"

  sleep "$PING_DELAY"
}

check() {
  local title=$1 expectation=$2 path=$3

  printf '\n    >> %s\n' "$title"
  printf '       expected: %s\n' "$expectation"
  curl -sS "$API/$path" | format_json | sed 's/^/       /'
  printf '\n'
}

require_application() {
  if ! curl -sS --max-time 3 "$BASE_URL/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; then
    echo "No application answering at $BASE_URL." >&2
    echo >&2
    echo "Start it with the demo profile, then run this script again:" >&2
    echo "  ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo" >&2
    exit 1
  fi
}

require_clean_state() {
  local travelled
  travelled=$(curl -sS "$API/total-distance" | grep -o '"totalDistanceMeters":[0-9.E-]*' | cut -d: -f2 || true)

  if [ -z "$travelled" ]; then
    echo "Could not read the travelled distance for courier $COURIER_ID from $BASE_URL." >&2
    echo "The seeded couriers are 1, 2 and 3." >&2
    exit 1
  fi

  if [ "${travelled%%.*}" != "0" ]; then
    echo "Courier $COURIER_ID has already travelled $travelled m, so this run would not match" >&2
    echo "the expected values below. Restart the application to reset the demo database:" >&2
    echo "  ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo" >&2
    exit 1
  fi
}

require_application
require_clean_state

cat <<EOF
Courier tracking demo
  store            Ataşehir MMM Migros, 100 m radius, 60 s re-entry cooldown
  courier          $COURIER_ID
  ping lines       what the courier sent
  >> blocks        what the application reports back
EOF

section "1. The courier approaches the store"
ping $FAR     "2026-01-01T10:00:00Z" "497 m away, first ping"
ping $OUTSIDE "2026-01-01T10:02:00Z" "130 m away, still outside"

check "Distance so far" \
      "about 367 m, the first ping has nothing to measure against" \
      "total-distance"

section "2. The courier crosses into the radius"
ping $INSIDE "2026-01-01T10:03:00Z" "41 m away, inside"
ping $INSIDE "2026-01-01T10:03:15Z" "still inside, not moving"

check "Store entrances" \
      "one entrance at 10:03:00; staying inside is not a second one" \
      "store-entrances"

section "3. The courier leaves and comes back within the cooldown"
ping $OUTSIDE "2026-01-01T10:03:30Z" "back outside"
ping $INSIDE  "2026-01-01T10:03:50Z" "returns 50 s after entering"

check "Store entrances" \
      "still one; the cooldown runs from the entrance at 10:03:00" \
      "store-entrances"

section "4. The courier leaves and comes back after the cooldown"
ping $OUTSIDE "2026-01-01T10:04:20Z" "outside again"
ping $INSIDE  "2026-01-01T10:04:40Z" "returns 100 s after entering"

check "Store entrances" \
      "two entrances now, at 10:03:00 and 10:04:40" \
      "store-entrances"

section "5. A ping that could not have happened"
ping $AWAY "2026-01-01T10:04:50Z" "101 km away, 10 s later"

check "Distance travelled" \
      "about 812 m, unchanged: the impossible ping was rejected" \
      "total-distance"
