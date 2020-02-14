package com.jnj.honeur.webapi;

import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.health.CompositeHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class HoneurHealthEndpoint extends HealthEndpoint {

    private final HealthIndicator healthIndicatorLiveness;

    private static final List<String> livenessIndicators = new ArrayList<>();

    static {
        livenessIndicators.add("diskSpaceHealthIndicator");
        livenessIndicators.add("jmsHealthIndicator");
    }

    public HoneurHealthEndpoint(HealthAggregator healthAggregator, Map<String, HealthIndicator> healthIndicators) {
        super(healthAggregator, healthIndicators);

        CompositeHealthIndicator healthIndicator = new CompositeHealthIndicator(healthAggregator);
        Iterator var4 = healthIndicators.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<String, HealthIndicator> entry = (Map.Entry)var4.next();
            if(livenessIndicators.contains(entry.getKey()))
                healthIndicator.addHealthIndicator(this.getKey((String)entry.getKey()), (HealthIndicator)entry.getValue());
        }

        this.healthIndicatorLiveness = healthIndicator;
    }

    public Health invoke(boolean transitive) {
        if(transitive)
            return invoke();
        else
            return this.healthIndicatorLiveness.health();
    }

    private String getKey(String name) {
        int index = name.toLowerCase().indexOf("healthindicator");
        return index > 0 ? name.substring(0, index) : name;
    }
}
