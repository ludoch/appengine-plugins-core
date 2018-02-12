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
    Assert.assertTrue(config.getEnableJarClasses());
    config.setEnableJarClasses(false);
    Assert.assertFalse(config.getEnableJarClasses());
    config.setEnableJarClasses(true);
    Assert.assertTrue(config.getEnableJarClasses());
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
