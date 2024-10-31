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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public final class StandaloneJarProcessor {
  public static void run(File from, File to, JarProcessor proc) throws IOException {
    byte[] buf = new byte[0x2000];

    JarFile in = new JarFile(from);
    final File tmpTo = File.createTempFile("jarjar", ".jar");
    JarOutputStream out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(tmpTo)));
    Map<String, EntryStruct> entries = new HashMap<>();
    try {
      EntryStruct struct = new EntryStruct();
      Enumeration<JarEntry> e = in.entries();
      while (e.hasMoreElements()) {
        JarEntry entry = e.nextElement();
        struct.name = entry.getName();
        struct.time = entry.getTime();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IoUtil.pipe(in.getInputStream(entry), baos, buf);
        struct.data = baos.toByteArray();
        if (proc.process(struct)) {
          EntryStruct existEntry = entries.putIfAbsent(struct.name, struct);
          if (existEntry == null) {
            entry = new JarEntry(struct.name);
            entry.setTime(struct.time);
            entry.setCompressedSize(-1);
            out.putNextEntry(entry);
            out.write(struct.data);
          } else if (struct.name.endsWith("/")) {
            // TODO(chrisn): log
          } else if (!existEntry.equals(struct)) {
            throw new IllegalArgumentException("Duplicate jar entries: " + struct.name);
          }
        }
      }

    } finally {
      in.close();
      out.close();
    }

    // delete the empty directories
    IoUtil.copyZipWithoutEmptyDirectories(tmpTo, to);
    tmpTo.delete();
  }

  private StandaloneJarProcessor() {}
}
