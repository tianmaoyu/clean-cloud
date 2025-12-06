package org.clean.example.jacoco;

import org.jacoco.core.analysis.*;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MethodCoverageAnalyzer4 {
    
    // 存储类的字段信息：className -> (fieldName -> fieldType)
    private static Map<String, Map<String, String>> classFieldsMap = new HashMap<>();
    
    // 存储类的注解信息：className -> Author注解值
    private static Map<String, String> classAuthorMap = new HashMap<>();
    
    // 存储方法的注解信息：methodSignature -> Author注解值
    private static Map<String, String> methodAuthorMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        String path = "/Users/eric/clean-cloud/clean-system/clean-system-service/target";

        // 0. 先收集所有类的字段信息和注解信息
        collectFieldInfo(new File(path + "/classes"));

        // 1. 加载 exec 文件
        ExecutionDataStore executionData = new ExecutionDataStore();
        SessionInfoStore sessionInfos = new SessionInfoStore();
        try (FileInputStream in = new FileInputStream(path + "/jacoco.exec")) {
            ExecutionDataReader reader = new ExecutionDataReader(in);
            reader.setExecutionDataVisitor(executionData);
            reader.setSessionInfoVisitor(sessionInfos);
            reader.read();
        }

        // 2. 分析类文件目录
        CoverageBuilder coverageBuilder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
        analyzer.analyzeAll(new File(path + "/classes"));

        // 3. 遍历每个类、每个方法
        for (IClassCoverage clazz : coverageBuilder.getClasses()) {
            String className = clazz.getName().replace('/', '.');
            Map<String, String> fields = classFieldsMap.get(className);
            String classAuthor = classAuthorMap.get(className);
            
            for (IMethodCoverage method : clazz.getMethods()) {
                String methodName = method.getName();
                String methodDesc = method.getDesc();
                String methodSignature = getMethodSignature(className, methodName, methodDesc);
                
                // 跳过构造函数和静态初始化块
                if ("<init>".equals(methodName) || "<clinit>".equals(methodName)) {
                    continue;
                }
                
                // 跳过标准 getter/setter（基于字段匹配）
                if (fields != null && isStandardGetterSetter(methodName, methodDesc, fields)) {
                    continue;
                }

                // 跳过标准 Object 方法
                if (isStandardObjectMethod(methodName, methodDesc)) {
                    continue;
                }

                // 获取作者信息：方法优先，类次之
                String author = getAuthorInfo(methodSignature, classAuthor);
                
                int lineCovered = method.getLineCounter().getCoveredCount();
                int lineMissed = method.getLineCounter().getMissedCount();
                
                // 输出包含作者信息
                if (author != null) {
                    System.out.printf("%s.%s [作者: %s] -> lines: %d/%d%n", 
                        className, methodName, author, lineCovered, lineCovered + lineMissed);
                } else {
                    System.out.printf("%s.%s -> lines: %d/%d%n", 
                        className, methodName, lineCovered, lineCovered + lineMissed);
                }
            }
        }
        
        // 调试信息：输出收集到的注解信息
        System.out.println("\n=== 调试信息 ===");
        System.out.println("类注解数量: " + classAuthorMap.size());
        System.out.println("方法注解数量: " + methodAuthorMap.size());
        for (Map.Entry<String, String> entry : classAuthorMap.entrySet()) {
            System.out.println("类注解: " + entry.getKey() + " -> " + entry.getValue());
        }
        for (Map.Entry<String, String> entry : methodAuthorMap.entrySet()) {
            System.out.println("方法注解: " + entry.getKey() + " -> " + entry.getValue());
        }
    }

    /**
     * 获取作者信息：方法注解优先，类注解次之
     */
    private static String getAuthorInfo(String methodSignature, String classAuthor) {
        String methodAuthor = methodAuthorMap.get(methodSignature);
        if (methodAuthor != null) {
            return methodAuthor; // 方法上有注解
        }
        return classAuthor; // 返回类上的注解，可能为null
    }

    /**
     * 生成方法签名
     */
    private static String getMethodSignature(String className, String methodName, String methodDesc) {
        return className + "#" + methodName + methodDesc;
    }

    /**
     * 收集所有类的字段信息和注解信息
     */
    private static void collectFieldInfo(File classDir) throws IOException {
        if (classDir.isDirectory()) {
            for (File file : classDir.listFiles()) {
                if (file.isDirectory()) {
                    collectFieldInfo(file);
                } else if (file.getName().endsWith(".class")) {
                    parseClassFile(file);
                }
            }
        }
    }

    /**
     * 解析类文件，收集字段信息和注解信息
     */
    private static void parseClassFile(File classFile) throws IOException {
        try (InputStream in = new FileInputStream(classFile)) {
            ClassReader classReader = new ClassReader(in);
            ClassInfoCollector infoCollector = new ClassInfoCollector();
            classReader.accept(infoCollector, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
    }

    /**
     * 判断是否为标准 getter/setter（基于字段匹配）
     */
    private static boolean isStandardGetterSetter(String methodName, String methodDesc, Map<String, String> fields) {
        // 检查方法名是否符合 getter/setter 规则
        if (methodName.startsWith("get") || methodName.startsWith("is") || methodName.startsWith("set")) {
            String fieldName = extractFieldName(methodName);
            String returnType = getReturnType(methodDesc);
            int paramCount = countParameters(methodDesc);

            // 标准 getter: get + 字段名, 无参数, 返回字段类型
            if ((methodName.startsWith("get") || methodName.startsWith("is")) && paramCount == 0) {
                String expectedFieldType = fields.get(fieldName);
                if (expectedFieldType != null && isTypeCompatible(expectedFieldType, returnType)) {
                    return true; // 标准 getter
                }
                
                // 尝试首字母小写的字段名（Java Bean 规范）
                String lowerCaseFieldName = toFirstLowerCase(fieldName);
                expectedFieldType = fields.get(lowerCaseFieldName);
                if (expectedFieldType != null && isTypeCompatible(expectedFieldType, returnType)) {
                    return true; // 标准 getter
                }
            }

            // 标准 setter: set + 字段名, 1个参数, 返回 void
            if (methodName.startsWith("set") && paramCount == 1 && returnType.equals("V")) {
                String paramType = getFirstParameterType(methodDesc);
                String expectedFieldType = fields.get(fieldName);
                if (expectedFieldType != null && expectedFieldType.equals(paramType)) {
                    return true; // 标准 setter
                }
                
                // 尝试首字母小写的字段名
                String lowerCaseFieldName = toFirstLowerCase(fieldName);
                expectedFieldType = fields.get(lowerCaseFieldName);
                if (expectedFieldType != null && expectedFieldType.equals(paramType)) {
                    return true; // 标准 setter
                }
            }
        }
        return false;
    }

    /**
     * 检查类型是否兼容（处理基本类型和包装类型）
     */
    private static boolean isTypeCompatible(String fieldType, String returnType) {
        // 直接匹配
        if (fieldType.equals(returnType)) {
            return true;
        }
        
        // 处理 boolean 类型的特殊情况
        if (fieldType.equals("Z") && returnType.equals("Z")) {
            return true;
        }
        
        // 处理包装类型（这里简化处理，实际可能需要更复杂的类型映射）
        if (fieldType.equals("Ljava/lang/Boolean;") && returnType.equals("Z")) {
            return true;
        }
        if (fieldType.equals("Z") && returnType.equals("Ljava/lang/Boolean;")) {
            return true;
        }
        
        return false;
    }

    /**
     * 获取第一个参数的类型
     */
    private static String getFirstParameterType(String methodDesc) {
        int start = methodDesc.indexOf('(') + 1;
        int end = methodDesc.indexOf(')');
        if (start >= end) {
            return "";
        }
        
        String params = methodDesc.substring(start, end);
        // 简单处理：取第一个参数类型
        return extractType(params, 0);
    }

    /**
     * 从参数列表中提取指定位置的类型
     */
    private static String extractType(String params, int index) {
        int currentPos = 0;
        for (int i = 0; i <= index && currentPos < params.length(); i++) {
            if (i == index) {
                // 提取当前类型的完整描述符
                char firstChar = params.charAt(currentPos);
                if (firstChar == 'L') {
                    // 对象类型
                    int semicolon = params.indexOf(';', currentPos);
                    if (semicolon != -1) {
                        return params.substring(currentPos, semicolon + 1);
                    }
                } else if (firstChar == '[') {
                    // 数组类型
                    int arrayEnd = currentPos;
                    while (arrayEnd < params.length() && params.charAt(arrayEnd) == '[') {
                        arrayEnd++;
                    }
                    if (arrayEnd < params.length()) {
                        if (params.charAt(arrayEnd) == 'L') {
                            int semicolon = params.indexOf(';', arrayEnd);
                            if (semicolon != -1) {
                                return params.substring(currentPos, semicolon + 1);
                            }
                        } else {
                            return params.substring(currentPos, arrayEnd + 1);
                        }
                    }
                } else {
                    // 基本类型
                    return String.valueOf(firstChar);
                }
            } else {
                // 移动到下一个参数
                currentPos = skipType(params, currentPos);
            }
        }
        return "";
    }

    /**
     * 跳过当前类型，返回下一个参数的开始位置
     */
    private static int skipType(String params, int pos) {
        if (pos >= params.length()) {
            return pos;
        }
        
        char firstChar = params.charAt(pos);
        if (firstChar == 'L') {
            int semicolon = params.indexOf(';', pos);
            return semicolon != -1 ? semicolon + 1 : params.length();
        } else if (firstChar == '[') {
            int arrayEnd = pos;
            while (arrayEnd < params.length() && params.charAt(arrayEnd) == '[') {
                arrayEnd++;
            }
            if (arrayEnd < params.length()) {
                if (params.charAt(arrayEnd) == 'L') {
                    int semicolon = params.indexOf(';', arrayEnd);
                    return semicolon != -1 ? semicolon + 1 : params.length();
                } else {
                    return arrayEnd + 1;
                }
            }
        }
        return pos + 1;
    }

    /**
     * 将字符串首字母转为小写
     */
    private static String toFirstLowerCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.length() == 1) {
            return str.toLowerCase();
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    // 判断是否为标准 Object 方法
    private static boolean isStandardObjectMethod(String methodName, String methodDesc) {
        int paramCount = countParameters(methodDesc);
        String returnType = getReturnType(methodDesc);

        // toString: 无参数 + 返回 String
        if ("toString".equals(methodName) &&
                paramCount == 0 &&
                returnType.equals("Ljava/lang/String;")) {
            return true;
        }

        // hashCode: 无参数 + 返回 int
        if ("hashCode".equals(methodName) &&
                paramCount == 0 &&
                returnType.equals("I")) {
            return true;
        }

        // equals: 1个参数 + 返回 boolean
        if ("equals".equals(methodName) &&
                paramCount == 1 &&
                returnType.equals("Z")) {
            return true;
        }

        // canEqual: 1个参数 + 返回 boolean (Lombok 生成)
        if ("canEqual".equals(methodName) &&
                paramCount == 1 &&
                returnType.equals("Z")) {
            return true;
        }

        return false;
    }

    // 计算参数数量 (基于 JVM 方法描述符)
    private static int countParameters(String methodDesc) {
        int start = methodDesc.indexOf('(');
        int end = methodDesc.indexOf(')');
        if (start == -1 || end == -1) {
            return 0;
        }
        String params = methodDesc.substring(start + 1, end);
        if (params.isEmpty()) {
            return 0;
        }
        
        // 正确计算参数个数
        int count = 0;
        int pos = 0;
        while (pos < params.length()) {
            count++;
            pos = skipType(params, pos);
        }
        return count;
    }

    // 获取返回类型 (JVM 描述符)
    private static String getReturnType(String methodDesc) {
        int endParen = methodDesc.indexOf(')');
        if (endParen == -1) {
            return "";
        }
        return methodDesc.substring(endParen + 1);
    }

    private static String extractFieldName(String methodName) {
        if (methodName.startsWith("get")) {
            return methodName.substring(3);
        } else if (methodName.startsWith("is")) {
            return methodName.substring(2);
        } else if (methodName.startsWith("set")) {
            return methodName.substring(3);
        }
        return methodName;
    }

    /**
     * 用于收集类字段信息和注解信息的 ClassVisitor
     */
    static class ClassInfoCollector extends ClassVisitor {
        private String currentClassName;
        private Map<String, String> fields;

        public ClassInfoCollector() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.currentClassName = name.replace('/', '.');
            this.fields = new HashMap<>();
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            // 检查是否是 @Author 注解 - 使用更宽松的匹配
            if (descriptor.contains("Lorg/clean/Author;")) {
                System.out.println("找到类注解: " + currentClassName + " - " + descriptor);
                return new AuthorAnnotationVisitor(currentClassName, true);
            }
            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            // 收集字段名和类型描述符
            fields.put(name, descriptor);
            return super.visitField(access, name, descriptor, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            // 为每个方法创建一个 MethodVisitor 来收集方法注解
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new MethodAnnotationVisitor(mv, currentClassName, name, descriptor);
        }

        @Override
        public void visitEnd() {
            // 将收集到的字段信息存入全局映射
            classFieldsMap.put(currentClassName, fields);
            super.visitEnd();
        }
    }

    /**
     * 用于收集方法注解信息的 MethodVisitor
     */
    static class MethodAnnotationVisitor extends MethodVisitor {
        private String className;
        private String methodName;
        private String methodDesc;

        public MethodAnnotationVisitor(MethodVisitor mv, String className, String methodName, String methodDesc) {
            super(Opcodes.ASM9, mv);
            this.className = className;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            // 检查是否是 @Author 注解 - 使用更宽松的匹配
            if (descriptor.contains("Author")) {
                System.out.println("找到方法注解: " + className + "." + methodName + " - " + descriptor);
                String methodSignature = getMethodSignature(className, methodName, methodDesc);
                return new AuthorAnnotationVisitor(methodSignature, false);
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }

    /**
     * 用于解析 @Author 注解的 AnnotationVisitor
     */
    static class AuthorAnnotationVisitor extends AnnotationVisitor {
        private String target; // 目标：类名或方法签名
        private boolean isClass; // 是否是类注解
        private String authorValue;

        public AuthorAnnotationVisitor(String target, boolean isClass) {
            super(Opcodes.ASM9);
            this.target = target;
            this.isClass = isClass;
        }

        @Override
        public void visit(String name, Object value) {
            // 获取注解的 value 值
            if ("value".equals(name)) {
                authorValue = (String) value;
                System.out.println("解析注解值: " + target + " -> " + authorValue);
            }
            super.visit(name, value);
        }

        @Override
        public void visitEnd() {
            // 在注解访问结束时保存值
            if (authorValue != null) {
                if (isClass) {
                    classAuthorMap.put(target, authorValue);
                } else {
                    methodAuthorMap.put(target, authorValue);
                }
            }
            super.visitEnd();
        }
    }
}