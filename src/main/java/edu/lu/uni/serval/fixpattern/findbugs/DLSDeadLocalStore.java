package edu.lu.uni.serval.fixpattern.findbugs;

import java.util.ArrayList;
import java.util.List;

import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

public class DLSDeadLocalStore extends FixTemplate {
	
	/*
	 * Update Variable to another one.
	 */

	@Override
	public void generatePatches() {

		int type = this.getSuspiciousCodeTree().getType();
		List<ITree> suspVars;
		if (Checker.isVariableDeclarationStatement(type)) {
			ITree varDec = this.getSuspiciousCodeTree().getChild(this.getSuspiciousCodeTree().getChildren().size() - 1);
			if (varDec.getChildren().size() != 2) return;
			ITree codeTree = varDec.getChild(1);
			suspVars = identifyAuspiciousVariables(codeTree);
		} else if (Checker.isExpressionStatement(type) && Checker.isAssignment(this.getSuspiciousCodeTree().getChild(0).getType())) {
			ITree codeTree = this.getSuspiciousCodeTree().getChild(0).getChild(2);
			suspVars = identifyAuspiciousVariables(codeTree);
		} else {
			suspVars = identifyAuspiciousVariables(this.getSuspiciousCodeTree());
		}
		
		if (suspVars.isEmpty()) return;
		
//		List<ITree> variables = readLocalVariables(this.getSuspiciousCodeTree());
		allVarNamesMap = readAllVariableNames(this.getSuspiciousCodeTree(), varTypesMap, allVarNamesList);
		
		
		for (ITree suspVar : suspVars) {
			String codePart1;
			if (Checker.isFieldAccess(suspVar.getParent().getType())) {
				codePart1 = this.getSubSuspiciouCodeStr(suspCodeStartPos, suspVar.getParent().getPos());
			} else {
				codePart1 = this.getSubSuspiciouCodeStr(suspCodeStartPos, suspVar.getPos());
			}
			String codePart2 = this.getSubSuspiciouCodeStr(suspVar.getPos() + suspVar.getLength(), suspCodeEndPos);
			
			String suspVarName = suspVar.getLabel();
			if (suspVarName.startsWith("Name:"))
				suspVarName = suspVarName.substring(5);
			String suspVarType = varTypesMap.get(suspVarName);
			if (suspVarType == null) {
				suspVarType = varTypesMap.get("this." + suspVarName);
			}
			if ("boolean".equals(suspVarType)) {
				List<String> booleanVars = allVarNamesMap.get("boolean");
				if (booleanVars == null) continue;
				for (String var : booleanVars) {
					if (var.equals(suspVarName) || var.equals("this." + suspVarName)) continue;
					if (Character.isUpperCase(var.charAt(0))) continue;
					this.generatePatch(codePart1 + var + codePart2);
				}
			} else {
				for (String var : allVarNamesList) {
					if (var.equals(suspVarName) || var.equals("this." + suspVarName)) continue;
					if (Character.isUpperCase(var.charAt(0))) continue;
					this.generatePatch(codePart1 + var + codePart2);
				}
			}
			
		}
	}

	private List<ITree> identifyAuspiciousVariables(ITree tree) {
		List<ITree> suspVars = new ArrayList<>();
		List<ITree> children = tree.getChildren();
		for (ITree child : children) {
			int type = child.getType();
			if (Checker.isStatement(type)) break;
			if (Checker.isComplexExpression(type)) {
				suspVars.addAll(identifyAuspiciousVariables(child));
				continue;
			}
			if (Checker.isSimpleName(type)) {
				if (child.getLabel().startsWith("MethodName:")) {
					suspVars.addAll(identifyAuspiciousVariables(child));
					continue;
				}
				String var = readVariableName2(child);
				if (var == null) continue;
				if (Checker.isQualifiedName(tree.getType())) continue;
				suspVars.add(child);
			}
		}
		return suspVars;
	}

	@SuppressWarnings("unused")
	private List<ITree> readLocalVariables(ITree tree) {
		List<ITree> variables = new ArrayList<>();
		ITree parentTree = tree.getParent();
		List<ITree> peerStmts = parentTree.getChildren();
		for (ITree peerStmt : peerStmts) {
			if (peerStmt.equals(tree)) break;
			
		}
		
		if (Checker.isStatement(parentTree.getType())) {
			// TODO
		}
		return null;
	}

}
