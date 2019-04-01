package org.ohdsi.webapi.common.generation;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.webapi.executionengine.entity.AnalysisFile;
import org.ohdsi.webapi.executionengine.util.StringGenerationUtil;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.source.Source;
import org.springframework.batch.core.JobParametersBuilder;

import static org.ohdsi.webapi.Constants.Params.*;

public abstract class AnalysisExecutionSupport extends AbstractDaoService {


  protected AnalysisFile prepareAnalysisExecution(String packageName, String packageFilename, Number analysisId) {
      AnalysisFile execFile = new AnalysisFile();
      execFile.setFileName("runAnalysis.R");
      String[] paramNames = {"packageFile", "packageName", "analysisDir"};
      String[] paramValues = {packageFilename, packageName, String.format("analysis_%d", analysisId)};
      // renderSql is used to replace template params with actual values in the R script template
      String script = SqlRender.renderSql(getExecutionScript(), paramNames, paramValues);
      execFile.setContents(script.getBytes());
      return execFile;
  }

  protected JobParametersBuilder prepareJobParametersBuilder(Source source, Integer ananlysisId, String packageName, String packageFilename) {

      JobParametersBuilder builder = new JobParametersBuilder();
      builder.addString(SOURCE_ID, String.valueOf(source.getSourceId()));
      builder.addString(UPDATE_PASSWORD, StringGenerationUtil.generateRandomString());
      builder.addString(JOB_AUTHOR, getCurrentUserLogin());
      builder.addString(PACKAGE_NAME, packageName);
      builder.addString(PACKAGE_FILE_NAME, packageFilename);
      builder.addString(EXECUTABLE_FILE_NAME, "runAnalysis.R");
      return builder;
  }

  protected abstract String getExecutionScript();
}
