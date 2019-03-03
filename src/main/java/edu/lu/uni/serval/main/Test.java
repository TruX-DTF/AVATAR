package edu.lu.uni.serval.main;

import edu.lu.uni.serval.config.Configuration;

public class Test {

	public static void main(String[] args) {
		String defects4jPath = "/Users/kui.liu/Public/git/defects4j/";
		String projectName = "Chart_";
		
//		Main.fixBug(Configuration.BUGGY_PROJECTS_PATH, defects4jPath, projectName + "1");
		Main_Pos.fixBug(Configuration.BUGGY_PROJECTS_PATH, defects4jPath, projectName + "1");
	}
	
}
