package edu.lu.uni.serval.fixpattern.pmd;

import java.util.ArrayList;
import java.util.List;

import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

public class EP5 extends FixTemplate {
	
	/* 
	 * synchronized:
	 * StringBuilder --> StringBuffer.
	 */

	private List<ITree> buggyExps = new ArrayList<>();
	
	@Override
	public void generatePatches() {
		ITree tree = this.getSuspiciousCodeTree();
		findBuggyExpressions(tree);
		if (buggyExps.isEmpty()) return;
		
		ITree firstBuggyExp = buggyExps.get(0);
		int startPos = firstBuggyExp.getPos();
		StringBuilder fixedCodeStr1 = new StringBuilder(this.getSubSuspiciouCodeStr(this.suspCodeStartPos, startPos));
		fixedCodeStr1.append("StringBuffer");
		startPos = startPos + firstBuggyExp.getLength();
		
		for (int index = 1, size = buggyExps.size(); index < size; index ++) {
			ITree buggyExp = buggyExps.get(index);
			fixedCodeStr1.append(this.getSubSuspiciouCodeStr(startPos, buggyExp.getPos()));
			fixedCodeStr1.append("StringBuffer");
			startPos = buggyExp.getPos() + buggyExp.getLength();
		}
		
		fixedCodeStr1.append(this.getSubSuspiciouCodeStr(startPos, this.suspCodeEndPos));
		
		this.generatePatch(fixedCodeStr1.toString());
	}

	private void findBuggyExpressions(ITree tree) {
		if (Checker.isVariableDeclarationStatement(tree.getType())) {
			List<String> variables = new ArrayList<>();
			List<ITree> children = tree.getChildren();
			boolean readVar = false;
			for (ITree child : children) {
				if (readVar) {
					String varName = child.getChild(0).getLabel();
					variables.add(varName);
				} else if (!Checker.isModifier(child.getType())) {
					String varType = child.getLabel();
					if (!"StringBuilder".equals(varType)) break;
					readVar = true;
					buggyExps.add(child);
				}
			}
			
			// synchronization context.
			ITree parent = tree.getParent();
			while (true) {
				int parentType = parent.getType();
				if (Checker.isSynchronizedStatement(parentType)) {
					break;
				} else if (Checker.isMethodDeclaration(parentType) || Checker.isTypeDeclaration(parentType)) {
					List<ITree> subChildren = parent.getChildren();
					boolean isSynchronized = false;
					for (ITree subChild : subChildren) {
						if (Checker.isModifier(subChild.getType())) {
							if ("synchronized".equals(subChild.getLabel())) {
								isSynchronized = true;
								break;
							}
						} else break;
					}
					if (isSynchronized) break;
				}
				parent = parent.getParent();
				if (parent == null) {
					variables.clear();
					buggyExps.clear();
					break;
				}
			}
			
			if (!variables.isEmpty()) {
				identifyBuggyTypes(variables, tree);
			}
		}
	}

	private void identifyBuggyTypes(List<String> variables, ITree tree) {
		List<ITree> stmts = tree.getParent().getChildren();
		int index = stmts.indexOf(tree) + 1;
		for (int size = stmts.size(); index < size; index ++) {
			ITree stmt = stmts.get(index);
			int stmtType = stmt.getType();
			if (Checker.isExpressionStatement(stmtType)) {
				ITree exp = stmt.getChild(0);
				if (Checker.isAssignment(exp.getType())) {
					if (variables.contains(exp.getChild(0).getLabel())) {
						ITree rightHandExp = exp.getChild(2);
						if (Checker.isClassInstanceCreation(rightHandExp.getType())) {
							List<ITree> subChildren = rightHandExp.getChildren();
							boolean isType = false;
							for (ITree subChild : subChildren) {
								int subChildType = subChild.getType();
								if (isType) {
									buggyExps.add(subChild);
									break;
								} else if (Checker.isNewKeyword(subChildType)) {
									isType = true;
								}
							}
						}
					}
				}
			} else if (Checker.withBlockStatement(stmtType)) {
				identifyBuggyTypes(variables, stmt);
			}
		}
	}

}
