package edu.lu.uni.serval.findbugs.info;

public class BugInstance implements Comparable<BugInstance>, IBug {
	
	private String type;
	private int priority;
	private int rank;
	private String abbrev;
	private String category;
	private String sourcePath;
	private int startOfSourceLine;
	private int endOfSourceLine;

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public int getRank() {
		return rank;
	}

	@Override
	public void setRank(int rank) {
		this.rank = rank;
	}

	@Override
	public String getAbbrev() {
		return abbrev;
	}

	@Override
	public void setAbbrev(String abbrev) {
		this.abbrev = abbrev;
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public String getSourcePath() {
		return sourcePath;
	}

	@Override
	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	@Override
	public int getStartOfSourceLine() {
		return startOfSourceLine;
	}

	@Override
	public void setStartOfSourceLine(int startOfSourceLine) {
		this.startOfSourceLine = startOfSourceLine;
	}

	@Override
	public int getEndOfSourceLine() {
		return endOfSourceLine;
	}

	@Override
	public void setEndOfSourceLine(int endOfSourceLine) {
		this.endOfSourceLine = endOfSourceLine;
	}

	@Override
	public boolean equals(BugInstance bugInstance) {
		if (!this.type.equals(bugInstance.type)) {
			return false;
		} else if (!this.sourcePath.equals(bugInstance.sourcePath)) {
			return false;
//		} else if (this.priority != bugInstance.priority) {
//			return false;
//		} else if (this.rank != bugInstance.rank) {
//			return false;
//		} else if (!this.abbrev.equals(bugInstance.abbrev)) {
//			return false;
//		} else if (!this.category.equals(bugInstance.category)) {
//			return false;
		} else if (this.startOfSourceLine != bugInstance.startOfSourceLine) {
			return false;
		} else if (this.endOfSourceLine != bugInstance.endOfSourceLine) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public int compareTo(BugInstance bugInstance) {
		int comparedResult = this.sourcePath.compareTo(bugInstance.sourcePath);
		if ( comparedResult == 0) {
			if (this.startOfSourceLine < bugInstance.startOfSourceLine) {
				return -1;
			} else if (this.startOfSourceLine > bugInstance.startOfSourceLine) {
				return 1;
			} else {
				if (this.endOfSourceLine < bugInstance.endOfSourceLine) {
					return -1;
				} else if (this.endOfSourceLine > bugInstance.endOfSourceLine) {
					return 1;
				} else {
					return this.type.compareTo(bugInstance.type);
				}
			}
		} else {
			return comparedResult;
		}
	}

	@Override
	public String toString() {
		return this.type + " : " + this.sourcePath + " : " + this.startOfSourceLine + " : " + this.endOfSourceLine;
	}
	
}
