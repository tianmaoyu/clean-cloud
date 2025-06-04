package org.clean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clean.tenum.IEnum;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum TokenType implements IEnum<String> {
    Basic("Basic","基本认证（用户名:密码 base64编码）"),
    Bearer("Bearer","专门为令牌类认证设计"),
    Digest("Digest","摘要认证");

    private final String code;
    private final String desc;
    public static final TokenType fromCode(Integer code){
        return Arrays.stream(TokenType.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

}
