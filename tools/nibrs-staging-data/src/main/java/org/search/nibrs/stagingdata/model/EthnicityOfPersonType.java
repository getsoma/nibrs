/*
 * Copyright 2016 SEARCH-The National Consortium for Justice Information and Statistics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.search.nibrs.stagingdata.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Cacheable
public class EthnicityOfPersonType {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer ethnicityOfPersonTypeId; 
	
	private String ethnicityOfPersonCode; 
	
	public EthnicityOfPersonType() {
		super();
	}

	public EthnicityOfPersonType(Integer ethnicityOfPersonTypeId) {
		super();
		this.ethnicityOfPersonTypeId = ethnicityOfPersonTypeId;
	}

	public EthnicityOfPersonType(Integer ethnicityOfPersonTypeId, String ethnicityOfPersonCode, String raceOfPersonDescription) {
		this();
		this.setEthnicityOfPersonTypeId(ethnicityOfPersonTypeId);
		this.setEthnicityOfPersonCode(ethnicityOfPersonCode);
	}


	public String toString(){
		return ToStringBuilder.reflectionToString(this);
	}

	public String getEthnicityOfPersonCode() {
		return ethnicityOfPersonCode;
	}

	public void setEthnicityOfPersonCode(String ethnicityOfPersonCode) {
		this.ethnicityOfPersonCode = ethnicityOfPersonCode;
	}

	public Integer getEthnicityOfPersonTypeId() {
		return ethnicityOfPersonTypeId;
	}

	public void setEthnicityOfPersonTypeId(Integer ethnicityOfPersonTypeId) {
		this.ethnicityOfPersonTypeId = ethnicityOfPersonTypeId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ethnicityOfPersonCode == null) ? 0 : ethnicityOfPersonCode.hashCode());
		result = prime * result + ((ethnicityOfPersonTypeId == null) ? 0 : ethnicityOfPersonTypeId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EthnicityOfPersonType other = (EthnicityOfPersonType) obj;
		if (ethnicityOfPersonCode == null) {
			if (other.ethnicityOfPersonCode != null)
				return false;
		} else if (!ethnicityOfPersonCode.equals(other.ethnicityOfPersonCode))
			return false;
		if (ethnicityOfPersonTypeId == null) {
			if (other.ethnicityOfPersonTypeId != null)
				return false;
		} else if (!ethnicityOfPersonTypeId.equals(other.ethnicityOfPersonTypeId))
			return false;
		return true;
	}

}
