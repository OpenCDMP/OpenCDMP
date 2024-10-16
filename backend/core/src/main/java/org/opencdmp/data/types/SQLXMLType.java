package org.opencdmp.data.types;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

/**
 * Store and retrieve a PostgreSQL "xml" column as a Java string.
 * https://wiki.postgresql.org/wiki/Hibernate_XML_Type
 */
public class SQLXMLType implements UserType<String> {

	private final int[] sqlTypesSupported = new int[] { Types.VARCHAR };

	@Override
	public int getSqlType() {
		return Types.VARCHAR;
	}

	@Override
	public Class returnedClass() {
		return String.class;
	}

	@Override
	public boolean equals(String x, String y) throws HibernateException {
		if (x == null) {
			return y == null;
		} else {
			return x.equals(y);
		}
	}

	@Override
	public int hashCode(String x) throws HibernateException {
		return x == null ? null : x.hashCode();
	}

	@Override
	public String nullSafeGet(ResultSet rs, int i, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws SQLException {
		String xmldoc = rs.getString( i );
		return rs.wasNull() ? null : xmldoc;
	}

	@Override
	public void nullSafeSet(PreparedStatement st, String value, int index, SharedSessionContractImplementor sharedSessionContractImplementor) throws SQLException {
		if (value == null) {
			st.setNull(index, Types.OTHER);
		} else {
			st.setObject(index, value, Types.OTHER);
		}
	}

	@Override
	public String deepCopy(String value) throws HibernateException {
		if (value == null)
			return null;
		return new String( (String)value );
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(String value) throws HibernateException {
		return (String) value;
	}

	@Override
	public String assemble(Serializable cached, Object owner) throws HibernateException {
		return (String) cached;
	}

	@Override
	public String replace(String original, String target, Object owner) throws HibernateException {
		return original;
	}
}