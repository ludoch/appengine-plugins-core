/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.managedcloudsdk.components;

import com.google.cloud.tools.managedcloudsdk.ConsoleListener;
import com.google.cloud.tools.managedcloudsdk.OsInfo;
import com.google.cloud.tools.managedcloudsdk.ProgressListener;
import com.google.cloud.tools.managedcloudsdk.command.CommandCaller;
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.command.CommandRunner;
import com.google.common.base.Preconditions;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** Update an SDK. */
public class SdkUpdater {

  private final Path gcloudPath;
  private final CommandRunner commandRunner;
  @Nullable private final BundledPythonCopier pythonCopier;

  SdkUpdater(
      Path gcloudPath, CommandRunner commandRunner, @Nullable BundledPythonCopier pythonCopier) {
    Preconditions.checkArgument(gcloudPath.isAbsolute());
    this.gcloudPath = gcloudPath;
    this.commandRunner = commandRunner;
    this.pythonCopier = pythonCopier;
  }

  /**
   * Update the Cloud SDK.
   *
   * @param progressListener listener to action progress feedback
   * @param consoleListener listener to process console feedback
   */
  public void update(ProgressListener progressListener, ConsoleListener consoleListener)
      throws InterruptedException, CommandExitException, CommandExecutionException {
    progressListener.start("Updating Cloud SDK", ProgressListener.UNKNOWN);

    Map<String, String> environment = null;
    if (pythonCopier != null) {
      environment = pythonCopier.copyPython();
    }

    Path workingDirectory = gcloudPath.getRoot();
    List<String> command = Arrays.asList(gcloudPath.toString(), "components", "update", "--quiet");
    commandRunner.run(command, workingDirectory, environment, consoleListener);
    progressListener.done();
  }

  /**
   * Configure and create a new Updater instance.
   *
   * @param gcloudPath path to gcloud in the Cloud SDK
   * @return a new configured Cloud SDK updater
   */
  public static SdkUpdater newUpdater(OsInfo.Name osName, Path gcloudPath) {
    switch (osName) {
      case WINDOWS:
        return new SdkUpdater(
            gcloudPath,
            CommandRunner.newRunner(),
            new WindowsBundledPythonCopier(gcloudPath, CommandCaller.newCaller()));
      default:
        return new SdkUpdater(gcloudPath, CommandRunner.newRunner(), null);
    }
  }
}
