package com.instana.idea.php;

import com.instana.idea.php.codeInsight.template.LiveTemplateFactory;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.Statement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NonNls
public class WrapInSpanIntentionAction extends PsiElementBaseIntentionAction implements IntentionAction {
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiElement wrappableElement = PsiTreeUtil.getParentOfType(element, Statement.class);
        if (null == wrappableElement) {
            // Psi changed in the meantime
            return;
        }

        final Template wrapCallTemplate = LiveTemplateFactory.createWrapInPHPSpanTemplate();
        wrapCallTemplate.addVariable("SPAN_TYPE", new SpanTypeExpression("MESSAGING"), true);
        wrapCallTemplate.addVariable("STATEMENT", new TextExpression(wrappableElement.getText()), false);

        int textOffset = wrappableElement.getTextOffset();
        editor.getCaretModel().moveToOffset(textOffset);

        wrappableElement.delete();

        final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        documentManager.doPostponedOperationsAndUnblockDocument(editor.getDocument());

        TemplateManager.getInstance(project).startTemplate(editor, wrapCallTemplate);

        documentManager.commitDocument(editor.getDocument());
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return element.getLanguage().equals(PhpLanguage.INSTANCE) && PsiTreeUtil.getParentOfType(element, Statement.class) != null;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return "Wrap call in Instana trace";
    }

    @NotNull
    @Override
    public String getText() {
        return "Instana SDK: Wrap statement in span";
    }

    private static class SpanTypeExpression extends TextExpression {
        public SpanTypeExpression(String string) {
            super(string);
        }

        @Nullable
        @Override
        public String getAdvertisingText() {
            return "Foo";
        }

        @Override
        public LookupElement[] calculateLookupItems(ExpressionContext expressionContext) {
            return new LookupElement[]{
                    LookupElementBuilder.create("MESSAGING"),
                    LookupElementBuilder.create("HTTP"),
                    LookupElementBuilder.create("RPC"),
            };
        }
    }
}
