package edu.lu.uni.serval.fixpattern.pmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

public class EP8 extends FixTemplate {

	/*
	 * insert 'final' modifier into Field, parameter, or variable.
	 */
	
	private List<ITree> parameterTrees = new ArrayList<>();
	private List<String> finalizedParemeters = new ArrayList<>();
	
	@Override
	public void generatePatches() {
		ITree suspTree = this.getSuspiciousCodeTree();
		int suspTreeType = suspTree.getType();
		if (Checker.isVariableDeclarationStatement(suspTreeType)) {
			List<ITree> children = suspTree.getChildren();
			boolean isFinal = false;
			for (ITree child : children) {
				if (Checker.isModifier(child.getType())) {
					if ("final".equals(child.getLabel())) {
						isFinal = true;
						break;
					}
				} else break;
			}
			if (!isFinal) {
				this.generatePatch("final " + this.getSuspiciousCodeStr());
			}
		} else if (Checker.isMethodDeclaration(suspTreeType)) {
			List<ITree> children = suspTree.getChildren();
			for (ITree child : children) {
				int childType = child.getType();
				if (Checker.isSingleVariableDeclaration(childType)) {
					List<ITree> subChildren = child.getChildren();
					boolean isFinal = false;
					for (ITree subChild : subChildren) {
						if (Checker.isModifier(subChild.getType())) {
							if ("final".equals(subChild.getLabel())) {
								isFinal = true;
								break;
							}
						} else break;
					}
					if (!isFinal) {
						parameterTrees.add(child);
						int startPos = child.getPos();
						int endPos = startPos + child.getLength();
						String finizedParameter = "final " + this.getSubSuspiciouCodeStr(startPos, endPos);
						this.finalizedParemeters.add(finizedParameter);
					}
				}
				if (Checker.isStatement(childType)) break;
			}
		} else if (Checker.isFieldDeclaration(suspTreeType)) {
			List<ITree> children = suspTree.getChildren();
			int pos = 0;
			boolean isFinal = false;
			for (ITree child : children) {
				if (Checker.isModifier(child.getType())) {
					String modifier = child.getLabel();
					if ("final".equals(modifier)) {
						isFinal = true;
						break;
					} else if ("private".equals(modifier)) {
						pos = child.getPos() + child.getLength() + 1;
					} else if ("protected".equals(modifier)) {
						pos = child.getPos() + child.getLength() + 1;
					} else if ("public".equals(modifier)) {
						pos = child.getPos() + child.getLength() + 1;
					} else if ("static".equals(modifier)) {
						pos = child.getPos() + child.getLength() + 1;
					}
				} else break;
			}
			if (!isFinal) {
				if (pos == 0) this.generatePatch("final " + this.getSuspiciousCodeStr());
				else {
					String fixedCodeStr = this.getSubSuspiciouCodeStr(this.suspCodeStartPos, pos);
					fixedCodeStr += "final " + this.getSubSuspiciouCodeStr(pos, this.suspCodeEndPos);
					this.generatePatch(fixedCodeStr);
				}
			}
		}
		
		if (!parameterTrees.isEmpty()) {
			Map<List<ITree>, List<String>> subSets = getSubSet(this.parameterTrees, this.finalizedParemeters);
			for (Map.Entry<List<ITree>, List<String>> entry : subSets.entrySet()) {
				List<ITree> buggyTrees = entry.getKey();
				List<String> fixedCodeList = entry.getValue();
				if (buggyTrees.isEmpty()) continue;
				
				ITree firstBuggyExp = buggyTrees.get(0);
				int startPos = firstBuggyExp.getPos();
				StringBuilder fixedCodeStr1 = new StringBuilder(this.getSubSuspiciouCodeStr(this.suspCodeStartPos, startPos));
				fixedCodeStr1.append(fixedCodeList.get(0));
				startPos = startPos + firstBuggyExp.getLength();
				
				for (int index = 1, size = buggyTrees.size(); index < size; index ++) {
					ITree buggyExp = buggyTrees.get(index);
					fixedCodeStr1.append(this.getSubSuspiciouCodeStr(startPos, buggyExp.getPos()));
					fixedCodeStr1.append(fixedCodeList.get(index));
					startPos = buggyExp.getPos() + buggyExp.getLength();
				}
				
				fixedCodeStr1.append(this.getSubSuspiciouCodeStr(startPos, this.suspCodeEndPos));
				
				this.generatePatch(fixedCodeStr1.toString());
			}
		}
	}
	
	public static Map<List<ITree>, List<String>> getSubSet(List<ITree> parameterTrees, List<String> finalizedParemeters) {
		Map<List<ITree>, List<String>> result = new HashMap<>();
		int length = parameterTrees.size();
		int num = length == 0 ? 0 : 1 << (length);

		for (int i = 0; i < num; i++) {
			List<ITree> subSet = new ArrayList<>();
			List<String> subSet2 = new ArrayList<>();

			int index = i;
			for (int j = 0; j < length; j++) {
				if ((index & 1) == 1) {
					subSet.add(parameterTrees.get(j));
					subSet2.add(finalizedParemeters.get(j));
				}
				index >>= 1;
			}
			result.put(subSet, subSet2);
		}
		return result;
	}

}
