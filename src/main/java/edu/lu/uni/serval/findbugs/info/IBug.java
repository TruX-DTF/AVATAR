package edu.lu.uni.serval.findbugs.info;

public interface IBug {
	
	public String getType();

	public void setType(String type);

	public int getPriority();

	public void setPriority(int priority);

	public int getRank();

	public void setRank(int rank);
	
	public String getAbbrev();

	public void setAbbrev(String abbrev);

	public String getCategory();

	public void setCategory(String category);

	public String getSourcePath();

	public void setSourcePath(String sourcePath);

	public int getStartOfSourceLine();

	public void setStartOfSourceLine(int startOfSourceLine);

	public int getEndOfSourceLine();

	public void setEndOfSourceLine(int endOfSourceLine);

	public boolean equals(BugInstance bugInstance);
}
