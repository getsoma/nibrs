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
package org.search.nibrs.stagingdata.repository.segment;

import javax.transaction.Transactional;

import org.search.nibrs.stagingdata.model.segment.AdministrativeSegment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.repository.CrudRepository;

@Transactional
public interface AdministrativeSegmentRepository 
	extends CrudRepository<AdministrativeSegment, Integer>{
	
	long deleteByIncidentNumber(String incidentNumber);
	
	@EntityGraph(value="allAdministrativeSegmentJoins", type=EntityGraphType.LOAD)
	AdministrativeSegment findByIncidentNumber(String incidentNumber);
	
	@EntityGraph(value="allAdministrativeSegmentJoins", type=EntityGraphType.LOAD)
	AdministrativeSegment findByAdministrativeSegmentId(Integer administrativeSegmentId);
	
	boolean existsByIncidentNumber(String incidentNumber);
}
