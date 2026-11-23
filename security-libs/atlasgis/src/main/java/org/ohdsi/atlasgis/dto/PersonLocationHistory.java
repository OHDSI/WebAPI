package org.ohdsi.atlasgis.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PersonLocationHistory {

    private List<PersonLocation> locations = new ArrayList<>();
    private GeoBoundingBox bbox;
}
