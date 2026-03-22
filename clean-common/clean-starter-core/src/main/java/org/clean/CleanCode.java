package org.clean;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clean.tenum.IEnum;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CleanCode implements IEnum<Integer> {

    RC200(200, "成功"),
    
    RC500(500, "网络异常");

    private final Integer code;
    private final String desc;

    @JsonCreator
    public static final CleanCode fromCode(Integer code){
        return Arrays.stream(CleanCode.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}