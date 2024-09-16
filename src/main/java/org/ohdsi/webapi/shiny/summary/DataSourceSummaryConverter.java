package org.ohdsi.webapi.shiny.summary;

import org.ohdsi.webapi.report.CDMAttribute;
import org.ohdsi.webapi.report.CDMDashboard;
import org.ohdsi.webapi.report.ConceptCountRecord;
import org.ohdsi.webapi.report.ConceptDistributionRecord;
import org.ohdsi.webapi.report.CumulativeObservationRecord;
import org.ohdsi.webapi.report.MonthObservationRecord;
import org.ohdsi.webapi.service.ShinyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@ConditionalOnBean(ShinyService.class)
public class DataSourceSummaryConverter {

    private static final String VALUE_NOT_AVAILABLE = "N/A";

    private double calculateVariance(List<Double> values, double mean) {
        double variance = 0;
        for (double value : values) {
            variance += Math.pow(value - mean, 2);
        }
        return variance / values.size();
    }

    public DataSourceSummary convert(CDMDashboard cdmDashboard) {
        DataSourceSummary dataSourceSummary = new DataSourceSummary();

        if (cdmDashboard.getSummary() != null) {
            for (CDMAttribute attribute : cdmDashboard.getSummary()) {
                switch (attribute.getAttributeName()) {
                    case "Source name":
                        dataSourceSummary.setSourceName(attribute.getAttributeValue());
                        break;
                    case "Number of persons":
                        double number = Double.parseDouble(attribute.getAttributeValue());
                        String formattedNumber = new DecimalFormat("#,###.###M").format(number / 1_000_000);
                        dataSourceSummary.setNumberOfPersons(formattedNumber);
                        break;
                }
            }
        }

        if (cdmDashboard.getGender() != null) {
            long maleCount = 0;
            long femaleCount = 0;
            for (ConceptCountRecord record : cdmDashboard.getGender()) {
                if (record.getConceptName().equalsIgnoreCase("MALE")) {
                    maleCount = record.getCountValue();
                } else if (record.getConceptName().equalsIgnoreCase("FEMALE")) {
                    femaleCount = record.getCountValue();
                }
            }
            long totalGenderCount = maleCount + femaleCount;
            String malePercentage = String.format("%,.1f %%", 100 * (double) maleCount / totalGenderCount);
            String femalePercentage = String.format("%,.1f %%", 100 * (double) femaleCount / totalGenderCount);
            dataSourceSummary.setMale(String.format("%,d (%s)", maleCount, malePercentage));
            dataSourceSummary.setFemale(String.format("%,d (%s)", femaleCount, femalePercentage));
        }

        if (cdmDashboard.getAgeAtFirstObservation() != null) {
            List<Double> percents = cdmDashboard.getAgeAtFirstObservation().stream()
                    .map(ConceptDistributionRecord::getPercentValue)
                    .collect(Collectors.toList());
            double sum = percents.stream().mapToDouble(Double::doubleValue).sum();
            double mean = sum / percents.size();
            double variance = calculateVariance(percents, mean);

            int minYear = cdmDashboard.getAgeAtFirstObservation().stream()
                    .min(Comparator.comparingInt(ConceptDistributionRecord::getIntervalIndex))
                    .orElse(new ConceptDistributionRecord()).getIntervalIndex();
            int maxYear = cdmDashboard.getAgeAtFirstObservation().stream()
                    .max(Comparator.comparingInt(ConceptDistributionRecord::getIntervalIndex))
                    .orElse(new ConceptDistributionRecord()).getIntervalIndex();
            dataSourceSummary.setAgeAtFirstObservation(String.format("[%d - %d] (M= %.1f; SD= %.1f)",
                    minYear, maxYear, mean, Math.sqrt(variance)));
        }

        if (cdmDashboard.getCumulativeObservation() != null) {
            List<Double> percentPersons = cdmDashboard.getCumulativeObservation().stream()
                    .map(CumulativeObservationRecord::getyPercentPersons)
                    .collect(Collectors.toList());
            double sum = percentPersons.stream().mapToDouble(Double::doubleValue).sum();
            double mean = sum / percentPersons.size();
            double variance = calculateVariance(percentPersons, mean);

            int minObs = cdmDashboard.getCumulativeObservation().stream()
                    .min(Comparator.comparingInt(CumulativeObservationRecord::getxLengthOfObservation))
                    .orElse(new CumulativeObservationRecord()).getxLengthOfObservation();
            int maxObs = cdmDashboard.getCumulativeObservation().stream()
                    .max(Comparator.comparingInt(CumulativeObservationRecord::getxLengthOfObservation))
                    .orElse(new CumulativeObservationRecord()).getxLengthOfObservation();
            dataSourceSummary.setCumulativeObservation(String.format("[%d - %d] (M= %.1f; SD= %.1f)",
                    minObs, maxObs, mean, Math.sqrt(variance)));
        }

        if (cdmDashboard.getObservedByMonth() != null && !cdmDashboard.getObservedByMonth().isEmpty()) {
            MonthObservationRecord startRecord = cdmDashboard.getObservedByMonth().get(0);
            MonthObservationRecord endRecord = cdmDashboard.getObservedByMonth()
                    .get(cdmDashboard.getObservedByMonth().size() - 1);
            dataSourceSummary.setContinuousObservationCoverage(String.format("Start: %02d/%02d, End: %02d/%02d",
                    startRecord.getMonthYear() % 100, startRecord.getMonthYear() / 100,
                    endRecord.getMonthYear() % 100, endRecord.getMonthYear() / 100));
        }

        return dataSourceSummary;
    }

    public DataSourceSummary emptySummary(String dataSourceName) {
        DataSourceSummary dataSourceSummary = new DataSourceSummary();
        dataSourceSummary.setSourceName(dataSourceName);
        dataSourceSummary.setFemale(VALUE_NOT_AVAILABLE);
        dataSourceSummary.setMale(VALUE_NOT_AVAILABLE);
        dataSourceSummary.setCumulativeObservation(VALUE_NOT_AVAILABLE);
        dataSourceSummary.setAgeAtFirstObservation(VALUE_NOT_AVAILABLE);
        dataSourceSummary.setContinuousObservationCoverage(VALUE_NOT_AVAILABLE);
        dataSourceSummary.setNumberOfPersons(VALUE_NOT_AVAILABLE);
        return dataSourceSummary;
    }
}
