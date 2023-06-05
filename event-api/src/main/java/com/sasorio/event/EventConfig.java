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
 * Event configuration.
 *
 * @since 1.0.0
 */
public interface EventConfig {
  /**
   * The default value for {@link #order()}.
   *
   * @since 1.0.0
   */
  int DEFAULT_ORDER = 0;
  /**
   * The default value for {@link #acceptsCancelled()}.
   *
   * @since 1.0.0
   */
  boolean DEFAULT_ACCEPTS_CANCELLED = true;
  /**
   * The default value for {@link #exact()}.
   *
   * @since 1.0.0
   */
  boolean DEFAULT_EXACT = false;

  /**
   * Gets the default configuration.
   *
   * @return the default configuration
   * @since 1.0.0
   */
  static @NotNull EventConfig defaults() {
    return EventConfigImpl.DEFAULTS;
  }

  /**
   * Gets the post order.
   *
   * @return the post order
   * @since 1.0.0
   */
  default int order() {
    return DEFAULT_ORDER;
  }

  /**
   * Sets the post order.
   *
   * @param order the post order
   * @return an {@link EventConfig}
   * @since 1.0.0
   */
  default @NotNull EventConfig order(final int order) {
    return new EventConfigImpl(order, this.acceptsCancelled(), this.exact());
  }

  /**
   * Gets if cancelled events are accepted.
   *
   * @return if cancelled events are accepted
   * @since 1.0.0
   */
  default boolean acceptsCancelled() {
    return DEFAULT_ACCEPTS_CANCELLED;
  }

  /**
   * Sets if cancelled events are accepted.
   *
   * @param acceptsCancelled if cancelled events are accepted
   * @return an {@link EventConfig}
   * @since 1.0.0
   */
  default @NotNull EventConfig acceptsCancelled(final boolean acceptsCancelled) {
    return new EventConfigImpl(this.order(), acceptsCancelled, this.exact());
  }

  /**
   * Gets if only the exact event type is accepted.
   *
   * @return if only the exact event type is accepted
   * @since 1.0.0
   */
  default boolean exact() {
    return DEFAULT_EXACT;
  }

  /**
   * Sets if only the exact event type is accepted.
   *
   * @param exact if only the exact event type is accepted
   * @return an {@link EventConfig}
   * @since 1.0.0
   */
  default @NotNull EventConfig exact(final boolean exact) {
    return new EventConfigImpl(this.order(), this.acceptsCancelled(), exact);
  }
}
