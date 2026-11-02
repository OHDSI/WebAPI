package org.ohdsi.webapi.security.provisioning;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.ohdsi.webapi.security.provisioning.model.GroupRoleImportEntity;

public class GroupRoleImportUtils {

  public static boolean equalsRoleGroupMapping(GroupRoleImportEntity a, GroupRoleImportEntity b) {
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

  public static Predicate<GroupRoleImportEntity> equalsPredicate(GroupRoleImportEntity e) {

    return m -> GroupRoleImportUtils.equalsRoleGroupMapping(e, m);
  }

  public static List<GroupRoleImportEntity> subtract(List<GroupRoleImportEntity> source, List<GroupRoleImportEntity> target) {

    return source
            .stream()
            .filter(m -> target.stream().noneMatch(GroupRoleImportUtils.equalsPredicate(m)))
            .collect(Collectors.toList());
  }

  public static List<GroupRoleImportEntity> findCreated(List<GroupRoleImportEntity> source, List<GroupRoleImportEntity> target) {

    return subtract(target, source);
  }

  public static List<GroupRoleImportEntity> findDeleted(List<GroupRoleImportEntity> source, List<GroupRoleImportEntity> target) {

    return subtract(source, target);
  }

}
