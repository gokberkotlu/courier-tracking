# Courier Tracking

A Spring Boot service that consumes a stream of courier geolocations. For each ping it decides
whether the courier has just entered the 100 metre circle around a Migros store, and it keeps a
running total of the distance that courier has travelled.

Store coordinates are loaded from `stores.json` on startup. Three couriers are seeded from
`couriers.csv` so there is somebody to send pings for.

## Running it

You need JDK 25. The Maven wrapper is in the repository, so nothing else has to be installed.

```
./mvnw spring-boot:run
```

The service listens on port 8099. Every endpoint is documented and callable from
[Swagger UI](http://localhost:8099/swagger-ui.html), and the database is browsable at
[the H2 console](http://localhost:8099/h2-console) (JDBC URL `jdbc:h2:file:./data/courier-tracking`,
user `sa`, password `password`).

## The demo

`demo.sh` walks a courier past a store and prints, at each interesting moment, what the
application reports back. It covers the entrance, the dwell that must not count as a second
entrance, a return inside the cooldown, a return after it, and a ping that implies an impossible
speed.

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
./demo.sh
```

The `demo` profile swaps the file-backed database for an in-memory one. The scenario replays a
fixed set of timestamps, and a courier location is unique per `(courier, timestamp)`, so a second
run against a database that still holds the first one would be rejected. Restarting the
application gives the demo a clean slate. Run without that profile and the data survives
restarts, which is what the section on restarts below relies on.

Lines starting with `ping` are what the courier sent. Blocks starting with `>>` are what came
back out of the application, each with the value to expect next to it.

## The API

| Method | Path | |
| --- | --- | --- |
| `POST` | `/api/v1/couriers/{courierId}/locations` | record one ping |
| `GET` | `/api/v1/couriers/{courierId}/total-distance` | metres travelled so far |
| `GET` | `/api/v1/couriers/{courierId}/store-entrances` | entrances logged for this courier |

```
curl -X POST http://localhost:8099/api/v1/couriers/1/locations \
  -H 'Content-Type: application/json' \
  -d '{"lat":40.9927,"lng":29.1244229,"recordedAt":"2026-01-01T10:03:00Z"}'
```

The response says whether the ping was accepted, the courier's new total distance, and any
entrance the ping triggered. Errors come back as RFC 7807 problem details: `404` for an unknown
courier, `409` for a ping whose timestamp was already recorded, and `400` for a malformed body,
with the offending fields named.

## How an entrance is decided

An entrance is the moment a courier crosses from outside the radius to inside it. A courier who
stays inside and keeps sending pings produces one entrance, not one per ping.

On top of that, a re-entry is ignored if it happens within `reentry-cooldown` of the last
recorded entrance. This is what keeps GPS jitter at the boundary from logging a dozen visits to
the same store. Note that the window runs from the previous *entrance*, not from the moment the
courier left.

Each detected entrance is written to the `store_entrance` table and logged:

```
Courier 1 entered Ataşehir MMM Migros at 2026-01-01T10:03:00Z (41 metres from the store)
```

## Pings that cannot be trusted

Two filters sit in front of the distance total.

`max-speed-kmh` rejects a ping when the implied speed from the previous position is impossible for
a courier. Such a ping is still stored, flagged as an outlier, but it does not move the courier:
the total distance and the inside/outside state are both left alone. A ping that arrives out of
order is treated the same way, since a negative interval describes no usable movement.

A ping that repeats a timestamp already recorded for that courier is a different case. It is
refused with `409` and nothing is written at all, because `(courier, timestamp)` is unique and a
resend is more likely to be a duplicate delivery than new information.

`min-movement-meters` drops movement below ten metres from the distance total. Consumer GPS
wanders by a few metres while a courier stands still, and without a floor that wander accumulates
into kilometres over a shift. The cost is that genuine slow movement under ten metres between two
pings is not counted; for a distance figure meant to summarise a courier's day that trade is
worth making.

## Surviving a restart

Distance and entrances would both be wrong after a restart if the service simply started over.
The running state is held in memory for speed, but it is rebuilt from the database the first time
a courier is seen again: the last known position and distance total come from `courier_state`,
the last entrance per store from `store_entrance`, and the inside/outside flags are recomputed by
measuring the last known position against every store.

That last part matters. Without it, a courier who was standing inside a store when the service
stopped would look like a fresh outside-to-inside transition on the next ping and be logged as
entering a store they had never left.

To see it:

```
./mvnw spring-boot:run

curl -X POST http://localhost:8099/api/v1/couriers/1/locations -H 'Content-Type: application/json' \
  -d '{"lat":40.9935,"lng":29.1244229,"recordedAt":"2026-01-01T10:00:00Z"}'
curl -X POST http://localhost:8099/api/v1/couriers/1/locations -H 'Content-Type: application/json' \
  -d '{"lat":40.9927,"lng":29.1244229,"recordedAt":"2026-01-01T10:03:00Z"}'
```

The second ping reports an entrance. Stop the application, start it again, and send a third ping
from a point that is still inside the radius:

```
curl -X POST http://localhost:8099/api/v1/couriers/1/locations -H 'Content-Type: application/json' \
  -d '{"lat":40.9930,"lng":29.1244229,"recordedAt":"2026-01-01T10:06:00Z"}'
```

No new entrance is reported, and the total distance carries on from where it stopped rather than
restarting at zero. If a ping comes back with `409`, these timestamps were already used on an
earlier run; delete `data/` and start again.

## Design patterns

**Observer.** `EntranceDetector` decides that an entrance happened and does nothing else with
that fact. The ingestion service publishes a `StoreEntranceDetectedEvent`, and `StoreEntranceLogger`
listens for it. Logging an arrival is a different concern from recording one, and it is the kind
of thing that tends to multiply: a notification to the store, a metric, an audit trail. None of
those would touch the detector or the service.

**Strategy.** Distance is measured through the `DistanceCalculator` interface, implemented by
`HaversineDistanceCalculator`. Entrance detection, outlier filtering and the distance total all
depend on the interface rather than the formula, so swapping in a different one is a single line
in `TrackingConfiguration`.

## How the code is arranged

```
model/       GeoPoint, Store, CourierLocation, StoreEntrance
geo/         DistanceCalculator and its haversine implementation
detection/   EntranceDetector, CourierProximityState, LocationFilter
tracking/    courier state, its registry and loader, the per-ping processing steps,
             the ingestion and query services
event/       the entrance event and its listener
store/       reading stores.json, seeding it, holding the catalog in memory
web/         controller, request and response types, error handling
entity/ repository/  JPA mapping and Spring Data repositories
config/      TrackingConfiguration
```

The classes that hold the rules carry no Spring annotations: everything under `model`, `geo` and
`detection`, plus `CourierState`, `CourierStateRegistry`, `CourierLocationProcessor` and
`TravelledDistanceCalculator`. They are constructed in `TrackingConfiguration`, which keeps the
framework in one file and lets the rules be unit tested without starting a container.

## Configuration

| Key | Default | |
| --- | --- | --- |
| `courier-tracking.store.radius-meters` | `100` | how close counts as being at a store |
| `courier-tracking.store.reentry-cooldown` | `PT1M` | how long after an entrance a re-entry is ignored |
| `courier-tracking.store.store-file-resource` | `classpath:db/data/stores.json` | where the store list is read from |
| `courier-tracking.courier.max-speed-kmh` | `150` | above this, a ping is treated as an outlier |
| `courier-tracking.courier.min-movement-meters` | `10` | movement below this is not added to the total |

The radius and the maximum speed have to be positive, the movement floor must not be negative,
and the cooldown and the store file have to be present. Breaking any of those stops the
application at startup rather than quietly changing how it behaves.

## Database

H2 with Liquibase migrations, on disk under `data/` so that state survives a restart. The point is
that the project runs and can be inspected with nothing installed but a JDK: no container to
start, no connection string to configure, and the H2 console to look at the tables. Nothing in the
code is H2-specific.

## Tests

```
./mvnw verify
```

Beyond the arithmetic of the haversine formula, the tests cover the entrance rules end to end
(transition, dwelling, both sides of the cooldown), the outlier and out-of-order filters, and the
state registry under parallel pings for the same courier. `CourierTrackingFlowTest` drives a
route through the real services and database, including the reload that makes a restart safe.

## Assumptions and trade-offs

The brief says "reentries to the same store's circumference over 1 minute should not count as
entrance", which can be read either way. It is read here as: a re-entry within a minute of the
previous entrance is not a new one. Reading it the other way would make the rule contradict its
own purpose of suppressing jitter.

"Log courier and store" is satisfied in both senses: every entrance is written to the database and
emitted to the application log.

Courier state is kept in a `ConcurrentHashMap`, which assumes one instance of the service. Pings
for a single courier are applied one at a time, so concurrent pings cannot lose distance or
interleave entrance transitions, but that guarantee stops at the process boundary. Running more
than one instance would need either couriers pinned to instances by hash, or the in-memory state
dropped in favour of a locked row in the database. `courier_state` already carries a version
column for the latter.

Every ping is compared against every store. With five stores that is the right answer; with
thousands it would want a spatial index, or a coarse bounding-box filter before the exact
distance.
