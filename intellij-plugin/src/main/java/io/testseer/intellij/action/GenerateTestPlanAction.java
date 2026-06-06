package io.testseer.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import io.testseer.intellij.IntellijTestSeerFacade;
import io.testseer.mock.MockProviders;
import io.testseer.render.Renderers;

import java.nio.file.Path;

public final class GenerateTestPlanAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        var project = e.getProject();
        if (project == null) {
            return;
        }
        PsiClass psiClass = resolvePsiClass(e);
        if (psiClass == null) {
            Messages.showWarningDialog(project, "Could not resolve a Java class for TestSeer.", "TestSeer");
            return;
        }
        String fqn = psiClass.getQualifiedName();
        if (fqn == null) {
            Messages.showWarningDialog(project, "Class has no qualified name.", "TestSeer");
            return;
        }
        try {
            String base = project.getBasePath() != null ? project.getBasePath() : ".";
            var plan = IntellijTestSeerFacade.generatePlan(project, fqn, false, null, false, w -> {
            });
            var rendered = new Renderers.JsonPlanRenderer().render(
                plan,
                MockProviders.select(null, null, java.util.List.of(), Path.of(base))
            );
            String content = rendered.content();
            int max = Math.min(4000, content.length());
            Messages.showInfoMessage(project, content.substring(0, max), "TestSeer Plan (JSON excerpt)");
        } catch (Exception ex) {
            Messages.showErrorDialog(project, String.valueOf(ex.getMessage()), "TestSeer");
        }
    }

    private static PsiClass resolvePsiClass(AnActionEvent e) {
        PsiElement elem = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (elem instanceof PsiMethod method) {
            return method.getContainingClass();
        }
        if (elem instanceof PsiClass cls) {
            return cls;
        }
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (file instanceof PsiJavaFile jf) {
            PsiClass[] classes = jf.getClasses();
            return classes.length > 0 ? classes[0] : null;
        }
        return null;
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }
}
