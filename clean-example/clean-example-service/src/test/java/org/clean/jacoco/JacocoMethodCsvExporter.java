package org.clean.jacoco;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JacocoMethodCsvExporter {

    public static void main(String[] args) {
        // 配置输入和输出路径
        String jacocoXmlPath="/Users/eric/clean-cloud/clean-system/clean-system-service/target/site/jacoco/jacoco.xml";
        String outputCsvPath = "method_coverage.csv";

        try {
            exportMethodCoverageToCsv(jacocoXmlPath, outputCsvPath);
            System.out.println("✅ 方法级覆盖率已导出到: " + Paths.get(outputCsvPath).toAbsolutePath());
        } catch (Exception e) {
            System.err.println("❌ 解析失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void exportMethodCoverageToCsv(String xmlPath, String csvPath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // 🔒 关键：禁用 DTD 和外部实体，防止加载 report.dtd
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false); // 允许 DOCTYPE 存在
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setValidating(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlPath);
        doc.getDocumentElement().normalize();

        XPath xpath = XPathFactory.newInstance().newXPath();

        try (FileWriter writer = new FileWriter(csvPath)) {
            // 写入 CSV 头部
            writer.write(
                "PACKAGE,CLASS,METHOD_NAME,METHOD_DESC,LINE_START," +
                "INSTRUCTION_MISSED,INSTRUCTION_COVERED," +
                "BRANCH_MISSED,BRANCH_COVERED," +
                "LINE_MISSED,LINE_COVERED," +
                "METHOD_MISSED,METHOD_COVERED\n"
            );

            // 获取所有 <package> 节点
            NodeList packages = (NodeList) xpath.compile("//package").evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < packages.getLength(); i++) {
                Element pkgElem = (Element) packages.item(i);
                String packageName = pkgElem.getAttribute("name").replace('/', '.');

                // 获取该包下所有 <class>
                NodeList classes = (NodeList) xpath.compile(".//class").evaluate(pkgElem, XPathConstants.NODESET);
                for (int j = 0; j < classes.getLength(); j++) {
                    Element classElem = (Element) classes.item(j);
                    String className = classElem.getAttribute("name");
                    // 提取简单类名（去掉包路径）
                    if (className.contains("/")) {
                        className = className.substring(className.lastIndexOf('/') + 1);
                    }

                    // 获取该类下所有 <method>
                    NodeList methods = (NodeList) xpath.compile(".//method").evaluate(classElem, XPathConstants.NODESET);
                    for (int k = 0; k < methods.getLength(); k++) {
                        Element methodElem = (Element) methods.item(k);
                        String methodName = methodElem.getAttribute("name");
                        String methodDesc = methodElem.getAttribute("desc");
                        String lineStart = methodElem.getAttribute("line");

                        // 初始化计数器
                        int instrMissed = 0, instrCovered = 0;
                        int branchMissed = 0, branchCovered = 0;
                        int lineMissed = 0, lineCovered = 0;

                        // 解析 <counter> 子节点
                        NodeList counters = methodElem.getElementsByTagName("counter");
                        for (int c = 0; c < counters.getLength(); c++) {
                            Element counter = (Element) counters.item(c);
                            String type = counter.getAttribute("type");
                            int missed = Integer.parseInt(counter.getAttribute("missed"));
                            int covered = Integer.parseInt(counter.getAttribute("covered"));

                            switch (type) {
                                case "INSTRUCTION":
                                    instrMissed = missed;
                                    instrCovered = covered;
                                    break;
                                case "BRANCH":
                                    branchMissed = missed;
                                    branchCovered = covered;
                                    break;
                                case "LINE":
                                    lineMissed = missed;
                                    lineCovered = covered;
                                    break;
                            }
                        }

                        // 判断方法是否被调用：只要 instruction covered > 0 就算覆盖
                        int methodMissed = (instrCovered > 0) ? 0 : 1;
                        int methodCovered = 1 - methodMissed;

                        // 写入 CSV 行（注意转义逗号和引号，此处假设无特殊字符）
                        String row = String.format(
                            "%s,%s,%s,%s,%s,%d,%d,%d,%d,%d,%d,%d,%d\n",
                            escapeCsv(packageName),
                            escapeCsv(className),
                            escapeCsv(methodName),
                            escapeCsv(methodDesc),
                            lineStart.isEmpty() ? "" : lineStart,
                            instrMissed, instrCovered,
                            branchMissed, branchCovered,
                            lineMissed, lineCovered,
                            methodMissed, methodCovered
                        );
                        writer.write(row);
                    }
                }
            }
        }
    }

    // 简单 CSV 转义（处理包含逗号、换行、引号的字段）
    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}