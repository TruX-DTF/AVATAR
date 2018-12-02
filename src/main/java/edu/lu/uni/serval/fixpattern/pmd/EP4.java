package edu.lu.uni.serval.fixpattern.pmd;

import java.util.ArrayList;
import java.util.List;

import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

public class EP4 extends FixTemplate {

	/*
	 * list.size() == 0 --> list.isEmpty().
	 * list.size() != 0 --> !list.isEmpty().
	 */
	
	private List<ITree> buggyExps = new ArrayList<>();
	private List<String> operators = new ArrayList<>();
	private List<String> collectionExps = new ArrayList<>();
	
	@Override
	public void generatePatches() {
		ITree tree = this.getSuspiciousCodeTree();
		findBuggyExpressions(tree);
		if (buggyExps.isEmpty()) return;
		
		ITree firstBuggyExp = buggyExps.get(0);
		int startPos = firstBuggyExp.getPos();
		StringBuilder fixedCodeStr1 = new StringBuilder(this.getSubSuspiciouCodeStr(this.suspCodeStartPos, startPos));
		fixedCodeStr1.append(generatedFix(operators.get(0), collectionExps.get(0)));
		startPos = startPos + firstBuggyExp.getLength();
		
		for (int index = 1, size = buggyExps.size(); index < size; index ++) {
			ITree buggyExp = buggyExps.get(index);
			fixedCodeStr1.append(this.getSubSuspiciouCodeStr(startPos, buggyExp.getPos()));
			fixedCodeStr1.append(generatedFix(operators.get(index), collectionExps.get(index)));
			startPos = buggyExp.getPos() + buggyExp.getLength();
		}
		
		fixedCodeStr1.append(this.getSubSuspiciouCodeStr(startPos, this.suspCodeEndPos));
		
		this.generatePatch(fixedCodeStr1.toString());
	}

	private void findBuggyExpressions(ITree tree) {
		List<ITree> children = tree.getChildren();
		
		for (ITree child : children) {
			int type = child.getType();
			if (Checker.isComplexExpression(type)) {
				if (Checker.isInfixExpression(type)) {
					List<ITree> subChildren = child.getChildren();
					String op = subChildren.get(1).getLabel();
					if ("==".equals(op) || "!=".equals(op)) {
						ITree exp = null;
						if ("0".equals(subChildren.get(2).getLabel())) {
							exp = subChildren.get(0);
						} else if ("0".equals(subChildren.get(0).getLabel())) {
							exp = subChildren.get(2);
						}
						if (exp != null && Checker.isMethodInvocation(exp.getType())) {
							int startPos = exp.getPos();
							int endPos = startPos + exp.getLength();
							String collectionExp = this.getSubSuspiciouCodeStr(startPos, endPos);
							if (collectionExp.endsWith(".size()")) {
								buggyExps.add(child);
								operators.add(op);
								collectionExps.add(collectionExp);
							}
						}
					}
				}
				findBuggyExpressions(child);
			} else if (Checker.isStatement(type)) {
				break;
			}
		}
	}

	private String generatedFix(String op, String collectionExp) {
		collectionExp = collectionExp.substring(0, collectionExp.length() - 6) + "isEmpty()";
		if ("!=".equals(op)) {
			collectionExp = "!" + collectionExp;
		}
		return collectionExp;
	}

}
