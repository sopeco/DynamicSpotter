/**
 * Copyright 2014 SAP AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spotter.detection.stifle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.aim.api.instrumentation.description.InstrumentationDescription;
import org.aim.api.instrumentation.description.InstrumentationDescriptionBuilder;
import org.aim.api.measurement.dataset.Dataset;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.aim.api.measurement.dataset.ParameterSelection;
import org.aim.api.measurement.utils.MeasurementDataUtils;
import org.aim.artifacts.probes.ResponsetimeProbe;
import org.aim.artifacts.probes.SQLQueryProbe;
import org.aim.artifacts.records.ResponseTimeRecord;
import org.aim.artifacts.records.SQLQueryRecord;
import org.aim.artifacts.scopes.JDBCScope;
import org.lpe.common.extension.IExtension;
import org.lpe.common.util.LpeNumericUtils;
import org.lpe.common.util.LpeStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.core.detection.AbstractDetectionController;
import org.spotter.core.detection.IDetectionController;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.result.model.SpotterResult;

public class StifleDetectionController extends AbstractDetectionController {

	private static final Logger LOGGER = LoggerFactory.getLogger(StifleDetectionController.class);
	
	private static final int NUM_EXPERIMENTS = 1;

	public StifleDetectionController(IExtension<IDetectionController> provider) {
		super(provider);
	}

	public void loadProperties() {
	}

	@Override
	protected void executeExperiments() throws InstrumentationException, MeasurementException, WorkloadException {
		instrumentApplication(getInstrumentationDescription());
		runExperiment(StifleDetectionController.class, 1);
		uninstrumentApplication();
	}

	/**
	 * A stifle antipattern can be detected with instrumenting the following:
	 * <ul>
	 * 		<li>servlet response time probe</li>
	 * 		<li>JDBC API queries</li>
	 * </ul>
	 * 
	 * @return	the build {@link InstrumentationDescription}
	 * @throws 	InstrumentationException
	 */
	private InstrumentationDescription getInstrumentationDescription() throws InstrumentationException {

		InstrumentationDescriptionBuilder idBuilder = new InstrumentationDescriptionBuilder();
		
		//return idBuilder.addAPIInstrumentation(ServletScope.class).addProbe(ResponsetimeProbe.class).entityDone()
		//		.addAPIInstrumentation(JDBCScope.class).addProbe(SQLQueryProbe.class).entityDone().build();
		
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.account.LoginController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.account.ChangePasswordController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.account.ManageCustomerAddressesController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.account.ManageWishlistController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.account.OrderHistoryController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.account.RedirectController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.account.RegisterController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.account.UpdateAccountController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.cart.CartController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.catalog.CategoryController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.catalog.ProductController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.catalog.RatingsController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.catalog.SearchController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.catalog.ContactUsController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.checkout.BillingInfoController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.checkout.CheckoutController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.checkout.NullGiftCardController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.checkout.OrderConfirmationController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.checkout.ShippingInfoController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.contactus.ContactUsController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.content.PageController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.seo.RobotsController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.seo.SiteMapController.*").addProbe(ResponsetimeProbe.class).entityDone();
		idBuilder.addMethodInstrumentation().addMethod("com.mycompany.controller.checkout.SendOrderConfirmationEmailActivity.*").addProbe(ResponsetimeProbe.class).entityDone();
		
		idBuilder.addAPIInstrumentation(JDBCScope.class).addProbe(SQLQueryProbe.class).entityDone();
		
		return idBuilder.build();
	}

	@Override
	protected SpotterResult analyze(DatasetCollection data) {

		LOGGER.info("Fetching datasets.");
		
		Dataset sqlDataset = data.getDataSet(SQLQueryRecord.class);
		Dataset rtDataset = data.getDataSet(ResponseTimeRecord.class);

		LOGGER.info("Converting SQL dataset.");
		
		List<SQLQueryRecord> sqlRecords = sqlDataset.getRecords(SQLQueryRecord.class);
		MeasurementDataUtils.sortRecordsAscending(sqlRecords, SQLQueryRecord.PAR_TIMESTAMP);

		LOGGER.info("Converting RT dataset.");
		
		List<ResponseTimeRecord> rtRecords = removeRTDataSetExclusions(rtDataset).getRecords(ResponseTimeRecord.class);
		MeasurementDataUtils.sortRecordsAscending(rtRecords, ResponseTimeRecord.PAR_TIMESTAMP);

		LOGGER.info("Analyzing datasets.");
		
		Map<String, List<StifleQuery>> stifleQueries = analyzeDatasets(rtRecords, sqlRecords);

		LOGGER.info("Creating results.");
		
		return createSpotterResults(stifleQueries);
	}

	/**
	 * This method analyzes given datasets for a stifle antipattern.
	 * 
	 * @param rtRecords		the dataset with {@link ResponseTimeRecord}s
	 * @param sqlRecords	the dataset with {@link SQLQueryRecord}s
	 * @return				the stiflequeries found, the keys are the operations
	 */
	private Map<String, List<StifleQuery>> analyzeDatasets(List<ResponseTimeRecord> rtRecords, List<SQLQueryRecord> sqlRecords) {
		
		if (rtRecords.size() < 2) {
			LOGGER.info("Less than two response time samples. We have too few data to do an analysis: Skipping stifle analyzing.");
			return new HashMap<String, List<StifleQuery>>(); 
		}

		Map<String, List<StifleQuery>> 	stifleQueries 		= new HashMap<>();
		
		// in this loop we will always be one index ahead of the element we currently analyze
		for (int RTindex = 1, SQLIndex = 0; RTindex < rtRecords.size(); RTindex++) {
			
			ResponseTimeRecord currentRtRecord	= rtRecords.get(RTindex-1);
			ResponseTimeRecord nextRtRecord		= rtRecords.get(RTindex);
			
			// the timespace is too inaccurate, hence we use the callId
			long currentRTCallId 	= currentRtRecord.getCallId();
			long nextRTCallId 		= nextRtRecord.getCallId();
			
			// add the operation to the stifleQueries if it is not already in the HashMap
			String operation = currentRtRecord.getOperation();
			if (!stifleQueries.containsKey(operation)) {
				stifleQueries.put(operation, new ArrayList<StifleQuery>());
			}

			// we skip the first SQL queries, which are not related to the first RT record
			while (SQLIndex < sqlRecords.size() && sqlRecords.get(SQLIndex).getCallId() < currentRTCallId) {
				SQLIndex++;
				
				if (SQLIndex >= sqlRecords.size()) {
					return stifleQueries;
				}
			}

			HashSet<String> queryOcurrences = new HashSet<>();
			String prevQuery 				= "";
			long prevSQLCallId 				= -1;
			
			while (SQLIndex < sqlRecords.size() && sqlRecords.get(SQLIndex).getCallId() <= nextRTCallId) {
				
				String query = sqlRecords.get(SQLIndex).getQueryString();
				
				if (query.equals(prevQuery) && sqlRecords.get(SQLIndex).getCallId() == prevSQLCallId + 1) {
					
					prevQuery 	  = query;
					prevSQLCallId = sqlRecords.get(SQLIndex).getCallId();
					SQLIndex++;
					
					continue;
					
				} else {
					
					prevQuery 	  = query;
					prevSQLCallId = sqlRecords.get(SQLIndex).getCallId();
					
				}
				
				boolean found 				= false;
				StifleQuery maxScoreQuery 	= null;
				
				for (StifleQuery stifleQuery : stifleQueries.get(operation)) {

					if (LpeStringUtils.areEqualSql(stifleQuery.getQuery(), query)) {

						found = true;
						maxScoreQuery = stifleQuery;
						break;
						
					}
					
				}

				if (found) {
					
					if (queryOcurrences.contains(maxScoreQuery.getQuery())) {
						
						maxScoreQuery.increaseOccurence();
						
					} else {
						
						maxScoreQuery.getOccurrences().offerFirst(1);
						queryOcurrences.add(maxScoreQuery.getQuery());
						
					}
					
				} else {
					
					StifleQuery newStifleQuery = new StifleQuery(query);
					newStifleQuery.getOccurrences().offerFirst(1);
					stifleQueries.get(operation).add(newStifleQuery);
					queryOcurrences.add(query);
					
				}
				
				SQLIndex++;

			}
			
			if (RTindex % 50 == 0) {
				LOGGER.info("Analyzed {}/{} operations for Stifle.", RTindex, rtRecords.size());
			}

		}
		
		return stifleQueries;
	}

	/**
	 * Selects only the {@link ResponseTimeRecord}s which are not excluded explicity.
	 * 
	 * @param rtDataset	the RT dataset to apply the exclusions to
	 * @return			the RT dataset without the exclusions
	 */
	private Dataset removeRTDataSetExclusions(Dataset rtDataset) {
		// why are the eclusions here?
		return ParameterSelection.newSelection()
						  .unequal(ResponseTimeRecord.PAR_PROCESS_ID, "8484@QKAD00220964A")
						  .unequal(ResponseTimeRecord.PAR_OPERATION, "javax.servlet.http.HttpServlet.service(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)")
						  .unequal(ResponseTimeRecord.PAR_OPERATION, "vuser_init_Transaction")
						  .unequal(ResponseTimeRecord.PAR_OPERATION, "home")
						  .unequal(ResponseTimeRecord.PAR_OPERATION, "browseCategory")
						  .unequal(ResponseTimeRecord.PAR_OPERATION, "searchProduct")
						  .unequal(ResponseTimeRecord.PAR_OPERATION, "searchSubject")
						  .unequal(ResponseTimeRecord.PAR_OPERATION, "viewProductDetails")
						  .unequal(ResponseTimeRecord.PAR_OPERATION, "addToBasket")
						  .unequal(ResponseTimeRecord.PAR_OPERATION, "checkout")
						  .unequal(ResponseTimeRecord.PAR_OPERATION, "checkout")
						  .unequal(ResponseTimeRecord.PAR_OPERATION, "login")
						  .unequal(ResponseTimeRecord.PAR_OPERATION, "purchase")
						  .unequal(ResponseTimeRecord.PAR_OPERATION, "Action_Transaction")
						  .unequal(ResponseTimeRecord.PAR_OPERATION, "javax.servlet.http.HttpServlet.service(javax.servlet.ServletRequest,javax.servlet.ServletResponse)")
					  	  .applyTo(rtDataset);
	}

	/**
	 * Generates the stifle result output in a {@link SpotterResult} object.
	 * 
	 * @param stifleQueries	the found stifle queries
	 * @return				the {@link SpotterResult} build from the given queries
	 */
	private SpotterResult createSpotterResults(Map<String, List<StifleQuery>> stifleQueries) {
		
		SpotterResult result = new SpotterResult();
		result.setDetected(false);
		
		for (String operation : stifleQueries.keySet()) {
			
			boolean operationProblematic = false;
			
			for (StifleQuery stifleQuery : stifleQueries.get(operation)) {
				
				int min = LpeNumericUtils.min(stifleQuery.getOccurrences());
				int max = LpeNumericUtils.max(stifleQuery.getOccurrences());
				
				if (max > 1) {
					
					if (!operationProblematic) {
						result.addMessage("");
						result.addMessage("");
						result.addMessage("***************************************************************");
						result.addMessage("Transaction containing a potential stifle antipattern detected:");

						result.addMessage("-->" + operation);
						operationProblematic = true;
					}
					
					result.addMessage("The following query appears " + min + " to " + max + " times in a similar way:");
					result.addMessage("    " + stifleQuery.getQuery());
				}
				
			}

		}
		
		return result;
	}

	@Override
	protected int getNumOfExperiments() {
		return NUM_EXPERIMENTS;
	}

}
