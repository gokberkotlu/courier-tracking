package com.gokberkotlu.couriertrackingapp.tracking;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CourierStateRegistry {
  private final ConcurrentHashMap<Long, CourierState> statesByCourierId = new ConcurrentHashMap<>();

  public <T> T update(
      Long courierId, Function<Long, CourierState> loader, Function<CourierState, T> update) {

    Holder<T> holder = new Holder<>();

    try {
      statesByCourierId.compute(
          courierId,
          (id, existing) -> {
            CourierState state = existing != null ? existing : loader.apply(id);
            holder.value = update.apply(state);
            return state;
          });
    } catch (RuntimeException e) {
      // The state may already have been mutated when the update failed, and the surrounding
      // transaction is about to roll back. Drop it so the next ping reloads what was persisted.
      statesByCourierId.remove(courierId);
      throw e;
    }

    return holder.value;
  }

  private static final class Holder<T> {
    private T value;
  }
}
