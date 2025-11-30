package org.clean.system.report;

import org.clean.Author;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AuthorTestReportListener implements TestExecutionListener {
    
    private final Map<String, TestClassInfo> testClassInfoMap = new HashMap<>();
    private final String csvFilePath = "test-author-report.csv";
    
    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        // 初始化时收集所有测试类的信息
        testPlan.getRoots().forEach(root -> 
            collectTestClasses(testPlan, root)
        );
    }
    
    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            String className = getClassName(testIdentifier);
            String methodName = getMethodName(testIdentifier);
            
            if (className != null && testClassInfoMap.containsKey(className)) {
                TestClassInfo classInfo = testClassInfoMap.get(className);

                TestMethodInfo testMethodInfo = new TestMethodInfo();
                testMethodInfo.setMethodName(methodName);
                testMethodInfo.setAuthor(classInfo.getClassAuthor());
                testMethodInfo.setStatus(testExecutionResult.getStatus().toString());
                testMethodInfo.setFailure(testExecutionResult.getThrowable().orElse(null));
                testMethodInfo.setExecutionTime(LocalDateTime.now());
                testMethodInfo.setAuthorDate(classInfo.getClassDate());

                classInfo.addMethodResult(testMethodInfo);

            }
        }
    }
    
    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        generateCSVReport();
    }
    
    private void collectTestClasses(TestPlan testPlan, TestIdentifier identifier) {
        if (identifier.isContainer() && identifier.getSource().isPresent()) {
            String source = identifier.getSource().get().toString();
            if (source.contains("ClassSource")) {
                try {
                    String className = extractClassName(source);
                    Class<?> testClass = Class.forName(className);
                    Author authorAnnotation = testClass.getAnnotation(Author.class);
                    
                    if (authorAnnotation != null) {
                        TestClassInfo classInfo = new TestClassInfo(
                            className,
                            testClass.getSimpleName(),
                            authorAnnotation.value(),
                            authorAnnotation.date(),
                            getTestMethods(testClass)
                        );
                        testClassInfoMap.put(className, classInfo);
                    }
                } catch (ClassNotFoundException e) {
                    // 忽略无法加载的类
                }
            }
        }
        
        // 递归处理子元素
        testPlan.getChildren(identifier).forEach(child -> 
            collectTestClasses(testPlan, child)
        );
    }
    
    private String extractClassName(String source) {
        // 从 ClassSource 字符串中提取类名
        // 示例: [class:com.example.MyTest]
        int start = source.indexOf("[class:") + 7;
        int end = source.lastIndexOf("]");
        return source.substring(start, end);
    }
    
    private String getClassName(TestIdentifier testIdentifier) {
        return testIdentifier.getSource()
            .map(source -> extractClassName(source.toString()))
            .orElse(null);
    }
    
    private String getMethodName(TestIdentifier testIdentifier) {
        return testIdentifier.getSource()
            .map(source -> {
                String sourceStr = source.toString();
                if (sourceStr.contains("method:")) {
                    int start = sourceStr.indexOf("method:") + 7;
                    int end = sourceStr.lastIndexOf(")");
                    return sourceStr.substring(start, end);
                }
                return testIdentifier.getDisplayName();
            })
            .orElse(testIdentifier.getDisplayName());
    }
    
    private List<TestMethodInfo> getTestMethods(Class<?> testClass) {
        List<TestMethodInfo> methods = new ArrayList<>();
        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(org.junit.jupiter.api.Test.class)) {
                methods.add(new TestMethodInfo(method.getName(), "PENDING", null, null));
            }
        }
        return methods;
    }
    
    private void generateCSVReport() {
        try (FileWriter writer = new FileWriter(csvFilePath)) {
            // 写入 CSV 头部
            writer.write("Class Name,Author,Date,Total Tests,Passed,Failed,Skipped,Execution Time\n");
            
            for (TestClassInfo classInfo : testClassInfoMap.values()) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",%d,%d,%d,%d,%s\n",
                    classInfo.getClassName(),
                    classInfo.getClassAuthor(),
                    classInfo.getClassDate(),
                    classInfo.getTotalTests(),
                    classInfo.getPassedCount(),
                    classInfo.getFailedCount(),
                    classInfo.getAbortedCount() + classInfo.getDisabledCount(),
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ));
            }
            
            System.out.println("CSV report generated: " + csvFilePath);
            
        } catch (IOException e) {
            System.err.println("Failed to generate CSV report: " + e.getMessage());
        }
    }
}