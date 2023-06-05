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

/**
 * Something that can be cancelled.
 *
 * @since 1.0.0
 */
public interface Cancellable {
  /**
   * Gets the cancelled state.
   *
   * @return the cancelled state
   * @since 1.0.0
   */
  boolean cancelled();

  /**
   * Sets the cancelled state.
   *
   * @param cancelled the cancelled state
   * @since 1.0.0
   */
  void cancelled(final boolean cancelled);
}
