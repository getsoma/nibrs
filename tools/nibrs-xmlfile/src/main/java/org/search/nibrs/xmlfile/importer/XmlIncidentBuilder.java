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
package org.search.nibrs.xmlfile.importer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.common.ReportSource;
import org.search.nibrs.importer.AbstractIncidentBuilder;
import org.search.nibrs.importer.ReportListener;
import org.search.nibrs.model.AbstractReport;
import org.search.nibrs.model.AbstractSegment;
import org.search.nibrs.model.ArresteeSegment;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.GroupBArrestReport;
import org.search.nibrs.model.NIBRSAge;
import org.search.nibrs.model.OffenderSegment;
import org.search.nibrs.model.OffenseSegment;
import org.search.nibrs.model.PropertySegment;
import org.search.nibrs.model.VictimSegment;
import org.search.nibrs.model.ZeroReport;
import org.search.nibrs.model.codes.NIBRSErrorCode;
import org.search.nibrs.xmlfile.util.NibrsStringUtils;
import org.search.nibrs.xmlfile.util.XmlUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/**
 * Builder class that constructs incidents from a stream of NIBRS report data.
 * Incidents are broadcast to listeners as events; this keeps the class as
 * memory-unintensive as possible (NIBRS report streams can be rather large).
 * <br/>
 * At some point, if other report elements than Incidents are desired, this will
 * need to be modified. Currently, it only broadcasts Incident "add" records.
 * 
 */
@Component
public class XmlIncidentBuilder extends AbstractIncidentBuilder{
	private static final Log log = LogFactory.getLog(XmlIncidentBuilder.class);;
	
	private DocumentBuilder documentBuilder; 
	private Map<String, String> victimToSubjectRelationshipCodeMap = new HashMap<>(); 

	public XmlIncidentBuilder() throws ParserConfigurationException {
		super();
		setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
		initDocumentBuilder();
		initVictimToSubjectRelationshipCodeMap();
	}

	private void initVictimToSubjectRelationshipCodeMap() {
		victimToSubjectRelationshipCodeMap.put("Accomplice", "OK");
		victimToSubjectRelationshipCodeMap.put("Acquaintance", "AQ");
		victimToSubjectRelationshipCodeMap.put("Authority Figure", "OK");
		victimToSubjectRelationshipCodeMap.put("Babysittee", "BE");
		victimToSubjectRelationshipCodeMap.put("Babysitter", "OK");
		victimToSubjectRelationshipCodeMap.put("Boyfriend", "BG");
		victimToSubjectRelationshipCodeMap.put("Caregiver", "OK");
		victimToSubjectRelationshipCodeMap.put("Child of Boyfriend_Girlfriend", "CF");
		victimToSubjectRelationshipCodeMap.put("Client", "OK");
		victimToSubjectRelationshipCodeMap.put("Cohabitant", "OK");
		victimToSubjectRelationshipCodeMap.put("Delivery Person", "OK");
		victimToSubjectRelationshipCodeMap.put("Employee", "EE");
		victimToSubjectRelationshipCodeMap.put("Employer", "ER");
		victimToSubjectRelationshipCodeMap.put("Ex_Spouse", "XS");
		victimToSubjectRelationshipCodeMap.put("Family Member", "OF");
		victimToSubjectRelationshipCodeMap.put("Family Member_Aunt", "OF");
		victimToSubjectRelationshipCodeMap.put("Family Member_Child", "CH");
		victimToSubjectRelationshipCodeMap.put("Family Member_Cousin", "OF");
		victimToSubjectRelationshipCodeMap.put("Family Member_Foster Child", "CH");
		victimToSubjectRelationshipCodeMap.put("Family Member_Foster Parent", "PA");
		victimToSubjectRelationshipCodeMap.put("Family Member_Grandchild", "GC");
		victimToSubjectRelationshipCodeMap.put("Family Member_Grandparent", "GP");
		victimToSubjectRelationshipCodeMap.put("Family Member_In-Law", "IL");
		victimToSubjectRelationshipCodeMap.put("Family Member_Nephew", "OF");
		victimToSubjectRelationshipCodeMap.put("Family Member_Niece", "OF");
		victimToSubjectRelationshipCodeMap.put("Family Member_Parent", "PA");
		victimToSubjectRelationshipCodeMap.put("Family Member_Sibling", "SB");
		victimToSubjectRelationshipCodeMap.put("Family Member_Spouse", "SE");
		victimToSubjectRelationshipCodeMap.put("Family Member_Spouse_Common Law", "CS");
		victimToSubjectRelationshipCodeMap.put("Family Member_Stepchild", "SC");
		victimToSubjectRelationshipCodeMap.put("Family Member_Stepparent", "SP");
		victimToSubjectRelationshipCodeMap.put("Family Member_Stepsibling", "SS");
		victimToSubjectRelationshipCodeMap.put("Family Member_Uncle", "OF");
		victimToSubjectRelationshipCodeMap.put("Former Employee", "OK");
		victimToSubjectRelationshipCodeMap.put("Former Employer", "OK");
		victimToSubjectRelationshipCodeMap.put("Friend", "FR");
		victimToSubjectRelationshipCodeMap.put("Girlfriend", "BG");
		victimToSubjectRelationshipCodeMap.put("Guardian", "OK");
		victimToSubjectRelationshipCodeMap.put("Homosexual relationship", "HR");
		victimToSubjectRelationshipCodeMap.put("Neighbor", "NE");
		victimToSubjectRelationshipCodeMap.put("NonFamily_Otherwise Known", "OK");
		victimToSubjectRelationshipCodeMap.put("Patient", "OK");
		victimToSubjectRelationshipCodeMap.put("Relationship Unknown", "RU");
		victimToSubjectRelationshipCodeMap.put("Stranger", "ST");
		victimToSubjectRelationshipCodeMap.put("Student", "OK");
		victimToSubjectRelationshipCodeMap.put("Teacher", "OK");
		victimToSubjectRelationshipCodeMap.put("Victim Was Offender", "VO");
	}

	private void initDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

