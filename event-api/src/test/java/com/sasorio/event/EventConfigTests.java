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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"removal", "EqualsWithItself"})
class EventConfigTests {

  @Test
  void testDefaults() {
    final EventConfig config = EventConfig.defaults();
    assertEquals(EventConfig.DEFAULT_PRIORITY, config.priority());
    assertEquals(EventConfig.DEFAULT_ORDER, config.order());
    assertEquals(EventConfig.DEFAULT_ACCEPTS_CANCELLED, config.acceptsCancelled());
    assertEquals(EventConfig.DEFAULT_EXACT, config.exact());

    // Multiple calls to #defaults should yield the same object.
    assertSame(EventConfig.defaults(), EventConfig.defaults());
  }

  @Test
  void testValueSetters() {
    final EventConfig config = EventConfig.defaults();

    final int newPriority = EventConfig.DEFAULT_PRIORITY + 125;
    final EventConfig priorityConfig = config.priority(newPriority);
    assertEquals(newPriority, priorityConfig.priority());
    assertNotEquals(config, priorityConfig);
    assertNotSame(config, priorityConfig);

    final int newOrder = EventConfig.DEFAULT_ORDER - 125;
    final EventConfig orderConfig = config.order(newOrder);
    assertEquals(newOrder, orderConfig.order());
    assertEquals(newOrder, orderConfig.priority());
    assertNotEquals(config, orderConfig);
    assertNotSame(config, orderConfig);

    final boolean newAcceptsCancelled = !EventConfig.DEFAULT_ACCEPTS_CANCELLED;
    final EventConfig acceptsCancelledConfig = config.acceptsCancelled(newAcceptsCancelled);
    assertEquals(newAcceptsCancelled, acceptsCancelledConfig.acceptsCancelled());
    assertNotEquals(config, acceptsCancelledConfig);
    assertNotSame(config, acceptsCancelledConfig);

    final boolean newExact = !EventConfig.DEFAULT_EXACT;
    final EventConfig exactConfig = config.exact(newExact);
    assertEquals(newExact, exactConfig.acceptsCancelled());
    assertNotEquals(config, exactConfig);
    assertNotSame(config, exactConfig);
  }

  @Test
  void testCreate() {
    final EventConfig created = EventConfig.of(65, false, true);
    assertNotSame(EventConfig.defaults(), created);
    assertEquals(65, created.priority());
    assertFalse(created.acceptsCancelled());
    assertTrue(created.exact());

    final EventConfig sameAsDefault = EventConfig.of(
      EventConfig.DEFAULT_PRIORITY,
      EventConfig.DEFAULT_ACCEPTS_CANCELLED,
      EventConfig.DEFAULT_EXACT
    );
    assertNotSame(EventConfig.defaults(), sameAsDefault);
  }

  @Test
  void testBuilder() {
    final EventConfig.Builder builder = EventConfig.builder();
    assertSame(EventConfig.defaults(), builder.build());

    builder.priority(100);
    builder.acceptsCancelled(false);
    builder.exact(true);
    final EventConfig built = builder.build();
    assertEquals(built, EventConfig.of(100, false, true));

    builder.order(200);
    assertEquals(200, builder.build().order());
    assertEquals(200, builder.build().priority());

    assertNotSame(builder.build(), builder.build());
  }
}
