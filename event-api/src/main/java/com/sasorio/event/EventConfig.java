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

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

/**
 * Event configuration.
 *
 * @since 1.0.0
 */
@NullMarked
public interface EventConfig {
  /**
   * The default value for {@link #priority()}.
   *
   * @since 1.1.0
   */
  int DEFAULT_PRIORITY = 0;
  /**
   * The default value for {@link #order()}.
   *
   * @deprecated use {@link #DEFAULT_PRIORITY}
   * @since 1.0.0
   */
  @Deprecated(since = "1.1.0", forRemoval = true)
  int DEFAULT_ORDER = DEFAULT_PRIORITY;
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
  @Contract(pure = true)
  static EventConfig defaults() {
    return EventConfigImpl.DEFAULTS;
  }

  /**
   * Creates a new configuration.
   *
   * @param priority the priority
   * @param acceptsCancelled if cancelled events are accepted
   * @param exact if only the exact event type is accepted
   * @return a configuration
   * @since 1.0.0
   */
  @Contract(pure = true)
  static EventConfig of(
    final int priority,
    final boolean acceptsCancelled,
    final boolean exact
  ) {
    return new EventConfigImpl(priority, acceptsCancelled, exact);
  }

  /**
   * Creates a new builder.
   *
   * @return a new builder
   * @since 1.0.0
   */
  @Contract(pure = true)
  static Builder builder() {
    return new EventConfigImpl.BuilderImpl();
  }

  /**
   * Gets the priority.
   *
   * <p>Lower values run first, higher values run later. Subscribers with the same priority may be invoked in any order.</p>
   *
   * <p>The default priority is {@link #DEFAULT_PRIORITY} ({@code 0}).</p>
   *
   * @return the priority
   * @since 1.1.0
   */
  default int priority() {
    return this.order();
  }

  /**
   * Sets the priority.
   *
   * <p>Lower values run first, higher values run later. Subscribers with the same priority may be invoked in any order.</p>
   *
   * <p>The default priority is {@link #DEFAULT_PRIORITY} ({@code 0}).</p>
   *
   * @param priority the priority
   * @return an {@link EventConfig}
   * @since 1.1.0
   */
  default EventConfig priority(final int priority) {
    return this.order(priority);
  }

  /**
   * Gets the post order.
   *
   * @deprecated use {@link #priority()}
   * @return the post order
   * @since 1.0.0
   */
  @Deprecated(since = "1.1.0", forRemoval = true)
  int order();

  /**
   * Sets the post order.
   *
   * @deprecated use {@link #priority(int)}
   * @param order the post order
   * @return an {@link EventConfig}
   * @since 1.0.0
   */
  @Deprecated(since = "1.1.0", forRemoval = true)
  EventConfig order(final int order);

  /**
   * Gets if cancelled events are accepted.
   *
   * @return if cancelled events are accepted
   * @since 1.0.0
   */
  boolean acceptsCancelled();

  /**
   * Sets if cancelled events are accepted.
   *
   * @param acceptsCancelled if cancelled events are accepted
   * @return an {@link EventConfig}
   * @since 1.0.0
   */
  EventConfig acceptsCancelled(final boolean acceptsCancelled);

  /**
   * Gets if only the exact event type is accepted.
   *
   * @return if only the exact event type is accepted
   * @since 1.0.0
   */
  boolean exact();

  /**
   * Sets if only the exact event type is accepted.
   *
   * @param exact if only the exact event type is accepted
   * @return an {@link EventConfig}
   * @since 1.0.0
   */
  EventConfig exact(final boolean exact);

  /**
   * Builder.
   *
   * @since 1.0.0
   */
  @NullMarked
  interface Builder {
    /**
     * Sets the priority.
     *
     * <p>Lower values run first, higher values run later. Subscribers with the same priority may be invoked in any order.</p>
     *
     * <p>The default priority is {@link #DEFAULT_PRIORITY} ({@code 0}).</p>
     *
     * @param priority the priority
     * @return {@code this}
     * @since 1.1.0
     */
    default Builder priority(final int priority) {
      return this.order(priority);
    }

    /**
     * Sets the post order.
     *
     * @deprecated use {@link #priority(int)}
     * @param order the post order
     * @return {@code this}
     * @since 1.0.0
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    Builder order(final int order);

    /**
     * Sets if cancelled events are accepted.
     *
     * @param acceptsCancelled if cancelled events are accepted
     * @return {@code this}
     * @since 1.0.0
     */
    Builder acceptsCancelled(final boolean acceptsCancelled);

    /**
     * Sets if only the exact event type is accepted.
     *
     * @param exact if only the exact event type is accepted
     * @return {@code this}
     * @since 1.0.0
     */
    Builder exact(final boolean exact);

    /**
     * Builds.
     *
     * @return an {@link EventConfig}
     * @since 1.0.0
     */
    @Contract(pure = true)
    EventConfig build();
  }
}
