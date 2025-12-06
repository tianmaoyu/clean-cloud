package org.clean.example.poi;

import lombok.Data;
//       // 保存最终文档
//       try (FileOutputStream fileOutputStream = new FileOutputStream(outPath)) {
//           outDoc.write(fileOutputStream);
//       }

@Data
public class TemplateValue {
    private Object value;
    private ValueType type;
    
    public enum ValueType {
        STRING,
        BARCODE,
        QRCODE
    }
    
    public static TemplateValue ofString(String value) {
        TemplateValue tv = new TemplateValue();
        tv.value = value;
        tv.type = ValueType.STRING;
        return tv;
    }
    
    public static TemplateValue ofImage(byte[] value, ValueType type) {
        TemplateValue tv = new TemplateValue();
        tv.value = value;
        tv.type = type;
        return tv;
    }

}
