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
package org.spotter.shared.environment.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Configuration.
 * 
 * @author Alexander Wert
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Configuration")
public class XMConfiguration implements Serializable, Comparable<XMConfiguration> {

	private static final long serialVersionUID = 2095252249149161297L;
	@XmlAttribute(name = "key", required = true)
	private String key;
	@XmlAttribute(name = "value", required = true)
	private String value;

	/**
	 * 
	 * @return key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * sets the key.
	 * 
	 * @param key
	 *            key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * 
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * sets the value.
	 * 
	 * @param value
	 *            value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int compareTo(XMConfiguration other) {
		final int BEFORE = -1;
	    final int AFTER = 1;
		
		if (this.getKey().compareTo(other.getKey()) < 0) {

			return BEFORE;
			
		} else if (this.getKey().compareTo(other.getKey()) > 0) {
			
			return AFTER;
			
		} else {
			
			return this.getValue().compareTo(other.getValue());
			
		}
	}
}
