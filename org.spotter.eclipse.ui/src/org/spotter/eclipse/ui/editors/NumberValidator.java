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
package org.spotter.eclipse.ui.editors;

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.lpe.common.util.LpeSupportedTypes;

/**
 * A cell editor validator for numbers.
 */
public class NumberValidator implements ICellEditorValidator {

	private final LpeSupportedTypes type;
	private final String lowerBound;
	private final String upperBound;
	private String errorMsg;
	private final String errorMsgBoundAppend;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param type
	 *            the number type
	 * @param lowerBound
	 *            lower bound for the number or <code>null</code>
	 * @param upperBound
	 *            upper bound for the number or <code>null</code>
	 */
	public NumberValidator(LpeSupportedTypes type, String lowerBound, String upperBound) {
		this.type = type;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		if (lowerBound != null) {
			if (upperBound != null) {
				errorMsgBoundAppend = " Must be a number between " + lowerBound + " and " + upperBound + ".";
			} else {
				errorMsgBoundAppend = " Must be a number greater than or equal " + lowerBound + ".";
			}
		} else if (upperBound != null) {
			errorMsgBoundAppend = " Must be a number smaller than or equal " + upperBound + ".";
		} else {
			errorMsgBoundAppend = null;
		}
	}

	@Override
	public String isValid(Object value) {
		errorMsg = "Not a valid " + type.toString() + ".";
		try {
			String valString = (String) value;
			if (valString.isEmpty()) {
				errorMsg = null;
			} else {
				switch (type) {
				case Integer:
					processAsInteger(valString);
					break;
				case Long:
					processAsLong(valString);
					break;
				case Float:
					processAsFloat(valString);
					break;
				case Double:
					processAsDouble(valString);
					break;
				default:
					errorMsg = "Type '" + type + "' not supported yet.";
					break;
				}
			}
		} catch (Exception e) {
		}
		if (errorMsg != null && errorMsgBoundAppend != null) {
			errorMsg = errorMsg + errorMsgBoundAppend;
		}
		return errorMsg;
	}

	private boolean testBoundsEqualNull() {
		if (lowerBound == null && upperBound == null) {
			errorMsg = null;
			return true;
		}
		return false;
	}

	private boolean testNoPrecedingZeroes(boolean equalsZero, String valString) {
		if (equalsZero && !valString.startsWith("00")) {
			return true;
		} else if (!equalsZero && !valString.startsWith("0")) {
			return true;
		}

		return false;
	}

	private void processAsInteger(String valString) {
		int num = Integer.parseInt(valString);
		if (!testNoPrecedingZeroes(num == 0, valString) || testBoundsEqualNull()) {
			return;
		}
		int lowInt = Integer.parseInt(lowerBound);
		int upInt = Integer.parseInt(upperBound);
		if (num >= lowInt && num <= upInt) {
			errorMsg = null;
		}
	}

	private void processAsLong(String valString) {
		long num = Long.parseLong(valString);
		if (!testNoPrecedingZeroes(num == 0, valString) || testBoundsEqualNull()) {
			return;
		}
		long lowLong = Long.parseLong(lowerBound);
		long upLong = Long.parseLong(upperBound);
		if (num >= lowLong && num <= upLong) {
			errorMsg = null;
		}
	}

	private void processAsFloat(String valString) {
		float num = Float.parseFloat(valString);
		if (!testNoPrecedingZeroes((int) num == 0, valString) || testBoundsEqualNull()) {
			return;
		}
		float lowLong = Float.parseFloat(lowerBound);
		float upLong = Float.parseFloat(upperBound);
		if (num >= lowLong && num <= upLong) {
			errorMsg = null;
		}
	}

	private void processAsDouble(String valString) {
		double num = Double.parseDouble(valString);
		if (!testNoPrecedingZeroes((int) num == 0, valString) || testBoundsEqualNull()) {
			return;
		}
		double lowLong = Double.parseDouble(lowerBound);
		double upLong = Double.parseDouble(upperBound);
		if (num >= lowLong && num <= upLong) {
			errorMsg = null;
		}
	}

}