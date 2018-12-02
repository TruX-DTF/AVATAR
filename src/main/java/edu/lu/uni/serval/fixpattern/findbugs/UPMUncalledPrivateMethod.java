package edu.lu.uni.serval.fixpattern.findbugs;

import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

public class UPMUncalledPrivateMethod extends FixTemplate {

	/*
	 * Fix Pattern:
	 * 
	 * DEL MethodDeclaration@...
	 */
	@Override
	public void generatePatches() {
		ITree suspCodeTree = this.getSuspiciousCodeTree();
		ITree parentTree = suspCodeTree.getParent();
		while (true) {
			if (Checker.isMethodDeclaration(parentTree.getType())) break;
			parentTree = parentTree.getParent();
			if (parentTree == null) break;
		}
		
		if (parentTree == null) return;
		
		int startPos = parentTree.getPos();
		int endPos = startPos + parentTree.getLength();
		
		String fixedCodeStr1 = "";// Replace the buggy code with empty string.
		this.generatePatch(startPos, endPos, fixedCodeStr1, null);
	}

}
