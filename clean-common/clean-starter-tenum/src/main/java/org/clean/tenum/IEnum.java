package org.clean.tenum;

import com.fasterxml.jackson.annotation.JsonValue;


public interface IEnum<T>  {
    @JsonValue
    T getCode();

    String getDesc();
}
