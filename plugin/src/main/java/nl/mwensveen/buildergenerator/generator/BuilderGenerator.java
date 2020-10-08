package nl.mwensveen.buildergenerator.generator;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import nl.mwensveen.buildergenerator.data.BuilderField;
import nl.mwensveen.buildergenerator.data.GeneratorData;
import nl.mwensveen.buildergenerator.util.BuilderFieldUtil;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * The Class BuilderGenerator performs the actual generation.
 */
public class BuilderGenerator {

    private final static String BUILDER_CLASSNAME = "Builder";
    private final static String IMPORT_ARRAYLIST = "java.util.ArrayList";
    private final static String IMPORT_COLLECTIONS = "java.util.Collections";
    private final static String IMPORT_HASHSET = "java.util.HashSet";

    private GeneratorData data;

    public BuilderGenerator(GeneratorData data) {
        this.data = data;
    }

    public void generate(ICompilationUnit cu) {

        try {
            removeOldClassConstructor(cu);
            removeOldBuilderClass(cu);
            removeOldFactoryMethod(cu);
            removeOldCopyMethod(cu);

            IType clazz = cu.getTypes()[0];

            IBuffer buffer = cu.getBuffer();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();

            createClassConstructor(clazz, pw);
            createBuildFactoryMethodOnBean(pw, clazz);
            createStaticBuilderMethod(pw, clazz);

            Set<String> importClasses = createBuilderClass(clazz, pw);
            for (String importClass : importClasses) {
                if (importClass != null) {
                    cu.createImport(importClass, null, null);
                }
            }

            int pos = buffer.getLength() - 1;
            while ('}' != buffer.getChar(pos)) {
                pos--;
            }

            if (data.isOption(GeneratorData.FORMAT_SOURCE)) {
                pw.println();
                buffer.replace(pos, 0, sw.toString());
                String builderSource = buffer.getContents();

                TextEdit text = ToolFactory.createCodeFormatter(null).format(CodeFormatter.K_COMPILATION_UNIT,
                        builderSource, 0, builderSource.length(), 0, "\n");
                // text is null if source cannot be formatted
                if (text != null) {
                    Document simpleDocument = new Document(builderSource);
                    text.apply(simpleDocument);
                    buffer.setContents(simpleDocument.get());
                }
            } else {
                buffer.replace(pos, 0, sw.toString());
            }
        } catch (JavaModelException e) {
            e.printStackTrace();
        } catch (MalformedTreeException e) {
            e.printStackTrace();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void createClassConstructor(IType clazz, PrintWriter pw) {
        if (!data.isOption(GeneratorData.CLASS_CONSTRUCTOR)) {
            return;
        }

        String clazzName = clazz.getElementName();
        if (generateJavaDoc()) {
            pw.println("/**");
            pw.println(" * Constructor that creates a " + clazzName + " with the values of a " + clazzName + "."
                    + BUILDER_CLASSNAME + ".");
            pw.println(" */");
        }
        pw.println("private " + clazzName + "(" + BUILDER_CLASSNAME + " builder){");
        for (BuilderField field : data.getSelectedFields()) {
            pw.println("  this." + BuilderFieldUtil.getName(field) + " = builder." + BuilderFieldUtil.getName(field)
                    + ";");
        }
        pw.println("}");
    }

    private boolean generateJavaDoc() {
        return data.isOption(GeneratorData.GENERATE_JAVADOC);
    }

    private Set<String> createBuilderClass(IType clazz, PrintWriter pw) throws JavaModelException {
        if (generateJavaDoc()) {
            pw.println(
                    "/**\n* This class provides methods to build a {@link " + clazz.getFullyQualifiedName() + "}.\n*/");
        }
        pw.println("public static class " + BUILDER_CLASSNAME + " {");

        Set<String> importClasses = createFieldDeclarations(pw);
        createCopyConstructor(pw, clazz);
        importClasses.addAll(createBuilderMethods(pw));
        createBuildWithClassConstructor(pw, clazz);
        createBuildWithBuilderMethod(pw, clazz);

        pw.println("}");
        return importClasses;

    }

    private void createBuildWithBuilderMethod(PrintWriter pw, IType clazz) {
        if (data.isOption(GeneratorData.CLASS_CONSTRUCTOR)) {
            return;
        }
        String clazzName = clazz.getElementName();
        String clazzVariable = clazzName.substring(0, 1).toLowerCase() + clazzName.substring(1);
        printBuildMethodJavadoc(pw, clazzName);

        pw.println("public " + clazzName + " build(){");
        pw.println(clazzName + " " + clazzVariable + "=new " + clazzName + "();");
        for (BuilderField field : data.getSelectedFields()) {
            String name = BuilderFieldUtil.getName(field);
            pw.println(clazzVariable + "." + name + "=" + name + ";");
        }
        pw.println("return " + clazzVariable + ";\n}");
    }

    private void printBuildMethodJavadoc(PrintWriter pw, String clazzName) {
        if (generateJavaDoc()) {
            pw.println("/**");
            pw.println(" * Creates a new {@link " + clazzName + "} based on this " + BUILDER_CLASSNAME + ".");
            pw.println(" * @return a new " + clazzName);
            pw.println(" */");
        }
    }

    private void createBuildWithClassConstructor(PrintWriter pw, IType clazz) {
        if (!data.isOption(GeneratorData.CLASS_CONSTRUCTOR)) {
            return;
        }
        String clazzName = clazz.getElementName();
        printBuildMethodJavadoc(pw, clazzName);
        pw.println("public " + clazzName + " build(){");
        pw.println("return new " + clazzName + "(this);\n}");
    }

    private void removeOldFactoryMethod(ICompilationUnit cu) throws JavaModelException {
        for (IMethod method : cu.getTypes()[0].getMethods()) {
            if (method.getParameterTypes().length == 0 && Flags.isStatic(method.getFlags())
                    && method.getElementName().equals("builder") && method.getReturnType().equals("QBuilder;")) {
                method.delete(true, null);
                break;
            }
        }
    }

    private void removeOldCopyMethod(ICompilationUnit cu) throws JavaModelException {
        for (IMethod method : cu.getTypes()[0].getMethods()) {
            if (method.getParameterTypes().length == 0 && method.getElementName().equals("toBuilder")
                    && method.getReturnType().equals("QBuilder;")) {
                method.delete(true, null);
                break;
            }
        }
    }

    private void removeOldClassConstructor(ICompilationUnit cu) throws JavaModelException {
        for (IMethod method : cu.getTypes()[0].getMethods()) {
            if (method.isConstructor() && method.getParameterTypes().length == 1
                    && method.getParameterTypes()[0].equals("QBuilder;")) {
                method.delete(true, null);
                break;
            }
        }
    }

    private void removeOldBuilderClass(ICompilationUnit cu) throws JavaModelException {
        for (IType type : cu.getTypes()[0].getTypes()) {
            if (type.getElementName().equals("Builder") && type.isClass()) {
                type.delete(true, null);
                break;
            }
        }
    }

    private void createCopyConstructor(PrintWriter pw, IType clazz) {
        if (!data.isOption(GeneratorData.GENERATE_COPY_CONSTRUCTOR)) {
            return;
        }

        // include a default constructor

        // the copy constructor
        String clazzName = clazz.getElementName();
        if (generateJavaDoc()) {
            pw.println("/** Default Constructor **/");
        }
        pw.println("public " + BUILDER_CLASSNAME + "(){}");
        pw.println("");
        if (generateJavaDoc()) {
            pw.println("/** Copy Constructor **/");
        }
        pw.println("public " + BUILDER_CLASSNAME + "(" + clazzName + " object){");
        for (BuilderField field : data.getSelectedFields()) {
            pw.println("this." + BuilderFieldUtil.getName(field) + "= object." + BuilderFieldUtil.getName(field) + ";");
        }
        pw.println("}");

    }

    private Set<String> createBuilderMethods(PrintWriter pw) throws JavaModelException {
        Set<String> importClasses = new HashSet<>();
        for (BuilderField field : data.getSelectedFields()) {

            String fieldName = BuilderFieldUtil.getName(field);
            String fieldType = BuilderFieldUtil.getType(field);
            String baseName = getFieldBaseName(fieldName);
            String parameterName = baseName;
            String methodNameSuffix;
            if (baseName.length() > 1) {
                methodNameSuffix = baseName.substring(0, 1).toUpperCase() + baseName.substring(1);
            } else {
                methodNameSuffix = baseName.toUpperCase();
            }
            String methodNamePrefix = data.isOption(GeneratorData.USE_WITH_PREFIX) ? " with" : " set";
            String methodName = methodNamePrefix + methodNameSuffix;
            boolean isCollection = BuilderFieldUtil.isCollection(fieldType);

            // Standard with/set method
            printJavadoc(pw, baseName, "Sets", false);
            pw.println("public " + BUILDER_CLASSNAME + " " + methodName + "(" + fieldType + " " + parameterName + ") {");
            if (isCollection && data.isOption(GeneratorData.PREVENT_NULL_VALUE_FOR_COLLECTOINS)) {
                printNotNullCheck(pw, parameterName);
            }
            pw.println("  this." + baseName + "=" + parameterName + ";");
            pw.println("return this;");
            pw.println("}");

            if (isCollection) {
                String collectionType = BuilderFieldUtil.findCollectionType(fieldType);
                String parameterType = BuilderFieldUtil.findParameterTypeForCollection(fieldType);
                // with/set vararg to collection converter.
                if (data.isOption(GeneratorData.GENERATE_VARARG_METHODS_FOR_COLLECTIONS)) {
                    printJavadoc(pw, baseName, "Sets", true);
                    pw.println("public " + BUILDER_CLASSNAME + " " + methodName + "(" + parameterType + " ... " + parameterName + ") {");
                    printNotNullCheck(pw, parameterName);
                    importClasses.add(printInitCollection(pw, fieldType, parameterName, parameterType, false, collectionType));
                    importClasses.add(printAddAll(pw, parameterName, true));
                    pw.println(" return this;");
                    pw.println("}");
                }

                // add object(s) to a collection
                if (data.isOption(GeneratorData.GENERATE_ADD_METHODS_FOR_COLLECTIONS)) {
                    printJavadoc(pw, baseName, "Adds to", true);
                    pw.println("public " + BUILDER_CLASSNAME + " add" + methodNameSuffix + "(" + fieldType + " " + parameterName + "Elements" + ") {");
                    printNotNullCheck(pw, parameterName + "Elements");
                    if (!data.isOption(GeneratorData.PREVENT_NULL_VALUE_FOR_COLLECTOINS)) {
                        importClasses.add(printInitCollection(pw, fieldType, parameterName, parameterType, true, collectionType));
                    }
                    importClasses.add(printAddAll(pw, parameterName + "Elements", fieldName, false));
                    pw.println(" return this;");
                    pw.println("}");

                    if (data.isOption(GeneratorData.GENERATE_VARARG_METHODS_FOR_COLLECTIONS)) {
                        printJavadoc(pw, baseName, "Adds to", true);
                        pw.println(
                                "public " + BUILDER_CLASSNAME + " add" + methodNameSuffix + "(" + parameterType + " ... " + parameterName + "Elements" + ") {");
                        printNotNullCheck(pw, parameterName + "Elements");
                        if (!data.isOption(GeneratorData.PREVENT_NULL_VALUE_FOR_COLLECTOINS)) {
                            importClasses.add(printInitCollection(pw, fieldType, parameterName, parameterType, true, collectionType));
                        }
                        importClasses.add(printAddAll(pw, parameterName + "Elements", fieldName, true));
                        pw.println(" return this;");
                        pw.println("}");
                    }

                    printJavadoc(pw, baseName, "Adds to", true);
                    pw.println("public " + BUILDER_CLASSNAME + " add" + methodNameSuffix + "(" + parameterType + " " + parameterName + "Element" + ") {");
                    if (!data.isOption(GeneratorData.PREVENT_NULL_VALUE_FOR_COLLECTOINS)) {
                        importClasses.add(printInitCollection(pw, fieldType, parameterName, parameterType, true, collectionType));
                    }
                    pw.println(" this." + parameterName + ".add(" + parameterName + "Element" + ");");
                    pw.println(" return this;");
                    pw.println("}");
                }

            }

        }
        return importClasses;
    }

    private String printAddAll(PrintWriter pw, String parameterName, boolean forVarArg) {
        return printAddAll(pw, parameterName, parameterName, forVarArg);
    }

    private String printAddAll(PrintWriter pw, String parameterName, String fieldName, boolean forVarArg) {
        if (forVarArg) {
            pw.println("Collections.addAll(this." + fieldName + ", " + parameterName + ");");
            return IMPORT_COLLECTIONS;
        } else {
            pw.println("this." + fieldName + ".addAll(" + parameterName + ");");
            return null;
        }
    }

    private String printInitCollection(PrintWriter pw, String fieldType, String parameterName, String parameterType, boolean nullCheck, String collectionType) {
        String newType = null;
        String importClass = null;
        if ("Set".equals(collectionType)) {
            newType = "HashSet";
            importClass = IMPORT_HASHSET;
        } else {
            newType = "ArrayList";
            importClass = IMPORT_ARRAYLIST;
        }
        if (nullCheck) {
            pw.println("  if (this." + parameterName + " == null) {");
            pw.print("  ");
        }
        pw.println("  this." + parameterName + " = new " + newType + "<" + parameterType + ">" + "();");
        if (nullCheck) {
            pw.println("  }");
        }
        return importClass;
    }

    private String printCreateFieldForCollection(PrintWriter pw, BuilderField field, String fieldType) {
        String parameterType = BuilderFieldUtil.findParameterTypeForCollection(fieldType);
        String newType = null;
        String importClass = null;
        String collectionType = BuilderFieldUtil.findCollectionType(fieldType);
        if ("Set".equals(collectionType)) {
            newType = "HashSet";
            importClass = IMPORT_HASHSET;
        } else {
            newType = "ArrayList";
            importClass = IMPORT_ARRAYLIST;
        }
        pw.println("private " + fieldType + " " + BuilderFieldUtil.getName(field) + " = new " + newType + "<" + parameterType + ">" + "();");
        return importClass;
    }

    private void printNotNullCheck(PrintWriter pw, String parameterName) {
        pw.println("  if(" + parameterName + " == null) {");
        pw.println("    throw new IllegalArgumentException(\"" + parameterName + " is null\");");
        pw.println("  }");
    }

    private void printJavadoc(PrintWriter pw, String fieldName, String verb, boolean throwsException) {
        if (generateJavaDoc()) {
            pw.println("/**");
            pw.println(" * " + verb + " the {@link #" + fieldName + "} property of this builder");
            pw.println(" * @param " + fieldName + "");
            pw.println(" * @return this builder");
            if (throwsException) {
                pw.println(
                        " * @throws IllegalArgumentException if " + fieldName + " is null or contains a null element");
            }
            pw.println(" */");
        }
    }

    private String getFieldBaseName(String fieldName) {
        IJavaProject javaProject = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject());
        return NamingConventions.getBaseName(NamingConventions.VK_INSTANCE_FIELD, fieldName, javaProject);
    }

    private Set<String> createFieldDeclarations(PrintWriter pw) throws JavaModelException {
        Set<String> importClasses = new HashSet<>();
        for (BuilderField field : data.getSelectedFields()) {
            String fieldType = BuilderFieldUtil.getType(field);
            boolean isCollection = BuilderFieldUtil.isCollection(fieldType);
            if (isCollection && data.isOption(GeneratorData.PREVENT_NULL_VALUE_FOR_COLLECTOINS)) {
                importClasses.add(printCreateFieldForCollection(pw, field, fieldType));
            } else {
                pw.println("private " + fieldType + " " + BuilderFieldUtil.getName(field) + ";");
            }
        }
        return importClasses;
    }

    private void createBuildFactoryMethodOnBean(PrintWriter pw, IType clazz) {
        if (!data.isOption(GeneratorData.GENERATE_BUILD_FACTORY_METHOD_ON_BEAN)
                || !data.isOption(GeneratorData.GENERATE_COPY_CONSTRUCTOR)) {
            return;
        }

        String methodName = "toBuilder";
        if (generateJavaDoc()) {
            pw.println("/**");
            pw.println(" * Creates a new {@link " + BUILDER_CLASSNAME
                    + "} populated with the properties of this object. This is a convenience method which calls the");
            pw.println(" * {@link #builder(" + clazz.getElementName()
                    + ")} method with this object as the passed parameter.");
            pw.println(" * @return a new " + BUILDER_CLASSNAME + " populated with this object's property values");
            pw.println(" */");
        }
        pw.println("public " + BUILDER_CLASSNAME + " " + methodName + "(){");
        pw.println("return new " + BUILDER_CLASSNAME + "(this);\n}");
    }

    private void createStaticBuilderMethod(PrintWriter pw, IType clazz) {
        if (!data.isOption(GeneratorData.GENERATE_BUILD_FACTORY_METHOD_ON_BEAN)) {
            return;
        }

        String methodName = "builder";
        if (generateJavaDoc()) {
            pw.println("/**");
            pw.println(" * Creates a new {@link " + BUILDER_CLASSNAME + "} for {@link " + clazz.getElementName() + "} objects.");
            pw.println(" * @return a new " + BUILDER_CLASSNAME);
            pw.println("");
            pw.println(" */");
        }
        pw.println("public static " + BUILDER_CLASSNAME + " " + methodName + "(){");
        pw.println("return new " + BUILDER_CLASSNAME + "();\n}");
    }
}
