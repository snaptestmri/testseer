package io.testseer.intellij.psi;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.JavaRecursiveElementVisitor;
import io.testseer.engine.CodeIntelligence;
import io.testseer.model.codeintel.CodeIntelModel.ParsedClass;
import io.testseer.model.codeintel.CodeIntelModel.ParsedMethod;
import io.testseer.model.codeintel.CodeIntelModel.ParsedModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Code intelligence backed by real IntelliJ PSI APIs (loaded only inside the IDE plugin).
 */
public final class PsiSdkCodeIntelligence implements CodeIntelligence {

    private static final Pattern CALL_WITH_PATH = Pattern.compile(
        "([a-zA-Z0-9_]+)\\.(get|post|put|patch|delete)\\s*\\(\\s*\"([^\"]+)\"\\s*\\)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    private final Project project;
    private final Path projectRoot;

    public PsiSdkCodeIntelligence(Project project) {
        this.project = project;
        String base = project.getBasePath();
        this.projectRoot = base != null ? Path.of(base) : Path.of(".");
    }

    @Override
    public ParsedModel parseTarget(String targetSymbol) {
        return ReadAction.compute(() -> parseInsideReadAction(targetSymbol));
    }

    private ParsedModel parseInsideReadAction(String targetSymbol) {
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        PsiClass psiClass = facade.findClass(targetSymbol, GlobalSearchScope.projectScope(project));
        if (psiClass == null) {
            return emptyFallback(targetSymbol);
        }

        List<String> annotations = extractClassAnnotations(psiClass);
        List<String> imports = extractImports(psiClass);
        List<ParsedMethod> methods = extractMethods(psiClass);

        PsiFile containing = psiClass.getContainingFile();
        VirtualFile vf = containing != null ? containing.getVirtualFile() : null;
        String sourcePath = vf != null ? vf.getPath() : "";
        String sourceSet = sourcePath.contains("/src/test/")
            ? "test"
            : sourcePath.contains("/src/main/") ? "main" : "unknown";

        Map<String, String> hints = new LinkedHashMap<>();
        hints.put("buildTool", detectBuildTool(projectRoot.toString()));
        List<String> fieldTypes = extractFieldTypes(psiClass);
        List<String> ctorTypes = extractConstructorParamTypes(psiClass);
        hints.put("dependency.fieldTypes", String.join(",", fieldTypes));
        hints.put("dependency.constructorParamTypes", String.join(",", ctorTypes));
        hints.put("dependency.total", String.valueOf(fieldTypes.size() + ctorTypes.size()));
        hints.put("dependency.depth", "1");
        hints.put("psi.adapter", "intellij-sdk");

        hints.put("sourceSet", sourceSet);
        if (!sourcePath.isBlank()) {
            hints.put("source.path", sourcePath);
        }

        List<String> outboundFromBodies = collectOutboundFromMethodBodies(methods);
        hints.put("outbound.calls", String.join(",", outboundFromBodies));
        hints.put("outbound.count", String.valueOf(outboundFromBodies.size()));

        String simpleName = psiClass.getName() != null ? psiClass.getName() : "Unknown";
        String pkg = Optional.ofNullable(psiClass.getQualifiedName())
            .map(fqn -> {
                int dot = fqn.lastIndexOf('.');
                return dot > 0 ? fqn.substring(0, dot) : "";
            })
            .orElse("");

        List<String> siblings = findSiblingTestsFromDisk(pkg, simpleName);

        return new ParsedModel(
            "intellij-psi:" + targetSymbol,
            new ParsedClass(pkg, simpleName, annotations, imports, methods),
            siblings.isEmpty() ? List.of(simpleName + "IT") : siblings,
            hints,
            true
        );
    }

    private ParsedModel emptyFallback(String targetSymbol) {
        String simple = targetSymbol.contains(".")
            ? targetSymbol.substring(targetSymbol.lastIndexOf('.') + 1)
            : targetSymbol;
        String pkg = targetSymbol.contains(".") ? targetSymbol.substring(0, targetSymbol.lastIndexOf('.')) : "";
        return new ParsedModel(
            "intellij-psi:missing:" + targetSymbol,
            new ParsedClass(pkg, simple, List.of(), List.of(), List.of()),
            List.of(simple + "IT"),
            Map.of("buildTool", detectBuildTool(projectRoot.toString()), "psi.adapter", "intellij-sdk"),
            false
        );
    }

    private List<String> extractClassAnnotations(PsiClass psiClass) {
        PsiModifierList mod = psiClass.getModifierList();
        if (mod == null) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (PsiAnnotation a : mod.getAnnotations()) {
            String name = a.getQualifiedName();
            if (name != null) {
                int dot = name.lastIndexOf('.');
                out.add("@" + (dot >= 0 ? name.substring(dot + 1) : name));
            }
        }
        return out;
    }

    private List<String> extractImports(PsiClass psiClass) {
        PsiFile file = psiClass.getContainingFile();
        if (!(file instanceof PsiJavaFile jf)) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        PsiImportStatement[] statements = jf.getImportList().getImportStatements();
        for (PsiImportStatement stmt : statements) {
            if (stmt.isOnDemand()) {
                continue;
            }
            PsiJavaCodeReferenceElement ref = stmt.getImportReference();
            if (ref != null) {
                String q = ref.getQualifiedName();
                if (q != null) {
                    out.add(q);
                }
            }
        }
        return out;
    }

    private List<ParsedMethod> extractMethods(PsiClass psiClass) {
        LinkedHashMap<String, ParsedMethod> byKey = new LinkedHashMap<>();
        for (PsiMethod m : psiClass.getMethods()) {
            if (m.isConstructor()) {
                continue;
            }
            byKey.put(methodKey(m), toParsedMethod(m));
        }
        for (PsiClass iface : psiClass.getInterfaces()) {
            for (PsiMethod m : iface.getMethods()) {
                if (m.getBody() != null) {
                    byKey.putIfAbsent(methodKey(m), toParsedMethod(m));
                }
            }
        }
        PsiClass sup = psiClass.getSuperClass();
        int depth = 0;
        while (sup != null && depth < 5 && !"java.lang.Object".equals(sup.getQualifiedName())) {
            for (PsiMethod m : sup.getMethods()) {
                if (m.isConstructor()) {
                    continue;
                }
                if (hasWebMapping(m)) {
                    byKey.putIfAbsent(methodKey(m), toParsedMethod(m));
                }
            }
            sup = sup.getSuperClass();
            depth++;
        }
        return new ArrayList<>(byKey.values());
    }

    private boolean hasWebMapping(PsiMethod m) {
        PsiModifierList mod = m.getModifierList();
        if (mod == null) {
            return false;
        }
        for (PsiAnnotation a : mod.getAnnotations()) {
            String q = a.getQualifiedName();
            if (q != null && q.contains("web.bind.annotation") && q.contains("Mapping")) {
                return true;
            }
        }
        return false;
    }

    private String methodKey(PsiMethod m) {
        String containing = m.getContainingClass() != null && m.getContainingClass().getQualifiedName() != null
            ? m.getContainingClass().getQualifiedName()
            : "";
        return containing + "#" + m.getName() + ":" + m.getParameterList().getParametersCount();
    }

    private ParsedMethod toParsedMethod(PsiMethod method) {
        List<String> annos = new ArrayList<>();
        PsiModifierList mod = method.getModifierList();
        if (mod != null) {
            for (PsiAnnotation a : mod.getAnnotations()) {
                String name = a.getQualifiedName();
                if (name != null) {
                    int dot = name.lastIndexOf('.');
                    annos.add("@" + (dot >= 0 ? name.substring(dot + 1) : name));
                }
            }
        }
        PsiCodeBlock body = method.getBody();
        String bodyText = body != null ? body.getText() : "";
        LiteralCallAppender appender = new LiteralCallAppender();
        if (body != null) {
            body.accept(appender);
        }
        String augmented = bodyText + (appender.extraBodyLines.isEmpty() ? "" : "\n" + String.join("\n", appender.extraBodyLines));
        return new ParsedMethod(method.getName(), annos, augmented);
    }

    private static final class LiteralCallAppender extends JavaRecursiveElementVisitor {
        private final List<String> extraBodyLines = new ArrayList<>();

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            String refName = expression.getMethodExpression().getReferenceName();
            if (refName != null && refName.matches("(?i)get|post|put|patch|delete")) {
                PsiExpression[] args = expression.getArgumentList().getExpressions();
                if (args.length > 0 && args[0] instanceof PsiLiteralExpression lit) {
                    Object val = lit.getValue();
                    if (val instanceof String) {
                        extraBodyLines.add(
                            refName.toLowerCase(Locale.ROOT) + "(\"" + val + "\")"
                        );
                    }
                }
            }
            super.visitMethodCallExpression(expression);
        }
    }

