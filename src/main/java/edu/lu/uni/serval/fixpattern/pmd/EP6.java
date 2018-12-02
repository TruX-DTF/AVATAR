package edu.lu.uni.serval.fixpattern.pmd;

import edu.lu.uni.serval.fixpattern.FixTemplate;

public class EP6 extends FixTemplate {

	/*
	 * List l = new ArrayList() --> List<T> l = new ArrayList<>().
	 */
	
	@Override
	public void generatePatches() {
		// Ignore this pattern, it won't fix any real bugs.
	}

}
