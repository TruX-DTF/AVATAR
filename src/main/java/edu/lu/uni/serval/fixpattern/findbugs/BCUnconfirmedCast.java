package edu.lu.uni.serval.fixpattern.findbugs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

/**
 * Fix pattern for BC_UNCONFIRMED_CAST violations.
 * 
 * @author kui.liu
 *
 */
public class BCUnconfirmedCast extends FixTemplate {
	
	/*
	 * Fix Pattern:
	 * Insert instanceof checking.
	 *     if (var instanceof T) {...}
	 *     else {throw new IllegalArgumentException(...);}
	 * 
	 * 1. VariableDeclarationStatement.
	 * 2. Exp: Assignment.
	 * 3. One expression in the statement.
	 *
	 */
	
	@Override
	public void generatePatches() {
		ITree suspStmtTree = this.getSuspiciousCodeTree();
		
		Map<ITree, String> castExps = identifyCastExpressions(suspStmtTree);
		if (!castExps.isEmpty()) {
			for (Map.Entry<ITree, String> entity : castExps.entrySet()) {
				//Generate Patches with CastExpression.
				ITree castExp = entity.getKey();
				String varName = entity.getValue();
				ITree castingType = castExp.getChild(0);
				int castTypeStartPos = castingType.getPos();
				int castTypeEndPos = castTypeStartPos + castingType.getLength();
				String castTypeStr = this.getSubSuspiciouCodeStr(castTypeStartPos, castTypeEndPos);
				ITree castedExp = castExp.getChild(1);
				int castedExpStartPos = castedExp.getPos();
				int castedExpEndPos = castedExpStartPos + castedExp.getLength();
				String castedExpStr = this.getSubSuspiciouCodeStr(castedExpStartPos, castedExpEndPos);
				int castedExpType = castedExp.getType();
				
				String fixedCodeStr1 = "";
				if (Checker.isSimpleName(castedExpType) || Checker.isFieldAccess(castedExpType) 
						|| Checker.isQualifiedName(castedExpType) || Checker.isSuperFieldAccess(castedExpType)) {
					fixedCodeStr1 = "if (" + castedExpStr + " instanceof " + castTypeStr + ") {\n\t";
				} else if (Checker.isComplexExpression(castedExpType)) {
					fixedCodeStr1 = "Object _tempVar = " + castedExpStr + ";\n" +
									"if (_temVar instanceof " + castTypeStr + ") {\n\t";
					 this.getSuspiciousCodeStr().replace(castedExpStr, "_temVar");
				}
				
				int endPosition = suspCodeStartPos + suspStmtTree.getLength();
				if (!"".equals(varName)) { // statements related to variable.
					endPosition = identifyRelatedStatements(suspStmtTree, varName);
				}
				
				String fixedCodeStr2 = "\n} else {\n\tthrow new IllegalArgumentException(\"Illegal argument: " + castedExpStr + "\");\n}\n";
				generatePatch(suspCodeStartPos, endPosition, fixedCodeStr1, fixedCodeStr2);
			}
		}
	}

	private Map<ITree, String> identifyCastExpressions(ITree codeAst) {
		Map<ITree, String> castExps = new HashMap<>();
		
		List<ITree> children = codeAst.getChildren();
		if (children == null || children.isEmpty()) return castExps;
		
		int astNodeType = codeAst.getType();
		if (Checker.isVariableDeclarationStatement(astNodeType)) {
			boolean isType = true; // Identity data type
			for (ITree child : children) {
				int childNodeType = child.getType();
				if (Checker.isModifier(childNodeType)) {
					continue;
				}
				if (isType) { // Type Node.
					isType = false;
				} else { //VariableDeclarationFragment(s)
					String varName = child.getChild(0).getLabel();
					ITree assignedExp = child.getChild(1);
					if (Checker.isCastExpression(assignedExp.getType())) {
						castExps.put(assignedExp, varName);
					}
					castExps.putAll(identifyCastExpressions(assignedExp));
				}
			}
		} else if (Checker.isExpressionStatement(astNodeType)) {
			ITree expAst = children.get(0);
			int expAstType = expAst.getType();
			if (Checker.isAssignment(expAstType)) {
				String varName = expAst.getChild(0).getLabel();
				ITree subExpAst = expAst.getChild(2);
				int subExpType = subExpAst.getType();
				if (Checker.isCastExpression(subExpType)) {
					castExps.put(subExpAst, varName);
				}
				castExps.putAll(identifyCastExpressions(subExpAst));
			} else { // Other expressions.
				castExps.putAll(identifyCastExpressions(expAst));
			}
		} else if (Checker.isReturnStatement(astNodeType)) {
			ITree exp = children.get(0);
			int expType = exp.getType();
			if (Checker.isReturnStatement(expType)) { // Empty return statement, i.e., "return;".
			} else {
				if (Checker.isCastExpression(expType)) {
					castExps.put(exp, "");
				}
				castExps.putAll(identifyCastExpressions(exp));
			}
		} else if (Checker.isFieldDeclaration(astNodeType)) {
			// FIXME: 
		} else if(Checker.isComplexExpression(astNodeType) || Checker.isSimpleName(astNodeType)) { // expressions
			for (ITree child : children) {
				int childType = child.getType();
				if (Checker.isComplexExpression(childType) || Checker.isSimpleName(childType)) {
					if (Checker.isCastExpression(childType)) {
						castExps.put(child, "");
					}
					castExps.putAll(identifyCastExpressions(child));
				}
			}
		}
		return castExps;
	}

}
