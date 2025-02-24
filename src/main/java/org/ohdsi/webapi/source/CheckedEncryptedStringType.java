package org.ohdsi.webapi.source;

import com.odysseusinc.datasourcemanager.encryption.EncryptorUtils;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.jasypt.hibernate4.type.AbstractEncryptedAsStringType;

public class CheckedEncryptedStringType extends AbstractEncryptedAsStringType {

    @Override
    protected Object convertToObject(String value) {

        return value;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {

        checkInitialization();
        final String message = convertToString(value);

        if (Objects.isNull(message)) {
            st.setNull(index, Types.VARCHAR);
            return;
        }

        String encrypted = EncryptorUtils.encrypt(this.encryptor, message);
        st.setString(index, encrypted);
    }

    @Override
    public Class returnedClass() {

        return String.class;
    }

	@Override
	public int getSqlType() {
		return org.hibernate.type.SqlTypes.VARCHAR;
	}

	@Override
	public Object nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
			throws SQLException {

        checkInitialization();
        final String message = rs.getString(position);

        if (Objects.isNull(message)) {
            return null;
        }

        return EncryptorUtils.decrypt(this.encryptor, message);
	}
}
