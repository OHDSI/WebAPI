/*
 * Copyright 2015 fdefalco.
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
package org.ohdsi.webapi.conceptset;

import org.ohdsi.webapi.tag.domain.TagType;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author fdefalco
 */
public interface ConceptSetRepository extends CrudRepository<ConceptSet, Integer> {
  // Removed: clashes with CrudRepository.findById which returns Optional<T>
  //   ConceptSet findById(Integer conceptSetId);
  
  @Deprecated
  @Query("SELECT cs FROM ConceptSet cs WHERE cs.name = :conceptSetName and cs.id <> :conceptSetId")
  Collection<ConceptSet> conceptSetExists(@Param("conceptSetId") Integer conceptSetId, @Param("conceptSetName") String conceptSetName);
  
  @Query("SELECT COUNT(cs) FROM ConceptSet cs WHERE cs.name = :conceptSetName and cs.id <> :conceptSetId")
  int getCountCSetWithSameName(@Param("conceptSetId") Integer conceptSetId, @Param("conceptSetName") String conceptSetName);

  @Query("SELECT cs FROM ConceptSet cs WHERE cs.name LIKE ?1 ESCAPE '\\'")
  List<ConceptSet> findAllByNameStartsWith(String pattern);
  
  Optional<ConceptSet> findByName(String name);
  
  @Query("SELECT DISTINCT cs FROM ConceptSet cs JOIN FETCH cs.tags t WHERE lower(t.name) in :tagNames")
  List<ConceptSet> findByTags(@Param("tagNames") List<String> tagNames);

  @Query("SELECT cs FROM ConceptSet cs LEFT JOIN FETCH cs.createdBy LEFT JOIN FETCH cs.modifiedBy")
  List<ConceptSet> list();

  /**
   * Projection for the concept-set-tag-group query. Each row represents one
   * (conceptSet, tag, group) triple.
   */
  interface ConceptSetTagGroupRow {
    Integer getAssetId();
    Integer getTagId();
    String getTagName();
    TagType getTagType();
    Integer getTagCount();
    String getTagColor();
    String getTagIcon();
    Integer getGroupId();
    String getGroupName();
    TagType getGroupType();
    Boolean getGroupShowGroup();
    String getGroupColor();
    String getGroupIcon();
  }

  @Query("""
        SELECT ct.assetId.assetId as assetId,
          t.id as tagId, t.name as tagName, t.type as tagType, t.count as tagCount, t.color as tagColor, t.icon as tagIcon,
          g.id as groupId, g.name as groupName, g.type as groupType, g.showGroup as groupShowGroup, g.color as groupColor, g.icon as groupIcon
        FROM ConceptSetTag ct JOIN ct.tag t LEFT JOIN t.groups g
      """)
  List<ConceptSetTagGroupRow> findConceptSetTagGroupRows();

  /**
   * Builds a map of conceptSetId → List&lt;TagDTO&gt; where each TagDTO
   * has its {@code groups} set populated from the tag_group join table.
   * Executes a single query (no N+1) via {@link #findConceptSetTagGroupRows()}.
   */
  default Map<Integer, List<TagDTO>> getConceptSetTagMap() {

    List<ConceptSetTagGroupRow> rows = findConceptSetTagGroupRows();
    Map<Integer, List<TagDTO>> conceptSetTags = new HashMap<>();
    if (rows == null || rows.isEmpty())
      return conceptSetTags;

    Map<Integer, Set<Integer>> seenTags = new HashMap<>();
    Map<Integer, TagDTO> tagCache = new HashMap<>();
    Map<Integer, TagDTO> groupCache = new HashMap<>();

    for (ConceptSetTagGroupRow r : rows) {

      Integer assetId = r.getAssetId();
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

      // ---------- Attach to concept set ----------
      Set<Integer> seen = seenTags.computeIfAbsent(assetId, k -> new HashSet<>());

      if (seen.add(tagId)) {
        List<TagDTO> list = conceptSetTags.computeIfAbsent(assetId, k -> new ArrayList<>());

        list.add(tag);

        // ensure group appears as top-level entry if required by UI
        for (TagDTO group : tag.getGroups()) {
          Integer gid = group.getId();
          if (seen.add(gid)) {
            list.add(tagCache.getOrDefault(gid, group));
          }
        }
      }
    }

    return conceptSetTags;
  }
}
