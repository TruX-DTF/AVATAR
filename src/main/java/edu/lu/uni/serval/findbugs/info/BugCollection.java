package edu.lu.uni.serval.findbugs.info;

import java.util.Date;
import java.util.List;

public class BugCollection implements Comparable<BugCollection> {

	private String projectName;
	private Date releasedTime;
	private List<BugInstance> bugInstances;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Date getReleasedTime() {
		return releasedTime;
	}

	public void setReleasedTime(Date releasedTime) {
		this.releasedTime = releasedTime;
	}

	public List<BugInstance> getBugInstances() {
		return bugInstances;
	}

	public void setBugInstances(List<BugInstance> bugInstances) {
		this.bugInstances = bugInstances;
	}
	
	public BugCollection(String projectName, List<BugInstance> bugInstances) {
		super();
		this.projectName = projectName;
		this.bugInstances = bugInstances;
	}

	public BugCollection(String projectName, Date releasedTime, List<BugInstance> bugInstances) {
		super();
		this.projectName = projectName;
		this.releasedTime = releasedTime;
		this.bugInstances = bugInstances;
	}

	@Override
	public int compareTo(BugCollection bugCollection) {
		return this.projectName.compareTo(bugCollection.projectName);
	}
	
	@Override
	public String toString() {
		return this.projectName;
	}
}
