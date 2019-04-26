package org.ohdsi.webapi.common.sensitiveinfo;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.ohdsi.circe.helper.ResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class AbstractSensitiveInfoService extends AbstractAdminService {

  protected static final Logger LOGGER = LoggerFactory.getLogger(SensitiveFilter.class);

  private Collection<SensitiveFilter> filters;

  @PostConstruct
  public void init() {

    String filterSource = ResourceHelper.GetResourceAsString("/resources/generation/sensitive_filters.csv");
    filters = new ArrayList<>();
    try(Reader in = new StringReader(filterSource)) {
      try(CSVParser parser = new CSVParser(in, CSVFormat.RFC4180)) {
        for (final CSVRecord record : parser) {
          filters.add(new SensitiveFilter(record.get(0), record.get(1)));
        }
      }
    } catch (IOException e) {
      throw new BeanInitializationException("Failed to read sensitive_filters.csv", e);
    }
  }

  protected String filterSensitiveInfo(String text, Map<String, Object> variables, boolean isAdmin) {

    String result = text;
    if (Objects.nonNull(result) && !isAdmin) {
      ExpressionParser parser = new SpelExpressionParser();
      StandardEvaluationContext context = new StandardEvaluationContext();
      if (Objects.nonNull(variables)) {
        context.setVariables(variables);
      }
      //Apply filter
      for (SensitiveFilter filter : filters) {
        try {
          String value = parser.parseExpression(filter.expression).getValue(context, String.class);
          result = result.replaceAll(filter.regex, value);
        } catch (ParseException e) {
          LOGGER.warn("Cannot parse expression: {}", filter.expression, e);
        }
      }
    }
    return result;
  }

  public boolean isAdmin() {
    return super.isAdmin();
  }

  static class SensitiveFilter {
    private String regex;
    private String expression;

    public SensitiveFilter(String regex, String expression) {
      this.regex = regex;
      this.expression = expression;
    }

    public String getRegex() {
      return regex;
    }

    public String getExpression() {
      return expression;
    }
  }
}
