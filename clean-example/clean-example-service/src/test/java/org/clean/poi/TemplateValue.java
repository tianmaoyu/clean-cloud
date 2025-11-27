package org.clean.poi;

import lombok.Data;

@Data
public class TemplateValue {
    private Object value;
    private ValueType type;
    
    public enum ValueType {
        STRING, IMAGE
    }
    
    public static TemplateValue ofString(String value) {
        TemplateValue tv = new TemplateValue();
        tv.value = value;
        tv.type = ValueType.STRING;
        return tv;
    }
    
    public static TemplateValue ofImage(byte[] value) {
        TemplateValue tv = new TemplateValue();
        tv.value = value;
        tv.type = ValueType.IMAGE;
        return tv;
    }

}
