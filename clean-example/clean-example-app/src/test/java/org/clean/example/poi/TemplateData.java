package org.clean.example.poi;

import lombok.Data;

@Data
public class TemplateData {
    private String code;
    private String name;
    private String type;
    private String bachNo;
    private byte[] bachNoBcode;
    private byte[] bachNoQcode;
}
