package org.ohdsi.webapi.tag.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.domain.TagInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Integer> {
  @Query("SELECT t AS tag, COUNT(ct.cohortId) AS tagCount " +
          "FROM Tag t " +
          "LEFT JOIN CohortTag ct " +
          "ON ct.tag = t " +
          "AND LOWER(t.name) LIKE LOWER(concat(?1, '%')) " +
          "GROUP BY t.id")
  List<TagInfo> findAllCohortTagsByNameInterface(String namePart);
}
