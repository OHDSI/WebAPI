package org.ohdsi.webapi.source;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.jasypt.hibernate4.type.AbstractEncryptedAsStringType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

public class CheckedEncryptedStringType extends AbstractEncryptedAsStringType {

    public static final String ENCODED_PREFIX = "ENC(";
    public static final String ENCODED_SUFFIX = ")";

    @Override
    protected Object convertToObject(String value) {

        return value;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {

        checkInitialization();
        if (Objects.nonNull(value)){
            if (Objects.nonNull(this.encryptor) && !(this.encryptor instanceof NotEncrypted)) {
                String encrypted = this.encryptor.encrypt(convertToString(value));
                st.setString(index, ENCODED_PREFIX + encrypted + ENCODED_SUFFIX);
            } else {
                st.setString(index, convertToString(value));
            }
        } else {
            st.setNull(index, Types.VARCHAR);
        }
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {

        checkInitialization();
        final String message = rs.getString(names[0]);
        Object result;
        if (Objects.isNull(message)) {
            return null;
        }
        if (Objects.nonNull(this.encryptor) && !(this.encryptor instanceof NotEncrypted) && message.startsWith(ENCODED_PREFIX)) {
            String value = message.substring(ENCODED_PREFIX.length(), message.length() - ENCODED_SUFFIX.length());
            result = convertToObject(encryptor.decrypt(value));
        } else {
            result = message;
        }
        return result;
    }

    @Override
    public Class returnedClass() {

        return String.class;
    }
}
