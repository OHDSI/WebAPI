package org.ohdsi.webapi.statistic.dto;

import java.util.ArrayList;
import java.util.List;

public class AccessTrendsDto {
    private List<AccessTrendDto> trends = new ArrayList<>();

    public AccessTrendsDto(List<AccessTrendDto> trends) {
        this.trends = trends;
    }

    public List<AccessTrendDto> getTrends() {
        return trends;
    }
}
