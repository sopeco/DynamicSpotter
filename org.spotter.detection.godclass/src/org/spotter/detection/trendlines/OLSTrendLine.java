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
package org.spotter.detection.trendlines;

import java.util.Arrays;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

public abstract class OLSTrendLine implements TrendLine {

	RealMatrix coef = null; // will hold prediction coefs once we get values

	protected abstract double[] xVector(double x); // create vector of values
													// from x

	protected abstract boolean logY(); // set true to predict log of y (note: y
										// must be positive)

	@Override
	public void setValues(double[] y, double[] x) {
		if (x.length != y.length) {
			throw new IllegalArgumentException(String.format("The numbers of y and x values must be equal (%d != %d)",
					y.length, x.length));
		}
		double[][] xData = new double[x.length][];
		for (int i = 0; i < x.length; i++) {
			// the implementation determines how to produce a vector of
			// predictors from a single x
			xData[i] = xVector(x[i]);
		}
		if (logY()) { // in some models we are predicting ln y, so we replace
						// each y with ln y
			y = Arrays.copyOf(y, y.length); // user might not be finished with
											// the array we were given
			for (int i = 0; i < x.length; i++) {
				y[i] = Math.log(y[i]);
			}
		}
		OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
		ols.setNoIntercept(true); // let the implementation include a constant
									// in xVector if desired
		ols.newSampleData(y, xData); // provide the data to the model
		coef = MatrixUtils.createColumnRealMatrix(ols.estimateRegressionParameters()); // get
																						// our
																						// coefs
	}

	@Override
	public double predict(double x) {
		double yhat = coef.preMultiply(xVector(x))[0]; // apply coefs to xVector
		if (logY()) {
			yhat = (Math.exp(yhat)); // if we predicted ln y, we still need to
		}
		// get y
		return yhat;
	}
}