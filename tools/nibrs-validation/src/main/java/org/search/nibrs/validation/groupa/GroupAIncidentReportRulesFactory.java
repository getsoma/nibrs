package org.search.nibrs.validation.groupa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.OffenseSegment;
import org.search.nibrs.model.codes.CargoTheftIndicatorCode;
import org.search.nibrs.model.codes.ClearedExceptionallyCode;
import org.search.nibrs.model.codes.NIBRSErrorCode;
import org.search.nibrs.model.codes.OffenseCode;
import org.search.nibrs.validation.rules.NotBlankRule;
import org.search.nibrs.validation.rules.NumericValueRule;
import org.search.nibrs.validation.rules.Rule;
import org.search.nibrs.validation.rules.StringValueRule;
import org.search.nibrs.validation.rules.ValidValueListRule;

/**
 * Factory class that provides Rule implementations to validate the elements contained on the Group A report administrative segment.
 */
public class GroupAIncidentReportRulesFactory {
	
	private List<Rule<GroupAIncidentReport>> rulesList = new ArrayList<>();
	private Set<String> cargoTheftOffenses = new HashSet<>();
	
	public GroupAIncidentReportRulesFactory() {
		
		cargoTheftOffenses.add(OffenseCode._120.code);
		cargoTheftOffenses.add(OffenseCode._210.code);
		cargoTheftOffenses.add(OffenseCode._220.code);
		cargoTheftOffenses.add(OffenseCode._23D.code);
		cargoTheftOffenses.add(OffenseCode._23F.code);
		cargoTheftOffenses.add(OffenseCode._23H.code);
		cargoTheftOffenses.add(OffenseCode._240.code);
		cargoTheftOffenses.add(OffenseCode._26A.code);
		cargoTheftOffenses.add(OffenseCode._26B.code);
		cargoTheftOffenses.add(OffenseCode._26C.code);
		cargoTheftOffenses.add(OffenseCode._26E.code);
		cargoTheftOffenses.add(OffenseCode._510.code);
		cargoTheftOffenses.add(OffenseCode._270.code);
		
		rulesList.add(getRule101("ori", "1"));
		rulesList.add(getRule101("incidentNumber", "2"));
		rulesList.add(getRule101("yearOfTape", "Year of Tape"));
		rulesList.add(getRule101("monthOfTape", "Month of Tape"));
		rulesList.add(getRule101("incidentDate", "3"));
		rulesList.add(getRule101("exceptionalClearanceCode", "4"));
		rulesList.add(getRule104("reportDateIndicator"));
		rulesList.add(getRule104("yearOfTape"));
		rulesList.add(getRule104("monthOfTape"));
		rulesList.add(getRule104("cargoTheftIndicator"));
		rulesList.add(getRule115());
		rulesList.add(getRule117());
		rulesList.add(getRule119());
		rulesList.add(getRule152());
	}
	
