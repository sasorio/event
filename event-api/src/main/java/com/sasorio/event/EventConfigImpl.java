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

import org.jspecify.annotations.NullMarked;

@NullMarked
record EventConfigImpl(
  int order,
  boolean acceptsCancelled,
  boolean exact
) implements EventConfig {
  static final EventConfigImpl DEFAULTS = new EventConfigImpl(DEFAULT_ORDER, DEFAULT_ACCEPTS_CANCELLED, DEFAULT_EXACT);

  static EventConfigImpl create(
    final int order,
    final boolean acceptsCancelled,
    final boolean exact
  ) {
    if (order == DEFAULT_ORDER && acceptsCancelled == DEFAULT_ACCEPTS_CANCELLED && exact == DEFAULT_EXACT) {
      return DEFAULTS;
    }
    return new EventConfigImpl(order, acceptsCancelled, exact);
  }

  @Override
  public EventConfig order(final int order) {
    return create(order, this.acceptsCancelled, this.exact);
  }

  @Override
  public EventConfig acceptsCancelled(final boolean acceptsCancelled) {
    return create(this.order, acceptsCancelled, this.exact);
  }

  @Override
  public EventConfig exact(final boolean exact) {
    return create(this.order, this.acceptsCancelled, exact);
  }

  @NullMarked
  static final class BuilderImpl implements Builder {
    private int order = DEFAULT_ORDER;
    private boolean acceptsCancelled = DEFAULT_ACCEPTS_CANCELLED;
    private boolean exact = DEFAULT_EXACT;

    @Override
    public Builder order(final int order) {
      this.order = order;
      return this;
    }

    @Override
    public Builder acceptsCancelled(final boolean acceptsCancelled) {
      this.acceptsCancelled = acceptsCancelled;
      return this;
    }

    @Override
    public Builder exact(final boolean exact) {
      this.exact = exact;
      return this;
    }

    @Override
    public EventConfig build() {
      return create(this.order, this.acceptsCancelled, this.exact);
    }
  }
}
