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
import org.jspecify.annotations.NullMarked;

import static org.junit.jupiter.api.Assertions.fail;

@NullMarked
public final class TestFailingEventExceptionHandler implements EventBus.EventExceptionHandler {
  public static final TestFailingEventExceptionHandler INSTANCE = new TestFailingEventExceptionHandler();

  private TestFailingEventExceptionHandler() {
  }

  @Override
  public <E> void eventExceptionCaught(final EventSubscription<? super E> subscription, final E event, final Throwable throwable) {
    fail(subscription + " failed", throwable);
  }
}
