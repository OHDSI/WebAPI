package org.ohdsi.webapi.service.lock;

import org.ohdsi.webapi.source.Source;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SnapshotHistorySourceProvider {

    @Value("${datasource.url}")
    private String snapshotHistorySourceConnection;
    @Value("${datasource.ohdsi.schema}")
    private String snapshotHistorySourceSchema;
    @Value("${datasource.dialect}")
    private String snapshotHistorySourceDialect;
    @Value("${datasource.username}")
    private String snapshotHistorySourceUsername;
    @Value("${datasource.password}")
    private String snapshotHistorySourcePassword;

    public Source getSnapshotHistorySource() {
        Source source = new Source();
        source.setSourceConnection(snapshotHistorySourceConnection);
        source.setSourceDialect(snapshotHistorySourceDialect);
        source.setUsername(snapshotHistorySourceUsername);
        source.setPassword(snapshotHistorySourcePassword);
        return source;
    }

    public String getSnapshotHistorySourceSchema() {
        return snapshotHistorySourceSchema;
    }
}
