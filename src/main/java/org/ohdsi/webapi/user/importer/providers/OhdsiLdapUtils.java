package org.ohdsi.webapi.user.importer.providers;

import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.OrFilter;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class OhdsiLdapUtils {

  public static String valueAsString(Attribute attribute) throws NamingException {
    return Objects.nonNull(attribute) ? attribute.get().toString() : "";
  }

  public static List<String> valueAsList(Attribute attribute) throws NamingException {
    List<String> result = new ArrayList<>();
    if (Objects.nonNull(attribute)) {
      for (int i = 0; i < attribute.size(); i++) {
        result.add(attribute.get(i).toString());
      }
    }
    return result;
  }

  public static OrFilter getCriteria(String attribute, Set<String> values) {
    OrFilter filter = new OrFilter();
    values.forEach(v -> filter.or(new EqualsFilter(attribute, v)));
    return filter;
  }


}
