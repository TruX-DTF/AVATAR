package edu.lu.uni.serval.avatar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.utils.FileHelper;
import edu.lu.uni.serval.utils.SuspiciousCodeParser;
import edu.lu.uni.serval.utils.SuspiciousPosition;
import edu.lu.uni.serval.utils.SuspiciousCodeParser.BuggyMethod;

/**
 * Fix bugs with StaticBugFixer when the bug positions at the method level is provided.
 * 
 * @author kui.liu
 *
 */
public class Method_Fixer extends Avatar {
	
	private static Logger log = LoggerFactory.getLogger(Method_Fixer.class);
	
	public Method_Fixer(String path, String projectName, int bugId, String defects4jPath) {
		super(path, projectName, bugId, defects4jPath);
	}
	
	public Method_Fixer(String path, String metric, String projectName, int bugId, String defects4jPath) {
		super(path, metric, projectName, bugId, defects4jPath);
	}

	@Override
	public void fixProcess() {
		// Read paths of the buggy project.
		if (!dp.validPaths) return;
		
		// Read suspicious positions.
		List<BuggyMethod> buggyMethodList = readKnownMethodLevelBugPositions();
		if (buggyMethodList.isEmpty()) return;
		
		List<SuspiciousPosition> suspiciousCodeList = readSuspiciousCodeFromFile();
		if (suspiciousCodeList == null) return;
		
		List<SuspCodeNode> triedSuspNode = new ArrayList<>();
		log.info("=======MethodFixer: Start to fix suspicious code======");
		for (SuspiciousPosition suspiciousCode : suspiciousCodeList) {
			
			if (!isContained(buggyMethodList, suspiciousCode.classPath, suspiciousCode.lineNumber)) continue;
			
			SuspCodeNode scn = parseSuspiciousCode(suspiciousCode);
			if (scn == null) continue;

//			log.debug(scn.suspCodeStr);
			if (triedSuspNode.contains(scn)) continue;
			triedSuspNode.add(scn);
	        // Match fix templates for this suspicious code with its context information.
	        fixWithMatchedFixTemplates(scn);
	        
			if (minErrorTest == 0) break;
        }
		log.info("=======MethodFixer: Finish off fixing======");
		
		FileHelper.deleteDirectory(Configuration.TEMP_FILES_PATH + this.dataType + "/" + this.buggyProject);
	}

	private List<BuggyMethod> readKnownMethodLevelBugPositions() {
		List<BuggyMethod> buggyMethodList = new ArrayList<>();

    	String shortSrcPath = dp.srcPath.substring(dp.srcPath.indexOf(this.buggyProject) + this.buggyProject.length() + 1);
		
		String[] posArray = FileHelper.readFile(Configuration.knownBugPositions).split("\n");
		Boolean isBuggyProject = null;
		for (String pos : posArray) {
			if (isBuggyProject == null || isBuggyProject) {
				if (pos.startsWith(this.buggyProject + "@")) {
					isBuggyProject = true;
					
					String[] elements = pos.split("@");
	            	String classPath = elements[1];
	            	String tempClassPath = classPath.substring(shortSrcPath.length(), classPath.length() - 5).replace("/", ".");
	            	String[] lineStrArr = elements[2].split(",");
            		for (String lineStr : lineStrArr) {
	    				if (lineStr.contains("-")) {
	    					String[] subPos = lineStr.split("-");
	    					for (int line = Integer.valueOf(subPos[0]), endLine = Integer.valueOf(subPos[1]); line <= endLine; line ++) {
	    		            	addToBuggyMethod(buggyMethodList, tempClassPath, line, classPath);
	    					}
	    				} else {
	    	            	addToBuggyMethod(buggyMethodList, tempClassPath, Integer.valueOf(lineStr), classPath);
	    				}
	    			}
				} else if (isBuggyProject!= null && isBuggyProject) isBuggyProject = false;
			} else if (!isBuggyProject) break;
		}
		return buggyMethodList;
	}
	
	private void addToBuggyMethod(List<BuggyMethod> buggyMethodList, String tempClassPath, int line, String classPath) {
		if (!isContained(buggyMethodList, tempClassPath, line)) {
    		SuspiciousCodeParser scp = new SuspiciousCodeParser();
    		scp.parseSuspiciousMethod(new File(this.fullBuggyProjectPath + "/" + classPath), line);
    		BuggyMethod buggyMethod = scp.getBuggMethod();
    		if (buggyMethod != null) {
            	buggyMethod.classPath = tempClassPath;
            	buggyMethodList.add(buggyMethod);
    		}
    	}
	}

	private boolean isContained(List<BuggyMethod> buggyMethodList, String classPath, int line) {
		if (buggyMethodList.isEmpty()) return false;
		for (BuggyMethod bm : buggyMethodList) {
			if (classPath.equals(bm.classPath)) {
				if (bm.startLine <= line && line <= bm.endLine) return true;
			}
		}
		return false;
	}

}
