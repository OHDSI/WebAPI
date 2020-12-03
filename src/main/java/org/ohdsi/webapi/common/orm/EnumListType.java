package org.ohdsi.webapi.common.orm;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;
import org.hibernate.usertype.DynamicParameterizedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

public class EnumListType extends AbstractSingleColumnStandardBasicType<List> implements DynamicParameterizedType {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnumListType.class);

    public static final String TYPE_NAME = "enum-list";

    public EnumListType() {
        super(VarcharTypeDescriptor.INSTANCE, null);
    }


    @Override
    public String getName() {
        return TYPE_NAME;
    }

    @Override
    public String[] getRegistrationKeys() {
        return new String[]{ getName(), "List", List.class.getName() };
    }

    @Override
    public void setParameterValues(Properties properties) {

        String enumClassName = properties.getProperty("enumClass");
        try {
            setJavaTypeDescriptor(new EnumListTypeDescriptor((Class<Enum>) Class.forName(enumClassName)));
        } catch (ClassNotFoundException e) {
            LOGGER.error("Failed to initialize enum list type", e);
        }
    }
}
