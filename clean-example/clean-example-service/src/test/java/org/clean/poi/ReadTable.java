package org.clean.poi;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlObject;
import org.clean.xing.QRCodeGenerator;
import java.io.*;

import java.util.*;

@Slf4j
public class ReadTable {

    @SneakyThrows
    public static void main(String[] args) {
        ZipSecureFile.setMinInflateRatio(0.001);
//        fillDocumentSingleDoc()
//        fillDocument();
//        copyDocument();
//        fillDocument_firstFill(100);
//        fillDocument_firstAppend(10000);
        fillDocumentSingleDoc(1000);
//        copyDocumentAndSave();
    }


    //复制 count 一个 文档 到 目标文档

    @SneakyThrows
    private static void copyDocument() {

        long start = System.currentTimeMillis();
        String sourcePath = "./data/out1.docx";
        String outPath = "./data/append.docx";

        //先复制 文档
        copyFile(sourcePath, outPath);
        // 如果直接创建,可能样式丢失 todo
        XWPFDocument outDoc = new XWPFDocument(new FileInputStream(outPath));
        XWPFDocument sourceDoc = new XWPFDocument(new FileInputStream(sourcePath));

        for (int i = 0; i < 10000; i++) {
            appendDocument(outDoc, sourceDoc);
        }

        outDoc.write(new FileOutputStream(outPath));

        long end = System.currentTimeMillis();
        log.info("耗时: {}", end - start);
    }

    /**
     * 复制文件
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @throws IOException IO异常
     */
    public static void copyFile(String sourcePath, String targetPath) throws IOException {
        File sourceFile = new File(sourcePath);
        File targetFile = new File(targetPath);

        // 检查源文件是否存在
        if (!sourceFile.exists()) {
            throw new IOException("源文件不存在: " + sourcePath);
        }

        // 创建目标文件的父目录（如果不存在）
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // 执行文件复制
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }

    @SneakyThrows
    private static void fillDocument() {

        String templatePath = "./data/bigOut_firstAppend.docx";
//        String templatePath = "./data/templete.docx";
        String outPath = "./data/fill.docx";

        //读取模版
        XWPFDocument fillDocument = new XWPFDocument(new FileInputStream(templatePath));

        TemplateData templateData = generateData("0");
        Map<String, TemplateValue> keyValue = convetToTypedMap(templateData);
        replaceTable(fillDocument, keyValue);

        try (FileOutputStream fileOutputStream = new FileOutputStream(outPath)) {
            fillDocument.write(fileOutputStream);
        }
    }

    /**
     * 填充模版
     */
    @SneakyThrows
    private static void fillDocument_firstAppend(int  count) {
        String templatePath = "./data/templete.docx";
        String outPath = "./data/bigOut_firstAppend.docx";

        long start = System.currentTimeMillis();

        //复制
        List<XWPFDocument> templateDocuments = copyDocument(templatePath, count);


        //数据
        List<Map<String, TemplateValue>> datalist = new ArrayList<>();
        for (Integer i = 0; i < templateDocuments.size(); i++) {
            TemplateData templateData = generateData(i.toString());
            Map<String, TemplateValue> keyValueMap = convetToTypedMap(templateData);
            datalist.add(keyValueMap);
        }

        //合并
        XWPFDocument xwpfDocument = templateDocuments.get(0);
        for(int i=1;i<templateDocuments.size();i++){
            appendDocument(xwpfDocument, templateDocuments.get(i));
            templateDocuments.get(i).close();
        }

        // 强制刷新
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        xwpfDocument.write(baos);
        //从字节数组重新加载文档
        XWPFDocument freshDocument = new XWPFDocument(new ByteArrayInputStream(baos.toByteArray()));

        replaceTable(freshDocument, datalist);

        //保存
        try (FileOutputStream fileOutputStream = new FileOutputStream(outPath)) {
            freshDocument.write(fileOutputStream);
        }
        long end = System.currentTimeMillis();

        log.info("数量:{} 耗时: {}",count, end - start);

    }

