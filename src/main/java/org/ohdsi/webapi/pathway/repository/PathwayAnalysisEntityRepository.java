package org.ohdsi.webapi.pathway.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PathwayAnalysisEntityRepository extends EntityGraphJpaRepository<PathwayAnalysisEntity, Integer> {
  @Query("SELECT pa FROM PathwayAnalysis pa WHERE pa.name LIKE ?1 ESCAPE '\\'")
  List<PathwayAnalysisEntity> findAllByNameStartsWith(String pattern);

  Optional<PathwayAnalysisEntity> findByName(String name);

  @Query("SELECT COUNT(pa) FROM PathwayAnalysis pa WHERE pa.name = :name and pa.id <> :id")
  int getCountPAWithSameName(@Param("id") Integer id, @Param("name") String name);

  @Query("SELECT DISTINCT pa FROM PathwayAnalysis pa JOIN FETCH pa.tags t WHERE lower(t.name) in :tagNames")
  List<PathwayAnalysisEntity> findByTags(@Param("tagNames") List<String> tagNames);

    interface PathwayTagGroupRow {
      Integer getAssetId();
      Integer getTagId();
      String getTagName();
      org.ohdsi.webapi.tag.domain.TagType getTagType();
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
        SELECT pt.assetId.assetId as assetId,
          t.id as tagId, t.name as tagName, t.type as tagType, t.count as tagCount, t.color as tagColor, t.icon as tagIcon,
          g.id as groupId, g.name as groupName, g.type as groupType, g.showGroup as groupShowGroup, g.color as groupColor, g.icon as groupIcon
        FROM PathwayTag pt JOIN pt.tag t LEFT JOIN t.groups g
      """)
    List<PathwayTagGroupRow> findPathwayTagGroupRows();

    default java.util.Map<Integer, java.util.List<org.ohdsi.webapi.tag.dto.TagDTO>> getPathwayAnalysisTagMap() {
      List<PathwayTagGroupRow> rows = findPathwayTagGroupRows();
      java.util.Map<Integer, java.util.List<org.ohdsi.webapi.tag.dto.TagDTO>> result = new java.util.HashMap<>();
      if (rows == null || rows.isEmpty()) return result;

      java.util.Map<Integer, java.util.Set<Integer>> seenTags = new java.util.HashMap<>();
      java.util.Map<Integer, org.ohdsi.webapi.tag.dto.TagDTO> tagCache = new java.util.HashMap<>();
      java.util.Map<Integer, org.ohdsi.webapi.tag.dto.TagDTO> groupCache = new java.util.HashMap<>();

      for (PathwayTagGroupRow r : rows) {
        Integer assetId = r.getAssetId();
        Integer tagId = r.getTagId();

        org.ohdsi.webapi.tag.dto.TagDTO tag = tagCache.computeIfAbsent(tagId, k -> {
          org.ohdsi.webapi.tag.dto.TagDTO t = new org.ohdsi.webapi.tag.dto.TagDTO();
          t.setId(tagId);
          t.setName(r.getTagName());
          t.setType(r.getTagType());
          t.setCount(r.getTagCount() == null ? 0 : r.getTagCount());
          t.setColor(r.getTagColor());
          t.setIcon(r.getTagIcon());
          t.setGroups(new java.util.HashSet<>());
          return t;
        });

        if (r.getGroupId() != null) {
          Integer groupId = r.getGroupId();
          org.ohdsi.webapi.tag.dto.TagDTO group = groupCache.computeIfAbsent(groupId, k -> {
            org.ohdsi.webapi.tag.dto.TagDTO g = new org.ohdsi.webapi.tag.dto.TagDTO();
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

        java.util.Set<Integer> seen = seenTags.computeIfAbsent(assetId, k -> new java.util.HashSet<>());
        if (seen.add(tagId)) {
          java.util.List<org.ohdsi.webapi.tag.dto.TagDTO> list = result.computeIfAbsent(assetId, k -> new java.util.ArrayList<>());
          list.add(tag);
          for (org.ohdsi.webapi.tag.dto.TagDTO group : tag.getGroups()) {
            Integer gid = group.getId();
            if (seen.add(gid)) {
              list.add(tagCache.getOrDefault(gid, group));
            }
          }
        }
      }

      return result;
    }
}
