package org.ohdsi.webapi.tool;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToolRepository extends JpaRepository<Tool, Integer> {
    List<Tool> findAllByEnabled(boolean enabled);
}
