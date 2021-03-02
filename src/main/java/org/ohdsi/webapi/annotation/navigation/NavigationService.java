package org.ohdsi.webapi.annotation.navigation;

import java.util.Collection;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import static org.ohdsi.webapi.util.SecurityUtils.whitelist;
import org.springframework.jdbc.core.RowMapper;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.source.Source;
import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.ohdsi.webapi.cohortsample.CohortSampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.annotation.navigation.Navigation;
import org.ohdsi.webapi.cohortsample.CohortSample;




@Service
public class NavigationService extends AbstractDaoService {
    private final String BASE_SQL_PATH = "/resources/navigation/";

    @Autowired
    private CohortSampleRepository cohortSampleRepository;


    public Navigation navData(Navigation nav, Long subjectId, String sampleName, int cohortId, Source source)
    {
        if (sampleName.indexOf("_") != -1) {
          sampleName = sampleName.replaceAll("_", " ");
        }
//        List<Object[]> sample = cohortSampleRepository.getNavSample(sampleName);
//        TODO impmlement above funtion
        List<Object[]> sample = new ArrayList<Object[]>();
        int annCount = 0;
        boolean foundNext = false;
        for(int i = 0; i < sample.size(); i++) {
          if((long) sample.get(i)[0] == subjectId) {
            nav.setPrevSubjectId(i == 0 ? (long) sample.get(sample.size() - 1)[0] : (long) sample.get(i-1)[0]);
            nav.setNextSubjectId(sample.size() == i + 1 ? (long) sample.get(0)[0] : (long) sample.get(i+1)[0]);
          }
          if ((boolean) sample.get(i)[1] == true) {
              annCount++;
          }
          if (!foundNext && (boolean) sample.get(i)[1] == true) {
            nav.setNextUnannotatedSubjectId((long) sample.get(i)[0]);
            foundNext = true;
          }
          
        }
        nav.setNumProfileSamples(sample.size());
        nav.setNumAnnotations(annCount);
        System.out.println(nav.getNumAnnotations());
        return nav;
    }

}
