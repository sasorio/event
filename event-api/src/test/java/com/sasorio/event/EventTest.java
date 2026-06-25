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
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Predicate;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NullMarked
class EventTest {
  protected final EventRegistry<Object> registry = new SimpleEventRegistry<>(Object.class);
  protected final EventBus<Object> bus = this.createBus();
  protected final List<String> flow = new ArrayList<>();

  protected EventBus<Object> createBus() {
    return new SimpleEventBus<>(
      this.registry,
      TestFailingEventExceptionHandler.INSTANCE,
      this.isCancelled()
    );
  }

  protected Predicate<Object> isCancelled() {
    return event -> event instanceof Cancellable && ((Cancellable) event).cancelled();
  }

  protected void flow(final String action) {
    this.flow.add(action);
  }

  protected void assertFlow(final List<String> flow) {
    assertEquals(flow, this.flow);
  }

  @Nested
  @NullMarked
  public class Subscription {
    @Test
    public void notSubscribedBeforeRegistering() {
      assertFalse(EventTest.this.registry.subscribed(TestEvent1.class));
    }

    @Test
    public void subscribedAfterRegistering() {
      EventTest.this.registry.subscribe(TestEvent1.class, event -> EventTest.this.flow("touch"));
      assertTrue(EventTest.this.registry.subscribed(TestEvent1.class));
    }

    @Test
    public void subscriberReceivesEvent() {
      EventTest.this.registry.subscribe(TestEvent1.class, event -> EventTest.this.flow("touch"));

      final TestEvent1 event = new TestEvent1();
      EventTest.this.bus.emit(event);

      EventTest.this.assertFlow(List.of("touch"));
    }

    @Test
    public void notSubscribedAfterDisposing() {
      final EventSubscription<TestEvent1> subscription = EventTest.this.registry.subscribe(TestEvent1.class, event -> EventTest.this.flow("touch"));
      subscription.dispose();

      assertFalse(EventTest.this.registry.subscribed(TestEvent1.class));
    }

    @Test
    public void disposedSubscriberDoesNotReceiveEvent() {
      final EventSubscription<TestEvent1> subscription = EventTest.this.registry.subscribe(TestEvent1.class, event -> EventTest.this.flow("touch"));
      subscription.dispose();

      final TestEvent1 event = new TestEvent1();
      EventTest.this.bus.emit(event);

      EventTest.this.assertFlow(List.of());
    }
  }

  @Nested
  @NullMarked
  public class Hierarchy {
    @Test
    public void parentSubscriberReceivesChildEvent() {
      EventTest.this.registry.subscribe(TestEvent1.class, event -> EventTest.this.flow("touch"));

      // TestEvent2 extends TestEvent1, so its subscribers should also be notified
      final TestEvent2 event = new TestEvent2();
      EventTest.this.bus.emit(event);

      EventTest.this.assertFlow(List.of("touch"));
    }

    @Test
    public void childSubscriberDoesNotReceiveParentEvent() {
      EventTest.this.registry.subscribe(TestEvent2.class, event -> EventTest.this.flow("touch"));

      final TestEvent1 event = new TestEvent1();
      EventTest.this.bus.emit(event);

      EventTest.this.assertFlow(List.of());
    }

    @Test
    public void bothParentAndChildSubscribersReceiveChildEvent() {
      EventTest.this.registry.subscribe(TestEvent1.class, event -> EventTest.this.flow("parent"));
      EventTest.this.registry.subscribe(TestEvent2.class, event -> EventTest.this.flow("child"));

      final TestEvent2 event = new TestEvent2();
      EventTest.this.bus.emit(event);

      EventTest.this.assertFlow(List.of("child", "parent"));
    }
  }

  @Nested
  @NullMarked
  public class Exact {
    @Test
    public void exactSubscriberReceivesExactType() {
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().exact(true), event -> EventTest.this.flow("touch"));

      final TestEvent1 event = new TestEvent1();
      EventTest.this.bus.emit(event);

