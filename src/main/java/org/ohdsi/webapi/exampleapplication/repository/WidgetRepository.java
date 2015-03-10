package org.ohdsi.webapi.exampleapplication.repository;

import org.ohdsi.webapi.exampleapplication.model.Widget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 */
@Repository
public interface WidgetRepository extends CrudRepository<Widget, Long> {
    
    Page<Widget> findAll(Pageable pageable);
    
    Page<Widget> findByNameContainingIgnoreCase(String name, String country, Pageable pageable);
    
    Widget findByNameIgnoreCase(String name);
    
}
