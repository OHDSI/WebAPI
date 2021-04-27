package org.ohdsi.webapi.tag.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.tag.domain.Tag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends EntityGraphJpaRepository<Tag, Integer> {
  @Query("SELECT tag FROM Tag tag WHERE lower(tag.name) LIKE LOWER(concat(?1, '%')) ESCAPE '\\'")
  List<Tag> findAllByNameStartsWith(String pattern);

//  @Query("SELECT U.name FROM User U WHERE LOWER(U.name) LIKE LOWER(concat(?1, '%'))")
//  Optional<PathwayAnalysisEntity> findByName(String name);
//
//  @Query("SELECT COUNT(pa) FROM pathway_analysis pa WHERE pa.name = :name and pa.id <> :id")
//  int getCountPAWithSameName(@Param("id") Integer id, @Param("name") String name);
}