      EventTest.this.assertFlow(List.of("touch"));
    }

    @Test
    public void exactSubscriberDoesNotReceiveSubtype() {
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().exact(true), event -> EventTest.this.flow("touch"));

      final TestEvent2 event = new TestEvent2();
      EventTest.this.bus.emit(event);

      EventTest.this.assertFlow(List.of());
    }
  }

  @Nested
  @NullMarked
  public class Cancellation {
    @Test
    public void nonCancellingSubscriberReceivesUncancelledEvent() {
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().acceptsCancelled(false), event -> EventTest.this.flow("touch"));

      final TestEvent1 event = new TestEvent1();
      EventTest.this.bus.emit(event);

      EventTest.this.assertFlow(List.of("touch"));
    }

    @Test
    public void nonCancellingSubscriberSkipsCancelledEvent() {
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().acceptsCancelled(false), event -> EventTest.this.flow("touch"));

      final TestEvent1 event = new TestEvent1();
      event.cancelled(true);
      EventTest.this.bus.emit(event);

      EventTest.this.assertFlow(List.of());
    }
  }

  @Nested
  @NullMarked
  public class Body {
    @Test
    public void bodyIsInvokedWhenNoSubscribersAreRegistered() {
      final TestEvent1 event = new TestEvent1();
      EventTest.this.bus.emit(event, e -> EventTest.this.flow("body"));

      EventTest.this.assertFlow(List.of("body"));
    }

    @Test
    public void bodyIsInvokedAfterSubscribers() {
      EventTest.this.registry.subscribe(TestEvent1.class, event -> EventTest.this.flow("subscriber"));

      final TestEvent1 event = new TestEvent1();
      EventTest.this.bus.emit(event, e -> EventTest.this.flow("body"));

      EventTest.this.assertFlow(List.of("subscriber", "body"));
    }

    @Test
    public void bodyIsInvokedEvenWhenEventIsCancelled() {
      // The body always runs regardless of cancellation; cancellation only affects subscribers
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().acceptsCancelled(false), event -> EventTest.this.flow("touch"));

      final TestEvent1 event = new TestEvent1();
      event.cancelled(true);
      EventTest.this.bus.emit(event, e -> EventTest.this.flow("body"));

      EventTest.this.assertFlow(List.of("body"));
    }

    @Test
    public void nullBodyIsNoOp() {
      EventTest.this.registry.subscribe(TestEvent1.class, event -> EventTest.this.flow("touch"));

      final TestEvent1 event = new TestEvent1();
      EventTest.this.bus.emit(event, (EventConsumer<? super TestEvent1>) null);

      EventTest.this.assertFlow(List.of("touch"));
    }
  }

  @Nested
  @NullMarked
  public class UnsubscribeAll {
    @Test
    public void unsubscribeIfRemovesAllMatchingSubscriptions() {
      EventTest.this.registry.subscribe(TestEvent1.class, event -> EventTest.this.flow("touch"));
      EventTest.this.registry.subscribe(TestEvent1.class, event -> EventTest.this.flow("touch"));
      EventTest.this.registry.unsubscribeIf(subscription -> true);

      assertFalse(EventTest.this.registry.subscribed(TestEvent1.class));
    }

    @Test
    public void removedSubscribersDoNotReceiveEvents() {
      EventTest.this.registry.subscribe(TestEvent1.class, event -> EventTest.this.flow("touch"));
      EventTest.this.registry.unsubscribeIf(subscription -> true);

      final TestEvent1 event = new TestEvent1();
      EventTest.this.bus.emit(event);

      EventTest.this.assertFlow(List.of());
    }
  }

  @Nested
  @NullMarked
  public class OwnerUnsubscribe {
    @Test
    public void onlyTargetOwnerIsUnsubscribed() {
      final UUID owner1 = UUID.randomUUID();
      final UUID owner2 = UUID.randomUUID();

      EventTest.this.registry.subscribe(TestEvent1.class, new OwnedSubscriber<>(owner1, event -> EventTest.this.flow("owner1")));
      EventTest.this.registry.subscribe(TestEvent1.class, new OwnedSubscriber<>(owner2, event -> EventTest.this.flow("owner2")));

      EventTest.this.registry.unsubscribeIf(OwnedSubscriber.unsubscribeOwner(owner2));

      final TestEvent1 event = new TestEvent1();
      EventTest.this.bus.emit(event);

      EventTest.this.assertFlow(List.of("owner1"));
    }

    @Test
    public void remainingOwnerIsStillSubscribed() {
      final UUID owner1 = UUID.randomUUID();
      final UUID owner2 = UUID.randomUUID();

      EventTest.this.registry.subscribe(TestEvent1.class, new OwnedSubscriber<>(owner1, event -> EventTest.this.flow("owner1")));
      EventTest.this.registry.subscribe(TestEvent1.class, new OwnedSubscriber<>(owner2, event -> EventTest.this.flow("owner2")));

      EventTest.this.registry.unsubscribeIf(OwnedSubscriber.unsubscribeOwner(owner2));

      assertTrue(EventTest.this.registry.subscribed(TestEvent1.class));
    }

    @Test
    public void removingAllOwnersLeavesNoSubscriptions() {
      final UUID owner1 = UUID.randomUUID();
      final UUID owner2 = UUID.randomUUID();

      EventTest.this.registry.subscribe(TestEvent1.class, new OwnedSubscriber<>(owner1, event -> EventTest.this.flow("owner1")));
      EventTest.this.registry.subscribe(TestEvent1.class, new OwnedSubscriber<>(owner2, event -> EventTest.this.flow("owner2")));

      EventTest.this.registry.unsubscribeIf(OwnedSubscriber.unsubscribeOwner(owner1));
      EventTest.this.registry.unsubscribeIf(OwnedSubscriber.unsubscribeOwner(owner2));

      assertFalse(EventTest.this.registry.subscribed(TestEvent1.class));
    }
  }

  @Nested
  @NullMarked
  public class Priority {
    @Test
    public void lowerPrioritySubscriberIsCalledFirst() {
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().priority(10), event -> EventTest.this.flow("1"));
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().priority(20), event -> EventTest.this.flow("2"));

      EventTest.this.bus.emit(new TestEvent1());

      EventTest.this.assertFlow(List.of("1", "2"));
    }

    @Test
    public void subscribersWithSamePriorityAreCalledInRegistrationOrder() {
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().priority(10), event -> EventTest.this.flow("1"));
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().priority(10), event -> EventTest.this.flow("2"));
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().priority(10), event -> EventTest.this.flow("3"));

      EventTest.this.bus.emit(new TestEvent1());

      EventTest.this.assertFlow(List.of("1", "2", "3"));
    }

    @Test
    public void emitWithPriorityFilterOnlyNotifiesMatchingSubscribers() {
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().priority(10), event -> EventTest.this.flow("1"));
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().priority(20), event -> EventTest.this.flow("2"));

      EventTest.this.bus.emit(new TestEvent1(), null, OptionalInt.of(10));

      EventTest.this.assertFlow(List.of("1"));
    }

    @Test
    public void bodyIsCalledAfterAllPrioritizedSubscribers() {
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().priority(10), event -> EventTest.this.flow("low"));
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().priority(20), event -> EventTest.this.flow("high"));

      EventTest.this.bus.emit(new TestEvent1(), e -> EventTest.this.flow("body"));

      EventTest.this.assertFlow(List.of("low", "high", "body"));
    }

    @Test
    public void bodyIsCalledAfterPriorityFilteredSubscribers() {
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().priority(10), event -> EventTest.this.flow("low"));
      EventTest.this.registry.subscribe(TestEvent1.class, EventConfig.defaults().priority(20), event -> EventTest.this.flow("high"));

      EventTest.this.bus.emit(new TestEvent1(), e -> EventTest.this.flow("body"), OptionalInt.of(10));

      EventTest.this.assertFlow(List.of("low", "body"));
    }
  }
}
