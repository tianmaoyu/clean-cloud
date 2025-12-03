package org.clean.easyexcel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clean.tenum.IEnum;

@Getter
@AllArgsConstructor
public enum UserRole implements IEnum<Integer> {
    ADMIN(1, "管理员"),
    MANAGER(2, "经理"),
    STAFF(3, "员工"),
    GUEST(4, "访客"),
    AUDITOR(5, "审计员");

    private final Integer code;
    private final String desc;
}