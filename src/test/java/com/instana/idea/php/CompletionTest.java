package com.instana.idea.php;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class CompletionTest extends BasePlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/php/completion";
    }

    public void testStubsAreProvided() {
        myFixture.testCompletion("tracer.php", "tracer.after.php");
    }
}
