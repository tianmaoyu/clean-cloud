package org.clean.mybatis;
import java.time.OffsetDateTime;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MybatisPlusAutoFillHandler implements MetaObjectHandler {

    // 插入时触发填充
    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject.hasGetter("createId") &&  metaObject.getValue("createId") == null)
            this.strictInsertFill(metaObject, "createId", Long.class, 10L);
        this.handleCreateTimeField(metaObject);

        if (metaObject.hasGetter("updateId") &&  metaObject.getValue("updateId") == null)
            this.strictInsertFill(metaObject, "updateId", Long.class, 10L);
        this.handleUpdateTimeField(metaObject);
    }

    // 更新时触发填充
    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasGetter("updateId") && metaObject.getValue("updateId") == null)
            this.strictUpdateFill(metaObject, "updateId", Long.class, 10L);
        this.handleUpdateTimeField(metaObject);
    }

    private void handleUpdateTimeField(MetaObject metaObject) {
        if (metaObject.hasGetter("updateTime")) {
            // 根据字段类型进行相应的填充
            Class<?> fieldType = metaObject.getGetterType("updateTime");
            if (OffsetDateTime.class.isAssignableFrom(fieldType)) {
                this.strictUpdateFill(metaObject, "updateTime", OffsetDateTime.class, OffsetDateTime.now());
            } else if (Date.class.isAssignableFrom(fieldType)) {
                this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
            }
        }
    }

    private void handleCreateTimeField(MetaObject metaObject) {
        if (metaObject.hasGetter("createTime")) {
            Object createTime = metaObject.getValue("createTime");
            if (createTime == null) {
                // 根据字段类型进行相应的填充
                Class<?> fieldType = metaObject.getGetterType("createTime");
                if (OffsetDateTime.class.isAssignableFrom(fieldType)) {
                    this.strictInsertFill(metaObject, "createTime", OffsetDateTime.class, OffsetDateTime.now());
                } else if (Date.class.isAssignableFrom(fieldType)) {
                    this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
                }
            }
        }
    }

}