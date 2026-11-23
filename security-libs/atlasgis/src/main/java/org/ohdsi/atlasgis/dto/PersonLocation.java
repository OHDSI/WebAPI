package org.ohdsi.atlasgis.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class PersonLocation {

    private Double longitude;
    private Double latitude;
    private Date startDate;
    private Date endDate;
}
