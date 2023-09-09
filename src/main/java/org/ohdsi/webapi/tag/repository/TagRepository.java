package org.ohdsi.webapi.tag.repository;

import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.domain.TagInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Integer> {
    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT(?1, '%'))")
    List<Tag> findAllTags(String namePart);

    List<Tag> findByIdIn(List<Integer> ids);

    @Query("SELECT ct.assetId.tagId as id, " +
            "COUNT(ct.assetId.tagId) AS count " +
            "FROM CohortTag ct " +
            "GROUP BY ct.assetId.tagId")
    List<TagInfo> findCohortTagInfo();

    @Query("SELECT cct.assetId.tagId as id, " +
            "COUNT(cct.assetId.tagId) AS count " +
            "FROM CohortCharacterizationTag cct " +
            "GROUP BY cct.assetId.tagId")
    List<TagInfo> findCcTagInfo();

    @Query("SELECT cst.assetId.tagId as id, " +
            "COUNT(cst.assetId.tagId) AS count " +
            "FROM ConceptSetTag cst " +
            "GROUP BY cst.assetId.tagId")
    List<TagInfo> findConceptSetTagInfo();

    @Query("SELECT it.assetId.tagId as id, " +
            "COUNT(it.assetId.tagId) AS count " +
            "FROM IrTag it " +
            "GROUP BY it.assetId.tagId")
    List<TagInfo> findIrTagInfo();

    @Query("SELECT pt.assetId.tagId as id, " +
            "COUNT(pt.assetId.tagId) AS count " +
            "FROM PathwayTag pt " +
            "GROUP BY pt.assetId.tagId")
    List<TagInfo> findPathwayTagInfo();

    @Query("SELECT rt.assetId.tagId as id, " +
            "COUNT(rt.assetId.tagId) AS count " +
            "FROM ReusableTag rt " +
            "GROUP BY rt.assetId.tagId")
    List<TagInfo> findReusableTagInfo();

    @Query("SELECT t FROM Tag t WHERE t.mandatory = 'TRUE'")
    List<Tag> findMandatoryTags();
}
