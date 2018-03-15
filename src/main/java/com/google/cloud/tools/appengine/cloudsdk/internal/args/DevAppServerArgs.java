/*
 * Copyright 2016 Google Inc.
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

package com.google.cloud.tools.appengine.cloudsdk.internal.args;

import com.google.cloud.tools.appengine.api.Configuration;
import com.google.common.collect.Lists;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Command Line argument helper for dev_appserver based command. */
public class DevAppServerArgs {

  /** Returns {@code [--name=value]} or {@code []} if value=null. */
  public static List<String> get(String name, String value) {
    return Args.stringWithEq(name, value);
  }

  /** Returns {@code [--name=value1, --name=value2, ...]} or {@code []} if value=null. */
  public static List<String> get(String name, List<String> values) {
    return Args.stringsWithEq(name, values);
  }

  /** Returns {@code [--name=value]} or {@code []} if value=null. */
  public static List<String> get(String name, Integer value) {
    return Args.integerWithEq(name, value);
  }

  /**
   * Returns {@code [--name=true]} if value=true, {@code [--name=false]} if value=false, {@code []}
   * if value=null.
   */
  public static List<String> get(String name, Boolean value) {
    if (value == null) {
      return Collections.emptyList();
    }
    return Arrays.asList("--" + name + "=" + value.toString());
  }

  /** Returns {@code [--name=filePath]} or {@code []} if file=null. */
  public static List<String> get(String name, File file) {
    if (file != null) {
      Path path = file.toPath();
      if (!path.toString().isEmpty()) {
        return Arrays.asList("--" + name + "=" + path.toString());
      }
    }
    return Collections.emptyList();
  }

  /**
   * Returns {@code [--name, key1=val1, --name, key2=val2, ...]} or {@code []} if
   * keyValues=empty/null.
   */
  public static List<String> get(String name, Map<String, String> keyValues) {
    return Args.flaggedKeyValues(name, keyValues);
  }


  /** Returns a list of args for the common arguments in {@link Configuration}. */
  public static List<String> get(Configuration configuration) {
    List<String> result = new ArrayList<>();

    // Set the application, overriding "application" in appengine-web.xml.
    result.addAll(get("application", configuration.getProject()));

    return result;
  }
}
