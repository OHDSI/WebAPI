package org.ohdsi.atlasgis.converter;

import java.util.HashMap;
import java.util.Map;
import org.mapstruct.Mapper;
import org.ohdsi.atlasgis.dto.GeoBoundingBox;

@Mapper(componentModel = "spring")
public interface GeoBoundingBoxMapper {

	default Map<String, Object> toMap(GeoBoundingBox geoBoundingBox) {

		Map<String, Object> params = new HashMap<>();
		params.put("westLongitude", geoBoundingBox.getWestLongitude());
		params.put("eastLongitude", geoBoundingBox.getEastLongitude());
		params.put("northLatitude", geoBoundingBox.getNorthLatitude());
		params.put("southLatitude", geoBoundingBox.getSouthLatitude());
		return params;
	}
}