		documentBuilderFactory.setValidating(false);
		documentBuilderFactory.setIgnoringComments(false);
		documentBuilderFactory.setIgnoringElementContentWhitespace(true);
		documentBuilderFactory.setNamespaceAware(true);

		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		documentBuilder.setEntityResolver(new NullResolver());
	}

	public void addIncidentListener(ReportListener listener) {
		getListeners().add(listener);
	}

	public void removeIncidentListener(ReportListener listener) {
		getListeners().remove(listener);
	}

	/**
	 * Read NIBRS incidents in the XML format from the input stream.
	 * @param reader the source of the data
	 * @throws Exception 
	 */
	public void buildIncidents(InputStream inputStream, String readerLocationName) {

		AbstractReport currentReport = null;
		
		log.info("Processing NIBRS XML file");
		
		List<NIBRSError> errorList = new ArrayList<NIBRSError>();
		
		Document document;
		
		try {
			document = documentBuilder.parse(inputStream);
			NodeList reportElements = (NodeList) XmlUtils.xPathNodeListSearch(document, "nibrs:Submission/nibrs:Report");
			
			for(int i=0; i < reportElements.getLength(); i++){
				ReportSource reportSource = new ReportSource();
				reportSource.setSourceLocation(String.valueOf(i+1));
				reportSource.setSourceName(readerLocationName);
				
				Element reportNode = (Element)reportElements.item(i);
				
				ReportBaseData reportBaseData = new ReportBaseData();
				List<NIBRSError> reportBaseDataErrors = reportBaseData.setData(reportSource, reportNode);
				errorList.addAll(reportBaseDataErrors);

				if (reportBaseDataErrors.isEmpty()){
					currentReport = buildReport(errorList, reportNode, readerLocationName, reportBaseData);
					errorList = new ArrayList<NIBRSError>();
					handleNewReport(currentReport, errorList);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		
		
		log.info("finished processing file");
		log.info("Encountered " + getLogListener().errorCount + " error(s).");
		log.info("Created " + getLogListener().reportCount + " incident(s).");

	}

	AbstractReport buildReport(List<NIBRSError> errorList, Element reportElement, String readerLocationName, ReportBaseData reportBaseData) throws Exception {
		int errorListSize = errorList.size();
		AbstractReport ret = null;
		
		String nibrsReportCategoryCode = XmlUtils.xPathStringSearch(reportElement, "nibrs:ReportHeader/nibrs:NIBRSReportCategoryCode"); 
		
		switch (nibrsReportCategoryCode){
		case "GROUP A INCIDENT REPORT":
			ret = buildGroupAIncidentReport(reportBaseData, errorList); 
			break; 
		case "GROUP B ARREST REPORT": 
			ret = builGroupBArrestReport(reportBaseData, errorList);
			break; 
		case "ZERO REPORT": 
			ret = buildZeroReport(reportBaseData, errorList); 
			break;
		}
		
		if (errorList.size() > errorListSize) {
			ret.setHasUpstreamErrors(true);
		}
		ret.setSource(reportBaseData.getReportSource());
		return ret;
	}

	private AbstractReport buildZeroReport(ReportBaseData reportBaseData, List<NIBRSError> errorList) throws Exception {
		ZeroReport ret = new ZeroReport();
		ret.setOri(reportBaseData.getOri());
		ret.setReportActionType(reportBaseData.getActionType());
		ret.setIncidentNumber(reportBaseData.getIncidentNumber());
		
		List<NIBRSError> newErrorList = getSubmissionYearMonth(reportBaseData, ret, NIBRSErrorCode._001);
		
		//TODO find out Zero Report Year and Zero report Month xPath.
		//TODO find out city indicator's xPath
		//ret.setCityIndicator(StringUtils.getStringBetween(13, 16, s.getData()));
			
		for (NIBRSError e : newErrorList) {
			e.setReport(ret);
		}
		
		errorList.addAll(newErrorList);
		
		return ret;
	}

	private List<NIBRSError> getSubmissionYearMonth(ReportBaseData reportBaseData, 
			AbstractReport ret, NIBRSErrorCode nibrsErrorCode) {
		List<NIBRSError> errorList = new ArrayList<>();
		String submissionDateString = ""; 
		
		try {
			
			submissionDateString = XmlUtils.xPathStringSearch(reportBaseData.getReportElement(), "nibrs:ReportHeader/nibrs:ReportDate/nc:YearMonthDate");
			if (StringUtils.isNotBlank(submissionDateString) && submissionDateString.length() == 7){
				YearMonth submissionDate = YearMonth.parse(submissionDateString);
				ret.setYearOfTape(submissionDate.getYear());
				ret.setMonthOfTape(submissionDate.getMonth().getValue());
			}
		}
		catch (DateTimeParseException e){
			log.info(e);
			NIBRSError nibrsError = new NIBRSError();
			nibrsError.setContext(reportBaseData.getReportSource());
			nibrsError.setReportUniqueIdentifier(reportBaseData.getIncidentNumber());
			nibrsError.setNIBRSErrorCode(nibrsErrorCode);
			nibrsError.setValue(submissionDateString);
			nibrsError.setSegmentType('0');
			errorList.add(nibrsError);
			log.debug("Error in DateTimeParse conversion: position=" + reportBaseData.getReportSource()
				+ ", xPath = 'nibrs:ReportHeader/nibrs:ReportDate/nc:YearMonthDate'"
				+ ", value=" + StringUtils.trimToEmpty(submissionDateString));

		}
		
		return errorList;
	}

	private AbstractReport builGroupBArrestReport(ReportBaseData reportBaseData, List<NIBRSError> errorList) {
		List<NIBRSError> newErrorList = new ArrayList<>();
		GroupBArrestReport ret = new GroupBArrestReport();
		ArresteeSegment arrestee = new ArresteeSegment(ArresteeSegment.GROUP_B_ARRESTEE_SEGMENT_TYPE_IDENTIFIER);
		
		Element reportElement = reportBaseData.getReportElement();
		ret.setOri(reportBaseData.getOri());
		ret.setReportActionType(reportBaseData.getActionType());
		
		newErrorList.addAll(getSubmissionYearMonth(reportBaseData,  ret, NIBRSErrorCode._701));

		//TODO find out cityIndicator's xPath.
//			ret.setCityIndicator(NibrsStringUtils.getStringBetween(13, 16, segmentData));
		
		ParsedObject<Integer> sequenceNumber = arrestee.getArresteeSequenceNumber();
		sequenceNumber.setMissing(false);
		sequenceNumber.setInvalid(false);
		String sequenceNumberString = XmlUtils.xPathStringSearch(reportElement, "j:Arrestee/j:ArrestSequenceID");
		if (sequenceNumberString == null) {
			sequenceNumber.setMissing(true);
			sequenceNumber.setValue(null);
		} else {
			getIntegerValue(newErrorList, sequenceNumber, sequenceNumberString, NIBRSErrorCode._701, "40", reportBaseData );
		}
			
		arrestee.setArresteeSequenceNumber(sequenceNumber);
		
		String arrestTransactionNumber = XmlUtils.xPathStringSearch(reportElement, "j:Arrest/nc:ActivityIdentification/nc:IdentificationID");
		arrestee.setArrestTransactionNumber(arrestTransactionNumber);

		
		ParsedObject<Date> arrestDate = arrestee.getArrestDate();
		arrestDate.setMissing(false);
		arrestDate.setInvalid(false);
		
		String arrestDateString = XmlUtils.xPathStringSearch(reportElement, "j:Arrest/nc:ActivityDate/nc:Date");
		if (arrestDateString == null) {
			arrestDate.setMissing(true);
			arrestDate.setValue(null);
		} else {
			try {
				Date d = getDateFormat().parse(arrestDateString);
				arrestDate.setValue(d);
			} catch (ParseException pe) {
				NIBRSError e = new NIBRSError();
				ReportSource reportSource = new ReportSource(reportBaseData.getReportSource()); 
				reportSource.setSourceLocation((String)XmlUtils.xPathStringSearch(reportElement, "j:Arrest/@s:id"));
				e.setContext(reportBaseData.getReportSource());
				e.setReportUniqueIdentifier(reportBaseData.getIncidentNumber());
				e.setSegmentType(reportBaseData.getSegmentType());
				e.setValue(arrestDateString);
				e.setNIBRSErrorCode(NIBRSErrorCode._705);
				e.setDataElementIdentifier("42");
				newErrorList.add(e);
				arrestDate.setInvalid(true);
				arrestDate.setValidationError(e);
			}
		}
		arrestee.setArrestDate(arrestDate);

		arrestee.setTypeOfArrest(XmlUtils.xPathStringSearch(reportElement, "j:Arrest/j:ArrestCategoryCode"));
		arrestee.setUcrArrestOffenseCode(XmlUtils.xPathStringSearch(reportElement, "j:Arrest/j:ArrestCharge/nibrs:ChargeUCRCode"));
		
		NodeList arresteeArmedWithElements = (NodeList) XmlUtils.xPathNodeListSearch(reportElement, "j:Arrestee/j:ArresteeArmedWithCode");
		
		for(int i=0; i < arresteeArmedWithElements.getLength() && i < 2; i++){
			Element arresteeArmedWithElement = (Element)arresteeArmedWithElements.item(i);
			arrestee.setArresteeArmedWith(i , arresteeArmedWithElement.getTextContent());
			
			//TODO did not find the Automatic weapon indicator  in the xml schema. 
//			arrestee.setAutomaticWeaponIndicator(i, NibrsStringUtils.getStringBetween(54 + 3 * i, 54 + 3 * i, segmentData));
		}

		Node personNode = XmlUtils.xPathNodeSearch(reportElement,  "nc:Person");

		NIBRSAge arresteeAge = parseAgeNode(personNode, arrestee);
		
		arrestee.setAge(arresteeAge);
		
		arrestee.setSex(XmlUtils.xPathStringSearch(reportElement, "nc:Person/j:PersonSexCode"));
		arrestee.setRace(XmlUtils.xPathStringSearch(reportElement, "nc:Person/j:PersonRaceNDExCode"));
		arrestee.setEthnicity(XmlUtils.xPathStringSearch(reportElement, "nc:Person/j:PersonEthnicityCode"));
		arrestee.setResidentStatus(XmlUtils.xPathStringSearch(reportElement, "nc:Person/j:PersonResidentCode"));
		arrestee.setDispositionOfArresteeUnder18(XmlUtils.xPathStringSearch(reportElement, "nc:Person/j:ArresteeJuvenileDispositionCode"));
		
		for (NIBRSError e : newErrorList) {
			e.setReport(ret);
		}
		
		ret.addArrestee(arrestee);
		errorList.addAll(newErrorList);
	
		return ret;
	}

	private NIBRSAge parseAgeNode(Node personNode, AbstractSegment segmentContext) {
		
		NIBRSAge ret = null;

		String ageString = XmlUtils.xPathStringSearch(personNode, "nc:PersonAgeMeasure/nc:MeasureIntegerValue|nc:PersonAgeMeasure/nc:MeasureValueText");
		ageString = StringUtils.leftPad(ageString, 2); 
		
		if (StringUtils.isBlank(ageString)){
			String ageMinString = XmlUtils.xPathStringSearch(personNode, "nc:PersonAgeMeasure/nc:MeasureIntegerRange/nc:RangeMinimumIntegerValue"); 
			String ageMaxString = XmlUtils.xPathStringSearch(personNode, "nc:PersonAgeMeasure/nc:MeasureIntegerRange/nc:RangeMaximumIntegerValue");
			ageString = StringUtils.join(StringUtils.leftPad(ageMinString, 2), StringUtils.leftPad(ageMaxString, 2)); 
		}
		
		if (!StringUtils.isBlank(ageString)) {

			ret = new NIBRSAge();

			switch (ageString) {
			case "NEONATAL":
				ret = NIBRSAge.getNeonateAge();
				break;
			case "NEWBORN":
				ret = NIBRSAge.getNewbornAge();
				break;
			case "BABY":
				ret = NIBRSAge.getBabyAge();
				break;
			case "UNKNOWN":
				ret = NIBRSAge.getUnknownAge();
				break;
			default:
				log.info(ageString);
				ret = NIBRSAgeBuilder.buildAgeFromRawString(ageString, segmentContext);
			}

		}
		
		return ret;
	}
	
	private void getIntegerValue(List<NIBRSError> errorList, ParsedObject<Integer> parsedObject,
			String stringValue, NIBRSErrorCode nibrsErrorCode,  String dataElementId, ReportBaseData reportBaseData) {
		try {
			Integer sequenceNumberI = Integer.parseInt(stringValue);
			parsedObject.setValue(sequenceNumberI);
		} catch (NumberFormatException nfe) {
			NIBRSError e = new NIBRSError();
			e.setContext(reportBaseData.getReportSource());
			e.setReportUniqueIdentifier(reportBaseData.getIncidentNumber());
			e.setSegmentType(reportBaseData.getSegmentType());
			e.setValue(stringValue);
			e.setNIBRSErrorCode(nibrsErrorCode);
			e.setDataElementIdentifier(dataElementId);
			errorList.add(e);
			parsedObject.setInvalid(true);
			parsedObject.setValidationError(e);
		}
	}

	private AbstractReport buildGroupAIncidentReport(ReportBaseData reportBaseData, List<NIBRSError> errorList) {
		List<NIBRSError> newErrorList = new ArrayList<>();
		GroupAIncidentReport newIncident = new GroupAIncidentReport();
		newIncident.setSource(reportBaseData.getReportSource());
		
		newIncident.setIncidentNumber(reportBaseData.getIncidentNumber());
		newIncident.setOri(reportBaseData.getOri());
		newIncident.setReportActionType(reportBaseData.getActionType());

		Element reportElement = reportBaseData.getReportElement();
		newErrorList.addAll(getSubmissionYearMonth(reportBaseData,  newIncident, NIBRSErrorCode._101));

		//TODO findout the xPath for the cityIndicator 
//			newIncident.setCityIndicator(NibrsStringUtils.getStringBetween(13, 16, segmentData));
		
		ParsedObject<Date> incidentDate = newIncident.getIncidentDate();
		incidentDate.setMissing(false);
		incidentDate.setInvalid(false);
		String incidentDateString = XmlUtils.xPathStringSearch(reportElement, "nc:Incident/nc:ActivityDate/nc:Date");
		String incidentDatetimeString = XmlUtils.xPathStringSearch(reportElement, "nc:Incident/nc:ActivityDate/nc:DateTime");
		if (StringUtils.isBlank(incidentDateString) && StringUtils.isBlank(incidentDatetimeString)) {
			incidentDate.setMissing(true);
			incidentDate.setValue(null);
		} else {
			try {
				
				if (StringUtils.isNotBlank(incidentDateString)){
					Date d = getDateFormat().parse(incidentDateString);
					incidentDate.setValue(d);
				}
				else {
					Date d = getDateFormat().parse(incidentDatetimeString.substring(0, 10));
					incidentDate.setValue(d);
				}
			} catch (ParseException pe) {
				NIBRSError e = new NIBRSError();
				e.setContext(reportBaseData.getReportSource());
				e.setReportUniqueIdentifier(reportBaseData.getIncidentNumber());
				e.setSegmentType(reportBaseData.getSegmentType());
				e.setValue(incidentDateString);
				e.setNIBRSErrorCode(NIBRSErrorCode._105);
				e.setDataElementIdentifier("3");
				newErrorList.add(e);
				incidentDate.setInvalid(true);
				incidentDate.setValidationError(e);
			}
		}
		newIncident.setIncidentDate(incidentDate);
			
		String reportDateIncidator = XmlUtils.xPathStringSearch(reportElement, "nc:Incident/cjis:IncidentAugmentation/cjis:IncidentReportDateIndicator");
		
		newIncident.setReportDateIndicator(BooleanUtils.toString(BooleanUtils.toBoolean(reportDateIncidator), "R", ""));

		String hourString = null;
		if (StringUtils.isNotBlank(incidentDatetimeString) && incidentDatetimeString.length() > 11){
			hourString = StringUtils.substringBefore(StringUtils.substringAfter(incidentDatetimeString, "T"), ":"); 
		}
		
		ParsedObject<Integer> hour = newIncident.getIncidentHour();
		hour.setMissing(false);
		hour.setInvalid(false);
		if (StringUtils.isNotBlank(hourString)) {
			try {
				if (hourString.length() != 2){
					throw new NumberFormatException(); 
				}
				Integer hourI = new Integer(hourString);
				hour.setValue(hourI);
			} catch(NumberFormatException nfe) {
				NIBRSError e152 = new NIBRSError();
				e152.setContext(reportBaseData.getReportSource());
				e152.setReportUniqueIdentifier(reportBaseData.getIncidentNumber());
				e152.setSegmentType(reportBaseData.getSegmentType());
				e152.setValue(hourString);
				e152.setNIBRSErrorCode(NIBRSErrorCode._152);
				e152.setDataElementIdentifier("3");
				newErrorList.add(e152);
				
				NIBRSError e = new NIBRSError();
				e.setContext(reportBaseData.getReportSource());
				e.setReportUniqueIdentifier(reportBaseData.getIncidentNumber());
				e.setSegmentType(reportBaseData.getSegmentType());
				e.setValue(hourString);
				e.setNIBRSErrorCode(NIBRSErrorCode._104);
				e.setDataElementIdentifier("3");
				newErrorList.add(e);
				hour.setInvalid(true);
				hour.setValidationError(e);
			}
		} else {
			hour.setMissing(true);
		}
			
		newIncident.setExceptionalClearanceCode(XmlUtils.xPathStringSearch(reportElement, "nc:Incident/j:IncidentAugmentation/j:IncidentExceptionalClearanceCode"));
		
		ParsedObject<Date> clearanceDate = newIncident.getExceptionalClearanceDate();
		clearanceDate.setMissing(false);
		clearanceDate.setInvalid(false);
		String clearanceDateString = XmlUtils.xPathStringSearch(reportElement, "nc:Incident/j:IncidentAugmentation/j:IncidentExceptionalClearanceDate/nc:Date");
		if (clearanceDateString == null) {
			clearanceDate.setMissing(true);
			clearanceDate.setValue(null);
		} else {
			try {
				Date d = getDateFormat().parse(clearanceDateString);
				clearanceDate.setValue(d);
			} catch (ParseException pe) {
				NIBRSError e = new NIBRSError();
				e.setContext(reportBaseData.getReportSource());
				e.setReportUniqueIdentifier(reportBaseData.getIncidentNumber());
				e.setSegmentType(reportBaseData.getSegmentType());
				e.setValue(clearanceDateString);
				e.setNIBRSErrorCode(NIBRSErrorCode._105);
				e.setDataElementIdentifier("5");
				newErrorList.add(e);
				incidentDate.setInvalid(true);
				incidentDate.setValidationError(e);
			}
		}
		newIncident.setExceptionalClearanceDate(clearanceDate);
		
		String cargoTheftYN = XmlUtils.xPathStringSearch(reportElement, "nc:Incident/cjis:IncidentAugmentation/j:OffenseCargoTheftIndicator");
		
		if (StringUtils.isNotBlank(cargoTheftYN)){
			String cargoTheft = BooleanUtils.toString(BooleanUtils.toBoolean(cargoTheftYN), "Y", "N", "N"); 
			newIncident.setCargoTheftIndicator(cargoTheft);
			newIncident.setIncludesCargoTheft(true);
		}
		
		for (NIBRSError e : newErrorList) {
			e.setReport(newIncident);
		}
		errorList.addAll(newErrorList);
		
		buildOffenseSegments(reportElement, newIncident, errorList);
		buildPropertySegments(reportElement, newIncident, errorList);
		buildVictimSegments(reportElement, newIncident, errorList);
		buildOffenderSegments(reportElement, newIncident, errorList);
		buildGroupAArresteeSegments(reportElement, newIncident, errorList);
		return newIncident;
	}

	private void buildGroupAArresteeSegments(Element reportElement, GroupAIncidentReport incident,
			List<NIBRSError> errorList) {
		char segmentType = '6';
		NodeList arresteeElements = (NodeList) XmlUtils.xPathNodeListSearch(reportElement, "j:Arrestee");
		for(int i=0; i < arresteeElements.getLength() ; i++){
			Element arresteeElement = (Element) arresteeElements.item(i);
			ArresteeSegment newArrestee = new ArresteeSegment(segmentType);
			
			ReportSource reportSource = new ReportSource(incident.getSource());
			String arresteeId = XmlUtils.xPathStringSearch(arresteeElement, "@s:id");
			reportSource.setSourceLocation(arresteeId);
			
			ParsedObject<Integer> sequenceNumber = newArrestee.getArresteeSequenceNumber();
			sequenceNumber.setMissing(false);
			sequenceNumber.setInvalid(false);
			String sequenceNumberString = XmlUtils.xPathStringSearch(arresteeElement, "j:ArrestSequenceID");
			if (sequenceNumberString == null) {
				sequenceNumber.setMissing(true);
				sequenceNumber.setValue(null);
			} else {
				try {
					Integer sequenceNumberI = Integer.parseInt(sequenceNumberString);
					sequenceNumber.setValue(sequenceNumberI);
				} catch (NumberFormatException nfe) {
					NIBRSError e = new NIBRSError();
					e.setContext(reportSource);
					e.setReportUniqueIdentifier(incident.getIncidentNumber());
					e.setSegmentType(segmentType);
					e.setValue(sequenceNumberString);
					e.setNIBRSErrorCode(NIBRSErrorCode._601);
					e.setDataElementIdentifier("40");
					errorList.add(e);
					sequenceNumber.setInvalid(true);
					sequenceNumber.setValidationError(e);
				}
			}
			
			newArrestee.setArresteeSequenceNumber(sequenceNumber);
			
			Node arrestNode = XmlUtils.xPathNodeSearch(reportElement, "j:Arrest[@s:id = ../j:ArrestSubjectAssociation[j:Subject/@s:ref='"+ arresteeId +  "']/nc:Activity/@s:ref]");
			
			if (arrestNode != null){
				newArrestee.setArrestTransactionNumber(XmlUtils.xPathStringSearch(arrestNode, "nc:ActivityIdentification/nc:IdentificationID"));
				
				ParsedObject<Date> arrestDate = newArrestee.getArrestDate();
				arrestDate.setMissing(false);
				arrestDate.setInvalid(false);
				String arrestDateString = XmlUtils.xPathStringSearch(arrestNode, "nc:ActivityDate/nc:Date");
				if (arrestDateString == null) {
					arrestDate.setMissing(true);
					arrestDate.setValue(null);
				} else {
					try {
						Date d = getDateFormat().parse(arrestDateString);
						arrestDate.setValue(d);
					} catch (ParseException pe) {
						NIBRSError e = new NIBRSError();
						e.setContext(reportSource);
						e.setReportUniqueIdentifier(incident.getIncidentNumber());
						e.setSegmentType(segmentType);
						e.setValue(arrestDateString);
						e.setNIBRSErrorCode(NIBRSErrorCode._705);
						e.setDataElementIdentifier("42");
						errorList.add(e);
						arrestDate.setInvalid(true);
						arrestDate.setValidationError(e);
					}
				}
				newArrestee.setArrestDate(arrestDate);
				newArrestee.setUcrArrestOffenseCode(XmlUtils.xPathStringSearch(arrestNode, "j:ArrestCharge/nibrs:ChargeUCRCode"));
				newArrestee.setTypeOfArrest(XmlUtils.xPathStringSearch(arrestNode, "j:ArrestCategoryCode"));
			}
			
			newArrestee.setMultipleArresteeSegmentsIndicator(XmlUtils.xPathStringSearch(arresteeElement, "j:ArrestSubjectCountCode"));
			
			NodeList arresteeArmedWithElements = (NodeList) XmlUtils.xPathNodeListSearch(reportElement, "j:Arrestee/j:ArresteeArmedWithCode");
			
			for(int j=0; j < arresteeArmedWithElements.getLength() && j < 2; j++){
				Element arresteeArmedWithElement = (Element)arresteeArmedWithElements.item(j);
				newArrestee.setArresteeArmedWith(j , arresteeArmedWithElement.getTextContent());
				
				//TODO did not find the Automatic weapon indicator  in the xml schema. 
//				arrestee.setAutomaticWeaponIndicator(i, NibrsStringUtils.getStringBetween(54 + 3 * i, 54 + 3 * i, segmentData));
			}
			
			String personRef = XmlUtils.xPathStringSearch(arresteeElement, "nc:RoleOfPerson/@s:ref");
			Node personNode = XmlUtils.xPathNodeSearch(reportElement, "nc:Person[@s:id ='" + personRef + "']");
			
			newArrestee.setAge(parseAgeNode(personNode, newArrestee));
			newArrestee.setSex(XmlUtils.xPathStringSearch(personNode, "j:PersonSexCode"));
			newArrestee.setRace(XmlUtils.xPathStringSearch(personNode, "j:PersonRaceNDExCode"));
			newArrestee.setEthnicity(XmlUtils.xPathStringSearch(personNode, "j:PersonEthnicityCode"));
			newArrestee.setResidentStatus(XmlUtils.xPathStringSearch(personNode, "j:PersonResidentCode"));
			
			newArrestee.setDispositionOfArresteeUnder18(XmlUtils.xPathStringSearch(arresteeElement, "j:ArresteeJuvenileDispositionCode"));

			incident.addArrestee(newArrestee);
		}		
	}

	private void buildOffenderSegments(Element reportElement, GroupAIncidentReport incident,
			List<NIBRSError> errorList) {
		char segmentType = '5';
		NodeList offenderElements = (NodeList) XmlUtils.xPathNodeListSearch(reportElement, "j:Subject");
		for(int i=0; i < offenderElements.getLength(); i++){
			Element offenderElement = (Element) offenderElements.item(i);
			OffenderSegment newOffender = new OffenderSegment();
			
			ReportSource reportSource = new ReportSource(incident.getSource());
			String offenderId = XmlUtils.xPathStringSearch(offenderElement, "@s:id");
			reportSource.setSourceLocation(offenderId);
			
			ParsedObject<Integer> sequenceNumber = newOffender.getOffenderSequenceNumber();
			sequenceNumber.setMissing(false);
			sequenceNumber.setInvalid(false);
			String sequenceNumberString = XmlUtils.xPathStringSearch(offenderElement, "j:SubjectSequenceNumberText");
			if (sequenceNumberString == null) {
				sequenceNumber.setMissing(true);
				sequenceNumber.setValue(null);
			} else {
				try {
					Integer sequenceNumberI = Integer.parseInt(sequenceNumberString);
					sequenceNumber.setValue(sequenceNumberI);
				} catch (NumberFormatException nfe) {
					NIBRSError e = new NIBRSError();
					e.setContext(reportSource);
					e.setReportUniqueIdentifier(incident.getIncidentNumber());
					e.setSegmentType(segmentType);
					e.setValue(sequenceNumberString);
					e.setNIBRSErrorCode(NIBRSErrorCode._301);
					e.setDataElementIdentifier("36");
					errorList.add(e);
					sequenceNumber.setInvalid(true);
					sequenceNumber.setValidationError(e);
				}
			}
			
			String personRef = XmlUtils.xPathStringSearch(offenderElement, "nc:RoleOfPerson/@s:ref");
			Node personNode = XmlUtils.xPathNodeSearch(reportElement, "nc:Person[@s:id ='" + personRef + "']");
			
			newOffender.setAge(parseAgeNode(personNode, newOffender));
			newOffender.setSex(XmlUtils.xPathStringSearch(personNode, "j:PersonSexCode"));
			newOffender.setRace(XmlUtils.xPathStringSearch(personNode, "j:PersonRaceNDExCode"));
			newOffender.setEthnicity(XmlUtils.xPathStringSearch(personNode, "j:PersonEthnicityCode"));
			
			incident.addOffender(newOffender);
		}
	}

	private void buildVictimSegments(Element reportElement, GroupAIncidentReport incident,
			List<NIBRSError> errorList) {
		char segmentType = '4';
		NodeList victimElements = (NodeList) XmlUtils.xPathNodeListSearch(reportElement, "j:Victim");
		for(int i=0; i < victimElements.getLength(); i++){
			Element victimElement = (Element) victimElements.item(i);
			VictimSegment newVictim = new VictimSegment();
			
			ReportSource reportSource = new ReportSource(incident.getSource());
			String victimId = XmlUtils.xPathStringSearch(victimElement, "@s:id");
			reportSource.setSourceLocation(victimId);

			Integer sequenceNumberI = null;
			ParsedObject<Integer> sequenceNumber = newVictim.getVictimSequenceNumber();
			sequenceNumber.setMissing(false);
			sequenceNumber.setInvalid(false);
			String sequenceNumberString = XmlUtils.xPathStringSearch(victimElement, "j:VictimSequenceNumberText");
			if (sequenceNumberString == null) {
				sequenceNumber.setMissing(true);
				sequenceNumber.setValue(null);
			} else {
				try {
					sequenceNumberI = Integer.parseInt(sequenceNumberString);
					sequenceNumber.setValue(sequenceNumberI);
				} catch (NumberFormatException nfe) {
					NIBRSError e = new NIBRSError();
					e.setContext(reportSource);
					e.setReportUniqueIdentifier(incident.getIncidentNumber());
					e.setSegmentType(segmentType);
					e.setValue(sequenceNumberString);
					e.setNIBRSErrorCode(NIBRSErrorCode._401);
					e.setDataElementIdentifier("23");
					errorList.add(e);
					sequenceNumber.setInvalid(true);
					sequenceNumber.setValidationError(e);
				}
			}

			NodeList offenseUcrCodeNodes = XmlUtils.xPathNodeListSearch(reportElement, "j:Offense[@s:id = ../j:OffenseVictimAssociation[j:Victim/@s:ref = '" 
						+ victimId + "']/j:Offense/@s:ref]/nibrs:OffenseUCRCode");
			for (int j = 0; j < offenseUcrCodeNodes.getLength() && j < VictimSegment.UCR_OFFENSE_CODE_CONNECTION_COUNT; j++) {
				Element offenseUcrCode = (Element) offenseUcrCodeNodes.item(j);
				newVictim.setUcrOffenseCodeConnection(j, offenseUcrCode.getTextContent());
			}
			
			NodeList subjectVictimAssociations = XmlUtils.xPathNodeListSearch(reportElement, "j:SubjectVictimAssociation[j:Victim/@s:ref = '"+ victimId + "']"); 
			for (int j = 0; j < subjectVictimAssociations.getLength() && j < VictimSegment.OFFENDER_NUMBER_RELATED_COUNT; j++){
				Element subjectVictimAssociation = ( Element ) subjectVictimAssociations.item(j); 
				String offenderRef = XmlUtils.xPathStringSearch(subjectVictimAssociation, "j:Subject/@s:ref");
				
				String offenderNumberRelatedString = 
						XmlUtils.xPathStringSearch(reportElement, "j:Subject[@s:id='"+ offenderRef + "']/j:SubjectSequenceNumberText");
				
				ParsedObject<Integer> offenderNumberRelated = newVictim.getOffenderNumberRelated(j);
				offenderNumberRelated.setInvalid(false);
				offenderNumberRelated.setMissing(false);
				if (offenderNumberRelatedString == null) {
					offenderNumberRelated.setMissing(true);
					offenderNumberRelated.setInvalid(false);
				} else {
					try {
						Integer offenderNumberRelatedValue = Integer.parseInt(offenderNumberRelatedString);
						offenderNumberRelated.setValue(offenderNumberRelatedValue);
					} catch (NumberFormatException nfe) {
						NIBRSError e = new NIBRSError();
						e.setContext(reportSource);
						e.setReportUniqueIdentifier( incident.getIncidentNumber() );
						e.setSegmentType(segmentType);
						e.setValue(offenderNumberRelatedString);
						e.setNIBRSErrorCode(NIBRSErrorCode._402);
						e.setWithinSegmentIdentifier(sequenceNumberI);
						e.setDataElementIdentifier("34");
						errorList.add(e);
						offenderNumberRelated.setMissing(false);
						offenderNumberRelated.setInvalid(true);
					}
				}

				newVictim.setOffenderNumberRelated(j, offenderNumberRelated);
				String victimOffenderRelationshipCode = XmlUtils.xPathStringSearch(subjectVictimAssociation, "j:VictimToSubjectRelationshipCode");
				newVictim.setVictimOffenderRelationship(j, victimToSubjectRelationshipCodeMap.get(victimOffenderRelationshipCode));
			}
			

			newVictim.setTypeOfVictim(XmlUtils.xPathStringSearch(victimElement, "j:VictimCategoryCode"));
			
			String personRef = XmlUtils.xPathStringSearch(victimElement, "nc:RoleOfPerson/@s:ref");
			Node personNode = XmlUtils.xPathNodeSearch(reportElement, "nc:Person[@s:id ='" + personRef + "']");
			
			newVictim.setAge(parseAgeNode(personNode, newVictim));
			newVictim.setSex(XmlUtils.xPathStringSearch(personNode, "j:PersonSexCode"));
			newVictim.setRace(XmlUtils.xPathStringSearch(personNode, "j:PersonRaceNDExCode"));
			newVictim.setEthnicity(XmlUtils.xPathStringSearch(personNode, "j:PersonEthnicityCode"));
			newVictim.setResidentStatus(XmlUtils.xPathStringSearch(personNode, "j:PersonResidentCode"));
			newVictim.setAggravatedAssaultHomicideCircumstances(0, XmlUtils.xPathStringSearch(victimElement, "j:VictimAggravatedAssaultHomicideFactorCode"));
			newVictim.setAdditionalJustifiableHomicideCircumstances(XmlUtils.xPathStringSearch(victimElement, "j:VictimJustifiableHomicideFactorCode"));

			NodeList victimInjuries = XmlUtils.xPathNodeListSearch(victimElement, "j:VictimInjury/j:InjuryCategoryCode");
			for (int j = 0; j < victimInjuries.getLength() && j < VictimSegment.TYPE_OF_INJURY_COUNT; j++) {
				Node injuryCategoryCode = victimInjuries.item(j);
				newVictim.setTypeOfInjury(j, injuryCategoryCode.getTextContent());
			}

			Node enforcementOfficialNode = XmlUtils.xPathNodeSearch(reportElement, "j:EnforcementOfficial[nc:RoleOfPerson/@s:ref = '" + personRef + "']"); 
			
			if (enforcementOfficialNode != null){
				newVictim.setTypeOfOfficerActivityCircumstance(XmlUtils.xPathStringSearch(enforcementOfficialNode, 
						"j:EnforcementOfficialActivityCategoryCode"));
				newVictim.setOfficerAssignmentType(XmlUtils.xPathStringSearch(enforcementOfficialNode, 
						"j:EnforcementOfficialAssignmentCategoryCode"));
				newVictim.setOfficerOtherJurisdictionORI(XmlUtils.xPathStringSearch(enforcementOfficialNode, 
						"j:EnforcementOfficialUnit/j:OrganizationAugmentation/j:OrganizationORIIdentification/nc:IdentificationID"));
			}
			
			incident.setIncludesLeoka(StringUtils.isNotBlank(newVictim.getOfficerOtherJurisdictionORI()));
			incident.addVictim(newVictim);
		}
	}

	private void buildPropertySegments(Element reportElement, GroupAIncidentReport incident,
			List<NIBRSError> errorList) {
		char segmentType = '3';

		//TODO find out nc:Item and nc:Substance's relationship.
		NodeList propertyElements = (NodeList) XmlUtils.xPathNodeListSearch(reportElement, "nc:Item");
//		for(int i=0; i < offenseElements.getLength(); i++){
//			Element offenseElement = (Element) offenseElements.item(i);
//			OffenseSegment newOffense = new OffenseSegment();
//		}
//		PropertySegment newProperty = new PropertySegment();
//		String segmentData = s.getData();
//		int length = s.getSegmentLength();
//
//		if (length == 307) {
//
//			String typeOfPropertyLoss = NibrsStringUtils.getStringBetween(38, 38, segmentData);
//			newProperty.setTypeOfPropertyLoss(typeOfPropertyLoss);
//
//			for (int i = 0; i < PropertySegment.PROPERTY_DESCRIPTION_COUNT; i++) {
//				newProperty.setPropertyDescription(i, NibrsStringUtils.getStringBetween(39 + 19 * i, 40 + 19 * i, segmentData));
//			}
//			for (int i = 0; i < PropertySegment.VALUE_OF_PROPERTY_COUNT; i++) {
//				String propertyValueString = NibrsStringUtils.getStringBetween(41 + 19 * i, 49 + 19 * i, segmentData);
//				ParsedObject<Integer> propertyValue = newProperty.getValueOfProperty(i);
//				propertyValue.setInvalid(false);
//				propertyValue.setMissing(false);
//				if (propertyValueString == null) {
//					propertyValue.setValue(null);
//					propertyValue.setInvalid(false);
//					propertyValue.setMissing(true);
//				} else {
//					try {
//						String valueOfPropertyPattern = "\\d{1,9}";
//						if (propertyValueString.matches(valueOfPropertyPattern)){
//							Integer propertyValueI = Integer.parseInt(propertyValueString);
//							propertyValue.setValue(propertyValueI);
//						}
//						else{
//							throw new NumberFormatException(); 
//						}
//					} catch (NumberFormatException nfe) {
//						NIBRSError e = new NIBRSError();
//						e.setContext(s.getReportSource());
//						e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
//						e.setSegmentType(s.getSegmentType());
//						e.setValue(org.apache.commons.lang3.StringUtils.leftPad(propertyValueString, 9));
//						e.setNIBRSErrorCode(NIBRSErrorCode._302);
//						e.setWithinSegmentIdentifier(null);
//						e.setDataElementIdentifier("16");
//						errorList.add(e);
//						propertyValue.setMissing(false);
//						propertyValue.setInvalid(true);
//					}
//				}
//			}
//			for (int i = 0; i < PropertySegment.DATE_RECOVERED_COUNT; i++) {
//				
//				ParsedObject<Date> d = newProperty.getDateRecovered(i);
//				d.setMissing(false);
//				d.setInvalid(false);
//				String ds = NibrsStringUtils.getStringBetween(50 + 19 * i, 57 + 19 * i, segmentData);
//				if (ds == null) {
//					d.setMissing(true);
//					d.setValue(null);
//				} else {
//					try {
//						Date dd = dateFormat.parse(ds);
//						d.setValue(dd);
//					} catch (ParseException pe) {
//						NIBRSError e = new NIBRSError();
//						e.setContext(s.getReportSource());
//						e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
//						e.setSegmentType(s.getSegmentType());
//						e.setValue(ds);
//						e.setNIBRSErrorCode(NIBRSErrorCode._305);
//						e.setDataElementIdentifier("17");
//						errorList.add(e);
//						d.setInvalid(true);
//						d.setValidationError(e);
//					}
//				}
//				
//			}
//
//			parseIntegerObject(segmentData, newProperty.getNumberOfStolenMotorVehicles(), 229, 230);
//			parseIntegerObject(segmentData, newProperty.getNumberOfRecoveredMotorVehicles(), 231, 232);
//
//			for (int i = 0; i < PropertySegment.SUSPECTED_DRUG_TYPE_COUNT; i++) {
//				newProperty.setSuspectedDrugType(i, NibrsStringUtils.getStringBetween(233 + 15 * i, 233 + 15 * i, segmentData));
//				String drugQuantityWholePartString = NibrsStringUtils.getStringBetween(234 + 15 * i, 242 + 15 * i, segmentData);
//				String drugQuantityFractionalPartString = NibrsStringUtils.getStringBetween(243 + 15 * i, 245 + 15 * i, segmentData);
//				if (drugQuantityWholePartString != null || drugQuantityFractionalPartString != null) {
//					String fractionalValueString = "000";
//					String value = org.apache.commons.lang3.StringUtils.isBlank(drugQuantityWholePartString)? "0":drugQuantityWholePartString.trim();
//					if (drugQuantityFractionalPartString != null) {
//						fractionalValueString = drugQuantityFractionalPartString;
//						value += fractionalValueString;
//					}
//					
//					String drugQuantityFullValueString = org.apache.commons.lang3.StringUtils.trimToEmpty(drugQuantityWholePartString) + "." + fractionalValueString;
//					
//					try{
//						Double doubleValue = new Double(drugQuantityFullValueString);
//						newProperty.setEstimatedDrugQuantity(i, new ParsedObject<Double>(doubleValue));
//					}
//					catch (NumberFormatException ne){
//						log.error(ne);
//						ParsedObject<Double> estimatedDrugQuantity = ParsedObject.getInvalidParsedObject();
//						newProperty.setEstimatedDrugQuantity(i, estimatedDrugQuantity);
//						NIBRSError e = new NIBRSError();
//						e.setContext(s.getReportSource());
//						e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
//						e.setSegmentType(s.getSegmentType());
//						e.setValue(value);
//						e.setNIBRSErrorCode(NIBRSErrorCode._302);
//						e.setWithinSegmentIdentifier(null);
//						e.setDataElementIdentifier("21");
//						errorList.add(e);
//						estimatedDrugQuantity.setValidationError(e);
//
//					}
//				}
//				else{
//					newProperty.setEstimatedDrugQuantity(i, ParsedObject.getMissingParsedObject());
//				}
//				
//				newProperty.setTypeDrugMeasurement(i, NibrsStringUtils.getStringBetween(246 + 15 * i, 247 + 15 * i, segmentData));
//			}
//
//		} else {
//			NIBRSError e = new NIBRSError();
//			e.setContext(s.getReportSource());
//			e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
//			e.setSegmentType(s.getSegmentType());
//			e.setValue(length);
//			e.setNIBRSErrorCode(NIBRSErrorCode._401);
//			errorList.add(e);
//		}
//
//		return newProperty;

		
	}

	private void buildOffenseSegments(Element reportElement, GroupAIncidentReport incident,
			List<NIBRSError> errorList) {
		
		char segmentType = '2';
		
		NodeList offenseElements = (NodeList) XmlUtils.xPathNodeListSearch(reportElement, "j:Offense");
		for(int i=0; i < offenseElements.getLength(); i++){
			Element offenseElement = (Element) offenseElements.item(i);
			OffenseSegment newOffense = new OffenseSegment();
			
			ReportSource reportSource = new ReportSource(incident.getSource());
			String offenseId = XmlUtils.xPathStringSearch(offenseElement, "@s:id");
			reportSource.setSourceLocation(offenseId);
			
			NodeList offenseFactorBiasMotivationCodes = 
					XmlUtils.xPathNodeListSearch(offenseElement, "j:OffenseFactorBiasMotivationCode");
			
			if (offenseFactorBiasMotivationCodes.getLength() > 0){
				
				for(int j=0; j < offenseFactorBiasMotivationCodes.getLength(); j++){
					Element offenseFactorBiasMotivationCode = 
							(Element) offenseFactorBiasMotivationCodes.item(j);
					newOffense.setBiasMotivation(j, offenseFactorBiasMotivationCode.getTextContent());
				}
				
			}
			else {
				NIBRSError e = new NIBRSError();
				e.setContext(reportSource);
				e.setReportUniqueIdentifier(incident.getIncidentNumber());
				e.setSegmentType(segmentType);
				e.setNIBRSErrorCode(NIBRSErrorCode._201);
				errorList.add(e);
				
				break; 
			}
			
			newOffense.setUcrOffenseCode(XmlUtils.xPathStringSearch(offenseElement, "nibrs:OffenseUCRCode")); 
			newOffense.setOffenseAttemptedCompleted(XmlUtils.xPathStringSearch(offenseElement, "j:OffenseAttemptedIndicator"));
			newOffense.setLocationType(XmlUtils.xPathStringSearch(reportElement, "nc:Location[@s:id = ../j:OffenseLocationAssociation[j:Offense/@s:ref = '" + offenseId + "']/nc:Location/@s:ref]/nibrs:LocationCategoryCode"));
			
			String premisesEnteredString = XmlUtils.xPathStringSearch(offenseElement, "j:OffenseStructuresEnteredQuantity");
			ParsedObject<Integer> premisesEntered = newOffense.getNumberOfPremisesEntered();
			
			if (premisesEnteredString == null) {
				premisesEntered.setMissing(true);
				premisesEntered.setInvalid(false);
				premisesEntered.setValue(null);
			} else {
				
				try {
					Integer value = Integer.parseInt(premisesEnteredString);
					premisesEntered.setValue(value);
					premisesEntered.setMissing(false);
					premisesEntered.setInvalid(false);
				} catch (NumberFormatException nfe) {
					NIBRSError e = new NIBRSError();
					e.setContext(reportSource);
					e.setReportUniqueIdentifier(incident.getIncidentNumber());
					e.setSegmentType(segmentType);
					e.setValue(premisesEnteredString);
					e.setNIBRSErrorCode(NIBRSErrorCode._204);
					e.setDataElementIdentifier("10");
					errorList.add(e);
					premisesEntered.setInvalid(true);
					premisesEntered.setValidationError(e);
				}
				
			}
			
			newOffense.setMethodOfEntry(XmlUtils.xPathStringSearch(offenseElement, "j:OffenseEntryPoint/j:PassagePointMethodCode"));
			
			parseOffendersSuspectedOfUsings(offenseElement, newOffense);

			parseTypesOfCriminalActivities(offenseElement, newOffense);
			
			parseTypesWeaponForceInvolved(offenseElement, newOffense);
			
			//TODO find out AutomaticWeaponIndicator's xPath.
//			NodeList automaticWeaponIndicators = 
//					XmlUtils.xPathNodeListSearch(offenseElement, "j:OffenseForce/j:ForceCategoryCode");
//			
//			for( int j = 0; j < automaticWeaponIndicators.getLength() && j < OffenseSegment.AUTOMATIC_WEAPON_INDICATOR_COUNT; j++ ){
//				Element automaticWeaponIndicator = 
//						(Element) automaticWeaponIndicators.item(j);
//				newOffense.setAutomaticWeaponIndicator(j, automaticWeaponIndicator.getTextContent());
//			}
			
			incident.addOffense(newOffense); 
			
		}
	}
	
	private void parseTypesWeaponForceInvolved(Element offenseElement, OffenseSegment newOffense) {
		NodeList typesWeaponForceInvolved = 
				XmlUtils.xPathNodeListSearch(offenseElement, "j:OffenseForce/j:ForceCategoryCode");
		
		for( int j = 0; j < typesWeaponForceInvolved.getLength() && j < OffenseSegment.TYPE_OF_WEAPON_FORCE_INVOLVED_COUNT; j++ ){
			Element typeWeaponForceInvolved = 
					(Element) typesWeaponForceInvolved.item(j);
			newOffense.setTypeOfWeaponForceInvolved(j, typeWeaponForceInvolved.getTextContent());
		}
	}

	private void parseTypesOfCriminalActivities(Element offenseElement, OffenseSegment newOffense) {
		NodeList typesOfCriminalActivities = 
				XmlUtils.xPathNodeListSearch(offenseElement, "nibrs:CriminalActivityCategoryCode");
		
		for( int j = 0; j < typesOfCriminalActivities.getLength() && j < OffenseSegment.TYPE_OF_CRIMINAL_ACTIVITY_COUNT; j++ ){
			Element typeOfCriminalActivity = 
					(Element) typesOfCriminalActivities.item(j);
			newOffense.setTypeOfCriminalActivity(j, typeOfCriminalActivity.getTextContent());
		}
	}

	private void parseOffendersSuspectedOfUsings(Element offenseElement, OffenseSegment newOffense) {
		NodeList offendersSuspectedOfUsings = 
				XmlUtils.xPathNodeListSearch(offenseElement, "j:OffenseFactor/j:OffenseFactorCode");
		
		for( int j = 0; j < offendersSuspectedOfUsings.getLength() && j < OffenseSegment.OFFENDERS_SUSPECTED_OF_USING_COUNT; j++ ){
			Element offendersSuspectedOfUsing = 
					(Element) offendersSuspectedOfUsings.item(j);
			newOffense.setOffendersSuspectedOfUsing(j, offendersSuspectedOfUsing.getTextContent());
		}
	}

	private final void handleNewReport(AbstractReport newReport, List<NIBRSError> errorList) {
		if (newReport != null) {
			for (Iterator<ReportListener> it = getListeners().iterator(); it.hasNext();) {
				ReportListener listener = it.next();
				listener.newReport(newReport, errorList);
			}
		}
	}

	private Integer getIntValueFromXpath(ReportBaseData reportBaseData, String xPath,  List<NIBRSError> errorList, NIBRSErrorCode errorCode) 
			throws Exception {
		Element parent = reportBaseData.getReportElement();
		String value = XmlUtils.xPathStringSearch(parent, xPath);
		Integer i = null;
		try {
			i = new Integer(value);
		} catch (NumberFormatException nfe) {
			NIBRSError e = new NIBRSError();
			e.setContext(reportBaseData.getReportSource());
			e.setReportUniqueIdentifier(reportBaseData.getIncidentNumber());
			e.setNIBRSErrorCode(errorCode);
			e.setValue(value);
			e.setSegmentType(reportBaseData.getSegmentLevel());
			errorList.add(e);
			log.debug("Error in int conversion: position =" + reportBaseData.getReportSource()
				+ ", xPath = '" + xPath
				+ "', value=" + value);
		}
		return i;
	}
	
	private PropertySegment buildPropertySegment(Segment s, List<NIBRSError> errorList) {

		PropertySegment newProperty = new PropertySegment();
		String segmentData = s.getData();
		int length = s.getSegmentLength();

		if (length == 307) {

			String typeOfPropertyLoss = NibrsStringUtils.getStringBetween(38, 38, segmentData);
			newProperty.setTypeOfPropertyLoss(typeOfPropertyLoss);

			for (int i = 0; i < PropertySegment.PROPERTY_DESCRIPTION_COUNT; i++) {
				newProperty.setPropertyDescription(i, NibrsStringUtils.getStringBetween(39 + 19 * i, 40 + 19 * i, segmentData));
			}
			for (int i = 0; i < PropertySegment.VALUE_OF_PROPERTY_COUNT; i++) {
				String propertyValueString = NibrsStringUtils.getStringBetween(41 + 19 * i, 49 + 19 * i, segmentData);
				ParsedObject<Integer> propertyValue = newProperty.getValueOfProperty(i);
				propertyValue.setInvalid(false);
				propertyValue.setMissing(false);
				if (propertyValueString == null) {
					propertyValue.setValue(null);
					propertyValue.setInvalid(false);
					propertyValue.setMissing(true);
				} else {
					try {
						String valueOfPropertyPattern = "\\d{1,9}";
						if (propertyValueString.matches(valueOfPropertyPattern)){
							Integer propertyValueI = Integer.parseInt(propertyValueString);
							propertyValue.setValue(propertyValueI);
						}
						else{
							throw new NumberFormatException(); 
						}
					} catch (NumberFormatException nfe) {
						NIBRSError e = new NIBRSError();
						e.setContext(s.getReportSource());
						e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
						e.setSegmentType(s.getSegmentType());
						e.setValue(org.apache.commons.lang3.StringUtils.leftPad(propertyValueString, 9));
						e.setNIBRSErrorCode(NIBRSErrorCode._302);
						e.setWithinSegmentIdentifier(null);
						e.setDataElementIdentifier("16");
						errorList.add(e);
						propertyValue.setMissing(false);
						propertyValue.setInvalid(true);
					}
				}
			}
			for (int i = 0; i < PropertySegment.DATE_RECOVERED_COUNT; i++) {
				
				ParsedObject<Date> d = newProperty.getDateRecovered(i);
				d.setMissing(false);
				d.setInvalid(false);
				String ds = NibrsStringUtils.getStringBetween(50 + 19 * i, 57 + 19 * i, segmentData);
				if (ds == null) {
					d.setMissing(true);
					d.setValue(null);
				} else {
					try {
						Date dd = getDateFormat().parse(ds);
						d.setValue(dd);
					} catch (ParseException pe) {
						NIBRSError e = new NIBRSError();
						e.setContext(s.getReportSource());
						e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
						e.setSegmentType(s.getSegmentType());
						e.setValue(ds);
						e.setNIBRSErrorCode(NIBRSErrorCode._305);
						e.setDataElementIdentifier("17");
						errorList.add(e);
						d.setInvalid(true);
						d.setValidationError(e);
					}
				}
				
			}

			parseIntegerObject(segmentData, newProperty.getNumberOfStolenMotorVehicles(), 229, 230);
			parseIntegerObject(segmentData, newProperty.getNumberOfRecoveredMotorVehicles(), 231, 232);

			for (int i = 0; i < PropertySegment.SUSPECTED_DRUG_TYPE_COUNT; i++) {
				newProperty.setSuspectedDrugType(i, NibrsStringUtils.getStringBetween(233 + 15 * i, 233 + 15 * i, segmentData));
				String drugQuantityWholePartString = NibrsStringUtils.getStringBetween(234 + 15 * i, 242 + 15 * i, segmentData);
				String drugQuantityFractionalPartString = NibrsStringUtils.getStringBetween(243 + 15 * i, 245 + 15 * i, segmentData);
				if (drugQuantityWholePartString != null || drugQuantityFractionalPartString != null) {
					String fractionalValueString = "000";
					String value = org.apache.commons.lang3.StringUtils.isBlank(drugQuantityWholePartString)? "0":drugQuantityWholePartString.trim();
					if (drugQuantityFractionalPartString != null) {
						fractionalValueString = drugQuantityFractionalPartString;
						value += fractionalValueString;
					}
					
					String drugQuantityFullValueString = org.apache.commons.lang3.StringUtils.trimToEmpty(drugQuantityWholePartString) + "." + fractionalValueString;
					
					try{
						Double doubleValue = new Double(drugQuantityFullValueString);
						newProperty.setEstimatedDrugQuantity(i, new ParsedObject<Double>(doubleValue));
					}
					catch (NumberFormatException ne){
						log.error(ne);
						ParsedObject<Double> estimatedDrugQuantity = ParsedObject.getInvalidParsedObject();
						newProperty.setEstimatedDrugQuantity(i, estimatedDrugQuantity);
						NIBRSError e = new NIBRSError();
						e.setContext(s.getReportSource());
						e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
						e.setSegmentType(s.getSegmentType());
						e.setValue(value);
						e.setNIBRSErrorCode(NIBRSErrorCode._302);
						e.setWithinSegmentIdentifier(null);
						e.setDataElementIdentifier("21");
						errorList.add(e);
						estimatedDrugQuantity.setValidationError(e);

					}
				}
				else{
					newProperty.setEstimatedDrugQuantity(i, ParsedObject.getMissingParsedObject());
				}
				
				newProperty.setTypeDrugMeasurement(i, NibrsStringUtils.getStringBetween(246 + 15 * i, 247 + 15 * i, segmentData));
			}

		} else {
			NIBRSError e = new NIBRSError();
			e.setContext(s.getReportSource());
			e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
			e.setSegmentType(s.getSegmentType());
			e.setValue(length);
			e.setNIBRSErrorCode(NIBRSErrorCode._401);
			errorList.add(e);
		}

		return newProperty;

	}

	private void parseIntegerObject(String segmentData,
			ParsedObject<Integer> parsedObject, 
			int startPosition, 
			int endPosition) {
		
		parsedObject.setMissing(false);
		parsedObject.setInvalid(false);
		
		String parsedString = 
				NibrsStringUtils.getStringBetween(startPosition, endPosition, segmentData);
		if (parsedString == null) {
			parsedObject.setMissing(true);
			parsedObject.setValue(null);
		} else {
			try {
				parsedObject.setValue(Integer.parseInt(parsedString));
			} catch (NumberFormatException nfe) {
				parsedObject.setInvalid(true);
			}
		}
	}

	class NullResolver implements EntityResolver {
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			return new InputSource(new StringReader(""));
		}
	}

}
