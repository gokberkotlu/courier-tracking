package com.gokberkotlu.couriertrackingapp.detection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CourierStateRegistry {
  private final ConcurrentHashMap<Long, CourierState> statesByCourierId = new ConcurrentHashMap<>();

  public <T> T update(
      Long courierId, Function<Long, CourierState> loader, Function<CourierState, T> update) {

    Holder<T> holder = new Holder<>();

    statesByCourierId.compute(
        courierId,
        (id, existing) -> {
          CourierState state = existing != null ? existing : loader.apply(id);
          holder.value = update.apply(state);
          return state;
        });

    return holder.value;
  }

  private static final class Holder<T> {
    private T value;
  }
}
