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

import com.sasorio.event.registry.EventRegistry;
import com.sasorio.event.registry.SimpleEventRegistry;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NullMarked
class EventRegistryTests {

  @Test
  void testSubscribe() {
    assertThrows(NullPointerException.class, () -> new SimpleEventRegistry<>(null));

    final EventRegistry<SimpleEvent> registry = new SimpleEventRegistry<>(SimpleEvent.class);
    assertEquals(SimpleEvent.class, registry.type());
    assertFalse(registry.subscribed(SimpleEvent.class));
    assertFalse(registry.subscribed(ExtendedEvent.class));

    final EventSubscription<?> subscription = registry.subscribe(SimpleEvent.class, new SimpleSubscriber<>());
    assertTrue(registry.subscribed(SimpleEvent.class));
    assertTrue(registry.subscribed(ExtendedEvent.class));

    assertSame(subscription.config(), EventConfig.defaults());
    assertEquals(SimpleEvent.class, subscription.event());
    assertInstanceOf(SimpleSubscriber.class, subscription.subscriber());

    subscription.dispose();
    assertFalse(registry.subscribed(SimpleEvent.class));
    assertFalse(registry.subscribed(ExtendedEvent.class));

    registry.subscribe(ExtendedEvent.class, new SimpleSubscriber<>());
    assertFalse(registry.subscribed(SimpleEvent.class));
    assertTrue(registry.subscribed(ExtendedEvent.class));
  }

  @Test
  void testUnsubscribe() {
    final EventRegistry<SimpleEvent> registry = new SimpleEventRegistry<>(SimpleEvent.class);

    final SimpleSubscriber<SimpleEvent> subscriber1 = new SimpleSubscriber<>();
    final SimpleSubscriber<SimpleEvent> subscriber2 = new SimpleSubscriber<>();

    registry.subscribe(SimpleEvent.class, subscriber1);
    assertTrue(registry.subscribed(SimpleEvent.class));

    registry.unsubscribeIf(sub -> true);
    assertFalse(registry.subscribed(SimpleEvent.class));

    registry.subscribe(SimpleEvent.class, subscriber1);
    assertTrue(registry.subscribed(SimpleEvent.class));
    subscriber1.unregisterMe = true;
    registry.unsubscribeIf(sub -> sub.subscriber() instanceof SimpleSubscriber<?> simple && simple.unregisterMe);

    registry.subscribe(SimpleEvent.class, subscriber1);
    registry.subscribe(SimpleEvent.class, subscriber2);
    assertTrue(registry.subscribed(SimpleEvent.class));
    registry.unsubscribeIf(sub -> sub.subscriber() instanceof SimpleSubscriber<?> simple && simple.unregisterMe);
    assertTrue(registry.subscribed(SimpleEvent.class));

    registry.unsubscribeIf(sub -> false);
    assertTrue(registry.subscribed(SimpleEvent.class));
  }

  @Test
  void testAncestry() {
    final EventRegistry<SimpleEvent> registry = new SimpleEventRegistry<>(SimpleEvent.class);
    registry.subscribe(NonBaseEvent.class, new SimpleSubscriber<>());
    assertFalse(registry.subscribed(SimpleEvent.class));
    assertTrue(registry.subscribed(NonBaseEvent.class));
    assertTrue(registry.subscribed(ExtendedEvent.class));
    assertFalse(registry.subscribed(AnotherEvent.class));
  }

  @Test
  void runToString() {
    // This is really just to get the last 8% line coverage.
    final EventRegistry<SimpleEvent> registry = new SimpleEventRegistry<>(SimpleEvent.class);
    final EventSubscription<?> subscription = registry.subscribe(SimpleEvent.class, new SimpleSubscriber<>());
    final String ignored = subscription.toString();
  }

  private interface SimpleEvent {
  }

  private interface BetterSimpleEvent extends SimpleEvent {
  }

  private static class NonBaseEvent implements SimpleEvent, BetterSimpleEvent {
  }

  private static final class ExtendedEvent extends NonBaseEvent {
  }

  private static final class AnotherEvent implements SimpleEvent {
  }

  private static final class SimpleSubscriber<E extends SimpleEvent> implements EventSubscriber<E> {
    public boolean unregisterMe = false;

    @Override
    public void on(final E event) {
      // ...
    }
  }
}
