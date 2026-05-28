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
package com.sasorio.event.registry;

import com.sasorio.event.EventConfig;
import com.sasorio.event.EventSubscriber;
import com.sasorio.event.EventSubscription;
import java.util.List;
import java.util.function.Predicate;
import org.jspecify.annotations.NullMarked;

/**
 * An event registry.
 *
 * @param <E> the base event type
 * @since 1.0.0
 */
@NullMarked
public interface EventRegistry<E> {
  /**
   * Gets the base event type.
   *
   * <p>This is represented by the {@code E} type parameter.</p>
   *
   * @return the base event type
   * @since 1.0.0
   */
  Class<E> type();

  /**
   * Determines whether the specified event has subscribers.
   *
   * @param event the event type
   * @return whether the specified event has subscribers
   * @since 1.0.0
   */
  default boolean subscribed(final Class<? extends E> event) {
    return !this.subscriptions(event).isEmpty();
  }

  /**
   * Registers the given {@code subscriber} to receive events, using the default {@link EventConfig configuration}.
   *
   * @param event the event type
   * @param subscriber the subscriber
   * @param <T> the event type
   * @since 1.0.0
   */
  default <T extends E> EventSubscription<T> subscribe(
    final Class<T> event,
    final EventSubscriber<? super T> subscriber
  ) {
    return this.subscribe(event, EventConfig.defaults(), subscriber);
  }

  /**
   * Registers the given {@code subscriber} to receive events.
   *
   * @param event the event type
   * @param config the event configuration
   * @param subscriber the subscriber
   * @param <T> the event type
   * @since 1.0.0
   */
  <T extends E> EventSubscription<T> subscribe(
    final Class<T> event,
    final EventConfig config,
    final EventSubscriber<? super T> subscriber
  );

  /**
   * Removes subscriptions matching {@code predicate}.
   *
   * @param predicate the predicate used to determine which subscriptions to remove
   * @since 1.0.0
   */
  void unsubscribeIf(final Predicate<EventSubscription<? super E>> predicate);

  /**
   * Gets an unmodifiable list containing all subscriptions currently registered for events of type {@code event}.
   *
   * @return a list of all subscriptions for events of type {@code event}
   * @since 1.0.0
   */
  List<EventSubscription<? super E>> subscriptions(final Class<? extends E> event);
}
