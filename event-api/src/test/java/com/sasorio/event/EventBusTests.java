package com.sasorio.event;

import com.sasorio.event.bus.EventBus;
import com.sasorio.event.bus.SimpleEventBus;
import com.sasorio.event.registry.EventRegistry;
import com.sasorio.event.registry.SimpleEventRegistry;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NullMarked
class EventBusTests {

  @Test
  void testEmit() {
    EventRegistry<Event> registry = new SimpleEventRegistry<>(Event.class);
    EventBus<Event> bus = new SimpleEventBus<>(registry, new ExceptionHandler(), e -> e.cancelled);
    EventConfig config = EventConfig.defaults().acceptsCancelled(false);

    Event event = new Event();
    bus.emit(event);
    assertFalse(event.cancelled);
    assertTrue(event.handledBy.isEmpty());

    registry.subscribe(Event.class, config, new EventHandler("First", false, false));
    bus.emit(event);
    assertFalse(event.cancelled);
    assertEquals(1, event.handledBy.size());

    event = new Event();
    registry.subscribe(Event.class, config, new EventHandler("Second", true, false));
    bus.emit(event);
    assertTrue(event.cancelled);
    assertEquals(2, event.handledBy.size());

    event = new Event();
    registry.unsubscribeIf(sub -> sub.subscriber() instanceof EventHandler handler && handler.cancelIt);
    registry.subscribe(Event.class, config, new EventHandler("Third", true, false));
    registry.subscribe(Event.class, config, new EventHandler("Fourth", false, false));
    registry.subscribe(Event.class, config.acceptsCancelled(true), new EventHandler("ForthAndAHalf", false, false));
    bus.emit(event);
    assertTrue(event.cancelled);
    assertEquals(3, event.handledBy.size());
    assertEquals("First", event.handledBy.get(0).name());
    assertEquals("Third", event.handledBy.get(1).name());
    assertEquals("ForthAndAHalf", event.handledBy.get(2).name());

    event = new Event();
    registry.unsubscribeIf(sub -> sub.subscriber() instanceof EventHandler handler && handler.cancelIt);
    bus.emit(event);
    assertFalse(event.cancelled);
    assertEquals(3, event.handledBy.size());
    assertEquals("First", event.handledBy.get(0).name());
    assertEquals("Fourth", event.handledBy.get(1).name());
    assertEquals("ForthAndAHalf", event.handledBy.get(2).name());

    final Event throwingEvent = new Event();
    registry.subscribe(Event.class, config, new EventHandler("Fifth", false, true));
    registry.subscribe(Event.class, config, new EventHandler("Sixth", true, false));
    assertThrows(TestThrewExceptionException.class, () -> bus.emit(throwingEvent));
    assertFalse(throwingEvent.cancelled);
    assertEquals(4, throwingEvent.handledBy.size());
    assertEquals("First", throwingEvent.handledBy.get(0).name());
    assertEquals("Fourth", throwingEvent.handledBy.get(1).name());
    assertEquals("ForthAndAHalf", event.handledBy.get(2).name());
    assertEquals("Fifth", throwingEvent.handledBy.get(3).name());
  }

  @Test
  void testEmitWithPriority() {
    EventRegistry<Event> registry = new SimpleEventRegistry<>(Event.class);
    EventBus<Event> bus = new SimpleEventBus<>(registry, new ExceptionHandler(), e -> false);
    registry.subscribe(Event.class, EventConfig.defaults().priority(20), new EventHandler(""));

    Event event = new Event();
    bus.emit(event, OptionalInt.of(15));
    assertTrue(event.handledBy.isEmpty());

    bus.emit(event, OptionalInt.of(20));
    assertEquals(1, event.handledBy.size());

    event = new Event();
    bus.emit(event, OptionalInt.of(25));
    assertTrue(event.handledBy.isEmpty());
  }

  @Test
  void testEmitWithExact() {
    EventRegistry<Event> registry = new SimpleEventRegistry<>(Event.class);
    EventBus<Event> bus = new SimpleEventBus<>(registry, new ExceptionHandler(), e -> false);
    registry.subscribe(Event.class, new EventHandler("Default"));
    registry.subscribe(Event.class, EventConfig.defaults().exact(true), new EventHandler("Exact"));

    Event event = new Event();
    bus.emit(event);
    assertEquals(2, event.handledBy.size());

    SubEvent subEvent = new SubEvent();
    bus.emit(subEvent);
    assertEquals(1, subEvent.handledBy.size());
    assertEquals("Default", subEvent.handledBy.get(0).name());
  }

  private static class Event {
    public boolean cancelled = false;
    public List<EventHandler> handledBy = new ArrayList<>();
  }

  private static class SubEvent extends Event {
  }

  private record EventHandler(String name, boolean cancelIt, boolean throwIt) implements EventSubscriber<Event> {
    EventHandler(String name) {
      this(name, false, false);
    }

    @Override
    public void on(Event event) throws Throwable {
      event.handledBy.add(this);
      if (cancelIt) {
        event.cancelled = true;
      }
      if (throwIt) {
        throw new IllegalStateException();
      }
    }
  }

  private static class ExceptionHandler implements EventBus.EventExceptionHandler {
    @Override
    public <E> void eventExceptionCaught(
      EventBus<? super E> bus,
      EventSubscription<? super E> subscription,
      E event,
      Throwable throwable
    ) {
      throw new TestThrewExceptionException(throwable);
    }
  }

  private static class TestThrewExceptionException extends RuntimeException {
    private static final @Serial long serialVersionUID = 236237843L;

    public TestThrewExceptionException(Throwable cause) {
      super(cause);
    }
  }
}
