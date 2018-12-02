package edu.lu.uni.serval.fixpattern.pmd;

import java.util.ArrayList;
import java.util.List;

import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

public class EP1 extends FixTemplate {

	/*
	 * sb.append("a") --> sb.append(’a’). 
	 */
	
	@Override
	public void generatePatches() {
		ITree tree = this.getSuspiciousCodeTree();
		List<ITree> buggyExps = findBuggyExpressions(tree);
		if (buggyExps.isEmpty()) return;
		
		ITree firstBuggyExp = buggyExps.get(0);
		int startPos = firstBuggyExp.getPos();
		StringBuilder fixedCodeStr = new StringBuilder(this.getSubSuspiciouCodeStr(this.suspCodeStartPos, startPos));
		fixedCodeStr.append(firstBuggyExp.getLabel().replace("\"", "'"));
		startPos = startPos + firstBuggyExp.getLength();
		
		for (int index = 1, size = buggyExps.size(); index < size; index ++) {
			ITree buggyExp = buggyExps.get(index);
			fixedCodeStr.append(this.getSubSuspiciouCodeStr(startPos, buggyExp.getPos()));
			fixedCodeStr.append(buggyExp.getLabel().replace("\"", "'"));
			startPos = buggyExp.getPos() + buggyExp.getLength();
		}
		
		fixedCodeStr.append(this.getSubSuspiciouCodeStr(startPos, this.suspCodeEndPos));
		
		this.generatePatch(fixedCodeStr.toString());
	}

	private List<ITree> findBuggyExpressions(ITree tree) {
		List<ITree> children = tree.getChildren();
		List<ITree> buggyExps = new ArrayList<>();
		
		for (ITree child : children) {
			int type = child.getType();
			if (Checker.isComplexExpression(type)) {
				if (Checker.isMethodInvocation(type)) {
					if (child.getLabel().startsWith("MethodName:append:")) {
						List<ITree> args = child.getChildren();
						if (args.size() == 1) {
							ITree arg = args.get(0);
							if (Checker.isStringLiteral(arg.getType())) {
								String label = arg.getLabel();
								if (label.length() == 3) buggyExps.add(arg);
								continue;
							}
						}
					}
				}
				buggyExps.addAll(findBuggyExpressions(child));
			} else if (Checker.isSimpleName(type)) {
				String childLabel = child.getLabel();
				if (childLabel.startsWith("MethodName:")) {
					List<ITree> args = child.getChildren();
					if (childLabel.startsWith("MethodName:append:")) {
						if (args.size() == 1) {
							ITree arg = args.get(0);
							if (Checker.isStringLiteral(arg.getType())) {
								String label = arg.getLabel();
								if (label.length() == 3) buggyExps.add(arg);
								continue;
							}
						}
					}
					for (ITree arg : args) {
						buggyExps.addAll(findBuggyExpressions(arg));
					}
				}
			} else if (Checker.isStatement(type)) {
				break;
			}
		}
		
		return buggyExps;
	}

}
