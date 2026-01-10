package org.clean.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clean.tenum.IEnum;

@Getter
@AllArgsConstructor
public enum MessageStatus  implements IEnum<String> {
    PENDING("PENDING","待发送"),
    SENT("SENT","已经发送"),
    FAILED("SENT","发送失败");

    private final String code;
    private final String desc;
}