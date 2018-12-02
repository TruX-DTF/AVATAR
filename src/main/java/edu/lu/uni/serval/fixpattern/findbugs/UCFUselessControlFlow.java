package edu.lu.uni.serval.fixpattern.findbugs;

import java.util.List;

import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

public class UCFUselessControlFlow extends FixTemplate {

	/*
	 * Fix Pattern:
	 * 
	 * 1. DEL IfStatement@...
	 * 2. DEL SwithStatement@...
	 */
	
	@Override
	public void generatePatches() {
		int endPos1 = 0;
		List<ITree> children = this.getSuspiciousCodeTree().getChildren();
		int size = children.size();
		for (int index = 0; index < size; index ++) {
			ITree child = children.get(index);
			if (Checker.isStatement(child.getType())) {
				endPos1 = child.getPos();
				break;
			}
		}
		if (endPos1 == 0) {
			// No Statement in the control flow.
			return;
		}
		ITree lastStmt = children.get(size - 1);
		int endPos2 = lastStmt.getPos() + lastStmt.getLength();
		
		String fixedCodeStr1 = this.getSubSuspiciouCodeStr(endPos1, endPos2);
		
		this.generatePatch(fixedCodeStr1); // Just remove the control flow.
		this.generatePatch(""); // Remove all code in the control flow.
	}

}
