package edu.lu.uni.serval.fixpattern;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.serval.avatar.Patch;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

public abstract class FixTemplate implements IFixTemplate {
	
	private static Logger log = LoggerFactory.getLogger(FixTemplate.class);
	
	protected String sourceCodePath;
	protected String suspJavaFileCode;
	
	private String suspiciousCodeStr;
	private ITree suspiciousCodeTree;
	protected int suspCodeStartPos, suspCodeEndPos;
	private List<Patch> patchesList = new ArrayList<>();
	
	protected Map<String, String> varTypesMap = new HashMap<>();
	protected List<String> allVarNamesList = new ArrayList<>();
	protected Map<String, List<String>> allVarNamesMap;
	
	protected void generatePatch(String fixedCodeStr1) {
		log.debug("Patch Candiate: " + fixedCodeStr1);
		Patch patch = new Patch();
		patch.setFixedCodeStr1(fixedCodeStr1);
		this.patchesList.add(patch);
	}
	
	protected void generatePatch(int suspCodeEndPos, String fixedCodeStr1) {
		log.debug("Patch Candiate: " + fixedCodeStr1);
		Patch patch = new Patch();
		patch.setBuggyCodeEndPos(suspCodeEndPos);
		patch.setFixedCodeStr1(fixedCodeStr1);
		this.patchesList.add(patch);
	}
	
	protected void generatePatch(int suspCodeStartPos, int suspCodeEndPos, String fixedCodeStr1, String fixedCodeStr2) {
		log.debug("Patch Candiate: " + fixedCodeStr1 + "\n" + fixedCodeStr2);
		Patch patch = new Patch();
		patch.setBuggyCodeStartPos(suspCodeStartPos);
		patch.setBuggyCodeEndPos(suspCodeEndPos);
		patch.setFixedCodeStr1(fixedCodeStr1);
		patch.setFixedCodeStr2(fixedCodeStr2);
		this.patchesList.add(patch);
	}
	
	public void setSourceCodePath(String sourceCodePath) {
		this.sourceCodePath = sourceCodePath;
	}
	
	public void setSourceCodePath(String sourceCodePath, File sourceCodeFile) {
		this.sourceCodePath = sourceCodePath;
		try {
			this.suspJavaFileCode = FileUtils.readFileToString(sourceCodeFile, "UTF-8");
		} catch (IOException e) {
			this.suspJavaFileCode = "";
		}
	}

	@Override
	public String getSuspiciousCodeStr() {
		return suspiciousCodeStr;
	}
	
	@Override
	public void setSuspiciousCodeStr(String suspiciousCodeStr) {
		this.suspiciousCodeStr = suspiciousCodeStr;
	}
	
	@Override
	public ITree getSuspiciousCodeTree() {
		return suspiciousCodeTree;
	}
	
	@Override
	public void setSuspiciousCodeTree(ITree suspiciousCodeTree) {
		this.suspiciousCodeTree = suspiciousCodeTree;
		suspCodeStartPos = suspiciousCodeTree.getPos();
		suspCodeEndPos = suspCodeStartPos + suspiciousCodeTree.getLength();
	}
	
	@Override
	public List<Patch> getPatches() {
		return patchesList;
	}
	
	@Override
	public String getSubSuspiciouCodeStr(int startPos, int endPos) {
		int beginIndex = startPos -  suspCodeStartPos;
		int endIndex = endPos - suspCodeStartPos;
		
		return this.suspiciousCodeStr.substring(beginIndex, endIndex);
	}
	
	protected int identifyRelatedStatements(ITree suspStmtTree, String varName) {
		int endPosition = this.suspCodeEndPos;;
		List<String> varNames = new ArrayList<>();
		varNames.add(varName);
		List<ITree> peerStmts = suspStmtTree.getParent().getChildren();
		boolean isFollowingPeerStmt = false;
		for (ITree peerStmt : peerStmts) {
			if (isFollowingPeerStmt) {
				boolean isRelatedStmt = containsVar(peerStmt, varNames);
				if (isRelatedStmt) {
					endPosition = peerStmt.getPos() + peerStmt.getLength();
					int peerStmtType = peerStmt.getType();
					if (Checker.isVariableDeclarationStatement(peerStmtType) ||
								(Checker.isExpressionStatement(peerStmtType) && Checker.isAssignment(peerStmt.getChild(0).getType()))) {
						varNames.add(identifyVariableName(peerStmt));
					}
				}
			} else {
				if (peerStmt == suspStmtTree) {
					isFollowingPeerStmt = true;
					int suspStmtType = suspStmtTree.getType();
					if (Checker.isVariableDeclarationStatement(suspStmtType) ||
							(Checker.isExpressionStatement(suspStmtType) && Checker.isAssignment(suspStmtTree.getChild(0).getType()))) {
						varNames.add(identifyVariableName(suspStmtTree));
					}
				}
			}
		}
		return endPosition;
	}

