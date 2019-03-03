package edu.lu.uni.serval.faultlocalization;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gzoltar.core.GZoltar;
import com.gzoltar.core.components.Statement;
import com.gzoltar.core.instr.testing.TestResult;

import edu.lu.uni.serval.faultlocalization.Metrics.Metric;

/**
 * GZoltar Fault Localization.
 * 
 * @author kui.liu
 *
 */
public class GZoltarFaultLoclaization {
	
	private static Logger logger = LoggerFactory.getLogger(GZoltarFaultLoclaization.class);
	
	public String srcPath;
	
	private int totalFailedTestCases = 0;
	private int totalPassedTestCases = 0;
	
	public Double threshold = 0.0d;      // The threshold of suspiciousness.
	public int maxSuspCandidates = -1; // The number of top-X suspicious candidates.

	public List<TestResult> gzoltarTestResults;
	private List<Statement> suspiciousStatements;
	
	public List<SuspiciousCode> candidates = new ArrayList<SuspiciousCode>();
	public List<String> failingTestCases = new ArrayList<String>();
	public ArrayList<Statement> selectedSuspiciousStatements = new ArrayList<>();
	
	public void localizeSuspiciousCodeWithGZoltar(final URL[] clazzPaths, Collection<String> packageNames, String... testClasses) throws NullPointerException {
		ArrayList<String> classPaths = new ArrayList<String>();
		StringBuilder b = new StringBuilder();
        for (URL url : clazzPaths) {// Dependencies. lib
        	classPaths.add(url.getPath());
        	b.append(url.getPath()).append("\n");
//            if ("file".equals(url.getProtocol())) {
//                classPaths.add(url.getPath());
//                b.append(url.getPath()).append("\n");
//            } else {
//                classPaths.add(url.toExternalForm());
//                b.append(url.toExternalForm()).append("\n");
//            }
        }
//        FileHelper.outputToFile("logs/classPaths.txt", b, false);
        b.setLength(0);
        
        try {
			GZoltar gzoltar = new GZoltar(System.getProperty("user.dir"));
			
			if (classPaths != null && !classPaths.isEmpty()) {
				gzoltar.getClasspaths().addAll(classPaths);
			}
			
	        gzoltar.addPackageNotToInstrument("org.junit");
	        gzoltar.addPackageNotToInstrument("junit.framework");
	        gzoltar.addTestPackageNotToExecute("org.junit");
	        gzoltar.addTestPackageNotToExecute("junit.framework");
	        
	        for (String packageName : packageNames) {
	            gzoltar.addPackageToInstrument(packageName);
	        }
	        
	        for (URL url: clazzPaths){
	            if (url.getPath().endsWith(".jar")){
	                gzoltar.addClassNotToInstrument(url.getPath());
	                gzoltar.addPackageNotToInstrument(url.getPath());
	            }
	        }
	        
	        // Add test cases.
	        for (String className : checkNotNull(testClasses)) {
	            gzoltar.addTestToExecute(className);        // we want to execute the test
	            gzoltar.addClassNotToInstrument(className); // we don't want to include the test as root-cause candidate
	            b.append(className).append("\n");
	        }
	        
//	        FileHelper.outputToFile("logs/testCases.txt", b, false);
	        b.setLength(0);
	        
	        gzoltar.run();
	        
	        gzoltarTestResults = gzoltar.getTestResults();
	        suspiciousStatements = gzoltar.getSuspiciousStatements();
			Collections.sort(this.suspiciousStatements, new Comparator<Statement>() {
				@Override
				public int compare(Statement o1, Statement o2) {
					if (o2.getSuspiciousness() == o1.getSuspiciousness()) {
						return Integer.compare(o2.getLineNumber(), o1.getLineNumber());
					}
					return Double.compare(o2.getSuspiciousness(), o1.getSuspiciousness());
				}
			});

			parseTestResults();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	/**
	 * Sort suspicious code with GZoltar results.
	 */
	public void sortSuspiciousCode() {
		List<SuspiciousCode> allCandidates = new ArrayList<>();
		for (Statement gzoltarStatement : suspiciousStatements) {
			String className = gzoltarStatement.getMethod().getParent().getLabel();
			if (!isSource(className)) continue;
			if (gzoltarStatement.getSuspiciousness() > 0d && gzoltarStatement.getSuspiciousness() >= threshold) {
				selectedSuspiciousStatements.add(gzoltarStatement);
			}
			
			Double suspiciousness = gzoltarStatement.getSuspiciousness();
			if (suspiciousness.compareTo(0d) != 1) break;
			
			BitSet coverage = gzoltarStatement.getCoverage();
            int nextTest = coverage.nextSetBit(0);
            
            List<String> tests = new ArrayList<>();
            List<String> failedTests = new ArrayList<>();
            while (nextTest != -1) {
            	TestResult testResult = this.gzoltarTestResults.get(nextTest);
                tests.add(testResult.getName());
                if(!testResult.wasSuccessful()) {
                    failedTests.add(testResult.getName());
                }
                nextTest = coverage.nextSetBit(nextTest + 1);
            }
            
            String methodName = gzoltarStatement.getMethod().toString();
			int lineNumber = gzoltarStatement.getLineNumber();
			
            SuspiciousCode suspiciousCode = new SuspiciousCode(className, methodName, lineNumber, suspiciousness, null);
            suspiciousCode.setTests(tests);
            suspiciousCode.setFailedTests(failedTests);
            
        	if (suspiciousness.compareTo(0d) == 1 && suspiciousness >= threshold) {
                candidates.add(suspiciousCode);
			}
        	allCandidates.add(suspiciousCode);
            
		}
		// If we do not have candidate due the threshold is to high, we add all as suspicious
		if (candidates.isEmpty()) candidates.addAll(allCandidates);

		// Select the best top-X candidates.
		int size = candidates.size();
		if (maxSuspCandidates > 0 && maxSuspCandidates < size) {
			candidates = candidates.subList(0, maxSuspCandidates);
		}
		logger.info("Gzoltar found: " + size + " with suspiciousness > " + threshold + ", we consider top-" + candidates.size());
	}

	/**
	 * Sort suspicious code with specific metric.
	 * @param metric
	 */
	public void sortSuspiciousCode(Metric metric) {
		if (metric == null) {
			sortSuspiciousCode();
			return;
		}
		
		List<SuspiciousCode> allCandidates = new ArrayList<>();
		for (Statement statement : suspiciousStatements) {
			String className = statement.getMethod().getParent().getLabel();
			if (!isSource(className)) continue;
//			if (className.endsWith("Exception")) continue;
//			if (className.equals("org.jfree.chart.renderer.category.AbstractCategoryItemRenderer")) {
//				System.out.println(className);
//			}
			BitSet coverage = statement.getCoverage();
            int executedAndPassedCount = 0;
            int executedAndFailedCount = 0;
            int nextTest = coverage.nextSetBit(0);
            
            List<String> tests = new ArrayList<>();
            List<String> failedTests = new ArrayList<>();
            while (nextTest != -1) {
            	TestResult testResult = this.gzoltarTestResults.get(nextTest);
                tests.add(testResult.getName());
                if(testResult.wasSuccessful()) {
                    executedAndPassedCount++;
                } else {
                    executedAndFailedCount++;
                    failedTests.add(testResult.getName());
                }
                nextTest = coverage.nextSetBit(nextTest + 1);
            }
            
            String methodName = statement.getMethod().toString();
			int lineNumber = statement.getLineNumber();
            SuspiciousCode suspiciousCode = new SuspiciousCode(className, methodName, lineNumber, metric,
            		executedAndFailedCount, executedAndPassedCount, this.totalPassedTestCases - executedAndPassedCount,
            		this.totalFailedTestCases - executedAndFailedCount, statement.getLabel());
            suspiciousCode.setTests(tests);
            suspiciousCode.setFailedTests(failedTests);
            if (suspiciousCode.getSuspiciousValue().compareTo(0d) == 1 && suspiciousCode.getSuspiciousValue() >= threshold) {
                candidates.add(suspiciousCode);
			}
            if (suspiciousCode.getSuspiciousValue() > 0) {
                allCandidates.add(suspiciousCode);
			}
		}
		if (candidates.isEmpty()) candidates.addAll(allCandidates);
		
		// Order the suspicious DESC
//		Collections.sort(candidates, (c1, c2) -> Double.compare(c2.getSuspiciousValue(), c1.getSuspiciousValue()));
		Collections.sort(candidates, new Comparator<SuspiciousCode>() {

			@Override
			public int compare(SuspiciousCode o1, SuspiciousCode o2) {
				// reversed parameters because we want a descending order list
                if (o2.getSuspiciousValue() == o1.getSuspiciousValue()){
                	int compareName = o2.getClassName().compareTo(o1.getClassName());
                	if (compareName == 0) {
                		return Integer.compare(o2.getLineNumber(),o1.getLineNumber());
                	}
                    return compareName;
                }
                return Double.compare(o2.getSuspiciousValue(), o1.getSuspiciousValue());
			}
			
		});
		
		// Select the best top-X candidates.
		int size = candidates.size();
		if (maxSuspCandidates > 0 && maxSuspCandidates < size) {
			candidates = candidates.subList(0, maxSuspCandidates);
		}
		logger.info("Gzoltar found: " + size + " with suspiciousness > " + threshold + ", we consider top-" + candidates.size());
	}
	
	private void parseTestResults() {
//        List<String> failingTestCases = new ArrayList<String>();
		for (TestResult tr : this.gzoltarTestResults) {
			String testName = tr.getName().split("#")[0];
//			if (testName.startsWith("junit")) {
//				continue;
//			}
			
			if (tr.wasSuccessful()) {
				totalPassedTestCases ++;
			} else {
//				String trTrance = tr.getTrace().split("\n")[0];
//                if (trTrance.contains("java.lang.NoClassDefFoundError:") ||
//                		trTrance.contains("java.lang.ExceptionInInitializerError") ||
//                		trTrance.contains("java.lang.IllegalAccessError")){
//                	tr.setSuccessful(true);
//    				totalPassedTestCases ++;
//                } else {
//                	totalFailedTestCases ++;
//                }
                String testCaseName = testName.split("\\#")[0];
				if (!failingTestCases.contains(testCaseName)) {
					failingTestCases.add(testCaseName);
				}
				totalFailedTestCases ++;
			}
		}
        logger.info("Gzoltar Test Result Total: " + (totalPassedTestCases + totalFailedTestCases) + 
        		", fails: " + totalFailedTestCases + ", GZoltar suspicious " + suspiciousStatements.size());
	}
    
    private boolean isSource(String compName) {
    	// compName: org.apache.commons.math.linear.Array2DRowRealMatrix
    	String srcFile = srcPath + compName.replace(".", "/") + ".java";
    	return new File(srcFile).exists();
//    	String cmN = compName.toLowerCase(Locale.ENGLISH);
//		String clRoot = cmN.split("\\$")[0];
//		String[] segmentationName = clRoot.split("\\.");
//		String simpleClassName = segmentationName[segmentationName.length - 1];
//
//		return !cmN.endsWith("test") && !cmN.endsWith("tests")
//				&& !cmN.contains("exception") 
//				&& !simpleClassName.startsWith("test")
//				&& !simpleClassName.startsWith("validate");
	}

	public List<SuspiciousCode> getCandidates() {
		return candidates;
	}
    
}