    /**
     * 填充模版
     */
    @SneakyThrows
    private static void fillDocument_firstFill(int  count) {
        String templatePath = "./data/templete.docx";
        String outPath = "./data/outFirstFill.docx";

        long start = System.currentTimeMillis();

        //复制
        List<XWPFDocument> templateDocuments = copyDocument(templatePath, count);

        //数据
        List<Map<String, TemplateValue>> datalist = new ArrayList<>();
        for (Integer i = 0; i < templateDocuments.size(); i++) {
            TemplateData templateData = generateData(i.toString());
            Map<String, TemplateValue> keyValueMap = convetToTypedMap(templateData);
            datalist.add(keyValueMap);
        }

        //填充
        for(int i=0;i<templateDocuments.size();i++){
            replaceTable(templateDocuments.get(i), datalist.get(i));
        }
        //合并
        XWPFDocument xwpfDocument = templateDocuments.get(0);
        for(int i=1;i<templateDocuments.size();i++){
            appendDocument(xwpfDocument, templateDocuments.get(i));
//            templateDocuments.get(i).close();
        }

        //保存
        try (FileOutputStream fileOutputStream = new FileOutputStream(outPath)) {
            xwpfDocument.write(fileOutputStream);
        }
        long end = System.currentTimeMillis();

        log.info("数量:{} 耗时: {}",count, end - start);

    }

    private static void copyDocumentAndSave() throws IOException {
        String templatePath = "./data/templete.docx";
        String outPathPrefix = "./data/";

        List<XWPFDocument> xwpfDocuments = copyDocument(templatePath, 5);

        for (int i = 0; i < xwpfDocuments.size(); i++) {
            String outPath = outPathPrefix + i + "copy.docx";
            try (FileOutputStream fileOutputStream = new FileOutputStream(outPath)) {
                xwpfDocuments.get(i).write(fileOutputStream);
            }
        }

    }

    private static List<XWPFDocument> copyDocument(String templatePath, int count) throws IOException {

        List<XWPFDocument> documentList = new ArrayList<>();
        //加载模版到内存
        byte[] templateBytes = loadFileToMemory(templatePath);

        for (int i = 0; i < count; i++) {
            byte[] copyBytes = Arrays.copyOf(templateBytes, templateBytes.length);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(copyBytes);
            XWPFDocument document = new XWPFDocument(inputStream);
            documentList.add(document);
        }

        return documentList;
    }

    /**
     * 追加文档
     */
    @SneakyThrows
    private static void appendDocument(XWPFDocument toDocument, XWPFDocument fromDocument) {

        // 复制表格
        for (XWPFTable table : fromDocument.getTables()) {
            XWPFTable newTable = toDocument.createTable();
            newTable.getCTTbl().set(table.getCTTbl().copy());
        }

    }



    @SneakyThrows
    private static void fillDocumentSingleDoc(int count) {
        String templatePath = "./data/templete.docx";
        String outPath = "./data/singleDoc_out.docx";

        long start = System.currentTimeMillis();

        // 加载模板
        byte[] templateBytes = loadFileToMemory(templatePath);
        XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(templateBytes));

        // 获取模板中的第一个表格（假设只有一个表格）
        // 生成数据
        List<Map<String, TemplateValue>> dataList = new ArrayList<>();
        for (Integer i = 0; i < count; i++) {
            TemplateData templateData = generateData(i.toString());
            Map<String, TemplateValue> keyValueMap = convetToTypedMap(templateData);
            dataList.add(keyValueMap);
        }

        // 先填充第一个表格（模板中的表格）
        XWPFTable table = document.getTables().get(0);
        // 保存原始表格结构 原始
        XmlObject originalTale = table.getCTTbl().copy();
        // 复制并添加后续表格
        for (int i = 1; i < dataList.size(); i++) {
            // 复制表格
            XWPFTable newTable = document.createTable();
            newTable.getCTTbl().set(originalTale);
            // 添加分页符
            document.createParagraph().createRun().addBreak(BreakType.PAGE);
        }


        // 将当前文档写入字节数组, 从字节数组重新加载文档-解决内部缓存
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.write(baos);
        XWPFDocument freshDocument = new XWPFDocument(new ByteArrayInputStream(baos.toByteArray()));

        replaceTable(freshDocument, dataList);

        // 保存
        try (FileOutputStream fileOutputStream = new FileOutputStream(outPath)) {
            freshDocument.write(fileOutputStream);
        }

