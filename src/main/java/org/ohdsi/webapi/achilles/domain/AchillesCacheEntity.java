package org.ohdsi.webapi.achilles.domain;

import org.ohdsi.webapi.source.Source;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "achilles_cache")
public class AchillesCacheEntity {
    @Id
    @SequenceGenerator(name = "achilles_cache_seq", sequenceName = "achilles_cache_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "achilles_cache_seq")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;
    @Column(name = "cache_name", nullable = false)
    private String cacheName;
    @Column(name = "cache")
    private String cache;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public String getCache() {
        return cache;
    }

    public void setCache(String cacheName) {
        this.cache = cacheName;
    }
}
