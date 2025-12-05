package org.clean.coverage;

import java.util.HashMap;
import java.util.Map;

public  class AnnotationInfo {
    public Map<String, Object> annotations = new HashMap<>();
    
    public void addAnnotation(String name, Object value) {
        annotations.put(name, value);
    }
    
    public boolean hasAnnotation(String name) {
        return annotations.containsKey(name);
    }
    
    public Object getAnnotation(String name) {
        return annotations.get(name);
    }
}