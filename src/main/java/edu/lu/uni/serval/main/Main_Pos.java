package edu.lu.uni.serval.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.serval.avatar.AbstractFixer;
import edu.lu.uni.serval.avatar.File_Fixer;
import edu.lu.uni.serval.avatar.Line_Fixer;
import edu.lu.uni.serval.avatar.Method_Fixer;
import edu.lu.uni.serval.config.Configuration;

/**
 * Fix bugs with the known bug positions at three level of granularity: line, method, file.
 * 
 * @author kui.liu
 *
 */
public class Main_Pos {
	
	private static Logger log = LoggerFactory.getLogger(Main_Pos.class);
	
	private static Granularity granularity = Granularity.Method;
	
	enum Granularity {
		Line,   // It means that the bug position at line level is known.
		Method, // It means that the bug position at method level is known.
		File    // It means that the bug position at file level is known.
	}

	public static void main(String[] args) {
		if (args.length != 7) {
			System.out.println("Arguments: <Failed_Test_Cases_File_Path> <Known_Bug_Positions_File> <Buggy_Project_Path> <defects4j_Path> <Project_Name> <Suspicious_Code_Positions_File_Path> <Line/Method/File>");
			System.exit(0);
		}
		Configuration.failedTestCasesFilePath = args[0];
		Configuration.knownBugPositions = args[1];
		String buggyProjectsPath = args[2];// "../Defects4JData/"
		String defects4jPath = args[3]; // "../defects4j/"
		String projectName = args[4]; // "Chart_1"
		System.out.println(projectName);
		Configuration.suspPositionsFilePath = args[5];
		String granularityStr = args[6];
		if ("line".equalsIgnoreCase(granularityStr) || "l".equalsIgnoreCase(granularityStr)) {
			granularity = Granularity.Line;
			Configuration.outputPath += "Line/";
		} else if ("method".equalsIgnoreCase(granularityStr) || "m".equalsIgnoreCase(granularityStr)) {
			granularity = Granularity.Method;
			Configuration.outputPath += "Method/";
		} else if ("file".equalsIgnoreCase(granularityStr) || "f".equalsIgnoreCase(granularityStr)) {
			granularity = Granularity.File;
			Configuration.outputPath += "File/";
		} else {
			System.out.println("Last argument must be l, L, line, Line, m, M, method, Method, f, F, file, or File.");
			System.exit(0);
		}
		fixBug(buggyProjectsPath, defects4jPath, projectName);
	}

	public static void fixBug(String buggyProjectsPath, String defects4jPath, String buggyProjectName) {
		String dataType = "avatar";
		String[] elements = buggyProjectName.split("_");
		String projectName = elements[0];
		int bugId;
		try {
			bugId = Integer.valueOf(elements[1]);
		} catch (NumberFormatException e) {
			System.err.println("Please input correct buggy project ID, such as \"Chart_1\".");
			return;
		}
		
		AbstractFixer fixer = null;
		switch (granularity) {
		case Line:
			fixer = new Line_Fixer(buggyProjectsPath, projectName, bugId, defects4jPath);
			break;
		case Method:
			fixer = new Method_Fixer(buggyProjectsPath, projectName, bugId, defects4jPath);
			break;
		case File:
			fixer = new File_Fixer(buggyProjectsPath, projectName, bugId, defects4jPath);
			break;
		default:
			break;
		}
		if (fixer == null) return;
		
		fixer.dataType = dataType;
		if (Integer.MAX_VALUE == fixer.minErrorTest) {
			System.out.println("Failed to defects4j compile bug " + buggyProjectName);
			return;
		}
		fixer.metric = Configuration.faultLocalizationMetric;
		fixer.fixProcess();
		
		int fixedStatus = fixer.fixedStatus;
		switch (fixedStatus) {
		case 0:
			log.info("=======Failed to fix bug " + buggyProjectName);
			break;
		case 1:
			log.info("=======Succeeded to fix bug " + buggyProjectName);
			break;
		case 2:
			log.info("=======Partial succeeded to fix bug " + buggyProjectName);
			break;
		}
	}

}
