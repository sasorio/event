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
import com.sasorio.event.EventSubscription;
import com.sasorio.event.registry.EventRegistry;
import java.util.List;
import java.util.OptionalInt;
import org.jspecify.annotations.NullMarked;

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
  protected final EventExceptionHandler exceptions;

  /**
   * Constructs a new {@code SimpleEventBus}.
   *
   * @param registry the event registry
   * @param exceptions the event exception handler
   * @since 1.0.0
   */
  public SimpleEventBus(
    final EventRegistry<E> registry,
    final EventExceptionHandler exceptions
  ) {
    this.registry = requireNonNull(registry, "registry");
    this.exceptions = requireNonNull(exceptions, "exceptions");
  }

  @Override
  public void post(
    final E event,
    final OptionalInt order
  ) {
    @SuppressWarnings("unchecked")
    final Class<? extends E> type = (Class<? extends E>) event.getClass();
    final List<EventSubscription<? super E>> subscriptions = this.registry.subscriptions(type);
    if (subscriptions.isEmpty()) {
      return;
    }
    for (final EventSubscription<? super E> subscription : subscriptions) {
      if (this.accepts(subscription, event, order)) {
        try {
          subscription.subscriber().on(event);
        } catch (final Throwable t) {
          this.exceptions.eventExceptionCaught(this, subscription, event, t);
        }
      }
    }
  }

  @SuppressWarnings("RedundantIfStatement")
  protected boolean accepts(final EventSubscription<? super E> subscription, final E event, final OptionalInt order) {
    final EventConfig config = subscription.config();

    if (config.exact()) {
      if (event.getClass() != subscription.event()) {
        return false;
      }
    }

    if (order.isPresent()) {
      if (config.order() != order.getAsInt()) {
        return false;
      }
    }

    if (!config.acceptsCancelled()) {
      if (this.currentlyCancelled(event)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Checks if {@code event} is cancelled.
   *
   * @param event the event
   * @return {@code true} if the event is cancelled, {@code false} otherwise
   */
  protected boolean currentlyCancelled(final E event) {
    return event instanceof Cancellable && ((Cancellable) event).cancelled();
  }
}
