package org.ohdsi.webapi.shiro.mapper;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.shiro.Entities.UserPrincipal;
import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

public abstract class UserMapper implements AttributesMapper<UserPrincipal> {
    private String getAttrCoalesce(Attributes attrList, String key) throws NamingException {
        String result = null;
        Attribute attribute = attrList.get(key);
        if (attribute != null) {
            Object value = attribute.get();
            if(value instanceof String) {
                result = (String) value;
            }
        }
        return result;
    }

    public UserPrincipal mapFromAttributes(Attributes attrs) throws NamingException {
        UserPrincipal user = new UserPrincipal();

        user.setUsername(getAttrCoalesce(attrs, getUsernameAttr()));

        StringBuilder name = new StringBuilder();
        // If display name attribute value is set then use only it, otherwise use attributes for full name
        if (StringUtils.isNotEmpty(getDisplaynameAttr())) {
            processAttribute(attrs, getDisplaynameAttr(), name);
        } else {
            processAttribute(attrs, getFirstnameAttr(), name);
            processAttribute(attrs, getMiddlenameAttr(), name);
            processAttribute(attrs, getLastnameAttr(), name);
        }

        user.setName(name.toString().trim());

        return user;
    }

    private void processAttribute(Attributes attrs, String key, StringBuilder name) throws NamingException {
        if (key != null) {
            String attrValue = getAttrCoalesce(attrs, key);
            if (attrValue != null) {
                name.append(' ').append(attrValue);
            }
        }
    }

    public abstract String getFirstnameAttr();

    public abstract String getMiddlenameAttr();

    public abstract String getLastnameAttr();

    public abstract String getUsernameAttr();

    public abstract String getDisplaynameAttr();
}