package org.clean.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.clean.tenum.IEnum;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@MappedTypes(IEnum.class)
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.INTEGER})
public class EnumTypeHandler<E extends Enum<E> & IEnum<T>, T>
    extends BaseTypeHandler<E> {

    private Class<E> type;
    private Map<T, E> codeMap = new ConcurrentHashMap<>();

    public EnumTypeHandler(Class<E> type) {
        if (type == null) throw new IllegalArgumentException("Type argument cannot be null");
        this.type = type;
        E[] enums = type.getEnumConstants();
        for (E e : enums) {
            codeMap.put(e.getCode(), e);
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType)
        throws SQLException {
        if (jdbcType == null) {
            ps.setObject(i, parameter.getCode());
        } else {
            ps.setObject(i, parameter.getCode(), jdbcType.TYPE_CODE);
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        T code = (T) rs.getObject(columnName);
        return code != null ? codeMap.get(code) : null;
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        T code = (T) rs.getObject(columnIndex);
        return code == null ? null : codeMap.get(code);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        T code = (T) cs.getObject(columnIndex);
        return code == null ? null : codeMap.get(code);
    }


    // 其他 getNullableResult 方法类似实现...
}