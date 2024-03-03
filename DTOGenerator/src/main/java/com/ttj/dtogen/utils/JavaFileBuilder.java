package com.ttj.dtogen.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaFileBuilder {
    private static final Pattern PATTERN_PKG_REGEX = Pattern.compile("([a-zA-Z[0-9]]*[\\.][a-zA-Z[0-9]]*)*");
    private StringBuilder classBuilder = new StringBuilder();
    private Set<String> importSet = new HashSet<>();
    private StringBuilder variablesBuilder = new StringBuilder();
    private StringBuilder methodsBuilder = new StringBuilder();

    private String className = null;
    private String superClass; // new field to store the superclass
    private String superClassImport; // new field to store the superclass import

    // new method to set the superclass
    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setSuperClassImport(String superClassImport) {
        this.superClassImport = superClassImport;
    }

    public void setPackageName(String packageName) {
        classBuilder.append("package ").append(packageName).append(";").append(System.lineSeparator())
                .append(System.lineSeparator());
    };

    private String getFieldTypeAndSetImports(String fieldType) {
        String finalFieldType = fieldType;
        Matcher matcher = PATTERN_PKG_REGEX.matcher(fieldType);
        while (matcher.find()) {
            String qualifiedClass = matcher.group();
            if (matcher.start() != matcher.end() && qualifiedClass != null && qualifiedClass.length() > 0) {
                int nameIdx = qualifiedClass.lastIndexOf('.');
                String className = qualifiedClass;
                if (nameIdx > 0) {
                    className = qualifiedClass.substring(nameIdx + 1);
                }
                finalFieldType = finalFieldType.replaceAll(qualifiedClass, className);
                importSet.add(qualifiedClass);
            }
        }

        return finalFieldType;
    }

    public JavaFileBuilder addField(String fieldType, String fieldName, boolean genGetter, boolean genSetter) {

        String simpleFieldType = getFieldTypeAndSetImports(fieldType);

        variablesBuilder.append("\tprivate ").append(simpleFieldType).append(' ')
                .append(fieldName).append(';').append(System.lineSeparator());
        if (genGetter) {
            addGetter(simpleFieldType, fieldName);
        }
        if (genSetter) {
            addSetter(simpleFieldType, fieldName);
        }
        return this;
    }

    private void addGetter(String fieldType, String fieldName) {

        methodsBuilder.append("\tpublic ")
                .append(fieldType)
                .append(" get")
                .append(fieldName.substring(0, 1).toUpperCase())
                .append(fieldName.substring(1))
                .append("(){").append(System.lineSeparator())
                .append("\t\treturn ").append(fieldName).append(";").append(System.lineSeparator())
                .append("\t}").append(System.lineSeparator());
    }

    private void addSetter(String fieldType, String fieldName) {

        methodsBuilder.append("\tpublic void ")
                .append("set")
                .append(fieldName.substring(0, 1).toUpperCase())
                .append(fieldName.substring(1))
                .append("(").append(fieldType).append(" ")
                .append(fieldName).append("){").append(System.lineSeparator())
                .append("\t\tthis.").append(fieldName).append(" = ")
                .append(fieldName).append(";").append(System.lineSeparator())
                .append("\t}").append(System.lineSeparator());
    }

    public String toClassCodeString() {
        for (String fieldType : importSet) {
            classBuilder.append("import ")
                    .append(fieldType).append(";")
                    .append(System.lineSeparator());
        }
        if (superClassImport != null && superClassImport.length() > 0) {
            classBuilder.append("import ").append(superClassImport).append(";").append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        if (superClass != null && superClass.length() > 0) {
            classBuilder.append("public class ").append(className).append(" extends ").append(superClass).append(" {");
        } else {
            classBuilder.append("public class ").append(className).append(" {");
        }
        return classBuilder.append(System.lineSeparator())
                .append(System.lineSeparator())
                .append(variablesBuilder).append(System.lineSeparator())
                .append(methodsBuilder).append(System.lineSeparator())
                .append("}").toString();
    }
}
