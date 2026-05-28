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

import com.sasorio.event.bus.EventBus;
import com.sasorio.event.bus.SimpleEventBus;
import com.sasorio.event.registry.EventRegistry;
import com.sasorio.event.registry.SimpleEventRegistry;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NullMarked
class EventTest {
  private final EventRegistry<Object> registry = new SimpleEventRegistry<>(Object.class);
  private final EventBus<Object> bus = new SimpleEventBus<>(
    this.registry,
    TestFailingEventExceptionHandler.INSTANCE
  );

  @Test
  void testSubscribePostUnsubscribePost() {
    assertFalse(this.registry.subscribed(TestEvent1.class));

    final EventSubscription<TestEvent1> subscription = this.registry.subscribe(TestEvent1.class, event -> event.touches++);

    assertTrue(this.registry.subscribed(TestEvent1.class));

    final TestEvent1 event = new TestEvent1();
    this.bus.post(event);
    assertEquals(1, event.touches);

    subscription.dispose();

    assertFalse(this.registry.subscribed(TestEvent1.class));
    this.bus.post(event);
    assertEquals(1, event.touches);
  }

  @Test
  void testHierarchy() {
    assertFalse(this.registry.subscribed(TestEvent1.class));
    assertFalse(this.registry.subscribed(TestEvent2.class));

    this.registry.subscribe(TestEvent1.class, event -> event.touches++);
    this.registry.subscribe(TestEvent2.class, event -> event.touches++);

    assertTrue(this.registry.subscribed(TestEvent1.class));
    assertTrue(this.registry.subscribed(TestEvent2.class));

    final TestEvent1 event1 = new TestEvent1();
    this.bus.post(event1);
    assertEquals(1, event1.touches);

    final TestEvent2 event2 = new TestEvent2();
    this.bus.post(event2);
    assertEquals(2, event2.touches);
  }

  @Test
  void testCancellable() {
    this.registry.subscribe(TestEvent1.class, EventConfig.defaults().acceptsCancelled(false), event -> event.touches++);

    final TestEvent1 event = new TestEvent1();
    this.bus.post(event);
    assertEquals(1, event.touches);

    event.cancelled(true);

    this.bus.post(event);
    assertEquals(1, event.touches);
  }

  @Test
  void testExact() {
    this.registry.subscribe(TestEvent1.class, EventConfig.defaults().exact(true), event -> event.touches++);

    final TestEvent1 event1 = new TestEvent1();
    this.bus.post(event1);
    assertEquals(1, event1.touches);

    final TestEvent2 event2 = new TestEvent2();
    this.bus.post(event2);
    assertEquals(0, event2.touches);
  }

  @Test
  void testUnsubscribeAll() {
    assertFalse(this.registry.subscribed(TestEvent1.class));
    this.registry.subscribe(TestEvent1.class, event -> event.touches++);
    assertTrue(this.registry.subscribed(TestEvent1.class));
    this.registry.unsubscribeIf(subscription -> true); // removes all subscribers
    assertFalse(this.registry.subscribed(TestEvent1.class));
  }

  @Test
  void testUnsubscribeOwnedInstances() {
    assertFalse(this.registry.subscribed(TestEvent1.class));

    final UUID owner1 = UUID.randomUUID();
    final UUID owner2 = UUID.randomUUID();

    this.registry.subscribe(TestEvent1.class, new OwnedSubscriber<>(owner1, event -> event.touches++));
    this.registry.subscribe(TestEvent1.class, new OwnedSubscriber<>(owner2, event -> event.touches++));

    assertTrue(this.registry.subscribed(TestEvent1.class));

    final TestEvent1 event = new TestEvent1();
    this.bus.post(event);
    assertEquals(2, event.touches);

    this.registry.unsubscribeIf(OwnedSubscriber.unsubscribeOwner(owner2));

    assertTrue(this.registry.subscribed(TestEvent1.class));

    this.bus.post(event);
    assertEquals(3, event.touches); // only 3, since one subscriber is gone
  }
}
