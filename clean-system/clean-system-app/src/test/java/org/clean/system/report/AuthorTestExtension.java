package org.clean.system.report;

import org.clean.Author;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AuthorTestExtension implements BeforeAllCallback, AfterAllCallback, TestWatcher {
    
    private static final Map<String, TestClassInfo> testClassInfoMap = new ConcurrentHashMap<>();
    private static final String CSV_FILE_PATH = "target/test-author-report.csv";
    private static final String DETAILED_CSV_FILE_PATH = "target/test-author-detailed-report.csv";
    
    @Override
    public void beforeAll(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        String className = testClass.getName();
        
        if (isBaseTestSubclass(testClass)) {
            // 获取类级别的 @Author 注解
            Author classAuthorAnnotation = testClass.getAnnotation(Author.class);
            String classAuthor = classAuthorAnnotation != null ? classAuthorAnnotation.value() : "Unknown";
            String classDate = classAuthorAnnotation != null ? classAuthorAnnotation.date() : "";
            
            TestClassInfo classInfo = new TestClassInfo(
                className,
                testClass.getSimpleName(),
                classAuthor,
                classDate,
                getTestMethodsWithAuthor(testClass, classAuthor, classDate)
            );
            
            testClassInfoMap.put(className, classInfo);
            
            System.out.println("📊 开始监控测试类: " + testClass.getSimpleName() + 
                             " [默认作者: " + classAuthor + ", 日期: " + classDate + "]");
        }
    }
    
    @Override
    public void afterAll(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        String className = testClass.getName();
        
        if (testClassInfoMap.containsKey(className)) {
            System.out.println("✅ 完成测试类: " + testClass.getSimpleName());
            
            // 生成报告
            generateSummaryCSVReport();
            generateDetailedCSVReport();
            generateSummaryReport();
        }
    }
    
    @Override
    public void testSuccessful(ExtensionContext context) {
        recordTestResult(context, "SUCCESSFUL", null);
    }
    
    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        recordTestResult(context, "FAILED", cause);
    }
    
    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        recordTestResult(context, "ABORTED", cause);
    }
    
    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        recordTestResult(context, "DISABLED", null);
    }
    
    private void recordTestResult(ExtensionContext context, String status, Throwable failure) {
        try {
            Class<?> testClass = context.getRequiredTestClass();
            String className = testClass.getName();
            Method testMethod = context.getRequiredTestMethod();
            String methodName = testMethod.getName();
            
            if (testClassInfoMap.containsKey(className)) {
                TestClassInfo classInfo = testClassInfoMap.get(className);
                
                // 获取方法级别的 @Author 注解（优先）
                Author methodAuthorAnnotation = testMethod.getAnnotation(Author.class);
                String author;
                String date;
                
                if (methodAuthorAnnotation != null) {
                    author = methodAuthorAnnotation.value();
                    date = methodAuthorAnnotation.date();
                    System.out.println("  ↳ " + methodName + " - " + getStatusEmoji(status) + 
                                     " [方法作者: " + author + "]");
                } else {
                    // 使用类级别的作者信息
                    author = classInfo.getClassAuthor();
                    date = classInfo.getClassDate();
                    System.out.println("  ↳ " + methodName + " - " + getStatusEmoji(status) + 
                                     " [类作者: " + author + "]");
                }
                
                TestMethodInfo methodInfo = new TestMethodInfo(
                    methodName, author, date, status, failure, LocalDateTime.now()
                );
                classInfo.addMethodResult(methodInfo);
            }
        } catch (Exception e) {
            System.err.println("❌ 记录测试结果时出错: " + e.getMessage());
        }
    }
    
    private List<TestMethodInfo> getTestMethodsWithAuthor(Class<?> testClass, String classAuthor, String classDate) {
        List<TestMethodInfo> methods = new ArrayList<>();
        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(org.junit.jupiter.api.Test.class)) {
                // 检查方法级别的 @Author 注解
                Author methodAuthorAnnotation = method.getAnnotation(Author.class);
                String author;
                String date;
                
                if (methodAuthorAnnotation != null) {
                    author = methodAuthorAnnotation.value();
                    date = methodAuthorAnnotation.date();
                } else {
                    author = classAuthor;
                    date = classDate;
                }
                
                methods.add(new TestMethodInfo(method.getName(), author, date, "PENDING", null, null));
            }
        }
        return methods;
    }
    
    private String getStatusEmoji(String status) {
        switch (status) {
            case "SUCCESSFUL": return "✅";
            case "FAILED": return "❌";
            case "ABORTED": return "⚠️";
            case "DISABLED": return "⏸️";
            case "PENDING": return "⏳";
            default: return "❓";
        }
    }
    
    private boolean isBaseTestSubclass(Class<?> testClass) {
        Class<?> superClass = testClass.getSuperclass();
        while (superClass != null) {
            if ("BaseTest".equals(superClass.getSimpleName())) {
                return true;
            }
            superClass = superClass.getSuperclass();
        }
        return false;
    }
    
    private void generateSummaryCSVReport() {
        try {
            java.io.File file = new java.io.File(CSV_FILE_PATH);
            file.getParentFile().mkdirs();
            
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Class Name,Simple Name,Class Author,Class Date,Total Tests,Passed,Failed,Aborted,Disabled,Success Rate(%),Execution Time\n");
                
                for (TestClassInfo classInfo : testClassInfoMap.values()) {
                    int total = classInfo.getTotalTests();
                    int passed = classInfo.getPassedCount();
                    double successRate = total > 0 ? (passed * 100.0 / total) : 0;
                    
                    writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%d,%d,%d,%.2f,%s\n",
                        classInfo.getClassName(),
                        classInfo.getSimpleName(),
                        classInfo.getClassAuthor(),
                        classInfo.getClassDate(),
                        total,
                        passed,
                        classInfo.getFailedCount(),
                        classInfo.getAbortedCount(),
                        classInfo.getDisabledCount(),
                        successRate,
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    ));
                }
            }
            
            System.out.println("📈 类级别 CSV 报告已生成: " + CSV_FILE_PATH);
            
        } catch (IOException e) {
            System.err.println("❌ 生成类级别 CSV 报告失败: " + e.getMessage());
        }
    }
    
    private void generateDetailedCSVReport() {
        try {
            java.io.File file = new java.io.File(DETAILED_CSV_FILE_PATH);
            file.getParentFile().mkdirs();
            
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Class Name,Simple Name,Method Name,Author,Author Date,Status,Execution Time,Failure Message\n");
                
                for (TestClassInfo classInfo : testClassInfoMap.values()) {
                    for (TestMethodInfo methodInfo : classInfo.getMethodResults().values()) {
                        String failureMessage = methodInfo.getFailure() != null ? 
                            methodInfo.getFailure().getMessage() : "";
                        
                        writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            classInfo.getClassName(),
                            classInfo.getSimpleName(),
                            methodInfo.getMethodName(),
                            methodInfo.getAuthor(),
                            methodInfo.getAuthorDate(),
                            methodInfo.getStatus(),
                            methodInfo.getExecutionTime() != null ? 
                                methodInfo.getExecutionTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                            failureMessage.replace("\"", "'") // 避免 CSV 格式问题
                        ));
                    }
                }
            }
            
            System.out.println("📊 方法级别详细 CSV 报告已生成: " + DETAILED_CSV_FILE_PATH);
            
        } catch (IOException e) {
            System.err.println("❌ 生成方法级别详细 CSV 报告失败: " + e.getMessage());
        }
    }
    
    private void generateSummaryReport() {
        int totalClasses = testClassInfoMap.size();
        int totalTests = testClassInfoMap.values().stream().mapToInt(TestClassInfo::getTotalTests).sum();
        int totalPassed = testClassInfoMap.values().stream().mapToInt(TestClassInfo::getPassedCount).sum();
        int totalFailed = testClassInfoMap.values().stream().mapToInt(TestClassInfo::getFailedCount).sum();
        int totalAborted = testClassInfoMap.values().stream().mapToInt(TestClassInfo::getAbortedCount).sum();
        int totalDisabled = testClassInfoMap.values().stream().mapToInt(TestClassInfo::getDisabledCount).sum();
        
        // 统计作者信息
        Map<String, AuthorStats> authorStats = new HashMap<>();
        for (TestClassInfo classInfo : testClassInfoMap.values()) {
            for (TestMethodInfo methodInfo : classInfo.getMethodResults().values()) {
                String author = methodInfo.getAuthor();
                AuthorStats stats = authorStats.getOrDefault(author, new AuthorStats(author));
                stats.addTestResult(methodInfo.getStatus());
                authorStats.put(author, stats);
            }
        }
        
    }
    
    // 作者统计内部类
    private static class AuthorStats {
        private final String author;
        private int passed = 0;
        private int failed = 0;
        private int aborted = 0;
        private int disabled = 0;
        
        public AuthorStats(String author) {
            this.author = author;
        }
        
        public void addTestResult(String status) {
            switch (status) {
                case "SUCCESSFUL": passed++; break;
                case "FAILED": failed++; break;
                case "ABORTED": aborted++; break;
                case "DISABLED": disabled++; break;
            }
        }
        
        // Getters
        public String getAuthor() { return author; }
        public int getPassed() { return passed; }
        public int getFailed() { return failed; }
        public int getAborted() { return aborted; }
        public int getDisabled() { return disabled; }
    }
}