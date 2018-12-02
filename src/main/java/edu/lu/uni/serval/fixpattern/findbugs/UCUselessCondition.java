package edu.lu.uni.serval.fixpattern.findbugs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

/**
 * Fix pattern for UC_USELESS_CONDITION violations.
 * 
 * @author kui.liu
 *
 */
public class UCUselessCondition extends FixTemplate {

	/*
	 * UPD IfStatement@
	 * ---UPD InfixExpression@
	 * ------DEL Expression@
	 * 
	 */
	@Override
	public void generatePatches() {
		ITree suspStmtAst = this.getSuspiciousCodeTree();
		Map<ITree, Integer> allPredicateExps = readAllSuspiciousPredicateExpressions(suspStmtAst);
		
		for (Map.Entry<ITree, Integer> entry : allPredicateExps.entrySet()) {
			ITree predicateExp = entry.getKey();
			
			if (Checker.isConditionalExpression(predicateExp.getType())) {
				int startPos = predicateExp.getPos();
				int endPos = startPos + predicateExp.getLength();
				String fixedCodeStr1 = this.getSubSuspiciouCodeStr(suspCodeStartPos, startPos);
				String fixedCodeStr2 = this.getSubSuspiciouCodeStr(endPos, suspCodeEndPos);
				
				ITree thenExp = predicateExp.getChild(1);
				startPos = thenExp.getPos();
				endPos = startPos + thenExp.getLength();
				String fixedCodeStr = fixedCodeStr1 + this.getSubSuspiciouCodeStr(startPos, endPos) + fixedCodeStr2;
				this.generatePatch(fixedCodeStr);
				
				ITree elseExp = predicateExp.getChild(2);
				startPos = elseExp.getPos();
				endPos = startPos + elseExp.getLength();
				fixedCodeStr = fixedCodeStr1 + this.getSubSuspiciouCodeStr(startPos, endPos) + fixedCodeStr2;
				this.generatePatch(fixedCodeStr);
				
				continue;
			}
			
			int pos = entry.getValue();
			int predicateExpStartPos = predicateExp.getPos();
			
			String fixedCodeStr1;
			if (pos == 0) {
				continue;
			} else if (pos > predicateExpStartPos) {
				fixedCodeStr1 = this.getSubSuspiciouCodeStr(suspCodeStartPos, predicateExpStartPos);
				fixedCodeStr1 += this.getSubSuspiciouCodeStr(pos, suspCodeEndPos);
			} else {
				fixedCodeStr1 = this.getSubSuspiciouCodeStr(suspCodeStartPos, pos);
				fixedCodeStr1 += this.getSubSuspiciouCodeStr(predicateExpStartPos + predicateExp.getLength(), suspCodeEndPos);
			}
			this.generatePatch(fixedCodeStr1);
		}
	}
	
	/**
	 * Map<ITree, Integer>: ITree - predicate exp ast, Integer - start or end pos of the key value.
	 * @param suspStmtAst
	 * @return
	 */
	public Map<ITree, Integer> readAllSuspiciousPredicateExpressions(ITree suspStmtAst) {
		Map<ITree, Integer> predicateExps = new HashMap<>();
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
			if (Checker.isStatement(suspExpTreeType)) {
				predicateExps.putAll(readConditionalExpressions(suspExpTree));
			} else {
				predicateExps.put(suspExpTree, 0);
			}
			return predicateExps;
		}
		predicateExps.put(suspExpTree, 0);
		
		List<ITree> subExps = suspExpTree.getChildren();
		predicateExps.putAll(readSubPredicateExpressions(subExps));
		return predicateExps;
	}
	
	private Map<ITree, Integer> readConditionalExpressions(ITree suspExpTree) {
		Map<ITree, Integer> predicateExps = new HashMap<>();
		List<ITree> children = suspExpTree.getChildren();
		for (ITree child : children) {
			if (Checker.isComplexExpression(child.getType())) {
				if (Checker.isConditionalExpression(child.getType())) {
					predicateExps.put(child, child.getPos());
				}
				predicateExps.putAll(readConditionalExpressions(child));
			}
		}
		return predicateExps;
	}

	private Map<ITree, Integer> readSubPredicateExpressions(List<ITree> subExps) {
		Map<ITree, Integer> predicateExps = new HashMap<>();
		ITree operator = subExps.get(1);
		String op = operator.getLabel();
		if ("||".equals(op) || "&&".equals(op)) {
			ITree leftExp = subExps.get(0);
			ITree rightExp = subExps.get(2);
			predicateExps.put(leftExp, rightExp.getPos());
			if (Checker.isInfixExpression(leftExp.getType())) {
				predicateExps.putAll(readSubPredicateExpressions(leftExp.getChildren()));
			}
			predicateExps.put(rightExp, operator.getPos());
			if (Checker.isInfixExpression(rightExp.getType())) {
				predicateExps.putAll(readSubPredicateExpressions(rightExp.getChildren()));
			}
			for (int index = 3, size = subExps.size(); index < size; index ++) {
				ITree subExp = subExps.get(index);
				ITree prevExp = subExps.get(index - 1);
				int pos = prevExp.getPos() + prevExp.getLength();
				predicateExps.put(subExp, pos);
				if (Checker.isInfixExpression(subExp.getType())) {
					predicateExps.putAll(readSubPredicateExpressions(subExp.getChildren()));
				}
			}
		}
		return predicateExps;
	}

}
