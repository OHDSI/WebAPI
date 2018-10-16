package org.ohdsi.webapi.user.importer.utils;

import org.ohdsi.webapi.user.importer.model.RoleGroupEntity;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RoleGroupUtils {

  public static boolean equalsRoleGroupMapping(RoleGroupEntity a, RoleGroupEntity b) {
    if (Objects.isNull(a) && Objects.isNull(b)) {
      return true;
    }
    if (Objects.nonNull(a) && Objects.nonNull(b)) {
      return Objects.equals(a.getProvider(), b.getProvider())
              && Objects.equals(a.getGroupDn(), b.getGroupDn())
              && Objects.equals(a.getRole().getId(), b.getRole().getId());
    }
    return false;
  }

  public static Predicate<RoleGroupEntity> equalsPredicate(RoleGroupEntity e) {

    return m -> RoleGroupUtils.equalsRoleGroupMapping(e, m);
  }

  public static List<RoleGroupEntity> subtract(List<RoleGroupEntity> source, List<RoleGroupEntity> target) {

    return source
            .stream()
            .filter(m -> target.stream().noneMatch(RoleGroupUtils.equalsPredicate(m)))
            .collect(Collectors.toList());
  }

  public static List<RoleGroupEntity> findCreated(List<RoleGroupEntity> source, List<RoleGroupEntity> target) {

    return subtract(target, source);
  }

  public static List<RoleGroupEntity> findDeleted(List<RoleGroupEntity> source, List<RoleGroupEntity> target) {

    return subtract(source, target);
  }

}
