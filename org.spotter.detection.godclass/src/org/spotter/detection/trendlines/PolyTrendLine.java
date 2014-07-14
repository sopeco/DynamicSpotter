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

public class PolyTrendLine extends OLSTrendLine {
	final int degree;

	public PolyTrendLine(int degree) {
		if (degree < 0)
			throw new IllegalArgumentException("The degree of the polynomial must not be negative");
		this.degree = degree;
	}

	protected double[] xVector(double x) { // {1, x, x*x, x*x*x, ...}
		double[] poly = new double[degree + 1];
		double xi = 1;
		for (int i = 0; i <= degree; i++) {
			poly[i] = xi;
			xi *= x;
		}
		return poly;
	}

	@Override
	protected boolean logY() {
		return false;
	}
}
