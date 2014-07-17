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
package org.spotter.shared.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A wrapper for a Dynamic Spotter Service response.
 * 
 * @author Alexander Wert
 * 
 * @param <T>
 *            payload type
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SpotterServiceResponse<T> {
	private T payload;
	private ResponseStatus status;
	private String errorMessage;

	/**
	 * Default constructor.
	 */
	public SpotterServiceResponse() {

	}

	/**
	 * Constructor.
	 * 
	 * @param payload
	 *            response payload
	 * @param status
	 *            response status
	 */
	public SpotterServiceResponse(T payload, ResponseStatus status) {
		this.payload = payload;
		this.status = status;
	}

	/**
	 * Constructor.
	 * 
	 * @param payload
	 *            response payload
	 * @param status
	 *            response status
	 * @param errorMessage
	 *            error message (for the case that a error occured)
	 */
	public SpotterServiceResponse(T payload, ResponseStatus status, String errorMessage) {
		super();
		this.payload = payload;
		this.status = status;
		this.errorMessage = errorMessage;
	}

	/**
	 * Returns the response payload.
	 * 
	 * @param type
	 *            payload type
	 * @return the payload type
	 * @param <S>
	 *            class of the payload type
	 */
	@SuppressWarnings("unchecked")
	public <S> S getPayload(Class<S> type) {
		try {
			return (S) payload;
		} catch (Exception e) {
			throw new RuntimeException("Invalid type of payload!: " + type.getCanonicalName());
		}
	}

	/**
	 * @return the payload
	 */
	public T getPayload() {
		return payload;
	}

	/**
	 * @param payload
	 *            the payload to set
	 */
	public void setPayload(T payload) {
		this.payload = payload;
	}

	/**
	 * @return the status
	 */
	public ResponseStatus getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(ResponseStatus status) {
		this.status = status;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage
	 *            the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
