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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Predicate;
import org.jspecify.annotations.NullMarked;

import static java.util.Objects.requireNonNull;

/**
 * A simple implementation of an event registry.
 *
 * @param <E> the base event type
 * @since 1.0.0
 */
@NullMarked
public class SimpleEventRegistry<E> implements EventRegistry<E> {
  private static final Comparator<EventSubscription<?>> ORDER_COMPARATOR = Comparator.comparingInt(subscription -> subscription.config().order());

  private final Map<Class<? extends E>, Collection<? extends Class<?>>> classes = new HashMap<>();

  private final Map<Class<? extends E>, List<EventSubscription<? super E>>> unbaked = new HashMap<>();
  private final Map<Class<? extends E>, List<EventSubscription<? super E>>> baked = new HashMap<>();

  private final Object lock = new Object();

  private final Class<E> type;

  /**
   * Constructs a new {@code SimpleEventRegistry}.
   *
   * @param type the base event type
   * @since 1.0.0
   */
  public SimpleEventRegistry(final Class<E> type) {
    this.type = requireNonNull(type, "type");
  }

  @Override
  public Class<E> type() {
    return this.type;
  }

  @Override
  public <T extends E> EventSubscription<T> subscribe(final Class<T> event, final EventConfig config, final EventSubscriber<? super T> subscriber) {
    requireNonNull(event, "event");
    requireNonNull(config, "config");
    requireNonNull(subscriber, "subscriber");
    final EventSubscription<T> subscription = new EventSubscriptionImpl<>(event, config, subscriber);
    synchronized (this.lock) {
      final List<EventSubscription<? super T>> subscriptions = yayGenerics(this.unbaked.computeIfAbsent(event, key -> new ArrayList<>()));
      subscriptions.add(subscription);
      this.baked.clear();
    }
    return subscription;
  }

  @Override
  public void unsubscribeIf(final Predicate<EventSubscription<? super E>> predicate) {
    synchronized (this.lock) {
      boolean removedAny = false;
      for (final List<EventSubscription<? super E>> subscriptions : this.unbaked.values()) {
        removedAny |= subscriptions.removeIf(predicate);
      }
      if (removedAny) {
        this.baked.clear();
      }
    }
  }

  @Override
  public List<EventSubscription<? super E>> subscriptions(final Class<? extends E> event) {
    synchronized (this.lock) {
      return this.baked.computeIfAbsent(event, this::computeSubscriptions);
    }
  }

  private List<EventSubscription<? super E>> computeSubscriptions(final Class<? extends E> event) {
    final List<EventSubscription<? super E>> subscriptions = new ArrayList<>();
    final Collection<? extends Class<?>> types = this.classes.computeIfAbsent(event, this::findClasses);
    for (final Class<?> type : types) {
      subscriptions.addAll(this.unbaked.getOrDefault(type, Collections.emptyList()));
    }
    subscriptions.sort(ORDER_COMPARATOR);
    return subscriptions;
  }

  private Collection<? extends Class<?>> findClasses(final Class<?> type) {
    final Collection<? extends Class<?>> classes = Internals.ancestors(type);
    classes.removeIf(klass -> !this.type.isAssignableFrom(klass));
    return classes;
  }

  @SuppressWarnings("unchecked")
  private static <T extends U, U> List<U> yayGenerics(final List<T> list) {
    return (List<U>) list;
  }

  @NullMarked
  private class EventSubscriptionImpl<T extends E> implements EventSubscription<T> {
    private final Class<T> event;
    private final EventConfig config;
    private final EventSubscriber<? super T> subscriber;

    EventSubscriptionImpl(final Class<T> event, final EventConfig config, final EventSubscriber<? super T> subscriber) {
      this.event = event;
      this.config = config;
      this.subscriber = subscriber;
    }

    @Override
    public Class<T> event() {
      return this.event;
    }

    @Override
    public EventConfig config() {
      return this.config;
    }

    @Override
    public EventSubscriber<? super T> subscriber() {
      return this.subscriber;
    }

    @Override
    public void dispose() {
      synchronized (SimpleEventRegistry.this.lock) {
        final List<EventSubscription<? super T>> subscriptions = yayGenerics(SimpleEventRegistry.this.unbaked.get(this.event));
        if (subscriptions != null) {
          subscriptions.remove(this);
          SimpleEventRegistry.this.baked.clear();
        }
      }
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
        .add("event=" + this.event)
        .add("config=" + this.config)
        .add("subscriber=" + this.subscriber)
        .toString();
    }
  }
}
