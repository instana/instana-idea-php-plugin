package com.instana.idea.php.codeInspection;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.php.composer.ComposerDataService;

public class ComposerPackageInspectionTest extends BasePlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/php/codeInspection";
    }

    public void testInspectionIsVisibleWhenTracerIsUsed() {
        final ComposerDataService instance = ComposerDataService.getInstance(myFixture.getProject());
        final VirtualFile projectDir = myFixture.copyDirectoryToProject("inspection_trigger", "");

        final String configPath = myFixture.getTestDataPath() + "/inspection_trigger/composer.json";

        instance.setConfigPathAndLibraryUpdateStatus(configPath, true);

        myFixture.configureFromExistingVirtualFile(projectDir.findChild("index.php"));

        myFixture.enableInspections(new ComposerPackageInspection());

        myFixture.checkHighlighting();
    }

    public void testInspectionIsNotVisibleWhenTracerIsUsed() {
        final ComposerDataService instance = ComposerDataService.getInstance(myFixture.getProject());
        final VirtualFile projectDir = myFixture.copyDirectoryToProject("inspection_doesnt_trigger", "");

        final String configPath = myFixture.getTestDataPath() + "/inspection_doesnt_trigger/composer.json";

        instance.setConfigPathAndLibraryUpdateStatus(configPath, true);

        myFixture.configureFromExistingVirtualFile(projectDir.findChild("index.php"));

        myFixture.enableInspections(new ComposerPackageInspection());

        myFixture.checkHighlighting();
    }

    public void testInspectionDoesntTriggerOnRandomFunctionCalls() {
        final ComposerDataService instance = ComposerDataService.getInstance(myFixture.getProject());
        final VirtualFile projectDir = myFixture.copyDirectoryToProject("inspection_random_call", "");

        final String configPath = myFixture.getTestDataPath() + "/inspection_random_call/composer.json";

        instance.setConfigPathAndLibraryUpdateStatus(configPath, true);

        myFixture.configureFromExistingVirtualFile(projectDir.findChild("index.php"));

        myFixture.enableInspections(new ComposerPackageInspection());

        myFixture.checkHighlighting();
    }
}
