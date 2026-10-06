package org.ohdsi.webapi.arachne.datasource.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BigQueryUtils {

	private static final String BQ_KEYPATH_REGEX = "(OAuthPvtKeyPath=)(.+?)[;$]";

	public static String replaceBigQueryKeyPath(String connectionString, String replacement) {

		String cs = Objects.isNull(getBigQueryKeyPath(connectionString)) ? connectionString + ";OAuthPvtKeyPath=keypath;"
						: connectionString;
		return cs.replaceFirst(BQ_KEYPATH_REGEX,
						"$1" + Matcher.quoteReplacement(replacement) + ";");
	}

	public static String getBigQueryKeyPath(String connectionString) {

		Matcher matcher = Pattern.compile(".*(OAuthPvtKeyPath=)(.+?)[;$].*").matcher(connectionString);
		return matcher.matches() && matcher.groupCount() > 1 ? matcher.group(2) : null;
	}
}
