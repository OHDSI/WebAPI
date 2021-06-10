package org.ohdsi.webapi.generationcache;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.source.Source;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name="generation_cache")
public class GenerationCache {

    @Id
    @Column(name = "id")
    @GenericGenerator(
            name = "generation_cache_generator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "generation_cache_sequence"),
                    @Parameter(name = "initial_value", value = "1"),
                    @Parameter(name = "increment_size", value = "1")
            }
    )
    @GeneratedValue(generator = "generation_cache_generator")
    private Integer id;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private CacheableGenerationType type;

    @Column(name = "design_hash")
    private Integer designHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

    @Column(name = "result_checksum")
    private String resultChecksum;

    @Column(name = "created_date")
    private Date createdDate = new Date();

    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    public CacheableGenerationType getType() {

        return type;
    }

    public void setType(CacheableGenerationType type) {

        this.type = type;
    }

    public Integer getDesignHash() {

        return designHash;
    }

    public void setDesignHash(Integer designHash) {

        this.designHash = designHash;
    }

    public Source getSource() {

        return source;
    }

    public void setSource(Source source) {

        this.source = source;
    }

    public String getResultChecksum() {

        return resultChecksum;
    }

    public void setResultChecksum(String resultChecksum) {

        this.resultChecksum = resultChecksum;
    }

    public Date getCreatedDate() {

        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {

        this.createdDate = createdDate;
    }
}
