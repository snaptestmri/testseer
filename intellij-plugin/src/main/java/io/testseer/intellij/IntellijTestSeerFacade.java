package io.testseer.intellij;

import com.intellij.openapi.project.Project;
import io.testseer.intellij.psi.PsiSdkCodeIntelligence;
import io.testseer.model.plan.PlanModel.TestPlan;
import io.testseer.surfaces.TestSeerSurfaces;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * IDE entrypoint: runs the core pipeline using {@link PsiSdkCodeIntelligence} (real PSI, not reflection).
 */
public final class IntellijTestSeerFacade {

    private IntellijTestSeerFacade() {
    }

    public static TestPlan generatePlan(
        Project project,
        String targetSymbol,
        boolean llm,
        String providerOverride,
        boolean strict,
        Consumer<String> warningSink
    ) {
        PsiSdkCodeIntelligence intel = new PsiSdkCodeIntelligence(project);
        var model = intel.parseTarget(targetSymbol);
        Path root = Path.of(Objects.requireNonNullElse(project.getBasePath(), "."));
        return TestSeerSurfaces.generateTestPlan(model, providerOverride, llm, strict, root, warningSink);
    }
}
