/*******************************************************************************
 * Copyright 2016 Research Triangle Institute
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
 *******************************************************************************/
package org.search.nibrs.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.search.nibrs.model.GroupBArrestReport;

final class GroupBRuleViolationExemplarFactory {
	
	private static final GroupBRuleViolationExemplarFactory INSTANCE = new GroupBRuleViolationExemplarFactory();

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(GroupBRuleViolationExemplarFactory.class);

	private Map<Integer, Function<GroupBArrestReport, List<GroupBArrestReport>>> groupBTweakerMap;

	private GroupBRuleViolationExemplarFactory() {
		groupBTweakerMap = new HashMap<Integer, Function<GroupBArrestReport, List<GroupBArrestReport>>>();
		populateExemplarMap();
	}

	/**
	 * Get an instance of the factory.
	 * 
	 * @return the instance
	 */
	public static final GroupBRuleViolationExemplarFactory getInstance() {
		return INSTANCE;
	}

	Map<Integer, Function<GroupBArrestReport, List<GroupBArrestReport>>> getGroupBTweakerMap() {
		return groupBTweakerMap;
	}
	
	private void populateExemplarMap() {
		
		groupBTweakerMap.put(701, arrestReport -> {
			
			List<GroupBArrestReport> reports = new ArrayList<GroupBArrestReport>();
			
			GroupBArrestReport copy = new GroupBArrestReport(arrestReport);
			copy.setYearOfTape(null);
			reports.add(copy);
			copy = new GroupBArrestReport(arrestReport);
			copy.setMonthOfTape(null);
			reports.add(copy);
			copy = new GroupBArrestReport(arrestReport);
			copy.setOri(null);
			reports.add(copy);
			
			// TODO: fill out the list as desired...
			
			return reports;
			
		});
		
	}

}