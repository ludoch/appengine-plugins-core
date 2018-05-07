/*
 * Copyright 2018 Google LLC.
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
 
package com.google.cloud.tools.appengine.api.deploy;

import org.junit.Assert;
import org.junit.Test;

public class DefaultStageStandardConfigurationTest {

  private DefaultStageStandardConfiguration config = new DefaultStageStandardConfiguration();

  @Test
  public void testJarSplitting() {
    Assert.assertTrue(config.getEnableJarSplitting());
    config.setEnableJarSplitting(false);
    Assert.assertFalse(config.getEnableJarSplitting());
    config.setEnableJarSplitting(true);
    Assert.assertTrue(config.getEnableJarSplitting());
  }

  @Test
  public void testDeleteJsps() {
    Assert.assertTrue(config.getDeleteJsps());
    config.setDeleteJsps(false);
    Assert.assertFalse(config.getDeleteJsps());
    config.setDeleteJsps(true);
    Assert.assertTrue(config.getDeleteJsps());
  }

  @Test
  public void testJarClasses() {
    Assert.assertFalse(config.getEnableJarClasses());
    config.setEnableJarClasses(true);
    Assert.assertTrue(config.getEnableJarClasses());
    config.setEnableJarClasses(false);
    Assert.assertFalse(config.getEnableJarClasses());
  }

  @Test
  public void testJarJsps() {
    Assert.assertFalse(config.getDisableJarJsps());
    config.setDisableJarJsps(true);
    Assert.assertTrue(config.getDisableJarJsps());
    config.setDisableJarJsps(false);
    Assert.assertFalse(config.getDisableJarJsps());
  }
}
