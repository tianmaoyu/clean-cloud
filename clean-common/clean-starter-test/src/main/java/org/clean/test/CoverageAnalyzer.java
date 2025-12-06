package org.clean.test;// 引入必要的包

import lombok.extern.slf4j.Slf4j;
import org.jacoco.core.analysis.*;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * 1-5：简单方法，易于理解和测试
 * 6-10：中等复杂度，需要适当关注
 * 11-20：复杂方法，建议重构
 * 21+：非常复杂，急需重构
 */
@Slf4j
public class CoverageAnalyzer {

    private static final Set<String> STANDARD_OBJECT_METHODS = new HashSet<>(Arrays.asList(
            "toString", "hashCode", "equals", "clone", "finalize",
            "wait", "notify", "notifyAll"
    ));

    public void analyzeCoverage(String path) throws IOException {
        // 1. 加载 exec 文件
        ExecutionDataStore executionData = new ExecutionDataStore();
        SessionInfoStore sessionInfos = new SessionInfoStore();
        try (FileInputStream in = new FileInputStream(path + "/target/jacoco.exec")) {
            ExecutionDataReader reader = new ExecutionDataReader(in);
            reader.setExecutionDataVisitor(executionData);
            reader.setSessionInfoVisitor(sessionInfos);
            reader.read();
        }

        // 2. 分析类文件目录
        CoverageBuilder coverageBuilder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
        analyzer.analyzeAll(new File(path + "/target/classes"));

        // 3. 准备输出报告
        System.out.println("====================================================================");
        System.out.println("代码覆盖率分析报告");
        System.out.println("====================================================================");

        // 用于统计总体信息
        int totalMethods = 0;
        int coveredMethods = 0;
        int totalComplexity = 0;
        int coveredComplexity = 0;

        // 按包名分组统计
        Map<String, PackageStats> packageStats = new HashMap<>();

        // 4. 遍历每个类、每个方法
        for (IClassCoverage clazz : coverageBuilder.getClasses()) {
            String className = clazz.getName();
            String packageName = getPackageName(className);

            // 获取类的字节码以计算圈复杂度
            ClassNode classNode = parseClassFile(new File(path + "/target/classes",
                    clazz.getName().replace('.', '/') + ".class"));
//            AuthorInfo classAuthor = getClassAuthorInfo(classNode);

            // 初始化包统计信息
            PackageStats stats = packageStats.computeIfAbsent(packageName,
                    k -> new PackageStats());

            for (IMethodCoverage method : clazz.getMethods()) {
                String methodName = method.getName();
                String methodDesc = method.getDesc();

                // 跳过构造函数和静态初始化块
                if ("<init>".equals(methodName) || "<clinit>".equals(methodName)) {
                    continue;
                }
                // 跳过标准 Object 方法
                if (isStandardObjectMethod(methodName, methodDesc)) {
                    continue;
                }

                // 获取方法作者信息（优先方法注解，其次类注解）
                AuthorInfo methodAuthor = getMethodAuthorInfo(classNode, methodName, methodDesc);

                // 获取方法签名
                String methodSignature = getMethodSignature(methodName, methodDesc);

                // 计算行覆盖率
                int lineCovered = method.getLineCounter().getCoveredCount();
                int lineMissed = method.getLineCounter().getMissedCount();
                double lineCoverageRatio = method.getLineCounter().getCoveredRatio();

                // 计算分支覆盖率
                int branchCovered = method.getBranchCounter().getCoveredCount();
                int branchMissed = method.getBranchCounter().getMissedCount();
                double branchCoveredRatio = method.getBranchCounter().getCoveredRatio();

                // 计算圈复杂度
                int complexity = method.getComplexityCounter().getTotalCount();
                int complexityCovered = method.getComplexityCounter().getCoveredCount();
                int complexityMissed = method.getComplexityCounter().getMissedCount();


                // 计算指令覆盖率
                int instructionCovered = method.getInstructionCounter().getCoveredCount();
                int instructionMissed = method.getInstructionCounter().getMissedCount();
                double instructionCoverage = method.getInstructionCounter().getCoveredRatio();


                // 更新统计信息
                totalMethods++;
                if (lineCovered > 0) coveredMethods++;
                totalComplexity += complexity;
                if (lineCovered > 0) coveredComplexity += complexity;

                // 更新包统计
                stats.totalMethods++;
                if (lineCovered > 0) stats.coveredMethods++;
                stats.totalComplexity += complexity;
                if (lineCovered > 0) stats.coveredComplexity += complexity;

                // 输出方法详细信息
                System.out.println("\n方法详细信息:");
                System.out.printf("包名: %s\n", packageName);
                System.out.printf("类名: %s\n", className);
                System.out.printf("方法: %s\n", methodSignature);
                System.out.printf("圈复杂度: %d\n", complexity);
                System.out.printf("行覆盖率: %f\n",lineCoverageRatio);
                System.out.printf("分支覆盖率: %f\n", branchCoveredRatio);
                System.out.printf("指令覆盖率: %f\n",instructionCoverage);
                System.out.printf("是否被覆盖: %s\n", lineCovered > 0 ? "是" : "否");

                // 输出未覆盖的行号
                if (lineMissed > 0) {
                    System.out.print("未覆盖的行号: ");
                    for (int i = method.getFirstLine(); i <= method.getLastLine(); i++) {
                        if (method.getLine(i).getStatus() == ICounter.NOT_COVERED) {
                            System.out.print(i + " ");
                        }
                    }
                    System.out.println();
                }
            }
        }

        // 5. 输出总体统计信息
        System.out.println("\n" + "=".repeat(60));
        System.out.println("总体统计:");
        System.out.printf("总方法数: %d\n", totalMethods);
        System.out.printf("已覆盖方法数: %d (%.1f%%)\n", coveredMethods, totalMethods > 0 ? (double) coveredMethods / totalMethods * 100 : 0);
        System.out.printf("总圈复杂度: %d\n", totalComplexity);
        System.out.printf("已覆盖圈复杂度: %d (%.1f%%)\n", coveredComplexity, totalComplexity > 0 ? (double) coveredComplexity / totalComplexity * 100 : 0);

        // 6. 按包输出统计信息
        System.out.println("\n" + "=".repeat(60));
        System.out.println("按包统计:");
        System.out.printf("%-30s %-10s %-10s %-10s %-10s\n", "包名", "方法数", "覆盖率", "圈复杂度", "复杂度覆盖率");
        System.out.println("-".repeat(70));

        for (Map.Entry<String, PackageStats> entry : packageStats.entrySet()) {
            PackageStats stats = entry.getValue();
            double methodCoverage = stats.totalMethods > 0 ? (double) stats.coveredMethods / stats.totalMethods * 100 : 0;
            double complexityCoverage = stats.totalComplexity > 0 ? (double) stats.coveredComplexity / stats.totalComplexity * 100 : 0;

            System.out.printf("%-30s %-10d %-10.1f%% %-10d %-10.1f%%\n",
                    entry.getKey(), stats.totalMethods, methodCoverage,
                    stats.totalComplexity, complexityCoverage);
        }
    }

