package org.search.nibrs.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract class of objects representing types of "reports" in NIBRS...  Group A incident reports, Group B arrest reports, and Zero Reports.
 *
 */
public abstract class Report {

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(Report.class);

	private Integer monthOfTape;
	private Integer yearOfTape;
	private String cityIndicator;
	private String ori;
	private char adminSegmentLevel;
	private char reportActionType;
	private boolean hasUpstreamErrors;
	private List<Arrestee> arresteeSegmentList;
	
	public Report() {
		removeArrestees();
	}
	
	public Report(Report r) {
		this.monthOfTape = r.monthOfTape;
		this.yearOfTape = r.yearOfTape;
		this.cityIndicator = r.cityIndicator;
		this.ori = r.ori;
		this.adminSegmentLevel = r.adminSegmentLevel;
		this.reportActionType = r.reportActionType;
		this.hasUpstreamErrors = r.hasUpstreamErrors;
		arresteeSegmentList = CopyUtils.copyList(r.arresteeSegmentList);
	}
	
	public abstract String getUniqueReportDescription();
	public abstract String getUniqueReportIdentifier();

	public char getReportActionType() {
		return reportActionType;
	}
	public void setReportActionType(char reportActionType) {
		this.reportActionType = reportActionType;
	}
	
	public char getAdminSegmentLevel() {
		return adminSegmentLevel;
	}

	public void setAdminSegmentLevel(char c) {
		this.adminSegmentLevel = c;
	}

	public String getCityIndicator() {
	    return cityIndicator;
	}

	public void setCityIndicator(String cityIndicator) {
	    this.cityIndicator = cityIndicator;
	}

	public int getMonthOfTape() {
	    return monthOfTape;
	}

	public void setMonthOfTape(Integer monthOfTape) {
		this.monthOfTape = monthOfTape;
	}

	public String getOri() {
	    return ori;
	}

	public void setOri(String ori) {
	    this.ori = ori;
	}

	public int getYearOfTape() {
	    return yearOfTape;
	}

	public void setYearOfTape(Integer yearOfTape) {
	    this.yearOfTape = yearOfTape;
	}

	public boolean getHasUpstreamErrors() {
		return hasUpstreamErrors;
	}

	public void setHasUpstreamErrors(boolean hasUpstreamErrors) {
		this.hasUpstreamErrors = hasUpstreamErrors;
	}
	public void removeArrestee(int index) {
		arresteeSegmentList.remove(index);
	}
	public void removeArrestees() {
		arresteeSegmentList = new ArrayList<Arrestee>();
	}
	public void addArrestee(Arrestee arrestee) {
	    arresteeSegmentList.add(arrestee);
	}
	public int getArresteeCount() {
	    return arresteeSegmentList.size();
	}
	public Iterator<Arrestee> arresteeIterator() {
	    return getArrestees().iterator();
	}
	public List<Arrestee> getArrestees() {
		return Collections.unmodifiableList(arresteeSegmentList);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("[monthOfTape=" + monthOfTape + ", yearOfTape=" + yearOfTape + ", cityIndicator=" + cityIndicator + ", ori=" + ori + ", adminSegmentLevel=" + adminSegmentLevel
				+ ", reportActionType=" + reportActionType + ", hasUpstreamErrors=" + hasUpstreamErrors + "]");
		sb.append("\n").append(arresteeSegmentList.size() + " Arrestee Segments:\n");
		for (Arrestee a : arresteeSegmentList) {
			sb.append("\t").append(a.toString()).append("\n");
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + adminSegmentLevel;
		result = prime * result + ((arresteeSegmentList == null) ? 0 : arresteeSegmentList.hashCode());
		result = prime * result + ((cityIndicator == null) ? 0 : cityIndicator.hashCode());
		result = prime * result + (hasUpstreamErrors ? 1231 : 1237);
		result = prime * result + ((monthOfTape == null) ? 0 : monthOfTape.hashCode());
		result = prime * result + ((ori == null) ? 0 : ori.hashCode());
		result = prime * result + reportActionType;
		result = prime * result + ((yearOfTape == null) ? 0 : yearOfTape.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.hashCode() == hashCode();
	}
	
}
