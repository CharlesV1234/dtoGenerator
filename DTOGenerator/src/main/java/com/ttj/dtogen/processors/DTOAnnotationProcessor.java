package com.ttj.dtogen.processors;

import com.ttj.dtogen.annotations.DtoClass;
import com.ttj.dtogen.annotations.DtoProperty;
import com.ttj.dtogen.utils.JavaFileBuilder;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.*;
import java.io.File;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class DTOAnnotationProcessor extends AbstractProcessor {
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    private String getValue(String value, String defaultValue) {
        if (value == null || value.length() < 1)
            return defaultValue;
        else
            return value;
    }

    private Map<String, String> getClassDetails(DtoClass classAnnotation, TypeElement classElem) {
        Map<String, String> classDetails = new HashMap<>();
        String parentQualifiedClassName = classElem.getQualifiedName().toString();
        String parentClassName = classElem.getSimpleName().toString();
        String parentPackage = parentQualifiedClassName.substring(0, parentQualifiedClassName.indexOf(parentClassName));
        // set package details
        String packageDetails = getValue(classAnnotation.classPackage(), (parentPackage == null ? "" : parentPackage));
        if (packageDetails.endsWith(".")) {
            packageDetails = packageDetails.substring(0, packageDetails.lastIndexOf('.'));
        }
        packageDetails = packageDetails.replace("models", "dtos");
        classDetails.put("packageDetails", packageDetails);
        // set class name
        classDetails.put("className", getValue(classAnnotation.name(), parentClassName + "Dto"));
        // set superclass
        String superClassPath = classElem.getSuperclass().toString();
        if (!superClassPath.equals("java.lang.Object")) {

            String superClass = superClassPath.substring(superClassPath.lastIndexOf('.') + 1) + "Dto";
            superClassPath = superClassPath.replace("models", "dtos");
            classDetails.put("superClass", superClass);
            classDetails.put("superClassImport", superClassPath + "Dto");
        }

        return classDetails;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(DtoClass.class)) {
            try {
                DtoClass classAnnotation = element.getAnnotation(DtoClass.class);

                Map<String, String> classDetails = getClassDetails(classAnnotation, (TypeElement) element);
                String packageDetails = classDetails.get("packageDetails");
                String className = classDetails.get("className");
                String superClass = classDetails.get("superClass");
                String superClassImport = classDetails.get("superClassImport");
                String qualifiedGenClass = packageDetails + "." + className;

                JavaFileObject javaFileObject = filer.createSourceFile(qualifiedGenClass);

                if (new File(javaFileObject.toUri()).exists()) {
                    continue;
                }

                Writer writer = javaFileObject.openWriter();

                JavaFileBuilder javaFileBuilder = new JavaFileBuilder();
                javaFileBuilder.setPackageName(packageDetails);
                javaFileBuilder.setClassName(className);
                javaFileBuilder.setSuperClass(superClass);
                javaFileBuilder.setSuperClassImport(superClassImport);

                // iterating through annotated fields
                for (Element fieldElem : element.getEnclosedElements()) {
                    DtoProperty propAnnotation = fieldElem.getAnnotation(DtoProperty.class);
                    String fieldName = null;
                    String fieldType = null;
                    boolean isGetter = true, isSetter = true;
                    if (propAnnotation != null) {
                        fieldName = propAnnotation.name();
                        isGetter = propAnnotation.getter();
                        isSetter = propAnnotation.setter();
                    }
                    if (propAnnotation != null || classAnnotation.includeAllFields()) {
                        if (fieldElem instanceof VariableElement) {
                            if (fieldName == null || fieldName.length() < 1) {
                                fieldName = fieldElem.getSimpleName().toString();
                            }
                            VariableElement varElem = (VariableElement) fieldElem;
                            fieldType = varElem.asType().toString();
                            messager.printMessage(Diagnostic.Kind.NOTE,
                                    MessageFormat.format("[Class: %s] Processing field[%s] for type[%s]",
                                            qualifiedGenClass, fieldName, fieldType));
                            javaFileBuilder.addField(fieldType, fieldName, isGetter, isSetter);
                        }
                    }
                }
                writer.write(javaFileBuilder.toClassCodeString());
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new HashSet<>();
        annotationTypes.add(DtoClass.class.getCanonicalName());
        return annotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