    // 辅助类：包统计信息
    private static class PackageStats {
        int totalMethods = 0;
        int coveredMethods = 0;
        int totalComplexity = 0;
        int coveredComplexity = 0;
    }

    // 获取包名
    private String getPackageName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot > 0 ? className.substring(0, lastDot) : "(default package)";
    }

    // 获取方法签名
    private String getMethodSignature(String methodName, String methodDesc) {
        // 这里可以解析方法描述符以获得更友好的显示
        // 简化实现：直接返回名称和描述符
        return methodName + methodDesc;
    }

    // 解析类文件
    private ClassNode parseClassFile(File classFile) throws IOException {
        ClassNode classNode = new ClassNode();
        try (FileInputStream fis = new FileInputStream(classFile)) {
            ClassReader classReader = new ClassReader(fis);
            classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
        }
        return classNode;
    }


    // 检查是否是标准 Object 方法
    private boolean isStandardObjectMethod(String methodName, String methodDesc) {
        return STANDARD_OBJECT_METHODS.contains(methodName);
    }

    // 生成 HTML 报告
    public void generateHtmlReport(String sourceDir, String classDir, String execFile, String reportDir) throws IOException {
        // 创建执行数据存储
        ExecutionDataStore executionData = new ExecutionDataStore();
        SessionInfoStore sessionInfos = new SessionInfoStore();

        // 读取执行数据
        try (FileInputStream fis = new FileInputStream(execFile)) {
            ExecutionDataReader reader = new ExecutionDataReader(fis);
            reader.setExecutionDataVisitor(executionData);
            reader.setSessionInfoVisitor(sessionInfos);
            reader.read();
        }

        // 创建覆盖率构建器
        CoverageBuilder coverageBuilder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
        analyzer.analyzeAll(new File(classDir));

        // 创建 HTML 报告
        File reportDirectory = new File(reportDir);
        HTMLFormatter htmlFormatter = new HTMLFormatter();
        IReportVisitor visitor = htmlFormatter.createVisitor( new FileMultiReportOutput(reportDirectory));
        visitor.visitInfo(sessionInfos.getInfos(),executionData.getContents());
        visitor.visitBundle(coverageBuilder.getBundle("Project"),new DirectorySourceFileLocator(new File(sourceDir), "utf-8", 4));
        visitor.visitEnd();

        System.out.println("HTML 报告已生成到: " + reportDirectory.getAbsolutePath());
    }

    // 生成 CSV 报告
    public void generateCsvReport(String classDir, String execFile,
                                  String csvFile) throws IOException {
        ExecutionDataStore executionData = new ExecutionDataStore();
        SessionInfoStore sessionInfos = new SessionInfoStore();

        try (FileInputStream fis = new FileInputStream(execFile)) {
            ExecutionDataReader reader = new ExecutionDataReader(fis);
            reader.setExecutionDataVisitor(executionData);
            reader.setSessionInfoVisitor(sessionInfos);
            reader.read();
        }

        CoverageBuilder coverageBuilder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
        analyzer.analyzeAll(new File(classDir));

        try (FileOutputStream fos = new FileOutputStream(csvFile);
             PrintWriter writer = new PrintWriter(fos)) {

            writer.println("Package,Class,Method,Complexity," +
                    "Lines Covered,Lines Total,Line Coverage," +
                    "Branches Covered,Branches Total,Branch Coverage");

            for (IClassCoverage clazz : coverageBuilder.getClasses()) {
                String className = clazz.getName().replace('/', '.');
                String packageName = getPackageName(className);

                for (IMethodCoverage method : clazz.getMethods()) {
                    String methodName = method.getName();
                    if ("<init>".equals(methodName) || "<clinit>".equals(methodName)) {
                        continue;
                    }

                    writer.printf("%s,%s,%s,%d,%d,%d,%.2f,%d,%d,%.2f%n",
                            packageName,
                            className,
                            methodName,
                            method.getComplexityCounter().getTotalCount(),
                            method.getLineCounter().getCoveredCount(),
                            method.getLineCounter().getTotalCount(),
                            method.getLineCounter().getCoveredRatio(),
                            method.getBranchCounter().getCoveredCount(),
                            method.getBranchCounter().getTotalCount(),
                            method.getBranchCounter().getCoveredRatio());
                }
            }
        }

        System.out.println("CSV 报告已生成到: " + csvFile);
    }

    public static void main(String[] args) {

        String projectId=args[0];
        log.info("projectId：{}", projectId);
        String basePath=args[1];
        log.info("basePath：{}", basePath);
        System.out.println("====================================================================");

        CoverageAnalyzer analyzer = new CoverageAnalyzer();
        try {

            // 示例用法
            analyzer.analyzeCoverage(basePath);

            // 生成 HTML 报告
            analyzer.generateHtmlReport(
                    basePath+"/src/main/java",
                    basePath + "/target/classes",
                    basePath + "/target/jacoco.exec",
                    basePath + "/target/coverage-report"
            );

            // 生成 CSV 报告
            analyzer.generateCsvReport(
                    basePath + "/target/classes",
                    basePath + "/target/jacoco.exec",
                    basePath + "/target/coverage-report/coverage.csv"
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取方法的作者信息（优先从方法注解，其次从类注解）
     */
    public static AuthorInfo getMethodAuthorInfo(ClassNode classNode,String methodName,String methodDesc) {

        // 首先从方法注解中查找
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(methodName) && (methodDesc == null || method.desc.equals(methodDesc))) {

                // 检查方法上的可见注解
                AuthorInfo methodAuthor = extractAuthorFromAnnotations(method.visibleAnnotations);
                if (methodAuthor != null) {
                    methodAuthor.setFromMethod(true);
                    return methodAuthor;
                }

                // 检查方法上的不可见注解
                methodAuthor = extractAuthorFromAnnotations(method.invisibleAnnotations);
                if (methodAuthor != null) {
                    methodAuthor.setFromMethod(true);
                    return methodAuthor;
                }

                break; // 找到方法后就退出循环
            }
        }

        // 如果方法上没有找到，从类注解中查找
        AuthorInfo classAuthor = extractAuthorFromAnnotations(classNode.visibleAnnotations);
        if (classAuthor != null) {
            classAuthor.setFromMethod(false);
            return classAuthor;
        }

        classAuthor = extractAuthorFromAnnotations(classNode.invisibleAnnotations);
        if (classAuthor != null) {
            classAuthor.setFromMethod(false);
            return classAuthor;
        }

        return null; // 都没有找到
    }

    /**
     * 获取类的作者信息
     */
    public static AuthorInfo getClassAuthorInfo(ClassNode classNode) {
        // 检查可见注解
        AuthorInfo author = extractAuthorFromAnnotations(classNode.visibleAnnotations);
        if (author != null) return author;

        // 检查不可见注解
        author = extractAuthorFromAnnotations(classNode.invisibleAnnotations);
        return author;
    }

    private static AuthorInfo extractAuthorFromAnnotations(List<AnnotationNode> annotations) {
        if (annotations == null || annotations.isEmpty()) return null;

        for (AnnotationNode annotation : annotations) {
            // 检查是否是 @Author 注解
            // 注解描述符格式: "Lcom/example/Author;" 或 "Lcom/example/annotation/Author;"
            if (annotation.desc.contains("Author")) {
                log.info("Author: {}", annotation.desc);
                return parseAuthorAnnotation(annotation);
            }
        }
        return null;
    }

    /**
     * 解析 @Author 注解的值。新的写法
     */
    private static AuthorInfo parseAuthorAnnotation(AnnotationNode annotation) {

        if (annotation.values == null || annotation.values.isEmpty())  return null;

        AuthorInfo authorInfo = null;

        // annotation.values 是一个交替的键值对列表: [key1, value1, key2, value2, ...]
        for (int i = 0; i < annotation.values.size(); i += 2) {
            String key = (String) annotation.values.get(i);
            Object value = annotation.values.get(i + 1);
            if ("value".equals(key)) {
                authorInfo = new AuthorInfo();
                authorInfo.setValue((String) value);
            } else if ("date".equals(key)) {
                authorInfo.setDate((String) value);
            }
        }
        return authorInfo;
    }
}