/*
 * Copyright 2021 Sasorio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sasorio.event.bus;

import com.sasorio.event.EventSubscription;
import java.util.OptionalInt;
import org.jspecify.annotations.NullMarked;

/**
 * An event bus.
 *
 * @param <E> the base event type
 * @since 1.0.0
 */
@NullMarked
public interface EventBus<E> {
  /**
   * Emits an event to all registered subscribers.
   *
   * @param event the event
   * @since 1.1.0
   */
  default void emit(final E event) {
    this.emit(event, OptionalInt.empty());
  }

  /**
   * Emits an event to all registered subscribers at the order provided in {@code order}.
   *
   * @param event the event
   * @param order the order
   * @since 1.1.0
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  default void emit(
    final E event,
    final OptionalInt order
  ) {
    this.post(event, order);
  }

  /**
   * Posts an event to all registered subscribers.
   *
   * @deprecated use {@link #emit(Object)}
   * @param event the event
   * @since 1.0.0
   */
  @Deprecated(since = "1.1.0", forRemoval = true)
  default void post(final E event) {
    this.post(event, OptionalInt.empty());
  }

  /**
   * Posts an event to all registered subscribers at the order provided in {@code order}.
   *
   * @deprecated use {@link #emit(Object, OptionalInt)}
   * @param event the event
   * @param order the order
   * @since 1.0.0
   */
  @Deprecated(since = "1.1.0", forRemoval = true)
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  void post(
    final E event,
    final OptionalInt order
  );

  /**
   * An event exception handler.
   *
   * @since 1.0.0
   */
  @FunctionalInterface
  @NullMarked
  interface EventExceptionHandler {
    /**
     * Handles a caught exception.
     *
     * @param bus the event bus
     * @param subscription the event subscription
     * @param event the event
     * @param throwable the exception
     * @param <E> the event type
     * @since 1.0.0
     */
    <E> void eventExceptionCaught(
      final EventBus<? super E> bus,
      final EventSubscription<? super E> subscription,
      final E event,
      final Throwable throwable
    );
  }
}
