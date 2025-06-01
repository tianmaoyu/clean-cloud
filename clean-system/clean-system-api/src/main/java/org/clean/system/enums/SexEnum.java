package org.clean.system.enums;
import org.clean.common.tenum.IEnum;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@ApiModel
@Getter
@AllArgsConstructor
public enum SexEnum implements IEnum<Integer> {
    MALE(1, "男"),
    FEMALE(2, "女");


    private final Integer code;
    private final String desc;

    public static final SexEnum fromCode(Integer code){
        return Arrays.stream(SexEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