	protected boolean containsVar(ITree codeAst, List<String> varNames) {
		if (varNames.contains(codeAst.getLabel())) return true;
		List<ITree> children = codeAst.getChildren();
		if (children == null || children.isEmpty()) return false;
		for (ITree child : children) {
			if (Checker.isSimpleName(child.getType())) {
				String var = readVariableName(child);
				if (var != null && varNames.contains(var)) return true;
			} else if (Checker.isComplexExpression(child.getType())) {
				return containsVar(child, varNames);
			}
		}
		return false;
	}

	protected String identifyVariableName(ITree stmtAst) {
		List<ITree> children = stmtAst.getChildren();
		int stmtAstType = stmtAst.getType();
		if (Checker.isVariableDeclarationStatement(stmtAstType)) {
			for (int index = 0, size = children.size(); index < size; index ++) {
				if (!Checker.isModifier(children.get(index).getType())) {
					return children.get(index + 1).getChild(0).getLabel();
				}
			}
		} else if (Checker.isExpressionStatement(stmtAstType)) {
			return children.get(0).getChild(0).getLabel();
		} else if (Checker.isSingleVariableDeclaration(stmtAstType)) {
			for (int index = 0, size = children.size(); index < size; index ++) {
				if (!Checker.isModifier(children.get(index).getType())) {
					return children.get(index + 1).getLabel();
				}
			}
		}
		
		return null;
	}

	protected String readVariableName(ITree simpleNameAst) {
		String label = simpleNameAst.getLabel();
		if (label.startsWith("MethodName:") || label.startsWith("ClassName:")) {
			return null;
		} else if (label.startsWith("Name:")) {
			label = label.substring(5);
			if (!label.contains(".")) {
				char firstChar = label.charAt(0);
				if (Character.isUpperCase(firstChar)) {
					return null;
				}
			}
		}
		return label;
	}
	
	protected String readVariableName2(ITree simpleNameAst) {
		String label = simpleNameAst.getLabel();
		if (label.startsWith("MethodName:") || label.startsWith("ClassName:")) {
			return null;
		} else if (label.startsWith("Name:")) {
			label = label.substring(5);
		}
		return label;
	}
	
	/**
	 * Remove the type parameters of the data type.
	 * e.g., List<T> --> List.
	 * 
	 * @param returnType
	 * @return
	 */
	protected String readType(String returnType) {
		if (returnType.endsWith("[]")) {
			return readType(returnType.substring(0, returnType.length() - 2)) + "[]";
		}
		
		int index = returnType.indexOf("<");
		if (index != -1) {
			if (index == 0) {
				while (index == 0) {
					returnType = returnType.substring(returnType.indexOf(">") + 1).trim();
					index = returnType.indexOf(">");
				}
				index = returnType.indexOf("<");
				if (index == -1) index = returnType.length();
			}
			returnType = returnType.substring(0, index);
		}
		index = returnType.lastIndexOf(".");
		if (index != -1) { // && returnType.startsWith("java.")) {
			returnType = returnType.substring(index + 1);
		}

		return returnType;
	}

	protected List<String> identifySuspiciousVariables(ITree suspCodeAst) {
		List<String> allSuspVariables = new ArrayList<>();
		List<ITree> children = suspCodeAst.getChildren();
		for (ITree child : children) {
			int childType = child.getType();
			if (Checker.isStatement(childType)) {
				break;
			} else if (Checker.isSimpleName(childType)) {
				int parentType = suspCodeAst.getType();
				if ((Checker.isAssignment(parentType) || Checker.isVariableDeclarationFragment(parentType))
						&& suspCodeAst.getChildPosition(child) == 0) {
					continue;
				}
				String varName = readVariableName(child);
				if (varName != null) allSuspVariables.add(varName);
				else allSuspVariables.addAll(identifySuspiciousVariables(child));
			} else if (Checker.isQualifiedName(childType)) {
//				String label = child.getLabel();
			} else if (Checker.isFieldAccess(childType)) {
				// FieldAccess: this.var.get();
				// Without SuperFieldAccess.
				allSuspVariables.add(child.getLabel());// "this." + varName
			} else if (Checker.isComplexExpression(childType)) {
				allSuspVariables.addAll(identifySuspiciousVariables(child));
			}
		}
		return allSuspVariables;
	}
	