        freshDocument.close();
        long end = System.currentTimeMillis();
        log.info("数量:{} 耗时: {}", count, end - start);
    }

    /**
     * 替换单个表格的内容
     */
    public static void replaceSingleTable(XWPFTable table, Map<String, TemplateValue> keyValues) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    String text = paragraph.getText();
                    if (StringUtils.isNotBlank(text) && text.contains("{") && text.contains("}")) {
                        for (XWPFRun run : paragraph.getRuns()) {
                            replaceRunText(run, keyValues);
                        }
                    }
                }
            }
        }
    }

    public static void replaceTable(XWPFDocument document, Map<String, TemplateValue> keyValues) {
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        // 跳过部份
                        String text = paragraph.getText();
                        if (StringUtils.isNotBlank(text) && text.contains("{") && text.contains("}")) {
                            for (XWPFRun run : paragraph.getRuns()) {
                                replaceRunText(run, keyValues);
                            }
                        }
                    }
                }
            }
            return;
        }
    }

    /**
     * 替换表格内容 - 一条数据填充 一个表格
     */
    public static void replaceTable(XWPFDocument document, List<Map<String, TemplateValue>> keyValueList) {
        int tableIndex = 0;
        // 必须严谨:一个 table 一个条数据:这里有个问题未解决, 从第二个table 开始 row的行数就是1 ,已经跟模版中的不一样了
        for (XWPFTable table : document.getTables()) {
            Map<String, TemplateValue> keyValue = keyValueList.get(tableIndex);
            tableIndex++;
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        // 跳过部份
                        String text = paragraph.getText();
                        if (StringUtils.isNotBlank(text) && text.contains("{") && text.contains("}")) {
                            for (XWPFRun run : paragraph.getRuns()) {
                                replaceRunText(run, keyValue);
                            }
                        }
                    }
                }
            }
        }
    }

    @SneakyThrows
    public static Boolean replaceRunText(XWPFRun run, Map<String, TemplateValue> keyValues) {

        String text = run.getText(0);
        String placeholder = extractPlaceholder(text);
        log.debug("text: {},placeholder:{}", text, placeholder);
        if (placeholder == null) return false;

        TemplateValue templateValue = keyValues.get(placeholder);
        if (templateValue == null) {
            log.info("placeholder:{} not found", placeholder);
            return false;
        }
        // 清空文本
        run.setText("", 0);

        // 处理字符串
        if (templateValue.getType() == TemplateValue.ValueType.STRING) {
            run.setText(templateValue.getValue().toString());
            return true;
        }
        // 处理条形码
        if (templateValue.getType() == TemplateValue.ValueType.BARCODE) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream((byte[]) templateValue.getValue());
            String imageName = UUID.randomUUID().toString();
            run.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_PNG, imageName, Units.toEMU(150), Units.toEMU(20));
            return true;
        }
        // 处理二维码
        if (templateValue.getType() == TemplateValue.ValueType.QRCODE) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream((byte[]) templateValue.getValue());
            String imageName = UUID.randomUUID().toString();
            run.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_PNG, imageName, Units.toEMU(75), Units.toEMU(75));
            return true;
        }
        return false;
    }


    public static TemplateData generateData(String code) {

        String bachNo = "XC2025001-" + code;
        byte[] qcodeBytes = QRCodeGenerator.generateQRCodeNoMargin(bachNo);
        byte[] barCodeBtyes = QRCodeGenerator.generateBarcode(bachNo);

        TemplateData templateData = new TemplateData();
        templateData.setCode("code-"+code);
        templateData.setName("小火车" + code);
        templateData.setBachNo(bachNo);
        templateData.setType("Success" + code);
        templateData.setBachNoQcode(qcodeBytes);
        templateData.setBachNoBcode(barCodeBtyes);
        return templateData;
    }


    // 在 TemplateData 类中添加方法
    public static Map<String, TemplateValue> convetToTypedMap(TemplateData data) {
        Map<String, TemplateValue> map = new HashMap<>();
        map.put("code", TemplateValue.ofString(data.getCode()));
        map.put("name", TemplateValue.ofString(data.getName()));
        map.put("type", TemplateValue.ofString(data.getType()));
        map.put("bachNo", TemplateValue.ofString(data.getBachNo()));
        map.put("bachNoBcode", TemplateValue.ofImage(data.getBachNoBcode(), TemplateValue.ValueType.BARCODE));
        map.put("bachNoQcode", TemplateValue.ofImage(data.getBachNoQcode(), TemplateValue.ValueType.QRCODE));
        return map;
    }

    public static String extractPlaceholder(String text) {
        if (text != null && text.contains("{") && text.contains("}")) {
            log.info("text: {}", text);
            int start = text.indexOf("{") + 1;
            int end = text.indexOf("}");
            if (start < end) {
                return text.substring(start, end).trim();
            }
        }
        return null;
    }

    /**
     * 读取文件到内存
     *
     * @param filePath 文件路径
     * @return 文件内容字节数组
     */
    public static byte[] loadFileToMemory(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toByteArray();
        }
    }


}

