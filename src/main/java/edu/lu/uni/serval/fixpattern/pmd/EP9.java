package edu.lu.uni.serval.fixpattern.pmd;

import java.util.List;

import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

public class EP9 extends FixTemplate {

	/*
	 * String path = "../../.."; --> java.nio.Path path = "../../..";
	 */
	
	@Override
	public void generatePatches() {
		ITree tree = this.getSuspiciousCodeTree();
		findAndMutateBuggyExpressions(tree);
	}

	private void findAndMutateBuggyExpressions(ITree tree) {
		if (Checker.isVariableDeclarationStatement(tree.getType())) {
			List<ITree> children = tree.getChildren();
			ITree varTypeTree = null;
			boolean readVar = false;
			for (ITree child : children) {
				if (readVar) {
					List<ITree> subChildren = child.getChildren();
					if (subChildren.size() == 2) {
						ITree assignedExp = subChildren.get(1);
						if (Checker.isStringLiteral(assignedExp.getType())) {
							String assignedValue = assignedExp.getLabel();
							if (assignedValue.contains("/") || assignedValue.contains("\\\\")) {
								int startPos = varTypeTree.getPos();
								StringBuilder fixedCodeStr1 = new StringBuilder(this.getSubSuspiciouCodeStr(this.suspCodeStartPos, startPos));
								fixedCodeStr1.append("java.nio.Path");
								startPos = startPos + varTypeTree.getLength();
								fixedCodeStr1.append(this.getSubSuspiciouCodeStr(startPos, this.suspCodeEndPos));
								this.generatePatch(fixedCodeStr1.toString());
							}
						}
					}
				} else if (!Checker.isModifier(child.getType())) {
					String varType = child.getLabel();
					if (!"String".equals(varType)) break;
					readVar = true;
					varTypeTree = child;
				}
			}
		}
	}

}