	Rule<GroupAIncidentReport> getRule119() {
		
		Rule<GroupAIncidentReport> ret = new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				List<OffenseSegment> offenses = subject.getOffenses();
				boolean cargoTheftIncident = false;
				for (OffenseSegment o : offenses) {
					if (cargoTheftOffenses.contains(o.getUcrOffenseCode())) {
						cargoTheftIncident = true;
						break;
					}
				}
				NIBRSError ret = null;
				if (cargoTheftIncident && subject.getCargoTheftIndicator() == null) {
					ret = subject.getErrorTemplate();
					ret.setValue(null);
					ret.setDataElementIdentifier("2A");
					ret.setNIBRSErrorCode(NIBRSErrorCode._119);
				}
				return ret;
			}
		};
		
		return ret;
		
	}
	
	Rule<GroupAIncidentReport> getRule117() {
		Pattern p = getRule117Regex();
		Rule<GroupAIncidentReport> rule117 = new StringValueRule<>(
				subject -> {
					return subject.getIncidentNumber();
				},
				(value, target) -> {
					NIBRSError ret = null;
					if (value != null && !p.matcher(value).matches()) {
						ret = target.getErrorTemplate();
						ret.setNIBRSErrorCode(NIBRSErrorCode._117);
						ret.setDataElementIdentifier("2");
						ret.setValue(value);
					}
					return ret;
				});
		return rule117;
	}


	Rule<GroupAIncidentReport> getRule104(String propertyName) {
		Rule<GroupAIncidentReport> ret = null;
		if ("reportDateIndicator".equals(propertyName)) {
			ret = new StringValueRule<>(
					subject -> {
						return subject.getReportDateIndicator();
					},
					(value, target) -> {
						NIBRSError e = null;
						if (value != null && (!value.equals("R"))) {
							e = target.getErrorTemplate();
							e.setNIBRSErrorCode(NIBRSErrorCode._104);
							e.setDataElementIdentifier("3");
							e.setValue(value);
						}
						return e;
					});
		} else if ("yearOfTape".equals(propertyName)) {
			ret = new NumericValueRule<>(
					subject -> {
						return subject.getYearOfTape();
					},
					(value, target) -> {
						NIBRSError e = null;
						if (value != null && 1991 > value.intValue()) {
							e = target.getErrorTemplate();
							e.setNIBRSErrorCode(NIBRSErrorCode._104);
							e.setDataElementIdentifier("Year of Tape");
							e.setValue(value);
						}
						return e;
					});
		} else if ("monthOfTape".equals(propertyName)) {
			ret = new NumericValueRule<>(
					subject -> {
						return subject.getMonthOfTape();
					},
					(value, target) -> {
						NIBRSError e = null;
						if (value != null && (1 > value.intValue() || 12 < value.intValue())) {
							e = target.getErrorTemplate();
							e.setNIBRSErrorCode(NIBRSErrorCode._104);
							e.setDataElementIdentifier("Month of Tape");
							e.setValue(value);
						}
						return e;
					});
		} else if ("cargoTheftIndicator".equals(propertyName)) {
			ret = new ValidValueListRule<GroupAIncidentReport>(propertyName, "2A", GroupAIncidentReport.class, NIBRSErrorCode._104, CargoTheftIndicatorCode.codeSet(), false) {
				protected boolean ignore(GroupAIncidentReport r) {
					return !r.includesCargoTheft();
				}
			};
		}
		return ret;
	}
	
	Rule<GroupAIncidentReport> getRule152() {
		return new NumericValueRule<>(subject -> {
			return subject.getIncidentHour();
		} , (value, target) -> {
			NIBRSError e = null;
			if (value != null && (0 > value.intValue() || 23 < value.intValue())) {
				e = target.getErrorTemplate();
				e.setNIBRSErrorCode(NIBRSErrorCode._152);
				e.setDataElementIdentifier("3");
				e.setValue(value);
			}
			return e;
		});
	}
	
	Rule<GroupAIncidentReport> getRule101(String propertyName, String dataElementIdentifier) {
		if ("exceptionalClearanceCode".equals(propertyName)) {
			return new ValidValueListRule<GroupAIncidentReport>(propertyName, dataElementIdentifier, GroupAIncidentReport.class,
					NIBRSErrorCode._101, ClearedExceptionallyCode.codeSet(), false);
		}
		return new NotBlankRule<>(propertyName, dataElementIdentifier, GroupAIncidentReport.class, NIBRSErrorCode._101);
	}

	Rule<GroupAIncidentReport> getRule115() {
		Pattern p = getRule115Regex();
		Rule<GroupAIncidentReport> rule115 = new StringValueRule<>(
				subject -> {
					return subject.getIncidentNumber();
				},
				(value, target) -> {
					NIBRSError ret = null;
					if (value != null && (value.length() != 12 || !p.matcher(value).matches())) {
						ret = target.getErrorTemplate();
						ret.setNIBRSErrorCode(NIBRSErrorCode._115);
						ret.setDataElementIdentifier("2");
						ret.setValue(value);
					}
					return ret;
				});
		return rule115;
	}

	static Pattern getRule115Regex() {
		return Pattern.compile("^[^ ]+[ ]*$");
	}
	
	static Pattern getRule117Regex() {
		return Pattern.compile("[A-Z0-9\\-]+");
	}

	/**
	 * Get the list of rules for the administrative segment.
	 * @return the list of rules
	 */
	public List<Rule<GroupAIncidentReport>> getRulesList() {
		return Collections.unmodifiableList(rulesList);
	}

}