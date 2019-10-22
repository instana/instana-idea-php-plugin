package com.instana.idea.php;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.php.lang.PhpFileType;

public class WrapInSpanIntentionActionTest extends BasePlatformTestCase {
    public void testShouldBeAvailableAndVisible() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php\n" +
                "$foo->callX();");
    }
}
