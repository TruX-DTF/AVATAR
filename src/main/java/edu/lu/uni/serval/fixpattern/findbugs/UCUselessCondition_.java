package edu.lu.uni.serval.fixpattern.findbugs;

import java.util.ArrayList;
import java.util.List;

import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

/**
 * 
 * @author kui.liu
 *
 */
public class UCUselessCondition_ extends FixTemplate {
	
	/*
	 * a || b -> a && b;
	 */
	boolean isLogicOp = true;
	
	public UCUselessCondition_(boolean isLogicOp) {
		this.isLogicOp = isLogicOp;
	}
	
	@Override
	public void generatePatches() {
		ITree suspStmtAst = this.getSuspiciousCodeTree();
		List<ITree> allInfixExps = readAllSuspiciousPredicateExpressions(suspStmtAst);
		
		for (int i = allInfixExps.size() - 1; i >= 0; i --) {
			ITree suspExpAst = allInfixExps.get(i);
			fixOperatore(suspExpAst);
		}
	}

	private void fixOperatore(ITree suspExpTree) {
		ITree operator = suspExpTree.getChild(1);
		int startPos = operator.getPos();
		int endPos = suspExpTree.getChild(2).getPos();
		String codePart1 = this.getSubSuspiciouCodeStr(suspCodeStartPos, startPos);
		String codePart2 = this.getSubSuspiciouCodeStr(endPos, suspCodeEndPos);
		
		String op = operator.getLabel();
		
		if (isLogicOp) {
			if ("&&".equals(op)) {
				String fixedCodeStr1 = codePart1 + " || " + codePart2;
				this.generatePatch(fixedCodeStr1);
			} else if ("||".equals(op)) {
				String fixedCodeStr1 = codePart1 + " && " + codePart2;
				this.generatePatch(fixedCodeStr1);
			} 
		} else {
			if ("==".equals(op)) {
				String fixedCodeStr1 = codePart1 + " != " + codePart2;
				this.generatePatch(fixedCodeStr1);
			} else if ("!=".equals(op)) {
				String fixedCodeStr1 = codePart1 + " == " + codePart2;
				this.generatePatch(fixedCodeStr1);
			} else if (">".equals(op)) {
				String fixedCodeStr1 = codePart1 + " >= " + codePart2;
				this.generatePatch(fixedCodeStr1);
				fixedCodeStr1 = codePart1 + " <= " + codePart2;
				this.generatePatch(fixedCodeStr1);
				fixedCodeStr1 = codePart1 + " < " + codePart2;
				this.generatePatch(fixedCodeStr1);
			} else if (">=".equals(op)) {
				String fixedCodeStr1 = codePart1 + " > " + codePart2;
				this.generatePatch(fixedCodeStr1);
				fixedCodeStr1 = codePart1 + " < " + codePart2;
				this.generatePatch(fixedCodeStr1);
				fixedCodeStr1 = codePart1 + " <= " + codePart2;
				this.generatePatch(fixedCodeStr1);
			} else if ("<".equals(op)) {
				String fixedCodeStr1 = codePart1 + " <= " + codePart2;
				this.generatePatch(fixedCodeStr1);
				fixedCodeStr1 = codePart1 + " >= " + codePart2;
				this.generatePatch(fixedCodeStr1);
				fixedCodeStr1 = codePart1 + " > " + codePart2;
				this.generatePatch(fixedCodeStr1);
			} else if ("<=".equals(op)) {
				String fixedCodeStr1 = codePart1 + " < " + codePart2;
				this.generatePatch(fixedCodeStr1);
				fixedCodeStr1 = codePart1 + " > " + codePart2;
				this.generatePatch(fixedCodeStr1);
				fixedCodeStr1 = codePart1 + " >= " + codePart2;
				this.generatePatch(fixedCodeStr1);
			}
		}
	}
	
	/**
	 * Map<ITree, Integer>: ITree - predicate exp ast, Integer - start or end pos of the key value.
	 * @param suspStmtAst
	 * @return
	 */
	public List<ITree> readAllSuspiciousPredicateExpressions(ITree suspStmtAst) {
		List<ITree> infixExps = new ArrayList<>();
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
			infixExps.addAll(readConditionalExpressions(suspExpTree));
			return infixExps;
		}
		infixExps.add(suspExpTree);
		
		List<ITree> subExps = suspExpTree.getChildren();
		infixExps.addAll(readSubPredicateExpressions(subExps));
		return infixExps;
	}
	
	private List<ITree> readConditionalExpressions(ITree suspExpTree) {
		List<ITree> infixExps = new ArrayList<>();
		List<ITree> children = suspExpTree.getChildren();
		for (ITree child : children) {
			if (Checker.isComplexExpression(child.getType())) {
				if (Checker.isInfixExpression(child.getType())) {
					infixExps.add(child);
				}
				infixExps.addAll(readConditionalExpressions(child));
			}
		}
		return infixExps;
	}

	private List<ITree> readSubPredicateExpressions(List<ITree> subExps) {
		List<ITree> infixExps = new ArrayList<>();
		ITree operator = subExps.get(1);
		String op = operator.getLabel();
		if ("||".equals(op) || "&&".equals(op)) {
			ITree leftExp = subExps.get(0);
			ITree rightExp = subExps.get(2);
			if (Checker.isInfixExpression(leftExp.getType())) {
				infixExps.add(leftExp);
				infixExps.addAll(readSubPredicateExpressions(leftExp.getChildren()));
			}
			if (Checker.isInfixExpression(rightExp.getType())) {
				infixExps.add(rightExp);
				infixExps.addAll(readSubPredicateExpressions(rightExp.getChildren()));
			}
			for (int index = 3, size = subExps.size(); index < size; index ++) {
				ITree subExp = subExps.get(index);
				if (Checker.isInfixExpression(subExp.getType())) {
					infixExps.add(subExp);
					infixExps.addAll(readSubPredicateExpressions(subExp.getChildren()));
				}
			}
		}
		return infixExps;
	}
	
}
