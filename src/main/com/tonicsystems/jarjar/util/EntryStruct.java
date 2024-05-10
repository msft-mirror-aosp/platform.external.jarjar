/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tonicsystems.jarjar.util;

import java.util.Arrays;
import java.util.Objects;

public class EntryStruct {
  public byte[] data;
  public String name;
  public long time;

  /** Returns true if the entry is a class file. */
  public boolean isClass() {
    if (!name.endsWith(".class")) {
      return false;
    }
    if (name.startsWith("META-INF/version")) {
      // TODO(b/69678527): handle multi-release jar files
      return false;
    }
    return true;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof EntryStruct)) {
      return false;
    }

    EntryStruct that  = (EntryStruct) other;
    return this.name.equals(that.name) &&
        Arrays.equals(this.data, that.data) &&
        this.time == that.time;
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(data), name, time);
  }
}
