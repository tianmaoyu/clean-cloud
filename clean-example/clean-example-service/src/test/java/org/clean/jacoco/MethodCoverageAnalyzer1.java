package org.clean.jacoco;

import org.jacoco.core.analysis.*;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

public class MethodCoverageAnalyzer1 {
    public static void main(String[] args) throws Exception {

        String path="/Users/eric/clean-cloud/clean-system/clean-system-service/target";

        // 1. 加载 exec 文件
        ExecutionDataStore executionData = new ExecutionDataStore();
        SessionInfoStore sessionInfos = new SessionInfoStore();
        try (FileInputStream in = new FileInputStream(path+"/jacoco.exec")) {
            ExecutionDataReader reader = new ExecutionDataReader(in);
            reader.setExecutionDataVisitor(executionData);
            reader.setSessionInfoVisitor(sessionInfos);
            reader.read();
        }

        // 2. 分析类文件目录
        CoverageBuilder coverageBuilder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
        analyzer.analyzeAll(new File(path+"/classes"));

        // 3. 遍历每个类、每个方法
        for (IClassCoverage clazz : coverageBuilder.getClasses()) {
            String className = clazz.getName().replace('/', '.');
            for (IMethodCoverage method : clazz.getMethods()) {
                String methodName = method.getName();
                String methodDesc = method.getDesc();
                // 跳过构造函数和静态初始化块
                if ("<init>".equals(methodName) || "<clinit>".equals(methodName)) {
                    continue;
                }
                // 跳过标准 Object 方法
                if (isStandardObjectMethod(methodName, method.getDesc())) {
                    continue;
                }

                int lineCovered = method.getLineCounter().getCoveredCount();
                int lineMissed = method.getLineCounter().getMissedCount();
                System.out.printf("%s.%s -> lines: %d/%d%n", 
                    className, methodName, lineCovered, lineCovered + lineMissed);
            }
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

            // 标准 getter: get + 字段名, 返回字段类型
            if (methodName.startsWith("get") || methodName.startsWith("is")) {
                if (fields.containsKey(fieldName) && fields.get(fieldName).equals(returnType)) {
                    return true; // 标准 getter
                }
            }

            // 标准 setter: set + 字段名, 返回 void
            if (methodName.startsWith("set")) {
                if (fields.containsKey(fieldName) && returnType.equals("V")) {
                    return true; // 标准 setter
                }
            }
        }
        return false;
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
        return params.isEmpty() ? 0 : params.split(",").length;
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
        // 将 getFullName -> "fullName", setUrlX -> "urlX"
        if (methodName.startsWith("get")) {
            return methodName.substring(3);
        } else if (methodName.startsWith("is")) {
            return methodName.substring(2);
        } else if (methodName.startsWith("set")) {
            return methodName.substring(3);
        }
        return methodName;
    }
}