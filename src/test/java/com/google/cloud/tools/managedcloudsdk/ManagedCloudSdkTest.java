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

package com.google.cloud.tools.managedcloudsdk;

import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.command.CommandRunner;
import com.google.cloud.tools.managedcloudsdk.components.SdkComponent;
import com.google.cloud.tools.managedcloudsdk.components.WindowsBundledPythonCopierTestHelper;
import com.google.cloud.tools.managedcloudsdk.install.SdkInstallerException;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/** Tests for full functionality of the {@link ManagedCloudSdk}. */
public class ManagedCloudSdkTest {

  @Rule public TemporaryFolder tempDir = new TemporaryFolder();

  private static final String FIXED_VERSION = "178.0.0";
  private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();
  private final MessageCollector testListener = new MessageCollector();
  private final ProgressListener testProgressListener = new NullProgressListener();
  private final SdkComponent testComponent = SdkComponent.APP_ENGINE_JAVA;

  @Rule
  public TestWatcher watchman =
      new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
          System.out.println(description + "\n" + testListener.getOutput());
        }
      };

  private Path userHome;
  private final Properties fakeProperties = new Properties();

  @Before
  public void setUp() {
    userHome = tempDir.getRoot().toPath();
    fakeProperties.put("user.home", userHome.toString());
  }

  @Test
  public void testManagedCloudSdk_fixedVersion()
      throws BadCloudSdkVersionException, UnsupportedOsException, IOException, CommandExitException,
          InterruptedException, ManagedSdkVerificationException, ManagedSdkVersionMismatchException,
          CommandExecutionException, SdkInstallerException {
    ManagedCloudSdk testSdk =
        new ManagedCloudSdk(new Version(FIXED_VERSION), userHome, OsInfo.getSystemOsInfo());

    Assert.assertFalse(testSdk.isInstalled());
    Assert.assertFalse(testSdk.hasComponent(testComponent));
    Assert.assertFalse(testSdk.isUpToDate());

    testSdk.newInstaller().install(testProgressListener, testListener);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertFalse(testSdk.hasComponent(testComponent));
    Assert.assertTrue(testSdk.isUpToDate());

    testSdk
        .newComponentInstaller()
        .installComponent(testComponent, testProgressListener, testListener);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertTrue(testSdk.hasComponent(testComponent));
    Assert.assertTrue(testSdk.isUpToDate());

    // Make sure we can't update a versioned cloud SDK.
    try {
      testSdk.newUpdater();
      Assert.fail("UnsupportedOperationException expected but not thrown");
    } catch (UnsupportedOperationException ex) {
      Assert.assertEquals("Cannot update a fixed version SDK.", ex.getMessage());
    }
  }

  @Test
  public void testManagedCloudSdk_latest()
      throws UnsupportedOsException, ManagedSdkVerificationException,
          ManagedSdkVersionMismatchException, InterruptedException, CommandExecutionException,
          CommandExitException, IOException, SdkInstallerException {
    ManagedCloudSdk testSdk =
        new ManagedCloudSdk(Version.LATEST, userHome, OsInfo.getSystemOsInfo());

    Assert.assertFalse(testSdk.isInstalled());
    Assert.assertFalse(testSdk.isUpToDate());

    testSdk.newInstaller().install(testProgressListener, testListener);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertTrue(testSdk.isUpToDate());

    // Forcibly downgrade the cloud SDK so we can test updating.
    downgradeCloudSdk(testSdk);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertFalse(testSdk.isUpToDate());

    testSdk.newUpdater().update(testProgressListener, testListener);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertFalse(testSdk.hasComponent(testComponent));
    Assert.assertTrue(testSdk.isUpToDate());

    testSdk
        .newComponentInstaller()
        .installComponent(testComponent, testProgressListener, testListener);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertTrue(testSdk.hasComponent(testComponent));
    Assert.assertTrue(testSdk.isUpToDate());
  }

  private static final Path CLOUD_SDK_PARTIAL_PATH = Paths.get("google/ct4j-cloud-sdk");

  @Test
  public void testGetOsSpecificManagedSdk_windowsStandard() throws IOException {
    Path localAppData = Files.createDirectories(userHome.resolve("AppData").resolve("Local"));
    Path windowsPath =
        ManagedCloudSdk.getOsSpecificManagedSdkHome(
            OsInfo.Name.WINDOWS,
            fakeProperties,
            ImmutableMap.of("LOCALAPPDATA", localAppData.toString()));

    Assert.assertEquals(localAppData.resolve(CLOUD_SDK_PARTIAL_PATH), windowsPath);
  }

  @Test
  public void testGetOsSpecificManagedSdk_macStandard() throws IOException {
    Path expectedPath =
        Files.createDirectories(userHome.resolve("Library").resolve("Application Support"));
    Path macPath =
        ManagedCloudSdk.getOsSpecificManagedSdkHome(OsInfo.Name.MAC, fakeProperties, EMPTY_MAP);
    Assert.assertEquals(expectedPath.resolve(CLOUD_SDK_PARTIAL_PATH), macPath);
  }

  @Test
  public void testGetOsSpecificManagedSdk_linuxStandard() {
    Path expectedPath = userHome.resolve(".cache").resolve(CLOUD_SDK_PARTIAL_PATH);

    Path linuxPath =
        ManagedCloudSdk.getOsSpecificManagedSdkHome(OsInfo.Name.LINUX, fakeProperties, EMPTY_MAP);
    Assert.assertEquals(expectedPath, linuxPath);
  }

  @Test
  public void testGetOsSpecificManagedSdk_windowsFallbackLocalAppDataEnvNotSet() {
    Path expectedPath = userHome.resolve(".cache").resolve(CLOUD_SDK_PARTIAL_PATH);

    Path windowsPath =
        ManagedCloudSdk.getOsSpecificManagedSdkHome(OsInfo.Name.WINDOWS, fakeProperties, EMPTY_MAP);

    Assert.assertEquals(expectedPath, windowsPath);
  }

  @Test
  public void testGetOsSpecificManagedSdk_windowsFallbackLocalAppDataDoesntExist() {
    Path localAppData = userHome.resolve("AppData").resolve("Local"); // not created
    Path expectedPath = userHome.resolve(".cache").resolve(CLOUD_SDK_PARTIAL_PATH);

    Path windowsPath =
        ManagedCloudSdk.getOsSpecificManagedSdkHome(
            OsInfo.Name.WINDOWS,
            fakeProperties,
            ImmutableMap.of("LOCALAPPDATA", localAppData.toString()));

    Assert.assertEquals(expectedPath, windowsPath);
  }

  @Test
  public void testGetOsSpecificManagedSdk_macFallback() {
    Path expectedPath = userHome.resolve(".cache").resolve(CLOUD_SDK_PARTIAL_PATH);

    Path macPath =
        ManagedCloudSdk.getOsSpecificManagedSdkHome(OsInfo.Name.MAC, fakeProperties, EMPTY_MAP);
    Assert.assertEquals(expectedPath, macPath);
  }

  @Test
  public void testGetOsSpecificManagedSdkCandidates_windowsStandard() throws IOException {
    Path localAppData = Files.createDirectories(userHome.resolve("AppData").resolve("Local"));
    Path xdgPath = userHome.resolve(".cache").resolve(Paths.get("a/bc"));

    List<Path> candidates =
        ManagedCloudSdk.getOsSpecificManagedSdkHomeCandidates(
            OsInfo.Name.WINDOWS,
            fakeProperties,
            ImmutableMap.of("LOCALAPPDATA", localAppData.toString()),
            Paths.get("a/bc"));
    Assert.assertEquals(Arrays.asList(localAppData.resolve("a/bc"), xdgPath), candidates);
  }

  @Test
  public void testGetOsSpecificManagedSdkCandidates_windowsLocalAppDataEnvNotSet() {
    Path xdgPath = userHome.resolve(".cache").resolve(Paths.get("a/bc"));

    List<Path> candidates =
        ManagedCloudSdk.getOsSpecificManagedSdkHomeCandidates(
            OsInfo.Name.WINDOWS, fakeProperties, EMPTY_MAP, Paths.get("a/bc"));
    Assert.assertEquals(Arrays.asList(xdgPath), candidates);
  }

  @Test
  public void testGetOsSpecificManagedSdkCandidates_windowsLocalAppDataDoesntExist() {
    Path localAppData = userHome.resolve("AppData").resolve("Local"); // not created
    Path xdgPath = userHome.resolve(".cache").resolve(Paths.get("a/bc"));

    List<Path> candidates =
        ManagedCloudSdk.getOsSpecificManagedSdkHomeCandidates(
            OsInfo.Name.WINDOWS,
            fakeProperties,
            ImmutableMap.of("LOCALAPPDATA", localAppData.toString()),
            Paths.get("a/bc"));
    Assert.assertEquals(Arrays.asList(xdgPath), candidates);
  }

  @Test
  public void testGetOsSpecificManagedSdkCandidates_linux() {
    Path xdgPath = userHome.resolve(".cache").resolve(Paths.get("a/bc"));

    List<Path> candidates =
        ManagedCloudSdk.getOsSpecificManagedSdkHomeCandidates(
            OsInfo.Name.LINUX, fakeProperties, EMPTY_MAP, Paths.get("a/bc"));

    Assert.assertEquals(Arrays.asList(xdgPath), candidates);
  }

  @Test
  public void testGetOsSpecificManagedSdkCandidates_macStandard() throws IOException {
    Path applicationSupport =
        Files.createDirectories(userHome.resolve("Library").resolve("Application Support"));
    Path xdgPath = userHome.resolve(".cache").resolve(Paths.get("a/bc"));

    List<Path> candidates =
        ManagedCloudSdk.getOsSpecificManagedSdkHomeCandidates(
            OsInfo.Name.MAC, fakeProperties, EMPTY_MAP, Paths.get("a/bc"));

    Assert.assertEquals(Arrays.asList(applicationSupport.resolve("a/bc"), xdgPath), candidates);
  }

  @Test
  public void testGetOsSpecificManagedSdkCandidates_macFallback() {
    Path xdgPath = userHome.resolve(".cache").resolve(Paths.get("a/bc"));

    List<Path> candidates =
        ManagedCloudSdk.getOsSpecificManagedSdkHomeCandidates(
            OsInfo.Name.MAC, fakeProperties, EMPTY_MAP, Paths.get("a/bc"));

    Assert.assertEquals(Arrays.asList(xdgPath), candidates);
  }

  @Test
  public void testGetOsSpecificDeprecatedManagedSdkCandidates_windowsStandard() throws IOException {
    Path localAppData = Files.createDirectories(userHome.resolve("AppData").resolve("Local"));
    Path xdgPath = userHome.resolve(".cache").resolve("google-cloud-tools-java");

    List<Path> candidates =
        ManagedCloudSdk.getOsSpecificDeprecatedManagedSdkRoots(
            OsInfo.Name.WINDOWS,
            fakeProperties,
            ImmutableMap.of("LOCALAPPDATA", localAppData.toString()));
    Assert.assertEquals(
        Arrays.asList(localAppData.resolve("google-cloud-tools-java"), xdgPath), candidates);
  }

  @Test
  public void testGetOsSpecificDeprecatedManagedSdkCandidates_windowsLocalAppDataEnvNotSet() {
    Path xdgPath = userHome.resolve(".cache").resolve("google-cloud-tools-java");

    List<Path> candidates =
        ManagedCloudSdk.getOsSpecificDeprecatedManagedSdkRoots(
            OsInfo.Name.WINDOWS, fakeProperties, EMPTY_MAP);
    Assert.assertEquals(Arrays.asList(xdgPath), candidates);
  }

  @Test
  public void testGetOsSpecificDeprecatedManagedSdkCandidates_windowsLocalAppDataDoesntExist() {
    Path localAppData = userHome.resolve("AppData").resolve("Local"); // not created
    Path xdgPath = userHome.resolve(".cache").resolve("google-cloud-tools-java");

    List<Path> candidates =
        ManagedCloudSdk.getOsSpecificDeprecatedManagedSdkRoots(
            OsInfo.Name.WINDOWS,
            fakeProperties,
            ImmutableMap.of("LOCALAPPDATA", localAppData.toString()));
    Assert.assertEquals(Arrays.asList(xdgPath), candidates);
  }

  @Test
  public void testGetOsSpecificDeprecatedManagedSdkCandidates_linux() {
    Path xdgPath = userHome.resolve(".cache").resolve("google-cloud-tools-java");

    List<Path> candidates =
        ManagedCloudSdk.getOsSpecificDeprecatedManagedSdkRoots(
            OsInfo.Name.LINUX, fakeProperties, EMPTY_MAP);

    Assert.assertEquals(Arrays.asList(xdgPath), candidates);
  }

  @Test
  public void testGetOsSpecificDeprecatedManagedSdkCandidates_macStandard() throws IOException {
    Path macSpecial =
        Files.createDirectories(userHome.resolve("Library").resolve("Application Support"));
    Path xdgPath = userHome.resolve(".cache").resolve("google-cloud-tools-java");

    List<Path> candidates =
        ManagedCloudSdk.getOsSpecificDeprecatedManagedSdkRoots(
            OsInfo.Name.MAC, fakeProperties, EMPTY_MAP);

    Assert.assertEquals(
        Arrays.asList(macSpecial.resolve("google-cloud-tools-java"), xdgPath), candidates);
  }

  @Test
  public void testGetOsSpecificDeprecatedManagedSdkCandidates_macFallback() {
    Path xdgPath = userHome.resolve(".cache").resolve("google-cloud-tools-java");

    List<Path> candidates =
        ManagedCloudSdk.getOsSpecificDeprecatedManagedSdkRoots(
            OsInfo.Name.MAC, fakeProperties, EMPTY_MAP);

    Assert.assertEquals(Arrays.asList(xdgPath), candidates);
  }

  private void downgradeCloudSdk(ManagedCloudSdk testSdk)
      throws InterruptedException, CommandExitException, CommandExecutionException,
          UnsupportedOsException {
    Map<String, String> env = null;
    if (OsInfo.getSystemOsInfo().name().equals(OsInfo.Name.WINDOWS)) {
      env = WindowsBundledPythonCopierTestHelper.newInstance(testSdk.getGcloud()).copyPython();
    }
    CommandRunner.newRunner()
        .run(
            Arrays.asList(
                testSdk.getGcloud().toString(),
                "components",
                "update",
                "--quiet",
                "--version=" + FIXED_VERSION),
            null,
            env,
            testListener);
  }
}
