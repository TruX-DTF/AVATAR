package edu.lu.uni.serval.fixpattern.findbugs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

public class NPAlwaysNull extends FixTemplate {

	/*
	 * exp == null --> exp != null.
	 * or
	 * exp != null --> exp == null; 
	 */
	
	@Override
	public void generatePatches() {
		ITree suspStmtAst = this.getSuspiciousCodeTree();
		Map<ITree, Integer> allNullCheckExps = readAllNullCheckPredicateExpressions(suspStmtAst);
		
		for (Map.Entry<ITree, Integer> entry : allNullCheckExps.entrySet()) {
			ITree nullCheckExp = entry.getKey();
			String op = nullCheckExp.getChild(1).getLabel();
			
			ITree leftExp = nullCheckExp.getChild(0);
			ITree rightExp = nullCheckExp.getChild(2);
			String codePart1 = this.getSubSuspiciouCodeStr(this.suspCodeStartPos, leftExp.getPos() + leftExp.getLength());
			String codePart2 = this.getSubSuspiciouCodeStr(rightExp.getPos(), this.suspCodeEndPos);
			String fixedCodeStr = codePart1;
			
			if ("==".equals(op)) {
				fixedCodeStr += " != ";
			} else {
				fixedCodeStr += " == ";
			}
			fixedCodeStr += codePart2;
			this.generatePatch(fixedCodeStr);
		}
	}
	
	/**
	 * Map<ITree, Integer>: ITree - predicate exp ast, Integer - start or end pos of the key value.
	 * @param suspStmtAst
	 * @return
	 */
	public Map<ITree, Integer> readAllNullCheckPredicateExpressions(ITree suspStmtAst) {
		Map<ITree, Integer> nullCheckExps = new HashMap<>();
		ITree suspExpTree;
		if (Checker.isDoStatement(suspStmtAst.getType())) {
			List<ITree> children = suspStmtAst.getChildren();
			suspExpTree = children.get(children.size() - 1);
		} else if (Checker.withBlockStatement(suspStmtAst.getType())) {
			suspExpTree = suspStmtAst.getChild(0);
		} else {
			suspExpTree = suspStmtAst;
		}
		int suspExpTreeType = suspExpTree.getType();
		
		if (!Checker.isInfixExpression(suspExpTreeType)) {
			nullCheckExps.putAll(readConditionalExpressions(suspExpTree));
			return nullCheckExps;
		} else {
			if (Checker.isNullLiteral(suspExpTree.getChild(2).getType())) {
				nullCheckExps.put(suspExpTree, 0);
			}
		}
		
		List<ITree> subExps = suspExpTree.getChildren();
		nullCheckExps.putAll(readSubPredicateExpressions(subExps));
		return nullCheckExps;
	}
	

	
	private Map<ITree, Integer> readConditionalExpressions(ITree suspExpTree) {
		Map<ITree, Integer> nullCheckExps = new HashMap<>();
		List<ITree> children = suspExpTree.getChildren();
		for (ITree child : children) {
			if (Checker.isComplexExpression(child.getType())) {
				if (Checker.isInfixExpression(child.getType())) {
					if (Checker.isNullLiteral(child.getChild(2).getType())) {
						nullCheckExps.put(child, 0);
					}
				}
				nullCheckExps.putAll(readConditionalExpressions(child));
			}
		}
		return nullCheckExps;
	}
	
	private Map<ITree, Integer> readSubPredicateExpressions(List<ITree> subExps) {
		Map<ITree, Integer> nullCheckExps = new HashMap<>();
		ITree operator = subExps.get(1);
		String op = operator.getLabel();
		if ("||".equals(op) || "&&".equals(op)) {
			ITree leftExp = subExps.get(0);
			ITree rightExp = subExps.get(2);
			if (Checker.isInfixExpression(leftExp.getType())) {
				if (Checker.isNullLiteral(leftExp.getChild(2).getType())) {
					nullCheckExps.put(leftExp, 0);
				}
				nullCheckExps.putAll(readSubPredicateExpressions(leftExp.getChildren()));
			}
			
			if (Checker.isInfixExpression(rightExp.getType())) {
				if (Checker.isNullLiteral(rightExp.getChild(2).getType())) {
					nullCheckExps.put(rightExp, 0);
				}
				nullCheckExps.putAll(readSubPredicateExpressions(rightExp.getChildren()));
			}
			for (int index = 3, size = subExps.size(); index < size; index ++) {
				ITree subExp = subExps.get(index);
				if (Checker.isInfixExpression(subExp.getType())) {
					if (Checker.isNullLiteral(subExp.getChild(2).getType())) {
						nullCheckExps.put(subExp, 0);
					}
					nullCheckExps.putAll(readSubPredicateExpressions(subExp.getChildren()));
				}
			}
		}
		return nullCheckExps;
	}

}
