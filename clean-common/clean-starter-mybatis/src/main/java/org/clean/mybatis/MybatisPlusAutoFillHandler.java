package org.clean.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MybatisPlusAutoFillHandler implements MetaObjectHandler {

    // 插入时触发填充
    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject.getValue("createId") == null)
            this.strictInsertFill(metaObject, "createId", Long.class, 10L);
        if (metaObject.getValue("createTime") == null)
            this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        if (metaObject.getValue("updateId") == null)
            this.strictInsertFill(metaObject, "updateId", Long.class, 10L);
        if (metaObject.getValue("updateTime") == null)
            this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
    }

    // 更新时触发填充
    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.getValue("updateId") == null)
            this.strictUpdateFill(metaObject, "updateId", Long.class, 10L);
        if (metaObject.getValue("updateTime") == null)
            this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
    }
}