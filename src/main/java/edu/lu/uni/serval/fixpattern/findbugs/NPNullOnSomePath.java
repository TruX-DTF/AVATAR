package edu.lu.uni.serval.fixpattern.findbugs;

import java.util.ArrayList;
import java.util.List;

import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

/**
 * Fix pattern for NP_NULL_ON_SOME_PATH violations.
 * 
 * @author kui.liu
 *
 */
public class NPNullOnSomePath extends FixTemplate {

	/*
	 * 1. if (var != null) {...}.
	 * 2. ReturnType of the method declaration.
	 * 	  if (var == null) {
	 * 	  	  return ...;
	 *    }
	 * 3. throw an exception.
	 *    if (var == null) {
	 * 	  	  throw ...;
	 *    }
	 */
	
	@Override
	public void generatePatches() {
		ITree suspCodeAst = this.getSuspiciousCodeTree();
		List<String> allSuspVariables_ = identifySuspiciousVariables(suspCodeAst);
		List<String> allSuspVariables = new ArrayList<>();
		for (String var : allSuspVariables_) {
			if (!allSuspVariables.contains(var)) {
				allSuspVariables.add(var);
			}
		}
		allVarNamesMap = readAllVariableNames(suspCodeAst, varTypesMap, allVarNamesList);
		
		String returnType = readReturnType(suspCodeAst);
		for (String varName : allSuspVariables) {
			
			String varType = varTypesMap.get(varName);
			
			if ("int".equals(varType) || "long".equals(varType) || "short".equals(varType) || "byte".equals(varType)
					|| "float".equals(varType) || "double".equals(varType) || "char".equals(varType) || "boolean".equals(varType)) continue;
			
			if (suspCodeAst.getLabel().replace(" ", "").contains(varName + "!=null") 
					|| suspCodeAst.getLabel().replace(" ", "").contains(varName + "==null")) continue;
			// Patch 1.
			String fixedCodeStr1 = "if (" + varName + " != null) {\n\t";
			String fixedCodeStr2 = "\n}\n";
			int suspCodeEndPos = identifyRelatedStatements(suspCodeAst, varName);
			this.generatePatch(suspCodeStartPos, suspCodeEndPos, fixedCodeStr1, fixedCodeStr2);
			
			if (!returnType.isEmpty()) {
				// Patch 3.
				fixedCodeStr1 = "if (" + varName + " == null) {\n    throw new IllegalArgumentException(\"Null '" + varName + "' argument.\");\n}\n";
				this.generatePatch(suspCodeStartPos, fixedCodeStr1);
				
				
				// Patch 2.
				fixedCodeStr1 = "if (" + varName + " == null) {\n    return";
				if ("void".equals(returnType)) {
				} else if ("float".equals(returnType) || "double".equals(returnType)) {
					fixedCodeStr1 += " 0.0";
				} else if ("int".equals(returnType) || "long".equals(returnType)) {
					fixedCodeStr1 += " 0";
				} else if ("boolean".equalsIgnoreCase(returnType)) {
					fixedCodeStr1 += " false";
				} else {
					fixedCodeStr1 += " null";
				}
				fixedCodeStr1 += ";\n}\n";
				this.generatePatch(suspCodeStartPos, fixedCodeStr1);
			}
		}
	}

	private String readReturnType(ITree suspCodeAst) {
		ITree methodDeclarationTree = null;
		if (Checker.isMethodDeclaration(suspCodeAst.getType())) {
			methodDeclarationTree = suspCodeAst;
		} else  {
			methodDeclarationTree = suspCodeAst.getParent();
			do {
				if (methodDeclarationTree == null) return "";
				if (Checker.isMethodDeclaration(methodDeclarationTree.getType())) {// MethodDeclaration
					break;
				}
				methodDeclarationTree = methodDeclarationTree.getParent();
			} while (true);
		}
		
		String label = methodDeclarationTree.getLabel();
		String returnTypeStr = label.substring(label.indexOf("@@") + 2);
		returnTypeStr = returnTypeStr.substring(0, returnTypeStr.indexOf("MethodName:"));
		int index = returnTypeStr.indexOf("@@tp:");
		if (index > 0) index += 2;
		else index = returnTypeStr.length() - 2;
		returnTypeStr = returnTypeStr.substring(0, index);
		return returnTypeStr;
	}

}
