package org.ohdsi.webapi.service;

import com.jnj.honeur.webapi.DataSourceLookup;
import com.jnj.honeur.webapi.SourceDaimonContextHolder;
import com.jnj.honeur.webapi.source.SourceDaimonContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.ShiroConfiguration;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

@Path("/source/")
@Component
public class SourceService extends AbstractDaoService {

    private static final Log log = LogFactory.getLog(SourceService.class);

    public class SortByKey implements Comparator<SourceInfo> {
        private boolean isAscending;

        public SortByKey(boolean ascending) {
            isAscending = ascending;
        }

        public SortByKey() {
            this(true);
        }

        public int compare(SourceInfo s1, SourceInfo s2) {
            return s1.sourceKey.compareTo(s2.sourceKey) * (isAscending ? 1 : -1);
        }
    }

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private DataSourceLookup dataSourceLookup;

    private static Collection<SourceInfo> cachedSources = null;

    @Path("sources")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<SourceInfo> getSources() {

        if (cachedSources == null) {
            Iterable<Source> sourceIterable = sourceRepository.findAll();
            ArrayList<SourceInfo> sources = new ArrayList<>();
            for (Source source : sourceIterable) {
                sources.add(new SourceInfo(source));
            }
            Collections.sort(sources, new SortByKey());
            cachedSources = sources;
            initDataSource(sourceIterable);
        }

        //TODO to remove when in order.
//        createAdditionalTables();
        return cachedSources;
    }

    // TODO Suspected workaround for problem on AWS.
    private void createAdditionalTables() {
        for (SourceInfo info : cachedSources) {
            for (SourceDaimon sourceDaimon : info.daimons) {
                if (sourceDaimon.getDaimonType().equals(SourceDaimon.DaimonType.Results)) {

                    String sql = ResourceHelper.GetResourceAsString("/resources/additionalTables.sql");

                    String tableQualifier = sourceDaimon.getTableQualifier();
                    String[] params = new String[]{"resultTableQualifier"};
                    String[] values = new String[]{tableQualifier};
                    sql = SqlRender.renderSql(sql, params, values);
                    sql = SqlTranslate.translateSql(sql, info.sourceDialect);

                    DataSource datasource = dataSourceLookup.getDataSource(new SourceDaimonContext(info.sourceKey, sourceDaimon.getDaimonType()).getSourceDaimonContextKey());
                    Connection connection = null;
                    Statement statement = null;
                    try {
                        connection = datasource.getConnection();
                        statement = connection.createStatement();
                        statement.execute(sql);
                        statement.close();
                        connection.close();
                    } catch (SQLException e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        if(connection != null){
                            try {
                                connection.close();
                            } catch (SQLException e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                        if(statement != null){
                            try {
                                statement.close();
                            } catch (SQLException e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    }
                }
            }
        }
    }

    @Path("refresh")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<SourceInfo> refreshSources() {
        cachedSources = null;
        return getSources();
    }

    @Path("priorityVocabulary")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SourceInfo getPriorityVocabularySourceInfo() {
        int priority = 0;
        SourceInfo priorityVocabularySourceInfo = null;

        for (Source source : sourceRepository.findAll()) {
            for (SourceDaimon daimon : source.getDaimons()) {
                if (daimon.getDaimonType() == SourceDaimon.DaimonType.Vocabulary) {
                    int daimonPriority = Integer.parseInt(daimon.getPriority());
                    if (daimonPriority >= priority) {
                        priority = daimonPriority;
                        priorityVocabularySourceInfo = new SourceInfo(source);
                    }
                }
            }
        }

        return priorityVocabularySourceInfo;
    }

    @Path("{key}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SourceInfo getSource(@PathParam("key") final String sourceKey) {
        return sourceRepository.findBySourceKey(sourceKey).getSourceInfo();
    }

    /**
     * Initialize the application data sources based on the collection of Source / SourceDaimons
     *
     * @param sourceIterable iterable of sources (including their source daimons)
     */
    private void initDataSource(Iterable<Source> sourceIterable) {
        if (dataSourceLookup != null) {
            dataSourceLookup.initDataSources(sourceIterable);
        }
    }
}
