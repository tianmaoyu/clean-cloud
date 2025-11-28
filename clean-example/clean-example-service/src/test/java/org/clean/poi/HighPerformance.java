package org.clean.poi;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlObject;
import org.clean.xing.QRCodeGenerator;

import java.io.*;
import java.util.*;

@Slf4j
public class HighPerformance {

    private static final int BUFFER_SIZE = 8192;
    private static final String TEMPLATE_FILE_PATH = "./data/template.docx";
    private static final String OUTPUT_FILE_PATH = "./data/final_output.docx";
    private static final int BARCODE_WIDTH_EMU = Units.toEMU(150);
    private static final int BARCODE_HEIGHT_EMU = Units.toEMU(20);
    private static final int QRCODE_SIZE_EMU = Units.toEMU(75);
    private static final String BATCH_NO_PREFIX = "XC2025001-";

    public static void main(String[] args) {
        fillDocumentSingleDoc(1000);
    }

    @SneakyThrows
    private static void fillDocumentSingleDoc(int documentCount) {
        long startTime = System.currentTimeMillis();

        // 预生成所有数据
        List<Map<String, TemplateValue>> templateDataList = generateTemplateDataList(documentCount);

        // 处理文档
        processDocument(templateDataList);

        long endTime = System.currentTimeMillis();
        log.info("处理数量: {} 总耗时: {}ms", documentCount, endTime - startTime);
    }

    /**
     * 生成模板数据列表
     */
    private static List<Map<String, TemplateValue>> generateTemplateDataList(int dataCount) {
        List<Map<String, TemplateValue>> dataList = new ArrayList<>(dataCount);
        for (int index = 0; index < dataCount; index++) {
            TemplateData templateData = generateTemplateData(String.valueOf(index));
            Map<String, TemplateValue> fieldValueMap = convertToTemplateValueMap(templateData);
            dataList.add(fieldValueMap);
        }
        return dataList;
    }

    /**
     * 处理文档主流程
     */
    @SneakyThrows
    private static void processDocument(List<Map<String, TemplateValue>> templateDataList) {
        // 加载模板文档
        byte[] templateContent = loadFileToBytes(TEMPLATE_FILE_PATH);
        
        try (XWPFDocument sourceDocument = new XWPFDocument(new ByteArrayInputStream(templateContent))) {
            // 扩展文档表格数量
            XWPFDocument expandedDocument = duplicateDocumentTables(sourceDocument, templateDataList.size());
            
            try {
                // 填充模板占位符
                populateTablePlaceholders(expandedDocument, templateDataList);
                
                // 保存生成的文件
                saveGeneratedDocument(expandedDocument, OUTPUT_FILE_PATH);
            } finally {
                expandedDocument.close();
            }
        }
    }

    /**
     * 复制文档表格
     */
    @SneakyThrows
    private static XWPFDocument duplicateDocumentTables(XWPFDocument sourceDocument, int tableCount) {
        List<XWPFTable> tables = sourceDocument.getTables();
        if (tables.isEmpty()) {
            throw new IllegalStateException("源文档中未找到任何表格");
        }

        XWPFTable sourceTable = tables.get(0);
        XmlObject sourceTableXml = sourceTable.getCTTbl().copy();

        // 批量复制表格
        for (int i = 1; i < tableCount; i++) {
            XWPFTable newTable = sourceDocument.createTable();
            newTable.getCTTbl().set(sourceTableXml);
            
            // 添加分页符
            sourceDocument.createParagraph().createRun().addBreak(BreakType.PAGE);
        }

        // 重新序列化文档以刷新内部状态
        return recreateDocumentFromBytes(sourceDocument);
    }

    /**
     * 通过字节流重新创建文档
     */
    @SneakyThrows
    private static XWPFDocument recreateDocumentFromBytes(XWPFDocument document) {
        try (ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(BUFFER_SIZE * 4)) {
            document.write(byteOutputStream);
            return new XWPFDocument(new ByteArrayInputStream(byteOutputStream.toByteArray()));
        }
    }

    /**
     * 填充表格占位符
     */
    public static void populateTablePlaceholders(XWPFDocument document, List<Map<String, TemplateValue>> placeholderDataList) {
        int currentTableIndex = 0;
        
        for (XWPFTable table : document.getTables()) {
            if (currentTableIndex >= placeholderDataList.size()) {
                break;
            }
            
            Map<String, TemplateValue> placeholderValues = placeholderDataList.get(currentTableIndex);
            currentTableIndex++;
            
            processTablePlaceholders(table, placeholderValues);
        }
    }

    /**
     * 处理单个表格的占位符
     */
    private static void processTablePlaceholders(XWPFTable table, Map<String, TemplateValue> placeholderValues) {
        for (XWPFTableRow tableRow : table.getRows()) {
            for (XWPFTableCell tableCell : tableRow.getTableCells()) {
                processCellPlaceholders(tableCell, placeholderValues);
            }
        }
    }

    /**
     * 处理单元格占位符
     */
    private static void processCellPlaceholders(XWPFTableCell cell, Map<String, TemplateValue> placeholderValues) {
        for (XWPFParagraph paragraph : cell.getParagraphs()) {
            String paragraphText = paragraph.getText();
            if (containsPlaceholder(paragraphText)) {
                processParagraphPlaceholders(paragraph, placeholderValues);
            }
        }
    }

