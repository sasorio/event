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
package com.sasorio.event.registry;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

// based on code from sasorio/commons
final class Internals {
  private Internals() {
  }

  @SuppressWarnings("unchecked")
  static <T> @NotNull List<Class<? super T>> ancestors(final @NotNull Class<T> type) {
    final List<Class<? super T>> types = new ArrayList<>();
    types.add(type);
    for (int i = 0; i < types.size(); i++) {
      final Class<?> next = types.get(i);
      final Class<?> superclass = next.getSuperclass();
      if (superclass != null) {
        types.add((Class<? super T>) superclass);
      }
      final Class<?>[] interfaces = next.getInterfaces();
      for (final Class<?> iface : interfaces) {
        // we have a list because we want to preserve order, but we don't want duplicates
        if (!types.contains(iface)) {
          types.add((Class<? super T>) iface);
        }
      }
    }
    return types;
  }
}
