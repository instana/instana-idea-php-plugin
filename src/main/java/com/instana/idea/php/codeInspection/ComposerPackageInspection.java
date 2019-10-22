package com.instana.idea.php.codeInspection;

import com.instana.idea.php.util.PhpUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.json.psi.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.PhpBundle;
import com.jetbrains.php.composer.ComposerConfigUtils;
import com.jetbrains.php.composer.ComposerDataService;
import com.jetbrains.php.composer.InstalledPackageData;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpReference;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class ComposerPackageInspection extends PhpInspection {
    @NotNull
    @Override
    public String getShortName() {
        return "ComposerPackageInspection";
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getDisplayName() {
        return "Suggested composer packages";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean b) {

        ComposerDataService dataService = ComposerDataService.getInstance(problemsHolder.getProject());
        if (dataService != null && dataService.isConfigWellConfigured()) {
            VirtualFile config = LocalFileSystem.getInstance().refreshAndFindFileByPath(dataService.getConfigPath());
            if (config == null) {
                return PsiElementVisitor.EMPTY_VISITOR;
            }

            saveLaterIfNeeded(config);
            final List<InstalledPackageData> installedPackagesFromConfig = ComposerConfigUtils.getInstalledPackagesFromConfig(config);

            return new PhpElementVisitor() {
                @Override
                public void visitPhpClassReference(ClassReference classReference) {
                    this.inspect(classReference);
                }

                @Override
                public void visitPhpDocType(PhpDocType phpDocType) {
                    this.inspect(phpDocType);
                }

                @Override
                public void visitPhpFunctionCall(FunctionReference reference) {
                    this.inspect(reference);
                }

                @Override
                public void visitPhpConstantReference(ConstantReference reference) {
                    this.inspect(reference);
                }

                private void inspect(@NotNull PhpReference reference) {
                    final String fqn = reference.getFQN();
                    if (fqn == null || !PhpUtil.fqnBelongsToPhpSDK(fqn)) {
                        return;
                    }

                    if (PhpUtil.containsSdkPackage(installedPackagesFromConfig)) {
                        return;
                    }

                    final String descriptionTemplate = "Instana PHP SDK is missing in your root composer.json\n" +
                            "This could lead to problems in environments, where the <pre>instana</pre> native\n" +
                            "PHP extension is not loaded yet. Adding the SDK to your composer manifest will make\n" +
                            "sure you dont run into any issues.";
                    problemsHolder.registerProblem(reference, descriptionTemplate, AddToComposerJsonQuickFix.getInstance(b, false));
                }
            };
        }

        return PsiElementVisitor.EMPTY_VISITOR;
    }

    private static void saveLaterIfNeeded(@NotNull VirtualFile config) {
        FileDocumentManager documentManager = FileDocumentManager.getInstance();
        Document document = documentManager.getDocument(config);
        if (document != null && documentManager.isDocumentUnsaved(document)) {
            ApplicationManager.getApplication().invokeLater(() -> {
                documentManager.saveDocument(document);
            });
        }
    }

    private static class AddToComposerJsonQuickFix implements LocalQuickFix {
        private static final Set<ComposerPackageInspection.AddToComposerJsonQuickFix> fixCache = ContainerUtil.newConcurrentSet();
        private final String myExtension;
        private final boolean myOnTheFly;
        private final boolean myDev;

        private AddToComposerJsonQuickFix(@NotNull String extension, boolean onTheFly, boolean dev) {
            super();

            this.myExtension = extension;
            this.myOnTheFly = onTheFly;
            this.myDev = dev;
        }

        @Nls(
                capitalization = Nls.Capitalization.Sentence
        )
        @NotNull
        public String getFamilyName() {
            return "Add Instana SDK to root composer.json";
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            String path = ComposerDataService.getInstance(project).getConfigPath();
            VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
            if (file == null) {
                Messages.showMessageDialog(project, PhpBundle.message("action.detect.composer.json.not.found", path), PhpBundle.message("action.detect.composer.json.not.found.title"), Messages.getWarningIcon());
            } else {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                if (psiFile != null) {
                    JsonFile jsonFile = ObjectUtils.tryCast(psiFile, JsonFile.class);
                    if (jsonFile != null) {
                        JsonValue value = jsonFile.getTopLevelValue();
                        if (value == null) {
                            jsonFile.add((new JsonElementGenerator(project)).createValue("{}"));
                        }

                        JsonObject topObject = ObjectUtils.tryCast(jsonFile.getTopLevelValue(), JsonObject.class);
                        if (topObject != null) {
                            JsonProperty requireSection = findOrCreateProperty(project, topObject, this.myDev ? "require-dev" : "require", "{}");
                            JsonObject requireSectionValue = (JsonObject) requireSection.getValue();
                            if (requireSectionValue != null) {
                                JsonProperty property = findOrCreateProperty(project, requireSectionValue, this.myExtension, "\"*\"");
                                if (this.myOnTheFly) {
                                    FileEditorManager fileManager = FileEditorManager.getInstance(project);
                                    if (fileManager != null) {
                                        fileManager.openFile(file, true);
                                        Editor editor = fileManager.getSelectedTextEditor();
                                        if (editor != null) {
                                            editor.getCaretModel().moveToOffset(property.getTextRange().getEndOffset());
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }

        private static JsonProperty findOrCreateProperty(@NotNull Project project, @NotNull JsonObject parent, @NotNull String propertyName, @NotNull String propertyValue) {
            JsonProperty property = parent.findProperty(propertyName);
            if (property != null) {
                return property;
            } else {
                JsonElementGenerator generator = new JsonElementGenerator(project);
                property = generator.createProperty(propertyName, propertyValue);
                List<JsonProperty> list = parent.getPropertyList();
                if (list.isEmpty()) {
                    parent = (JsonObject) parent.replace(generator.createObject(property.getText()));
                    return parent.findProperty(propertyName);
                } else {
                    PsiElement comma = parent.addAfter(generator.createComma(), ContainerUtil.getLastItem(list));
                    return (JsonProperty) parent.addAfter(property, comma);
                }
            }
        }

        public static LocalQuickFix getInstance(boolean onTheFly, boolean dev) {
            ComposerPackageInspection.AddToComposerJsonQuickFix fix = ContainerUtil.find(fixCache, (i) -> {
                return i.myExtension.equals(PhpUtil.INSTANA_SDK_COMPOSER_NAME) && i.myDev == dev && i.myOnTheFly == onTheFly;
            });

            if (fix == null) {
                fix = new ComposerPackageInspection.AddToComposerJsonQuickFix(PhpUtil.INSTANA_SDK_COMPOSER_NAME, onTheFly, dev);
                fixCache.add(fix);
            }

            return fix;
        }
    }
}
