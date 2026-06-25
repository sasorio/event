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

import com.sasorio.event.EventConsumer;
import com.sasorio.event.EventSubscription;
import java.util.OptionalInt;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
    this.emit(event, null);
  }

  /**
   * Emits an event to all registered subscribers, then invokes {@code body} after all subscribers have been notified.
   *
   * <p>{@code body} is always invoked regardless of whether the event was cancelled; cancellation
   * only affects which subscribers are notified, not the body itself.</p>
   *
   * @param event the event
   * @param body the body
   * @since 1.1.0
   */
  default <T extends E> void emit(
    final T event,
    final @Nullable EventConsumer<? super T> body
  ) {
    this.emit(event, body, OptionalInt.empty());
  }

  /**
   * Emits an event to all registered subscribers at the order provided in {@code priority}, then invokes {@code body} after all subscribers have been notified.
   *
   * <p>{@code body} is always invoked regardless of whether the event was cancelled; cancellation
   * only affects which subscribers are notified, not the body itself.</p>
   *
   * @param event the event
   * @param body the body
   * @param priority the priority
   * @since 1.1.0
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  default <T extends E> void emit(
    final T event,
    final @Nullable EventConsumer<? super T> body,
    final OptionalInt priority
  ) {
    this.post(event, priority);
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
   * Posts an event to all registered subscribers at the priority provided in {@code priority}.
   *
   * @deprecated use {@link #emit(Object, EventConsumer, OptionalInt)}
   * @param event the event
   * @param priority the priority
   * @since 1.0.0
   */
  @Deprecated(since = "1.1.0", forRemoval = true)
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  void post(
    final E event,
    final OptionalInt priority
  );

  /**
   * An event exception handler.
   *
   * @since 1.1.0
   */
  @NullMarked
  interface ExceptionHandler {
    /**
     * Handles an exception thrown by a {@link EventConsumer body consumer} during event dispatch.
     *
     * @param bus the event bus
     * @param event the event
     * @param throwable the exception
     * @param <E> the event type
     * @since 1.1.0
     */
    <E> void eventExceptionCaught(
      final EventBus<? super E> bus,
      final EventConsumer<? super E> body,
      final E event,
      final Throwable throwable
    );

    /**
     * Handles an exception thrown by a {@link EventSubscription subscriber} during event dispatch.
     *
     * @param bus the event bus
     * @param subscription the event subscription
     * @param event the event
     * @param throwable the exception
     * @param <E> the event type
     * @since 1.1.0
     */
    <E> void eventExceptionCaught(
      final EventBus<? super E> bus,
      final EventSubscription<? super E> subscription,
      final E event,
      final Throwable throwable
    );
  }

  /**
   * An event exception handler.
   *
   * @deprecated use {@link ExceptionHandler}
   * @since 1.0.0
   */
  @Deprecated(since = "1.1.0", forRemoval = true)
  @FunctionalInterface
  @NullMarked
  interface EventExceptionHandler extends ExceptionHandler {
    @Override
    default <E> void eventExceptionCaught(
      final EventBus<? super E> bus,
      final EventConsumer<? super E> body,
      final E event,
      final Throwable throwable
    ) {
    }

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
    @Override
    <E> void eventExceptionCaught(
      final EventBus<? super E> bus,
      final EventSubscription<? super E> subscription,
      final E event,
      final Throwable throwable
    );
  }
}
