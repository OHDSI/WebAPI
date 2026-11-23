package org.ohdsi.atlasgis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeoBoundingBox {

    private Double eastLongitude;
    private Double northLatitude;
    private Double westLongitude;
    private Double southLatitude;
}
