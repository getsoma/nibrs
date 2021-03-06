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
public class ClearedExceptionallyType {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer clearedExceptionallyTypeId; 
	
	private String clearedExceptionallyCode; 
	private String clearedExceptionallyDescription; 
	
	public ClearedExceptionallyType() {
		super();
	}

	public ClearedExceptionallyType(Integer clearedExceptionallyTypeId) {
		super();
		this.clearedExceptionallyTypeId = clearedExceptionallyTypeId;
	}

	public ClearedExceptionallyType(Integer clearedExceptionallyTypeId, String clearedExceptionallyCode,
			String clearedExceptionallyDescription) {
		super();
		this.clearedExceptionallyTypeId = clearedExceptionallyTypeId;
		this.clearedExceptionallyCode = clearedExceptionallyCode;
		this.clearedExceptionallyDescription = clearedExceptionallyDescription;
	}

	public String toString(){
		return ToStringBuilder.reflectionToString(this);
	}


	public Integer getClearedExceptionallyTypeId() {
		return clearedExceptionallyTypeId;
	}

	public void setClearedExceptionallyTypeId(Integer clearedExceptionallyTypeId) {
		this.clearedExceptionallyTypeId = clearedExceptionallyTypeId;
	}

	public String getClearedExceptionallyCode() {
		return clearedExceptionallyCode;
	}

	public void setClearedExceptionallyCode(String clearedExceptionallyCode) {
		this.clearedExceptionallyCode = clearedExceptionallyCode;
	}

	public String getClearedExceptionallyDescription() {
		return clearedExceptionallyDescription;
	}

	public void setClearedExceptionallyDescription(String clearedExceptionallyDescription) {
		this.clearedExceptionallyDescription = clearedExceptionallyDescription;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clearedExceptionallyCode == null) ? 0 : clearedExceptionallyCode.hashCode());
		result = prime * result
				+ ((clearedExceptionallyDescription == null) ? 0 : clearedExceptionallyDescription.hashCode());
		result = prime * result + ((clearedExceptionallyTypeId == null) ? 0 : clearedExceptionallyTypeId.hashCode());
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
		ClearedExceptionallyType other = (ClearedExceptionallyType) obj;
		if (clearedExceptionallyCode == null) {
			if (other.clearedExceptionallyCode != null)
				return false;
		} else if (!clearedExceptionallyCode.equals(other.clearedExceptionallyCode))
			return false;
		if (clearedExceptionallyDescription == null) {
			if (other.clearedExceptionallyDescription != null)
				return false;
		} else if (!clearedExceptionallyDescription.equals(other.clearedExceptionallyDescription))
			return false;
		if (clearedExceptionallyTypeId == null) {
			if (other.clearedExceptionallyTypeId != null)
				return false;
		} else if (!clearedExceptionallyTypeId.equals(other.clearedExceptionallyTypeId))
			return false;
		return true;
	}

}
