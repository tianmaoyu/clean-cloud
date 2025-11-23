package org.clean.example.enums;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clean.tenum.IEnum;

import java.util.Arrays;


@Getter
@AllArgsConstructor
public enum CodeRuleType implements IEnum<String> {

    ORDER("ORDER", "订单"),
    USER("USER", "用户"),
    PAYMENT("PAYMENT", "支付流水"),
    PRODUCT("PRODUCT", "商品SKU"),
    INVOICE("INVOICE", "发票编号"),
    REFUND("REFUND", "退款单号"),
    COUPON("COUPON", "优惠券编码"),
    SHIPPING("SHIPPING", "物流单号"),
    WAREHOUSE("WAREHOUSE", "仓库管理"),
    AUDIT("AUDIT", "审计日志"),
    Other("Other", "审计日志"),
    Other1("Other1", "审计日志"),
    ;

    private final String code;
    private final String desc;

    public static final CodeRuleType fromCode(String code){
        return Arrays.stream(CodeRuleType.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
