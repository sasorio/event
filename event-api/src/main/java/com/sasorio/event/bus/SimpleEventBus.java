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

import com.sasorio.event.Cancellable;
import com.sasorio.event.EventConfig;
import com.sasorio.event.EventConsumer;
import com.sasorio.event.EventSubscription;
import com.sasorio.event.registry.EventRegistry;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Predicate;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * A simple implementation of an event bus.
 *
 * @param <E> the base event type
 * @since 1.0.0
 */
@NullMarked
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SimpleEventBus<E> implements EventBus<E> {
  protected final EventRegistry<E> registry;
  protected final ExceptionHandler exceptions;
  protected final Predicate<E> cancelled;

  /**
   * Constructs a new {@code SimpleEventBus}.
   *
   * @deprecated use {@link #SimpleEventBus(EventRegistry, ExceptionHandler, Predicate)}
   * @param registry the event registry
   * @param exceptions the event exception handler
   * @since 1.0.0
   */
  @Deprecated(since = "1.1.0", forRemoval = true)
  public SimpleEventBus(
    final EventRegistry<E> registry,
    final EventExceptionHandler exceptions
  ) {
    this(registry, exceptions, event -> event instanceof Cancellable && ((Cancellable) event).cancelled());
  }

  /**
   * Constructs a new {@code SimpleEventBus}.
   *
   * @param registry the event registry
   * @param exceptions the event exception handler
   * @param cancelled the predicate used to determine whether an event should be treated as cancelled
   * @since 1.1.0
   */
  public SimpleEventBus(
    final EventRegistry<E> registry,
    final ExceptionHandler exceptions,
    final Predicate<E> cancelled
  ) {
    this.registry = requireNonNull(registry, "registry");
    this.exceptions = requireNonNull(exceptions, "exceptions");
    this.cancelled = requireNonNull(cancelled, "cancelled");
  }

  @Override
  public <T extends E> void emit(
    final T event,
    final @Nullable EventConsumer<? super T> body,
    final OptionalInt priority
  ) {
    @SuppressWarnings("unchecked")
    final Class<? extends E> type = (Class<? extends E>) event.getClass();
    final List<EventSubscription<? super E>> subscriptions = this.registry.subscriptions(type);
    this.dispatch(event, body, priority, subscriptions);
  }

  protected <T extends E> void dispatch(
    final T event,
    final @Nullable EventConsumer<? super T> body,
    final OptionalInt priority,
    final List<EventSubscription<? super E>> subscriptions
  ) {
    if (!subscriptions.isEmpty()) {
      for (final EventSubscription<? super E> subscription : subscriptions) {
        if (this.accepts(subscription, event, priority)) {
          try {
            subscription.subscriber().on(event);
          } catch (final Throwable t) {
            this.exceptions.eventExceptionCaught(this, subscription, event, t);
          }
        }
      }
    }

    // The body is always dispatched after subscribers, regardless of whether any matched.
    this.dispatchBody(event, body);
  }

  protected <T extends E> void dispatchBody(final T event, final @Nullable EventConsumer<? super T> body) {
    if (body != null) {
      try {
        body.on(event);
      } catch (final Throwable t) {
        this.exceptions.eventExceptionCaught(this, body, event, t);
      }
    }
  }

  @Deprecated(since = "1.1.0", forRemoval = true)
  @Override
  public void post(
    final E event,
    final OptionalInt priority
  ) {
    this.emit(event, null, priority);
  }

  @SuppressWarnings("RedundantIfStatement")
  protected boolean accepts(final EventSubscription<? super E> subscription, final E event, final OptionalInt priority) {
    final EventConfig config = subscription.config();

    if (config.exact()) {
      if (event.getClass() != subscription.event()) {
        return false;
      }
    }

    if (priority.isPresent()) {
      if (config.priority() != priority.getAsInt()) {
        return false;
      }
    }

    if (!config.acceptsCancelled()) {
      if (this.cancelled.test(event)) {
        return false;
      }
    }

    return true;
  }
}
