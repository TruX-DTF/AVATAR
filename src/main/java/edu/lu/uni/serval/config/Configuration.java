package edu.lu.uni.serval.config;

public class Configuration {
	
	/*
	 * Ranking metrics for GZoltar 0.1.1.
	 */
	public static final String[] METRICS_0_1_1 = {"Ample", "Anderberg", "ArithmeticMean", "Barinel", "Dice", "DStar", "Euclid", "Fagge", "Fleiss", "GeometricMean", "Goodman", "Gp13", 
			"Hamann", "Hamming", "HarmonicMean", "Jaccard", "Kulczynski1", "Kulczynski2", "M1", "M2", "McCon", "Minus", "Muse", "Naish1", "Naish2", 
			"Ochiai", "Ochiai2", "Overlap", "Qe", "RogersTanimoto", "Rogot1", "Rogot2", "RussellRao", "Scott", "SimpleMatching", "Sokal", "SorensenDice", "Tarantula", 
			"Wong1", "Wong2", "Wong3", "Zoltar", "null"};
	/*
	 * Ranking metrics for GZoltar 1.6.0.
	 */
	public static final String[] METRICS_1_6_0 = {"barinel", "dstar2", "jaccard", "muse", "ochiai", "opt2", "tarantula"};
	
	/*
	 * Data path of Defects4J bugs.
	 */
	public static final String BUGGY_PROJECTS_PATH = "/Users/kui.liu/Public/Defects4JData/";
	
	public static final String TEMP_FILES_PATH = ".temp/";
	public static final long SHELL_RUN_TIMEOUT = 10800L;
	
	public static String knownBugPositions = "BugPositions.txt";
	public static String suspPositionsFilePath = "SuspiciousCodePositions/";
	public static String failedTestCasesFilePath = "FailedTestCases/";
	public static String faultLocalizationMetric = "Ochiai";
	public static String outputPath = "OUTPUT/";

}
