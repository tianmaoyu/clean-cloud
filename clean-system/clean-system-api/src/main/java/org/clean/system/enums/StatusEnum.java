package org.clean.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clean.common.tenum.IEnum;

@Getter
@AllArgsConstructor
public enum StatusEnum implements IEnum<Integer>  {
    SUCCESS(0, "操作成功"),
    ERROR(1, "系统异常");

    private  Integer code;
    private String desc;
}