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
package com.sasorio.event;

import org.jetbrains.annotations.NotNull;

/**
 * A subscription to an event.
 *
 * @param <E> the event type
 * @since 1.0.0
 */
public interface EventSubscription<E> {
  /**
   * Gets the event type.
   *
   * @return the event type
   * @since 1.0.0
   */
  @NotNull Class<E> event();

  /**
   * Gets the configuration.
   *
   * @return the configuration
   * @since 1.0.0
   */
  @NotNull EventConfig config();

  /**
   * Gets the subscriber.
   *
   * @return the subscriber
   * @since 1.0.0
   */
  @NotNull EventSubscriber<? super E> subscriber();

  /**
   * Disposes this subscription.
   *
   * <p>The subscriber held by this subscription will no longer receive events.</p>
   *
   * @since 1.0.0
   */
  void dispose();
}
