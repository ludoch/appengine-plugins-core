/*
 * Copyright 2018 Google Inc.
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
import com.google.cloud.tools.managedcloudsdk.ProgressListener;
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.command.CommandRunner;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link com.google.cloud.tools.managedcloudsdk.components.SdkUpdater} */
@RunWith(MockitoJUnitRunner.class)
public class SdkUpdaterTest {

  @Mock private ConsoleListener mockConsoleListener;
  @Mock private ProgressListener mockProgressListener;
  @Mock private CommandRunner mockCommandRunner;
  @Mock private BundledPythonCopier mockBundledPythonCopier;
  @Mock private Map<String, String> mockPythonEnv;

  private Path fakeGcloudPath;

  @Before
  public void setUpFakesAndMocks()
      throws InterruptedException, CommandExitException, CommandExecutionException {
    Path root = FileSystems.getDefault().getRootDirectories().iterator().next();
    fakeGcloudPath = root.resolve("my/path/to/fake-gcloud");
    Mockito.when(mockBundledPythonCopier.copyPython()).thenReturn(mockPythonEnv);
  }

  @Test
  public void testUpdate_successRun()
      throws InterruptedException, CommandExitException, CommandExecutionException {
    SdkUpdater testUpdater = new SdkUpdater(fakeGcloudPath, mockCommandRunner, null);
    testUpdater.update(mockProgressListener, mockConsoleListener);
    Mockito.verify(mockProgressListener).start(Mockito.anyString(), Mockito.eq(-1L));
    Mockito.verify(mockProgressListener).done();
    Mockito.verify(mockCommandRunner)
        .run(
            Mockito.eq(expectedCommand()),
            Mockito.nullable(Path.class),
            Mockito.<Map<String, String>>any(),
            Mockito.eq(mockConsoleListener));
  }

  @Test
  public void testUpdate_withBundledPythonCopier()
      throws InterruptedException, CommandExitException, CommandExecutionException {
    SdkUpdater testUpdater =
        new SdkUpdater(fakeGcloudPath, mockCommandRunner, mockBundledPythonCopier);
    testUpdater.update(mockProgressListener, mockConsoleListener);
    Mockito.verify(mockProgressListener).start(Mockito.anyString(), Mockito.eq(-1L));
    Mockito.verify(mockProgressListener).done();
    Mockito.verify(mockCommandRunner)
        .run(
            Mockito.eq(expectedCommand()),
            Mockito.nullable(Path.class),
            Mockito.eq(mockPythonEnv),
            Mockito.eq(mockConsoleListener));
  }

  @Test
  public void testInstallComponent_workingDirectorySet()
      throws InterruptedException, CommandExitException, CommandExecutionException {
    SdkUpdater testUpdater = new SdkUpdater(fakeGcloudPath, mockCommandRunner, null);
    testUpdater.update(mockProgressListener, mockConsoleListener);
    Mockito.verify(mockCommandRunner)
        .run(
            Mockito.anyList(),
            Mockito.eq(fakeGcloudPath.getRoot()),
            Mockito.<Map<String, String>>any(),
            Mockito.any(ConsoleListener.class));
  }

  private List<String> expectedCommand() {
    return Arrays.asList(fakeGcloudPath.toString(), "components", "update", "--quiet");
  }
}
