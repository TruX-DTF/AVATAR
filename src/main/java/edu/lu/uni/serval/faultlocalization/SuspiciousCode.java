package edu.lu.uni.serval.faultlocalization;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.lu.uni.serval.faultlocalization.Metrics.Metric;

public class SuspiciousCode implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7698475119588343389L;
	/**
	 * Suspicious class.
	 */
	String className;
	/**
	 * Suspicious method.
	 */
	String methodName;
	/**
	 * Suspicious line number
	 */
	public int lineNumber;
	/**
	 * Suspicious value of the line
	 */
	Double suspiciousValue;
	
	/**
	 * Key is the test identifier, value Numbers of time executed by that test.
	 */
	private Map<Integer,Integer> coverage = null;
	
	private int efn; // number of executed and failed test cases.
	private int epn; // number of executed and passed test cases.
	private int npn; // total number of passed test cases - {epn}.
	private int nfn; // total number of failed test cases - {efn}.
	
    private List<String> tests = new ArrayList<>();
    private List<String> failedTests = new ArrayList<>();

	public SuspiciousCode() {
	}

	public SuspiciousCode(String className, String methodName, int lineNumber, double suspiciousValue, Map<Integer, Integer> frequency) {
		super();
		this.className = className;
		this.methodName = methodName;
		this.lineNumber = lineNumber;
		this.suspiciousValue = Double.valueOf(suspiciousValue);
		this.coverage = frequency;
	}

	public SuspiciousCode(String className, String methodName, double susp) {
		super();
		this.className = className;
		this.methodName = methodName;
		this.suspiciousValue = Double.valueOf(susp);
	}
	
	public SuspiciousCode(String className, String methodName, int lineNumber, Metric metric,
			int efn, int epn, int npn, int nfn, String label) {
		super();
		this.className = className;
		this.methodName = methodName;
		this.lineNumber = lineNumber;
		this.efn = efn;
		this.epn = epn;
		this.npn = npn;
		this.nfn = nfn;
		
		resetSuspiciousness(metric);
	}
    
	private void resetSuspiciousness(Metric metric) {
		this.suspiciousValue = Double.valueOf(metric.value(efn, epn, nfn, npn));
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public Double getSuspiciousValue() {
		return suspiciousValue;
	}

	private DecimalFormat df = new DecimalFormat("#.##########");

	public String getSuspiciousValueString() {
		return df.format(this.suspiciousValue);
	}

	public void setSusp(double susp) {
		this.suspiciousValue = Double.valueOf(susp);
	}

	public String getClassName() {
		int i = className.indexOf("$");
		if (i != -1) {
			return className.substring(0, i);
		}

		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	@Override
	public String toString() {
		return "Candidate [className=" + className + ", methodName=" + methodName + ", lineNumber=" + lineNumber
				+ ", susp=" + suspiciousValue + "]";
	}

	public Map<Integer, Integer> getCoverage() {
		return coverage;
	}

	public void setCoverage(Map<Integer, Integer> coverage) {
		this.coverage = coverage;
	}

    public void setFailedTests(List<String> failedTests){
        this.failedTests = failedTests;
    }
    
    public void setTests(List<String> tests){
        this.tests = tests;
    }

    public List<String> getTests(){
        return tests;
    }
    
    public List<String> getFailedTests(){
        return failedTests;
    }

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SuspiciousCode) {
			SuspiciousCode s = (SuspiciousCode) obj;
			if (s.className.equals(this.className) && s.methodName.equals(this.methodName) && s.lineNumber == this.lineNumber) {
				return true;
			}
		}
		return false;
	}

}
