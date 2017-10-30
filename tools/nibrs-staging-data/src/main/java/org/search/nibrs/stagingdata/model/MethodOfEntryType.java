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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
public class MethodOfEntryType {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer methodOfEntryTypeId; 
	
	private String methodOfEntryCode; 
	private String methodOfEntryDescription; 
	
	public MethodOfEntryType() {
		super();
	}

	public MethodOfEntryType(Integer methodOfEntryTypeId, String methodOfEntryCode,
			String methodOfEntryDescription) {
		super();
		this.methodOfEntryTypeId = methodOfEntryTypeId;
		this.methodOfEntryCode = methodOfEntryCode;
		this.methodOfEntryDescription = methodOfEntryDescription;
	}

	public String toString(){
		return ToStringBuilder.reflectionToString(this);
	}

	public Integer getMethodOfEntryTypeId() {
		return methodOfEntryTypeId;
	}

	public void setMethodOfEntryTypeId(Integer methodOfEntryTypeId) {
		this.methodOfEntryTypeId = methodOfEntryTypeId;
	}

	public String getMethodOfEntryCode() {
		return methodOfEntryCode;
	}

	public void setMethodOfEntryCode(String methodOfEntryCode) {
		this.methodOfEntryCode = methodOfEntryCode;
	}

	public String getMethodOfEntryDescription() {
		return methodOfEntryDescription;
	}

	public void setMethodOfEntryDescription(String methodOfEntryDescription) {
		this.methodOfEntryDescription = methodOfEntryDescription;
	}

}