package org.search.nibrs.validation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.NIBRSSubmission;
import org.search.nibrs.model.Report;
import org.search.nibrs.xml.XmlUtils;
import org.search.nibrs.xml.exporter.XMLExporter;
import org.w3c.dom.Document;

public class RuleViolationExemplarFactoryTest {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(RuleViolationExemplarFactoryTest.class);
	
	@Test
	public void test() throws Exception {
		// This test doesn't really test anything meaningful...just demonstrates how to use the exemplar factory
		NIBRSSubmission report = new NIBRSSubmission();
		RuleViolationExemplarFactory exemplarFactory = RuleViolationExemplarFactory.getInstance();
		GroupAIncidentReport incident = exemplarFactory.getGroupAIncidentsThatViolateRule(115).get(0);
		report.addReport(incident);
		Document d = new XMLExporter().convertNIBRSSubmissionToDocument(report, new ArrayList<NIBRSError>());
		//XmlUtils.printNode(d, System.out);
		String incidentNumberValue = XmlUtils.xPathStringSearch(d, "/nibrs:Submission/nibrs:Report/nc:Incident/nc:ActivityIdentification/nc:IdentificationID");
		assertTrue(incidentNumberValue.trim().contains(" "));
		List<Report> incidents = new ArrayList<Report>();
		incidents.addAll(exemplarFactory.getGroupAIncidentsThatViolateRule(119));
		assertEquals(2, incidents.size());
		report = new NIBRSSubmission();
		report.addReports(incidents);
		d = new XMLExporter().convertNIBRSSubmissionToDocument(report, new ArrayList<NIBRSError>());
		//XmlUtils.printNode(d, System.out);
		assertNotNull(XmlUtils.xPathNodeSearch(d, "/nibrs:Submission/nibrs:Report/j:Offense[nibrs:OffenseUCRCode='13A']"));
		assertNotNull(XmlUtils.xPathNodeSearch(d, "/nibrs:Submission/nibrs:Report/j:Offense[nibrs:OffenseUCRCode='13B']"));
	}

}
