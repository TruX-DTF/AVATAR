package edu.lu.uni.serval.fixpattern.pmd;

import edu.lu.uni.serval.fixpattern.FixTemplate;

public class EP7 extends FixTemplate {

	/*
	 * e.g., List<String> a = new ArrayList() --> List<String> a = new ArrayList<>().
	 */
	
	@Override
	public void generatePatches() {
		// Ignore this pattern, it won't fix any real bugs.
	}

}
