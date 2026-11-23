package org.ohdsi.atlasgis.utils;

import com.odysseusinc.arachne.commons.types.DBMSType;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.dialect.SQLServerDialect;

public class DbmsUtils {
	private DbmsUtils() {}

	public static DBMSType getCurrentDBMSType(Dialect dialect) {

		DBMSType type;
		if (dialect instanceof PostgreSQL9Dialect) {
			type = DBMSType.POSTGRESQL;
		} else if (dialect instanceof SQLServerDialect) {
			type = DBMSType.MS_SQL_SERVER;
		} else if (dialect instanceof Oracle8iDialect) {
			type = DBMSType.ORACLE;
		} else {
			throw new IllegalArgumentException("Only PostgreSQL, SQL Server or Oracle is supported");
		}
		return type;
	}
}
