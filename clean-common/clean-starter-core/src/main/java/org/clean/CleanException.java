package org.clean;

import lombok.Data;
import lombok.EqualsAndHashCode;

@SuppressWarnings("unused")
@Data
@EqualsAndHashCode(callSuper = true)
public class CleanException extends RuntimeException  {

    private Integer code;

    private String message;

    public CleanException(CleanCode codeEnum) {
        super(codeEnum.getDesc());
        this.code = codeEnum.getCode();
        this.message = codeEnum.getDesc();
    }

    public CleanException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

}
