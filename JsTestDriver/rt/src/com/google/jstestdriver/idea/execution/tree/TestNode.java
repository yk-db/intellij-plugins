package com.google.jstestdriver.idea.execution.tree;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class TestNode extends AbstractNodeWithParent<TestNode> {

  public TestNode(@NotNull String name, @NotNull TestCaseNode parent) {
    super(name, parent);
  }

}