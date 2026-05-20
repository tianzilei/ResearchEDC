package org.researchedc.domain.enumsupport;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

/**
 * Hibernate UserType for coded enumerations.
 */
public class CodedEnumType implements EnhancedUserType<CodedEnum>, ParameterizedType {

    private Class<CodedEnum> enumClass;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @SuppressWarnings("unchecked")
    @Override
    public void setParameterValues(Properties parameters) {
        String enumClassName = parameters.getProperty("enumClassname");
        try {
            enumClass = ReflectHelper.classForName(enumClassName);
        } catch (ClassNotFoundException cnfe) {
            throw new HibernateException("Enum class not found", cnfe);
        }
    }

    @Override
    public Class<CodedEnum> returnedClass() {
        return enumClass;
    }

    public int[] sqlTypes() {
        return new int[] { Types.INTEGER };
    }

    @Override
    public int getSqlType() {
        return Types.INTEGER;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public CodedEnum deepCopy(CodedEnum value) {
        return value;
    }

    @Override
    public Serializable disassemble(CodedEnum value) {
        return (Serializable) value;
    }

    @Override
    public CodedEnum replace(CodedEnum original, CodedEnum target, Object owner) {
        return original;
    }

    @Override
    public CodedEnum assemble(Serializable cached, Object owner) {
        return (CodedEnum) cached;
    }

    @Override
    public boolean equals(CodedEnum x, CodedEnum y) {
        return x == y;
    }

    @Override
    public int hashCode(CodedEnum x) {
        return x.hashCode();
    }

    public CodedEnum fromXMLString(String xmlValue) {
        return getByCode(xmlValue);
    }

    public CodedEnum fromStringValue(CharSequence charSequence) {
        if (charSequence == null) return null;
        return getByCode(charSequence.toString());
    }

    public String objectToSQLString(CodedEnum value) {
        return '\'' + getCodeAsString(value) + '\'';
    }

    public String toXMLString(CodedEnum value) {
        return getCodeAsString(value);
    }

    @Override
    public String toString(CodedEnum value) {
        return value == null ? null : String.valueOf(getCode(value));
    }

    @Override
    public String toSqlLiteral(CodedEnum value) {
        return toString(value);
    }

    @Override
    public CodedEnum nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        String key = rs.getString(position);
        return rs.wasNull() ? null : getByCode(key);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, CodedEnum value, int index, SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.INTEGER);
        } else {
            Integer code = getCode(value);
            logger.debug("Binding '{}' to parameter: {}", code, index);
            st.setInt(index, code);
        }
    }

    private Integer getCode(Object value) {
        return ((CodedEnum) value).getCode();
    }

    private String getCodeAsString(Object value) {
        return getCode(value).toString();
    }

    private CodedEnum getByCode(String key) {
        Object value = null;
        Method method = null;
        Integer theKey = null;
        try {
            theKey = Integer.valueOf(key);
            method = enumClass.getMethod("getByCode", Integer.class);
            value = method.invoke(null, theKey);
        } catch (NumberFormatException e) {
            throw new CodedEnumPersistenceException("Value passed in to this Method has wrong type " + method + " being passed " + theKey + " on value "
                + value, e);
        } catch (SecurityException e) {
            throw new CodedEnumPersistenceException("SecurityException on Method " + method + " being passed " + theKey + " on value " + value, e);
        } catch (NoSuchMethodException e) {
            throw new CodedEnumPersistenceException("Method not found " + method + " being passed " + theKey + " on value " + value, e);
        } catch (IllegalArgumentException e) {
            throw new CodedEnumPersistenceException("Could not call Method " + method + " being passed " + theKey + " on value " + value, e);
        } catch (IllegalAccessException e) {
            throw new CodedEnumPersistenceException("Don't have access to Method " + method + " being passed " + theKey + " on value " + value, e);
        } catch (InvocationTargetException e) {
            throw new CodedEnumPersistenceException("InvocationTargetException on Method " + method + " being passed " + theKey + " on value " + value, e);
        }
        return (CodedEnum) value;
    }

}
