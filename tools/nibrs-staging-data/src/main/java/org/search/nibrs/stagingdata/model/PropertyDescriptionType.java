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

import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
@Cacheable
public class PropertyDescriptionType {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer propertyDescriptionTypeId; 
	
	private String propertyDescriptionCode; 
	private String propertyDescriptionDescription; 
	
	@OneToMany(mappedBy = "propertyDescriptionType")
	private Set<PropertyType> propertyTypes;

	
	public PropertyDescriptionType() {
		super();
	}

	public PropertyDescriptionType(Integer propertyDescriptionTypeId) {
		super();
		this.propertyDescriptionTypeId = propertyDescriptionTypeId;
	}

	public String toString(){
		ReflectionToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
        return ReflectionToStringBuilder.toStringExclude(this, "propertyTypes");		
	}

	public Integer getPropertyDescriptionTypeId() {
		return propertyDescriptionTypeId;
	}

	public void setPropertyDescriptionTypeId(Integer propertyDescriptionTypeId) {
		this.propertyDescriptionTypeId = propertyDescriptionTypeId;
	}

	public String getPropertyDescriptionCode() {
		return propertyDescriptionCode;
	}

	public void setPropertyDescriptionCode(String propertyDescriptionCode) {
		this.propertyDescriptionCode = propertyDescriptionCode;
	}

	public String getPropertyDescriptionDescription() {
		return propertyDescriptionDescription;
	}

	public void setPropertyDescriptionDescription(String propertyDescriptionDescription) {
		this.propertyDescriptionDescription = propertyDescriptionDescription;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((propertyDescriptionCode == null) ? 0 : propertyDescriptionCode.hashCode());
		result = prime * result
				+ ((propertyDescriptionDescription == null) ? 0 : propertyDescriptionDescription.hashCode());
		result = prime * result + ((propertyDescriptionTypeId == null) ? 0 : propertyDescriptionTypeId.hashCode());
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
		PropertyDescriptionType other = (PropertyDescriptionType) obj;
		if (propertyDescriptionCode == null) {
			if (other.propertyDescriptionCode != null)
				return false;
		} else if (!propertyDescriptionCode.equals(other.propertyDescriptionCode))
			return false;
		if (propertyDescriptionDescription == null) {
			if (other.propertyDescriptionDescription != null)
				return false;
		} else if (!propertyDescriptionDescription.equals(other.propertyDescriptionDescription))
			return false;
		if (propertyDescriptionTypeId == null) {
			if (other.propertyDescriptionTypeId != null)
				return false;
		} else if (!propertyDescriptionTypeId.equals(other.propertyDescriptionTypeId))
			return false;
		return true;
	}

}
