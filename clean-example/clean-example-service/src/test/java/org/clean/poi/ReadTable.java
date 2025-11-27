package org.clean.poi;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.clean.xing.QRCodeGenerator;

import java.io.FileOutputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class ReadTable {

    @SneakyThrows
    public static void main(String[] args) {

        String docx = "./data/templete.docx";
        String out = "./data/out1.docx";

        File file = new File(docx);
        if (!file.exists()) {
            log.info("file not exists");
            return;
        }
        FileInputStream fileInputStream = new FileInputStream(file);

        XWPFDocument document = new XWPFDocument(fileInputStream);

        TemplateData data = getData();
        Map<String, TemplateValue> stringObjectMap = toTypedMap(data);

  //在Apache POI中，每个XWPFRun都包含特定的格式信息（字体、大小、颜色等）。当您清空内容时，这些格式信息也会被影响
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        for (XWPFRun run : paragraph.getRuns()) {
                            String text = run.getText(0);
                            log.info("text: {}", text);
                            if (text != null && text.contains("{4444}")) {
                                // 替换占位符
//                                text = text.replace("{4444}", "replacement");
                                // 重新设置文本内容
//                                run.setText(text, 0);
                                run.setText("", 0);
                                String bachNo = "XC202500001";
                                byte[] qcodeBytes = QRCodeGenerator.generateBarcode(bachNo);
                                run.addPicture(
                                    new ByteArrayInputStream(qcodeBytes),
                                    XWPFDocument.PICTURE_TYPE_PNG,
                                    UUID.randomUUID().toString(),
                                     Units.toEMU(150), Units.toEMU(20) );

                            }
                        }
                    }
                }
            }
        }
//
//
//        document.getTables().forEach(table -> {
//            table.getRows().forEach(row -> {
//                row.getTableCells().forEach(cell -> {
//
//                    // 提取占位符
//                    String placeholder = extractPlaceholder(cell.getText());
//                    if (StringUtils.isEmpty(placeholder)) return;
//                    TemplateValue templateValue = stringObjectMap.get(placeholder);
//                    if (templateValue == null) return;
//                    if (templateValue.getType() == TemplateValue.ValueType.STRING) {
//
//                        cell.setText(templateValue.getValue().toString());
//                    }
//                    if (templateValue.getType() == TemplateValue.ValueType.IMAGE) {
//
//                        byte[] bytes = (byte[]) templateValue.getValue();
//                        try {
//                            cell.addParagraph().createRun().addPicture(
//                                    new ByteArrayInputStream(bytes),
//                                    XWPFDocument.PICTURE_TYPE_PNG,
//                                    UUID.randomUUID().toString(),
//                                    100, 100
//                            );
//                        } catch (InvalidFormatException e) {
//                            throw new RuntimeException(e);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                });
//            });
//        });
//
//
        // 保存修改后的文档
        try (FileOutputStream outFile = new FileOutputStream(out)) {
            document.write(outFile);
            log.info("文档已成功保存到: {}", out);
        } catch (IOException e) {
            log.error("保存文档时发生错误", e);
            throw new RuntimeException("保存文档失败", e);
        } finally {
            // 清理资源
            try {
                document.close();
                fileInputStream.close();
            } catch (IOException e) {
                log.error("关闭文档资源时发生错误", e);
            }
        }


    }

    public static TemplateData getData() {

        String bachNo = "XC202500001";
        byte[] qcodeBytes = QRCodeGenerator.generateQRCodeNoMargin(bachNo);
        byte[] barCodeBtyes = QRCodeGenerator.generateBarcode(bachNo);

        TemplateData templateData = new TemplateData();
        templateData.setCode("XC202500001");
        templateData.setName("小火车");
        templateData.setBachNo("B1234567890");
        templateData.setType("Success");
        templateData.setBachNoQcode(qcodeBytes);
        templateData.setBachNoBcode(barCodeBtyes);
        return templateData;
    }


    // 在 TemplateData 类中添加方法
    public static Map<String, TemplateValue> toTypedMap(TemplateData data) {
        Map<String, TemplateValue> map = new HashMap<>();
        map.put("code", TemplateValue.ofString(data.getCode()));
        map.put("name", TemplateValue.ofString(data.getName()));
        map.put("type", TemplateValue.ofString(data.getType()));
        map.put("bachNo", TemplateValue.ofString(data.getBachNo()));
        map.put("bachNoBcode", TemplateValue.ofImage(data.getBachNoBcode()));
        map.put("bachNoQcode", TemplateValue.ofImage(data.getBachNoQcode()));
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

}
