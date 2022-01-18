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

    @Query("SELECT t AS tag, " +
            "COUNT(ct.assetId.tagId) AS count " +
            "FROM Tag t " +
            "LEFT JOIN CohortTag ct ON ct.tag = t " +
            "GROUP BY t.id")
    List<TagInfo> findCohortTagInfo();

    @Query("SELECT t AS tag, " +
            "COUNT(cct.assetId.tagId) AS count " +
            "FROM Tag t " +
            "LEFT JOIN CohortCharacterizationTag cct ON cct.tag = t " +
            "GROUP BY t.id")
    List<TagInfo> findCcTagInfo();

    @Query("SELECT t AS tag, " +
            "COUNT(cst.assetId.tagId) AS count " +
            "FROM Tag t " +
            "LEFT JOIN ConceptSetTag cst ON cst.tag = t " +
            "GROUP BY t.id")
    List<TagInfo> findConceptSetTagInfo();

    @Query("SELECT t AS tag, " +
            "COUNT(it.assetId.tagId) AS count " +
            "FROM Tag t " +
            "LEFT JOIN IrTag it ON it.tag = t " +
            "GROUP BY t.id")
    List<TagInfo> findIrTagInfo();

    @Query("SELECT t AS tag, " +
            "COUNT(pt.assetId.tagId) AS count " +
            "FROM Tag t " +
            "LEFT JOIN PathwayTag pt ON pt.tag = t " +
            "GROUP BY t.id")
    List<TagInfo> findPathwayTagInfo();

    @Query("SELECT t AS tag, " +
            "COUNT(r.assetId.tagId) AS count " +
            "FROM Tag t " +
            "LEFT JOIN ReusableTag r ON r.tag = t " +
            "GROUP BY t.id")
    List<TagInfo> findReusableTagInfo();

    @Query("SELECT t FROM Tag t WHERE t.mandatory = 'TRUE'")
    List<Tag> findMandatoryTags();
}
