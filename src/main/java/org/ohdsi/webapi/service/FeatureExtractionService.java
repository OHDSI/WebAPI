/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.springframework.stereotype.Component;
import org.ohdsi.featureExtraction.FeatureExtraction;

/**
 *
 * @author asena5
 * @author alondhe2
 */
@Path("/featureextraction/")
@Component
public class FeatureExtractionService extends AbstractDaoService {
	/**
	 * Get default feature extraction settings
	 * @param temporal Use temporal covariate settings? true or false (default)
	 * @return JSON with default covariate settings object
	 */
	@GET
	@Path("defaultcovariatesettings")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDefaultCovariateSettings(@QueryParam("temporal") final String temporal) {
		boolean getTemporal = false;
		try {
			if (temporal != null && !temporal.isEmpty()) {
				getTemporal = Boolean.parseBoolean(temporal);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("The parameter temporal must be a string of true or false.");
		}

		FeatureExtraction.init(null);
		String settings = "";
		if (getTemporal) {
			settings = FeatureExtraction.getDefaultPrespecTemporalAnalyses();
		} else {
			settings = FeatureExtraction.getDefaultPrespecAnalyses();
		}

		return settings;
	}
}
