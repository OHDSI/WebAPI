package org.ohdsi.webapi.cohortcharacterization.repository;

import java.util.List;
import java.util.Optional;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CcRepository extends EntityGraphJpaRepository<CohortCharacterizationEntity, Long> {
    Optional<CohortCharacterizationEntity> findById(final Long id);

    @Query("SELECT cc FROM CohortCharacterizationEntity cc WHERE cc.name LIKE ?1 ESCAPE '\\'")
    List<CohortCharacterizationEntity> findAllByNameStartsWith(String pattern);

    Optional<CohortCharacterizationEntity> findByName(String name);
    
    @Query("SELECT COUNT(cc) FROM CohortCharacterizationEntity cc WHERE cc.name = :ccName and cc.id <> :ccId")
    int getCountCcWithSameName(@Param("ccId") Long ccId, @Param("ccName") String ccName);

    @Query("SELECT cc FROM CohortCharacterizationEntity cc JOIN cc.cohortDefinitions cd WHERE cd = ?1")
    List<CohortCharacterizationEntity> findByCohortDefinition(CohortDefinitionEntity cd);

    @Query("SELECT cc FROM CohortCharacterizationEntity cc JOIN cc.featureAnalyses fa WHERE fa.featureAnalysis = :fa")
    List<CohortCharacterizationEntity> findByFeatureAnalysis(@Param("fa") FeAnalysisEntity feAnalysis);

    @Query("SELECT DISTINCT cc FROM CohortCharacterizationEntity cc JOIN FETCH cc.tags t WHERE lower(t.name) in :tagNames")
    List<CohortCharacterizationEntity> findByTags(@Param("tagNames") List<String> tagNames);

        /**
         * Projection for the cohort-characterization-tag-group query. Each row
         * represents one (cohortCharacterization, tag, group) triple.
         */
        interface CohortCharacterizationTagGroupRow {
                Long getAssetId();
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
                SELECT ct.assetId.assetId as assetId,
                    t.id as tagId, t.name as tagName, t.type as tagType, t.count as tagCount, t.color as tagColor, t.icon as tagIcon,
                    g.id as groupId, g.name as groupName, g.type as groupType, g.showGroup as groupShowGroup, g.color as groupColor, g.icon as groupIcon
                FROM CohortCharacterizationTag ct JOIN ct.tag t LEFT JOIN t.groups g
            """)
        List<CohortCharacterizationTagGroupRow> findCohortCharacterizationTagGroupRows();

        /**
         * Builds a map of cohortCharacterizationId -> List<TagDTO> (groups populated)
         * executing a single query via {@link #findCohortCharacterizationTagGroupRows()}.
         */
        default java.util.Map<Long, java.util.List<org.ohdsi.webapi.tag.dto.TagDTO>> getCohortCharacterizationTagMap() {
            List<CohortCharacterizationTagGroupRow> rows = findCohortCharacterizationTagGroupRows();
            java.util.Map<Long, java.util.List<org.ohdsi.webapi.tag.dto.TagDTO>> result = new java.util.HashMap<>();
            if (rows == null || rows.isEmpty()) return result;

            java.util.Map<Long, java.util.Set<Integer>> seenTags = new java.util.HashMap<>();
            java.util.Map<Integer, org.ohdsi.webapi.tag.dto.TagDTO> tagCache = new java.util.HashMap<>();
            java.util.Map<Integer, org.ohdsi.webapi.tag.dto.TagDTO> groupCache = new java.util.HashMap<>();

            for (CohortCharacterizationTagGroupRow r : rows) {
                Long assetId = r.getAssetId();
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
