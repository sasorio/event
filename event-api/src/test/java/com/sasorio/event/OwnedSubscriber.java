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

import java.util.UUID;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

public class OwnedSubscriber<E> implements EventSubscriber<E> {
  public static <E> Predicate<EventSubscription<? super E>> unsubscribeOwner(final UUID owner) {
    return subscription -> {
      final EventSubscriber<? super E> subscriber = subscription.subscriber();
      return subscriber instanceof OwnedSubscriber<?> && ((OwnedSubscriber<?>) subscriber).owner.equals(owner);
    };
  }

  public final UUID owner;
  public final EventSubscriber<E> body;

  public OwnedSubscriber(final UUID owner, final EventSubscriber<E> body) {
    this.owner = owner;
    this.body = body;
  }

  @Override
  public void on(final @NotNull E event) throws Throwable {
    this.body.on(event);
  }
}
