package org.clean.test;

import lombok.Data;

@Data
public  class AuthorInfo {
        private String value;
        private String date;
        private String version;
        private boolean fromMethod; // true:来自方法注解, false:来自类注解

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            if (date != null && !date.isEmpty()) {
                sb.append(" (").append(date).append(")");
            }
            if (version != null && !version.isEmpty()) {
                sb.append(" v").append(version);
            }
            return sb.toString();
        }
    }