package edu.lu.uni.serval.findbugs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.findbugs.info.BugCollection;
import edu.lu.uni.serval.findbugs.info.BugInstance;
import edu.lu.uni.serval.findbugs.xml.parser.XmlParser;
import edu.lu.uni.serval.utils.FileHelper;
import edu.lu.uni.serval.utils.SuspiciousPosition;

public class FindBugsResults {
	
	public static void main(String[] args) throws IOException {
		
		File xmlFilePath = new File("FindBugs/");
		List<File> xmlFiles = FileHelper.getAllFilesInCurrentDiectory(xmlFilePath, ".xml");
		
//		int i = 0;
		StringBuilder builder = new StringBuilder();
		for (File xmlFile : xmlFiles) {
			
			XmlParser xmlParser = new XmlParser();
			xmlParser.parserXml(xmlFile);
			BugCollection bugCollection = xmlParser.getBugCollection();
			
			String projectName = bugCollection.getProjectName();
			
			List<SuspiciousPosition> bugPostions = readKnownBugPositionsFromFile(projectName);
			
			List<BugInstance> violations = bugCollection.getBugInstances();
			for (BugInstance violation : violations) {
//				builder.append(projectName + " : " + violation.toString() + "\n");
//				i ++;
				System.out.println(violation);
				for (SuspiciousPosition bugPos : bugPostions) {
					if (bugPos.classPath.endsWith(violation.getSourcePath())) {
						if (violation.getStartOfSourceLine() <= bugPos.lineNumber && bugPos.lineNumber <= violation.getEndOfSourceLine()) {
							builder.append(projectName + " : " + violation.toString() + "\n");
						}
					}
				}
			}
			
//			FileHelper.outputToFile("FindBugs.text", builder, false);
//			builder.setLength(0);
		}
		System.out.println(builder);
		FileHelper.outputToFile("FB/FindBugs.txt", builder, false);
		builder.setLength(0);
//		System.out.println(i);
		
//		File f = new File("");
//		if (! f.exists() && 
//			f.delete()) {
//			throw new IOException("");
//		}
	}
	
	private static List<SuspiciousPosition> readKnownBugPositionsFromFile(String buggyProject) {
		List<SuspiciousPosition> suspiciousCodeList = new ArrayList<>();
		
		String[] posArray = FileHelper.readFile(Configuration.knownBugPositions).split("\n");
		Boolean isBuggyProject = null;
		for (String pos : posArray) {
			if (isBuggyProject == null || isBuggyProject) {
				if (pos.startsWith(buggyProject + "@")) {
					isBuggyProject = true;
					
					String[] elements = pos.split("@");
	            	String[] lineStrArr = elements[2].split(",");
	            	String classPath = elements[1];

	            	for (String lineStr : lineStrArr) {
	    				if (lineStr.contains("-")) {
	    					String[] subPos = lineStr.split("-");
	    					for (int line = Integer.valueOf(subPos[0]), endLine = Integer.valueOf(subPos[1]); line <= endLine; line ++) {
	    						SuspiciousPosition sp = new SuspiciousPosition();
	    		            	sp.classPath = classPath;
	    		            	sp.lineNumber = line;
	    		            	suspiciousCodeList.add(sp);
	    					}
	    				} else {
	    					SuspiciousPosition sp = new SuspiciousPosition();
	    	            	sp.classPath = classPath;
	    	            	sp.lineNumber = Integer.valueOf(lineStr);
	    	            	suspiciousCodeList.add(sp);
	    				}
	    			}
				} else if (isBuggyProject!= null && isBuggyProject) isBuggyProject = false;
			} else if (!isBuggyProject) break;
		}
		return suspiciousCodeList;
	}
}
