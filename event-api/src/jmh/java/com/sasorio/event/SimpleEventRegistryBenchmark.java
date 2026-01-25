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

import com.sasorio.event.registry.SimpleEventRegistry;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NullMarked;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@NullMarked
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
public class SimpleEventRegistryBenchmark {
  @State(Scope.Thread)
  public static class CachedReadState {
    SimpleEventRegistry<BaseEvent> registry;

    @Setup(Level.Trial)
    public void setup() {
      this.registry = new SimpleEventRegistry<>(BaseEvent.class);
      seedRegistry(this.registry, 1);
      this.registry.subscriptions(EventA.class);
    }
  }

  @State(Scope.Thread)
  public static class WriteThenReadState {
    SimpleEventRegistry<BaseEvent> registry;

    @Setup(Level.Trial)
    public void setup() {
      this.registry = new SimpleEventRegistry<>(BaseEvent.class);
      seedRegistry(this.registry, 1);
      this.registry.subscriptions(EventA.class);
      this.registry.subscriptions(EventB.class);
    }
  }

  @State(Scope.Thread)
  public static class ColdState {
    SimpleEventRegistry<BaseEvent> registry;

    @Setup(Level.Invocation)
    public void setup() {
      this.registry = new SimpleEventRegistry<>(BaseEvent.class);
      seedRegistry(this.registry, 1);
    }
  }

  @State(Scope.Benchmark)
  public static class ContendedState {
    @Param({"32", "256"})
    int subscriberCount;
    SimpleEventRegistry<BaseEvent> registry;

    @Setup(Level.Trial)
    public void setup() {
      this.registry = new SimpleEventRegistry<>(BaseEvent.class);
      seedRegistry(this.registry, this.subscriberCount);
      this.registry.subscriptions(EventA.class);
      this.registry.subscriptions(EventB.class);
    }
  }

  @Benchmark
  public void cachedReads(final CachedReadState state, final Blackhole blackhole) {
    final List<EventSubscription<? super BaseEvent>> subscriptions = state.registry.subscriptions(EventA.class);
    blackhole.consume(subscriptions.size());
  }

  @Benchmark
  public void writeThenReadUnrelated(final WriteThenReadState state, final Blackhole blackhole) {
    final EventSubscription<EventA> temp = state.registry.subscribe(EventA.class, e -> {});
    blackhole.consume(state.registry.subscriptions(EventB.class).size());
    temp.dispose();
    blackhole.consume(state.registry.subscriptions(EventB.class).size());
  }

  @Benchmark
  public void coldSubscriptions(final ColdState state, final Blackhole blackhole) {
    blackhole.consume(state.registry.subscriptions(EventC.class).size());
  }

  @Group("contendedReadMostly")
  @GroupThreads(6)
  @Benchmark
  public void contendedReadMostly_read(final ContendedState state, final Blackhole blackhole) {
    blackhole.consume(state.registry.subscriptions(EventA.class).size());
  }

  @Benchmark
  @Group("contendedReadMostly")
  @GroupThreads(1)
  public void contendedReadMostly_write(final ContendedState state, final Blackhole blackhole) {
    final EventSubscription<EventA> temp = state.registry.subscribe(EventA.class, e -> {});
    blackhole.consume(state.registry.subscriptions(EventB.class).size());
    temp.dispose();
  }

  @Benchmark
  @Group("contendedWriteHeavy")
  @GroupThreads(2)
  public void contendedWriteHeavy_read(final ContendedState state, final Blackhole blackhole) {
    blackhole.consume(state.registry.subscriptions(EventA.class).size());
  }

  @Benchmark
  @Group("contendedWriteHeavy")
  @GroupThreads(2)
  public void contendedWriteHeavy_write(final ContendedState state, final Blackhole blackhole) {
    final EventSubscription<EventA> temp = state.registry.subscribe(EventA.class, e -> {});
    blackhole.consume(state.registry.subscriptions(EventB.class).size());
    temp.dispose();
  }

  private static void seedRegistry(final SimpleEventRegistry<BaseEvent> registry, final int perEventSubscribers) {
    for (int i = 0; i < perEventSubscribers; i++) {
      registry.subscribe(BaseEvent.class, e -> {});
      registry.subscribe(MarkerOne.class, e -> {});
      registry.subscribe(MarkerTwo.class, e -> {});
      registry.subscribe(EventA.class, e -> {});
      registry.subscribe(EventB.class, e -> {});
      registry.subscribe(EventC.class, e -> {});
    }
  }

  @NullMarked
  private interface BaseEvent {
  }

  @NullMarked
  private interface MarkerOne extends BaseEvent {
  }

  @NullMarked
  private interface MarkerTwo extends BaseEvent {
  }

  @NullMarked
  private static final class EventA implements MarkerOne, MarkerTwo {
  }

  @NullMarked
  private static class EventB implements MarkerTwo {
  }

  @NullMarked
  private static final class EventC extends EventB implements MarkerOne {
  }
}
