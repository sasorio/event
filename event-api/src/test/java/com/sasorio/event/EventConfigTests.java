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
    EventConfig config = EventConfig.defaults();
    assertEquals(EventConfig.DEFAULT_PRIORITY, config.priority());
    assertEquals(EventConfig.DEFAULT_ORDER, config.order());
    assertEquals(EventConfig.DEFAULT_ACCEPTS_CANCELLED, config.acceptsCancelled());
    assertEquals(EventConfig.DEFAULT_EXACT, config.exact());

    // Multiple calls to #defaults should yield the same object.
    assertSame(EventConfig.defaults(), EventConfig.defaults());
  }

  @Test
  void testValueSetters() {
    EventConfig config = EventConfig.defaults();

    int newPriority = EventConfig.DEFAULT_PRIORITY + 125;
    EventConfig priorityConfig = config.priority(newPriority);
    assertEquals(newPriority, priorityConfig.priority());
    assertNotEquals(config, priorityConfig);
    assertNotSame(config, priorityConfig);

    int newOrder = EventConfig.DEFAULT_ORDER - 125;
    EventConfig orderConfig = config.order(newOrder);
    assertEquals(newOrder, orderConfig.order());
    assertEquals(newOrder, orderConfig.priority());
    assertNotEquals(config, orderConfig);
    assertNotSame(config, orderConfig);

    boolean newAcceptsCancelled = !EventConfig.DEFAULT_ACCEPTS_CANCELLED;
    EventConfig acceptsCancelledConfig = config.acceptsCancelled(newAcceptsCancelled);
    assertEquals(newAcceptsCancelled, acceptsCancelledConfig.acceptsCancelled());
    assertNotEquals(config, acceptsCancelledConfig);
    assertNotSame(config, acceptsCancelledConfig);

    boolean newExact = !EventConfig.DEFAULT_EXACT;
    EventConfig exactConfig = config.exact(newExact);
    assertEquals(newExact, exactConfig.acceptsCancelled());
    assertNotEquals(config, exactConfig);
    assertNotSame(config, exactConfig);
  }

  @Test
  void testCreate() {
    EventConfig created = EventConfig.of(65, false, true);
    assertNotSame(EventConfig.defaults(), created);
    assertEquals(65, created.priority());
    assertFalse(created.acceptsCancelled());
    assertTrue(created.exact());

    EventConfig sameAsDefault = EventConfig.of(
      EventConfig.DEFAULT_PRIORITY,
      EventConfig.DEFAULT_ACCEPTS_CANCELLED,
      EventConfig.DEFAULT_EXACT
    );
    assertNotSame(EventConfig.defaults(), sameAsDefault);
  }

  @Test
  void testBuilder() {
    EventConfig.Builder builder = EventConfig.builder();
    assertSame(EventConfig.defaults(), builder.build());

    builder.priority(100);
    builder.acceptsCancelled(false);
    builder.exact(true);
    EventConfig built = builder.build();
    assertEquals(built, EventConfig.of(100, false, true));

    builder.order(200);
    assertEquals(200, builder.build().order());
    assertEquals(200, builder.build().priority());

    assertNotSame(builder.build(), builder.build());
  }
}
