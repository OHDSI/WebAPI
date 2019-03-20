package org.ohdsi.webapi.shiro.mapper;

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
            result = (String) attribute.get();
        }
        return result;
    }

    public UserPrincipal mapFromAttributes(Attributes attrs) throws NamingException {
        UserPrincipal user = new UserPrincipal();

        user.setUsername(getAttrCoalesce(attrs, getUsernameKey()));

        StringBuilder name = new StringBuilder();
        processAttribute(attrs, getFirstnameKey(), name);
        processAttribute(attrs, getMiddlenameKey(), name);
        processAttribute(attrs, getLastnameKey(), name);

        user.setName(name.toString().trim());

        return user;
    }

    private void processAttribute(Attributes attrs, String key, StringBuilder name) throws NamingException {
        if (key != null) {
            String attrValue = getAttrCoalesce(attrs, key);
            if(attrValue != null) {
                name.append(' ').append(getAttrCoalesce(attrs, key));
            }
        }
    }

    public abstract String getFirstnameKey();

    public abstract String getMiddlenameKey();

    public abstract String getLastnameKey();

    public abstract String getUsernameKey();
}