    /**
     * 处理段落占位符
     */
    private static void processParagraphPlaceholders(XWPFParagraph paragraph, Map<String, TemplateValue> placeholderValues) {
        for (XWPFRun textRun : paragraph.getRuns()) {
            replaceRunPlaceholder(textRun, placeholderValues);
        }
    }

    /**
     * 判断文本是否包含占位符
     */
    private static boolean containsPlaceholder(String text) {
        return StringUtils.isNotBlank(text) && text.contains("{") && text.contains("}");
    }

    @SneakyThrows
    public static boolean replaceRunPlaceholder(XWPFRun textRun, Map<String, TemplateValue> placeholderValues) {
        String runText = textRun.getText(0);
        String placeholderKey = extractPlaceholderKey(runText);
        
        if (placeholderKey == null) {
            return false;
        }

        TemplateValue templateValue = placeholderValues.get(placeholderKey);
        if (templateValue == null) {
            log.debug("未找到对应的占位符值: {}", placeholderKey);
            return false;
        }

        // 清空原始文本
        textRun.setText("", 0);

        // 根据数据类型处理内容
        return processTemplateValue(textRun, templateValue);
    }

    /**
     * 处理模板值
     */
    @SneakyThrows
    private static boolean processTemplateValue(XWPFRun textRun, TemplateValue templateValue) {
        switch (templateValue.getType()) {
            case STRING:
                textRun.setText(templateValue.getValue().toString());
                return true;
            case BARCODE:
                insertImageToRun(textRun, (byte[]) templateValue.getValue(), XWPFDocument.PICTURE_TYPE_PNG, BARCODE_WIDTH_EMU, BARCODE_HEIGHT_EMU);
                return true;
            case QRCODE:
                insertImageToRun(textRun, (byte[]) templateValue.getValue(), XWPFDocument.PICTURE_TYPE_PNG, QRCODE_SIZE_EMU, QRCODE_SIZE_EMU);
                return true;
            default:
                log.warn("不支持的模板值类型: {}", templateValue.getType());
                return false;
        }
    }

    /**
     * 插入图片到文本运行对象
     */
    @SneakyThrows
    private static void insertImageToRun(XWPFRun textRun, byte[] imageData, int imageType, 
                                    int widthEmu, int heightEmu) {
        try (ByteArrayInputStream imageStream = new ByteArrayInputStream(imageData)) {
            String imageId = UUID.randomUUID().toString();
            textRun.addPicture(imageStream, XWPFDocument.PICTURE_TYPE_PNG, imageId, widthEmu, heightEmu);
        }
    }

    /**
     * 提取占位符键
     */
    public static String extractPlaceholderKey(String text) {
        if (text == null) {
            return null;
        }

        int startIndex = text.indexOf("{");
        int endIndex = text.indexOf("}");
        
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return text.substring(startIndex + 1, endIndex).trim();
        }
        
        return null;
    }

    /**
     * 生成模板测试数据
     */
    public static TemplateData generateTemplateData(String sequence) {
        String batchNumber = BATCH_NO_PREFIX + sequence;
        byte[] qrCodeImage = QRCodeGenerator.generateQRCodeNoMargin(batchNumber);
        byte[] barCodeImage = QRCodeGenerator.generateBarcode(batchNumber);

        TemplateData templateData = new TemplateData();
        templateData.setCode("code-" + sequence);
        templateData.setName("小火车" + sequence);
        templateData.setBachNo(batchNumber);
        templateData.setType("Success" + sequence);
        templateData.setBachNoQcode(qrCodeImage);
        templateData.setBachNoBcode(barCodeImage);
        return templateData;
    }

    /**
     * 转换为模板值映射
     */
    public static Map<String, TemplateValue> convertToTemplateValueMap(TemplateData data) {
        Map<String, TemplateValue> valueMap = new HashMap<>();
        valueMap.put("code", TemplateValue.ofString(data.getCode()));
        valueMap.put("name", TemplateValue.ofString(data.getName()));
        valueMap.put("type", TemplateValue.ofString(data.getType()));
        valueMap.put("bachNo", TemplateValue.ofString(data.getBachNo()));
        valueMap.put("bachNoBcode", TemplateValue.ofImage(data.getBachNoBcode(), TemplateValue.ValueType.BARCODE));
        valueMap.put("bachNoQcode", TemplateValue.ofImage(data.getBachNoQcode(), TemplateValue.ValueType.QRCODE));
        return valueMap;
    }

    /**
     * 保存生成的文档
     */
    @SneakyThrows
    private static void saveGeneratedDocument(XWPFDocument document, String outputPath) {
        File outputFile = new File(outputPath);
        File outputDirectory = outputFile.getParentFile();
        if (outputDirectory != null && !outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            document.write(fileOutputStream);
        }
    }

    /**
     * 读取文件到字节数组
     */
    public static byte[] loadFileToBytes(String filePath) throws IOException {
        File templateFile = new File(filePath);
        if (!templateFile.exists()) {
            throw new FileNotFoundException("模板文件不存在: " + filePath);
        }

        try (FileInputStream fileInputStream = new FileInputStream(templateFile);
             ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream((int) templateFile.length())) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                byteOutputStream.write(buffer, 0, bytesRead);
            }
            return byteOutputStream.toByteArray();
        }
    }
}