	/**
	 * Read the all variables by traversing the ancestral tree of the suspicious code ast.
	 * 
	 * @param codeAst
	 * @param varTypesMap
	 * @param allVarNamesList
	 * @return
	 */
	protected Map<String, List<String>> readAllVariableNames(ITree codeAst, Map<String, String> varTypesMap, List<String> allVarNamesList) {
		Map<String, List<String>> varNamesMap = new HashMap<>();
		while (true) {
			int parentTreeType = codeAst.getType();
			if (Checker.isStatement(parentTreeType)) {// variable
				readVariableDeclaration(codeAst, parentTreeType, varNamesMap, varTypesMap, allVarNamesList);
				parentTreeType = codeAst.getParent().getType();
				if (Checker.isStatement(parentTreeType) || Checker.isMethodDeclaration(parentTreeType)) {
					List<ITree> children = codeAst.getParent().getChildren();
					int index = children.indexOf(codeAst) - 1;
					for (; index >= 0; index --) {
						ITree child = children.get(index);
						int childType = child.getType();
						if (!Checker.isStatement(childType)) break;
						readVariableDeclaration(child, childType, varNamesMap, varTypesMap, allVarNamesList);
					}
				}
			} else if (Checker.isMethodDeclaration(parentTreeType)) { // parameter type.
				List<ITree> children = codeAst.getChildren();
				for (ITree child : children) {
					int childType = child.getType();
					if (Checker.isStatement(childType)) break;
					readSingleVariableDeclaration(child, childType, varNamesMap, varTypesMap, allVarNamesList);
				}
			} else if (Checker.isTypeDeclaration(parentTreeType)) {// Field
				List<ITree> children = codeAst.getChildren();
				for (ITree child : children) {
					int childType = child.getType();
					if (Checker.isFieldDeclaration(childType)) {
						List<ITree> subChildren = child.getChildren();
						boolean readVar = false;
						boolean isStatic = false;
						String varType = null;
						for (ITree subChild : subChildren) {
							if (readVar) {
								String varName = (isStatic ? "" : "this.") + subChild.getChild(0).getLabel();
								List<String> varNames = varNamesMap.get(varType);
								if (varNames == null) {
									varNames = new ArrayList<>();
								}
								varNames.add(varName);
								allVarNamesList.add(varName);
								varNamesMap.put(varType, varNames);
								varTypesMap.put(varName, varType);
							} else if (!Checker.isModifier(subChild.getType())) {
								varType = this.readType(subChild.getLabel());
								readVar = true;
							} else {
								if (subChild.getLabel().equals("static")) {
									isStatic = true;
//									break;
								}
							}
						}
					}
				}
				// TODO: fields in the super class.
				break;
			}
			
			codeAst = codeAst.getParent();
			if (codeAst == null) break;
		}
		return varNamesMap;
	}
	
	/**
	 * Read the information of a variable in the variable declaration nodes.
	 * @param stmtTree
	 * @param stmtType
	 * @param varNamesMap
	 * @param varTypesMap
	 * @param allVarNamesList
	 */
	private void readVariableDeclaration(ITree stmtTree, int stmtType, Map<String, List<String>> varNamesMap, Map<String, String> varTypesMap, List<String> allVarNamesList) {
		String varType = null;
		if (Checker.isVariableDeclarationStatement(stmtType)) {
			List<ITree> children = stmtTree.getChildren();
			boolean readVar = false;
			for (ITree child : children) {
				if (readVar) {
					String varName = child.getChild(0).getLabel();
					List<String> varNames = varNamesMap.get(varType);
					if (varNames == null) {
						varNames = new ArrayList<>();
					}
					varNames.add(varName);
					varNamesMap.put(varType, varNames);
					varTypesMap.put(varName, varType);
					allVarNamesList.add(varName);
				} else if (!Checker.isModifier(child.getType())) {
					varType = this.readType(child.getLabel());
					readVar = true;
				}
			}
		} else if (Checker.isForStatement(stmtType)) {
			ITree varDecFrag = stmtTree.getChild(0);
			if (Checker.isVariableDeclarationExpression(varDecFrag.getType())) {
				List<ITree> children = varDecFrag.getChildren();
				varType = this.readType(children.get(0).getLabel());
				for (int i = 1, size = children.size(); i < size; i ++) {
					ITree child = children.get(i);
					String varName = child.getChild(0).getLabel();
					List<String> varNames = varNamesMap.get(varType);
					if (varNames == null) {
						varNames = new ArrayList<>();
					}
					varNames.add(varName);
					varNamesMap.put(varType, varNames);
					varTypesMap.put(varName, varType);
					allVarNamesList.add(varName);
				}
			}
		} else if (Checker.isEnhancedForStatement(stmtType)) {
			ITree singleVarDec = stmtTree.getChild(0);
			readSingleVariableDeclaration(singleVarDec, singleVarDec.getType(), varNamesMap, varTypesMap, allVarNamesList);
		}
	}
	
	private void readSingleVariableDeclaration(ITree codeTree, int treeType, Map<String, List<String>> varNamesMap, Map<String, String> varTypesMap, List<String> allVarNamesList) {
		if (Checker.isSingleVariableDeclaration(treeType)) {
			List<ITree> children = codeTree.getChildren();
			int size = children.size();
			String varType = this.readType(children.get(size - 2).getLabel());
			String varName = children.get(size - 1).getLabel();
			List<String> varNames = varNamesMap.get(varType);
			if (varNames == null) {
				varNames = new ArrayList<>();
			}
			varNames.add(varName);
			varNamesMap.put(varType, varNames);
			varTypesMap.put(varName, varType);
			allVarNamesList.add(varName);
		}
	}
	
}
