package org.clean.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.util.concurrent.TimeUnit;

public interface DocumentRedisService {

    boolean saveDocument(String documentId, XWPFDocument document);
    XWPFDocument getDocument(String documentId);
}
