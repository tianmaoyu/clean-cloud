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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class HighPerformanceV1 {
    private static final String TEMPLATE_FILE_PATH = "./data/template.docx";
    private static final String OUTPUT_FILE_PATH = "./data/final_output.docx";

    // 必须严谨:一个 table 一个条数据:这里有个问题未解决, 从第二个table 开始 row的行数就是1 ,已经跟模版中的不一样了
    public static void main(String[] args) {
        fillDocumentSingleDoc(1000);
    }


    @SneakyThrows
    private static void fillDocumentSingleDoc(int count) {
        String templatePath = "./data/templete.docx";
        String outPath = "./data/final_out.docx";

        long start = System.currentTimeMillis();


        //数据
        List<Map<String, TemplateValue>> dataList = new ArrayList<>();
        for (Integer i = 0; i < count; i++) {
            TemplateData templateData = generateData(i.toString());
            Map<String, TemplateValue> keyValueMap = convetToTypedMap(templateData);
            dataList.add(keyValueMap);
        }

        long start2 = System.currentTimeMillis();
        // 加载原始文档
        byte[] templateBytes = loadFileToMemory(templatePath);
        XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(templateBytes));
        //扩展文档数量
        XWPFDocument expandfDocument = duplicateDocumentTables(document, count);
        long end2 = System.currentTimeMillis();
        log.info("填充:{} 耗时: {}", count, end2 - start2);

        //填充模版替换占位符
        populateDocument(expandfDocument, dataList);

        // 保存文件
        try (FileOutputStream fileOutputStream = new FileOutputStream(outPath)) {
            expandfDocument.write(fileOutputStream);
        }
        expandfDocument.close();

        long end = System.currentTimeMillis();
        log.info("数量:{} 耗时: {}", count, end - start);
    }



    /**
     * 复制表格- 复制指定数量的表格
     *
     * @param document
     * @param count    复制的数量
     * @return
     */
    @SneakyThrows
    private static XWPFDocument duplicateDocumentTables(XWPFDocument document, int count) {

        // 先填充第一个表格- 必须有并只有一个
        XWPFTable table = document.getTables().get(0);
        //原始
        XmlObject originalTale = table.getCTTbl().copy();

        for (int i = 1; i < count; i++) {
            // 复制表格
            XWPFTable newTable = document.createTable();
            newTable.getCTTbl().set(originalTale);

            // 添加分页符
            document.createParagraph().createRun().addBreak(BreakType.PAGE);
        }

        // 从字节数组重新加载文档-解决内部缓存
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.write(outputStream);
        XWPFDocument freshDocument = new XWPFDocument(new ByteArrayInputStream(outputStream.toByteArray()));

        return freshDocument;
    }



    /**
     * 填充表格中的占位符
     */
    public static void populateDocument(XWPFDocument document, List<Map<String, TemplateValue>> keyValueList) {
        int tableIndex = 0;

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
                                replaceRunPlaceholder(run, keyValue);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 替换运行中的占位符
     */
    @SneakyThrows
    public  static   Boolean replaceRunPlaceholder(XWPFRun run, Map<String, TemplateValue> keyValues) {

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
            String imageName = UUID.randomUUID().toString()+Thread.currentThread().getId();
            run.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_PNG, imageName, Units.toEMU(150), Units.toEMU(20));
            return true;
        }
        // 处理二维码
        if (templateValue.getType() == TemplateValue.ValueType.QRCODE) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream((byte[]) templateValue.getValue());
            String imageName = UUID.randomUUID().toString()+Thread.currentThread().getId();
            run.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_PNG, imageName, Units.toEMU(75), Units.toEMU(75));
            return true;
        }
        return false;
    }

    /*
     * 提取占位符
     */
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

    public static TemplateData generateData(String code) {

        String bachNo = "XC2025001-" + code;
        byte[] qcodeBytes = QRCodeGenerator.generateQRCodeNoMargin(bachNo);
        byte[] barCodeBtyes = QRCodeGenerator.generateBarcode(bachNo);

        TemplateData templateData = new TemplateData();
        templateData.setCode("code-" + code);
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
