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
package org.spotter.demo.app.problems;

/**
 * Represents the One Lane Bridge problem.
 * 
 * @author C5170547
 * 
 */
public final class OLB {
	private static final int TIME_TO_SLEEP = 100;
	private static final int FIB_NUMBER = 25;
	private static OLB instance;

	/**
	 * 
	 * @return singleton instance
	 */
	public static OLB getInstnace() {
		if (instance == null) {
			instance = new OLB();
		}
		return instance;
	}

	private OLB() {

	}

	/**
	 * Method leading to a One Lane Bridge.
	 */
	public synchronized void olbMethod() {
		try {
			Thread.sleep(TIME_TO_SLEEP);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method leading to a One Lane Bridge.
	 */
	public synchronized void olbMethodFibonacci() {

		fibonacci(FIB_NUMBER);
	}

	/**
	 * 
	 * @param n fib parameter
	 * @return fib(n)
	 */
	public int fibonacci(int n) {
		if (n <= 1) {
			return 1;
		} else {
			return fibonacci(n - 2) + fibonacci(n - 1);
		}
	}
}
