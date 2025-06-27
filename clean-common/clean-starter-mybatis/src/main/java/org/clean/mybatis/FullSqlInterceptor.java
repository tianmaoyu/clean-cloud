package org.clean.mybatis;


import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.SqlFormatter;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.sql.Statement;
import java.util.*;

@Intercepts({@Signature(
        type = StatementHandler.class,
        method = "query",
        args = {Statement.class, ResultHandler.class}
), @Signature(
        type = StatementHandler.class,
        method = "update",
        args = {Statement.class}
), @Signature(
        type = StatementHandler.class,
        method = "batch",
        args = {Statement.class}
)})
@Slf4j
@Component
public class FullSqlInterceptor implements Interceptor {

    private static final Logger slowSqlLog = LoggerFactory.getLogger("MyBatisInterceptor.SlowSqlLog");
    private long maxTime = 0L;
    private boolean format = false;
    private boolean writeInLog = true;


    public FullSqlInterceptor() {
    }


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object firstArg = invocation.getArgs()[0];
        Statement statement;
        if (Proxy.isProxyClass(firstArg.getClass())) {
            statement = (Statement) SystemMetaObject.forObject(firstArg).getValue("h.statement");
        } else {
            statement = (Statement) firstArg;
        }

        MetaObject stmtMetaObj = SystemMetaObject.forObject(statement);

        try {
            statement = (Statement) stmtMetaObj.getValue("stmt.statement");
        } catch (Exception var17) {
        }


        if (stmtMetaObj.hasGetter("delegate")) {
            try {
                statement = (Statement) stmtMetaObj.getValue("delegate");
            } catch (Exception var16) {
            }
        }

        String originalSql = statement.toString();
        originalSql = originalSql.replaceAll("[\\s]+", " ");
        int index = this.indexOfSqlStart(originalSql);
        if (index > 0) {
            originalSql = originalSql.substring(index);
        }

        long start = System.currentTimeMillis();
        Object result;
        try {
            result = invocation.proceed();
        } catch (Exception e) {
            long timing = System.currentTimeMillis() - start;
//   超过一分钟
            if (timing > 60000) {
                log.error("执行sql超时:耗时=" + timing + "sql" + originalSql, e);
            } else {
                log.error("执行sql异常:耗时=" + timing + "sql=" + originalSql, e);
            }
            throw e;
        }

        long timing = System.currentTimeMillis() - start;
        Object target = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(target);
        MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        StringBuilder formatSql = (new StringBuilder()).append(ms.getId()).append("\n").append("Execute SQL：").append("\n").append(sqlFormat(originalSql, this.format)).append("\n");

        if (timing > 300) {
            slowSqlLog.warn("[SLOW SQL] " + timing + "ms " + formatSql);
        } else {
            log.info("[SQL] " + timing + "ms " + formatSql);
        }
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return target instanceof StatementHandler ? Plugin.wrap(target, this) : target;
    }

    @Override
    public void setProperties(Properties prop) {
        String maxTime = prop.getProperty("maxTime");
        String format = prop.getProperty("format");
        if (StrUtil.isNotBlank(maxTime)) {
            this.maxTime = Long.parseLong(maxTime);
        }

        if (StrUtil.isNotBlank(format)) {
            this.format = Boolean.parseBoolean(format);
        }

    }

    private int indexOfSqlStart(String sql) {
        String upperCaseSql = sql.toUpperCase();
        Set<Integer> set = new HashSet();
        set.add(upperCaseSql.indexOf("SELECT "));
        set.add(upperCaseSql.indexOf("UPDATE "));
        set.add(upperCaseSql.indexOf("INSERT "));
        set.add(upperCaseSql.indexOf("DELETE "));
        set.remove(-1);
        if (CollectionUtils.isEmpty(set)) {
            return -1;
        } else {
            List<Integer> list = new ArrayList(set);
            list.sort(Comparator.naturalOrder());
            return (Integer) list.get(0);
        }
    }

    private static String sqlFormat(String boundSql, boolean format) {
        if (format) {
            try {
                return SqlFormatter.format(boundSql);
            } catch (Exception var3) {
            }
        }

        return boundSql;
    }

    public FullSqlInterceptor setMaxTime(long maxTime) {
        this.maxTime = maxTime;
        return this;
    }

    public long getMaxTime() {
        return this.maxTime;
    }

    public FullSqlInterceptor setFormat(boolean format) {
        this.format = format;
        return this;
    }

    public boolean isFormat() {
        return this.format;
    }

    public FullSqlInterceptor setWriteInLog(boolean writeInLog) {
        this.writeInLog = writeInLog;
        return this;
    }

    public boolean isWriteInLog() {
        return this.writeInLog;
    }
}