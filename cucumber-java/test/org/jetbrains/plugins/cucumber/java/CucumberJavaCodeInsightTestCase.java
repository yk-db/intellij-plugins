package org.jetbrains.plugins.cucumber.java;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;

/**
 * User: zolotov
 * Date: 10/7/13
 */
abstract public class CucumberJavaCodeInsightTestCase extends CucumberCodeInsightTestCase {
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumberProjectDescriptor();
  }
}
