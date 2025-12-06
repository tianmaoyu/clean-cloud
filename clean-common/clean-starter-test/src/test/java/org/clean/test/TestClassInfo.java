package org.clean.test;

import lombok.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TestClassInfo {
    private  String className;
    private  String simpleName;
    private  String classAuthor;
    private  String classDate;
    private  List<TestMethodInfo> testMethods;
    private  Map<String, TestMethodInfo> methodResults = new HashMap<>();
    
    public TestClassInfo(String className, String simpleName, String classAuthor, String classDate, List<TestMethodInfo> testMethods) {
        this.className = className;
        this.simpleName = simpleName;
        this.classAuthor = classAuthor;
        this.classDate = classDate;
        this.testMethods = testMethods;
    }

    public void addMethodResult(TestMethodInfo methodInfo) {
        methodResults.put(methodInfo.getMethodName(), methodInfo);
    }

    public int getTotalTests() {
        return methodResults.size();
    }

    public int getPassedCount() {
        return (int) methodResults.values().stream()
                .filter(m -> "SUCCESSFUL".equals(m.getStatus()))
                .count();
    }

    public int getFailedCount() {
        return (int) methodResults.values().stream()
                .filter(m -> "FAILED".equals(m.getStatus()))
                .count();
    }

    public int getAbortedCount() {
        return (int) methodResults.values().stream()
                .filter(m -> "ABORTED".equals(m.getStatus()))
                .count();
    }

    public int getDisabledCount() {
        return (int) methodResults.values().stream()
                .filter(m -> "DISABLED".equals(m.getStatus()))
                .count();
    }
    
    public Map<String, TestMethodInfo> getMethodResults() {
        return methodResults;
    }
}