    private List<String> collectOutboundFromMethodBodies(List<ParsedMethod> methods) {
        Set<String> out = new LinkedHashSet<>();
        for (ParsedMethod m : methods) {
            Matcher matcher = CALL_WITH_PATH.matcher(m.body());
            while (matcher.find()) {
                String verb = matcher.group(2).toUpperCase(Locale.ROOT);
                String path = matcher.group(3);
                path = path.startsWith("/") ? path : "/" + path;
                out.add(verb + " " + path);
            }
        }
        return new ArrayList<>(out);
    }

    private List<String> extractFieldTypes(PsiClass psiClass) {
        List<String> out = new ArrayList<>();
        for (PsiField field : psiClass.getFields()) {
            out.add(field.getType().getPresentableText());
        }
        return out;
    }

    private List<String> extractConstructorParamTypes(PsiClass psiClass) {
        List<String> out = new ArrayList<>();
        for (PsiMethod ctor : psiClass.getConstructors()) {
            for (PsiParameter p : ctor.getParameterList().getParameters()) {
                out.add(p.getType().getPresentableText());
            }
        }
        return out;
    }

    private List<String> findSiblingTestsFromDisk(String packageName, String className) {
        Path testRoot = projectRoot.resolve("src/test/java");
        if (!Files.isDirectory(testRoot)) {
            return List.of();
        }
        String packagePath = packageName == null || packageName.isBlank() ? "" : packageName.replace('.', '/');
        Path candidateDir = packagePath.isBlank() ? testRoot : testRoot.resolve(packagePath);
        if (!Files.isDirectory(candidateDir)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        try (var walk = Files.walk(candidateDir, 2)) {
            walk.filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString())
                .filter(name -> name.startsWith(className))
                .filter(name -> name.endsWith("Test.java") || name.endsWith("IT.java") || name.endsWith("E2ETest.java"))
                .map(name -> name.substring(0, name.length() - ".java".length()))
                .forEach(result::add);
        } catch (IOException ignored) {
            return List.of();
        }
        return result.stream().distinct().toList();
    }

    @Override
    public List<String> findOutboundCalls(ParsedModel model) {
        List<String> outbound = new ArrayList<>();
        for (ParsedMethod method : model.targetClass().methods()) {
            Matcher matcher = CALL_WITH_PATH.matcher(method.body());
            while (matcher.find()) {
                outbound.add(matcher.group(2).toUpperCase(Locale.ROOT) + " " + normalizePath(matcher.group(3)));
            }
        }
        return outbound;
    }

    @Override
    public List<String> findSiblingTestClasses(ParsedModel model) {
        return model.siblingTests();
    }

    @Override
    public Optional<String> readTestNgXmlHint(String projectRoot) {
        return Optional.empty();
    }

    @Override
    public String detectBuildTool(String projectRootPath) {
        Path root = Path.of(projectRootPath);
        if (Files.exists(root.resolve("pom.xml"))) {
            return "maven";
        }
        if (Files.exists(root.resolve("build.gradle")) || Files.exists(root.resolve("build.gradle.kts"))) {
            return "gradle";
        }
        return "unknown";
    }

    private static String normalizePath(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }
}
