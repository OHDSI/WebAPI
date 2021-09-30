package org.ohdsi.webapi.common.orm;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;

import java.util.*;
import java.util.stream.Collectors;

public class EnumListTypeDescriptor extends AbstractTypeDescriptor<List> {

    public static final String DELIMITER = ",";
    private Class<Enum> enumClass;
    private Map<String, Enum> enumConstantMap = new HashMap<>();

    protected EnumListTypeDescriptor(Class<Enum> enumClass) {
        super(List.class);
        this.enumClass = enumClass;
        Enum[] enumConst = enumClass.getEnumConstants();
        for(Enum value : enumConst) {
            enumConstantMap.put(value.name(), value);
        }
    }

    @Override
    public List fromString(String s) {

        List result = new ArrayList();
        if (StringUtils.isNotBlank(s)) {
            result = Arrays.stream(StringUtils.split(s, DELIMITER))
                    .map(v -> enumConstantMap.get(v))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public String toString(List value) {

        return (String) value.stream()
                .map(Enum.class::cast)
                .map(v -> ((Enum) v).name())
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public <X> X unwrap(List value, Class<X> aClass, WrapperOptions wrapperOptions) {

        if (Objects.isNull(value)) {
            return null;
        }
        if (List.class.isAssignableFrom(aClass)) {
            return (X)value;
        }
        if (String.class.isAssignableFrom(aClass)) {
            return (X) toString(value);
        }
        throw unknownUnwrap(aClass);
    }

    @Override
    public <X> List wrap(X value, WrapperOptions wrapperOptions) {
        if (Objects.isNull(value)) {
            return null;
        }
        if (value instanceof List) {
            return (List)value;
        }
        if (value instanceof String) {
            return fromString((String) value);
        }
        throw unknownWrap(value.getClass());
    }
}
