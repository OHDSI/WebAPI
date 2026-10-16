/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.cohortdefinition;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.ohdsi.webapi.tag.domain.TagType;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author cknoll1
 */
public interface CohortDefinitionRepository extends CrudRepository<CohortDefinitionEntity, Integer> {
  Page<CohortDefinitionEntity> findAll(Pageable pageable);

  // Bug in hibernate, findById should use @EntityGraph, but details are not being
  // fetched. Workaround: mark details Fetch.EAGER,
  // but means findAll() will eager load definitions (what the @EntityGraph was
  // supposed to solve)
  @EntityGraph(value = "CohortDefinition.withDetail", type = EntityGraph.EntityGraphType.LOAD)
  @Query("select cd from CohortDefinition cd LEFT JOIN FETCH cd.createdBy LEFT JOIN FETCH cd.modifiedBy where cd.id = ?1")
  CohortDefinitionEntity findOneWithDetail(Integer id);

  @Query("select cd from CohortDefinition AS cd LEFT JOIN FETCH cd.createdBy LEFT JOIN FETCH cd.modifiedBy")
  List<CohortDefinitionEntity> list();

  @Query("select count(cd) from CohortDefinition AS cd WHERE cd.name = :name and cd.id <> :id")
  int getCountCDefWithSameName(@Param("id") Integer id, @Param("name") String name);

  @Query("SELECT cd FROM CohortDefinition cd WHERE cd.name LIKE ?1 ESCAPE '\\'")
  List<CohortDefinitionEntity> findAllByNameStartsWith(String pattern);

  Optional<CohortDefinitionEntity> findByName(String name);

  @Query("SELECT DISTINCT cd FROM CohortDefinition cd JOIN FETCH cd.tags t WHERE lower(t.name) in :tagNames")
  List<CohortDefinitionEntity> findByTags(@Param("tagNames") List<String> tagNames);

  /**
   * Returns a projection of cohort definitions including per-user authorization
   * hints (canRead/canWrite).
   * The query uses subselects against CohortDefinitionAccessEntity to provide
   * coarse-grained hints and
   * also treats the creator (cd.createdBy.id) as owner (implicit read/write).
   */
  @Query("""
      SELECT
        cd AS cohortDefinition,
        CASE
          WHEN (
            (cd.createdBy.id = :userId)
            OR EXISTS (
              SELECT 1
              FROM CohortDefinitionAccess ca
              JOIN UserRole ur ON ur.role.id = ca.roleId
              WHERE ur.user.id = :userId
                AND ca.cohortDefinitionId = cd.id
                AND ca.accessType IN (READ,WRITE)
            )
          ) THEN true
          ELSE false
        END AS canRead,
        CASE
          WHEN (
            (cd.createdBy.id = :userId)
            OR EXISTS (
              SELECT 1
              FROM CohortDefinitionAccess ca2
              JOIN UserRole ur2 ON ur2.role.id = ca2.roleId
              WHERE ur2.user.id = :userId
                AND ca2.cohortDefinitionId = cd.id
                AND ca2.accessType = WRITE
            )
          ) THEN true
          ELSE false
        END AS canWrite
      FROM CohortDefinition cd
      LEFT JOIN FETCH cd.createdBy
      LEFT JOIN FETCH cd.modifiedBy
      """)
  List<CohortDefinitionWithAccess> findAllWithAccessHints(@Param("userId") Long userId);

  /**
   * Projection for the cohort-tag-group query. Each row represents one
   * (cohort, tag, group) triple. Tags without groups produce a row with
   * null group columns thanks to the LEFT JOIN.
   */
  public static interface CohortTagGroupRow {
    Integer getAssetId();
    Integer getTagId();
    String getTagName();
    TagType getTagType();
    Integer getTagCount();
    String getTagColor();
    String getTagIcon();
    Integer getGroupId();
    String getGroupName();
    org.ohdsi.webapi.tag.domain.TagType getGroupType();
    Boolean getGroupShowGroup();
    String getGroupColor();
    String getGroupIcon();
  }

  @Query("""
        SELECT ct.assetId.assetId as assetId,
          t.id as tagId, t.name as tagName, t.type as tagType, t.count as tagCount, t.color as tagColor, t.icon as tagIcon,
          g.id as groupId, g.name as groupName, g.type as groupType, g.showGroup as groupShowGroup, g.color as groupColor, g.icon as groupIcon
        FROM CohortTag ct JOIN ct.tag t LEFT JOIN t.groups g
      """)
  List<CohortTagGroupRow> findCohortTagGroupRows();

  /**
   * Builds a map of cohortDefinitionId → List&lt;TagDTO&gt; where each TagDTO
   * has its {@code groups} set populated from the {@code tag_group} join table.
   * Executes a single query (no N+1) via {@link #findCohortTagGroupRows()}.
   */
  default Map<Integer, List<TagDTO>> getCohortTagMap() {

    List<CohortTagGroupRow> rows = findCohortTagGroupRows();
    Map<Integer, List<TagDTO>> cohortTags = new HashMap<>();
    if (rows == null || rows.isEmpty())
      return cohortTags;

    Map<Integer, Set<Integer>> cohortSeenTags = new HashMap<>();
    Map<Integer, TagDTO> tagCache = new HashMap<>();
    Map<Integer, TagDTO> groupCache = new HashMap<>();

    for (CohortTagGroupRow r : rows) {

      Integer cohortId = r.getAssetId();
      Integer tagId = r.getTagId();

      // ---------- Tag ----------
      TagDTO tag = tagCache.computeIfAbsent(tagId, k -> {
        TagDTO t = new TagDTO();
        t.setId(tagId);
        t.setName(r.getTagName());
        t.setType(r.getTagType());
        t.setCount(r.getTagCount() == null ? 0 : r.getTagCount());
        t.setColor(r.getTagColor());
        t.setIcon(r.getTagIcon());
        t.setGroups(new HashSet<>());
        return t;
      });

      // ---------- Group ----------
      if (r.getGroupId() != null) {
        Integer groupId = r.getGroupId();

        TagDTO group = groupCache.computeIfAbsent(groupId, k -> {
          TagDTO g = new TagDTO();
          g.setId(groupId);
          g.setName(r.getGroupName());
          g.setType(r.getGroupType());
          g.setShowGroup(Boolean.TRUE.equals(r.getGroupShowGroup()));
          g.setColor(r.getGroupColor());
          g.setIcon(r.getGroupIcon());
          return g;
        });

        tag.getGroups().add(group);
      }

      // ---------- Attach to cohort ----------
      Set<Integer> seenTags = cohortSeenTags.computeIfAbsent(cohortId, k -> new HashSet<>());

      if (seenTags.add(tagId)) {
        List<TagDTO> list = cohortTags.computeIfAbsent(cohortId, k -> new ArrayList<>());

        list.add(tag);

        // ensure group appears as top-level entry if required by UI
        for (TagDTO group : tag.getGroups()) {
          Integer gid = group.getId();
          if (seenTags.add(gid)) {
            list.add(tagCache.getOrDefault(gid, group));
          }
        }
      }
    }

    return cohortTags;
  }

}
