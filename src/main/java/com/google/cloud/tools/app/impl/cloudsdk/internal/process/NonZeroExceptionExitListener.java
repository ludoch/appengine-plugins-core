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

package com.google.cloud.tools.app.impl.cloudsdk.internal.process;

import com.google.cloud.tools.app.api.AppEngineException;

/**
 * Exit listener that throws a {@link AppEngineException} on a non-zero exit value.
 */
// Is this being used by the clients?  I'm not a big fan of unused code and it has no tests.
public class NonZeroExceptionExitListener implements ProcessExitListener {

  @Override
  public void onExit(int exitCode) {
    if (exitCode != 0) {
      throw new AppEngineException("Non zero exit: " + exitCode);
    }
  }

}