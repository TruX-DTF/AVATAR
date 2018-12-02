package edu.lu.uni.serval.fixpattern.pmd;

import java.util.ArrayList;
import java.util.List;

import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

public class EP3 extends FixTemplate {

	/*
	 *
	 * https://dzone.com/articles/fileinputstream-fileoutputstream-considered-harmful
	 * new FileInputStream --> Files.newInputStream.
	 * new FileOutputStream --> Files.newOutputStream.
	 */
	
	private List<ITree> buggyFISExps = new ArrayList<>();
	private List<ITree> fisTypes = new ArrayList<>();
	private List<ITree> buggyFOSExps = new ArrayList<>();
	private List<ITree> fosTypes = new ArrayList<>();
	
	@Override
	public void generatePatches() {
		ITree tree = this.getSuspiciousCodeTree();
		findBuggyExpressions(tree);
		if (buggyFISExps.isEmpty() && buggyFOSExps.isEmpty()) return;
		
		if (!buggyFISExps.isEmpty()) {
			ITree firstBuggyExp = buggyFISExps.get(0);
			int startPos = firstBuggyExp.getPos();
			StringBuilder fixedCodeStr1 = new StringBuilder(this.getSubSuspiciouCodeStr(this.suspCodeStartPos, startPos));
			
			fixedCodeStr1.append(generatedFix(firstBuggyExp, fisTypes.get(0)));
			startPos = startPos + firstBuggyExp.getLength();
			
			fixedCodeStr1.append(this.getSubSuspiciouCodeStr(startPos, this.suspCodeEndPos));
			
			this.generatePatch(fixedCodeStr1.toString());
		} else if (!buggyFOSExps.isEmpty()) {
			ITree firstBuggyExp = buggyFOSExps.get(0);
			int startPos = firstBuggyExp.getPos();
			StringBuilder fixedCodeStr1 = new StringBuilder(this.getSubSuspiciouCodeStr(this.suspCodeStartPos, startPos));
			
			fixedCodeStr1.append(generatedFix(firstBuggyExp, fosTypes.get(0)));
			startPos = startPos + firstBuggyExp.getLength();
			
			fixedCodeStr1.append(this.getSubSuspiciouCodeStr(startPos, this.suspCodeEndPos));
			
			this.generatePatch(fixedCodeStr1.toString());
		}
		
	}

	private void findBuggyExpressions(ITree tree) {
		List<ITree> children = tree.getChildren();
		
		for (ITree child : children) {
			int type = child.getType();
			if (Checker.isComplexExpression(type)) {
				if (Checker.isClassInstanceCreation(type)) {
					List<ITree> subChildren = child.getChildren();
					boolean isType = false;
					for (ITree subChild : subChildren) {
						int subChildType = subChild.getType();
						if (isType) {
							String typeStr = subChild.getLabel();
							if ("FileInputStream".equals(typeStr)) {
								buggyFISExps.add(child);
								fisTypes.add(subChild);
							} else if ("FileOutputStream".equals(typeStr)) {
								buggyFOSExps.add(child);
								fosTypes.add(subChild);
							}
							break;
						} else if (Checker.isNewKeyword(subChildType)) {
							isType = true;
						}
					}
				}
				findBuggyExpressions(child);
			} else if (Checker.isStatement(type)) {
				break;
			}
		}
	}

	private String generatedFix(ITree buggyExp, ITree typeTree) {
		String typeStr = typeTree.getLabel();
		String typeStr2;
		if ("FileInputStream".equals(typeStr)) {
			typeStr2 = "java.nio.file.Files.newInputStream(java.nio.file.Paths.get";
		} else {
			typeStr2 = "java.nio.file.Files.newOutputStream(java.nio.file.Paths.get";
		}
		
		int startPos = buggyExp.getPos() + typeTree.getPos();
		int endPos = buggyExp.getPos() + buggyExp.getLength();
		String arg = this.getSubSuspiciouCodeStr(startPos, endPos);
		
		return typeStr2 + arg + ")";
	}

}
