package edu.lu.uni.serval.avatar;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.fixpattern.findbugs.BCUnconfirmedCast;
import edu.lu.uni.serval.fixpattern.findbugs.DLSDeadLocalStore;
import edu.lu.uni.serval.fixpattern.findbugs.EQDoesNotOverrideEquals;
import edu.lu.uni.serval.fixpattern.findbugs.NPAlwaysNull;
import edu.lu.uni.serval.fixpattern.findbugs.NPNullOnSomePath;
import edu.lu.uni.serval.fixpattern.findbugs.UCFUselessControlFlow;
import edu.lu.uni.serval.fixpattern.findbugs.UCUselessCondition;
import edu.lu.uni.serval.fixpattern.findbugs.UCUselessCondition_;
import edu.lu.uni.serval.fixpattern.findbugs.UCUselessObject;
import edu.lu.uni.serval.fixpattern.findbugs.UPMUncalledPrivateMethod;
import edu.lu.uni.serval.fixpattern.pmd.EP1;
import edu.lu.uni.serval.fixpattern.pmd.EP2;
import edu.lu.uni.serval.fixpattern.pmd.EP3;
import edu.lu.uni.serval.fixpattern.pmd.EP4;
import edu.lu.uni.serval.fixpattern.pmd.EP5;
import edu.lu.uni.serval.fixpattern.pmd.EP8;
import edu.lu.uni.serval.fixpattern.pmd.EP9;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;
import edu.lu.uni.serval.utils.FileHelper;
import edu.lu.uni.serval.utils.SuspiciousPosition;

/**
 * AVATAR. 
 * 
 * @author kui.liu
 *
 */
public class Avatar extends AbstractFixer {
	
	private static Logger log = LoggerFactory.getLogger(Avatar.class);
	
	public Avatar(String path, String projectName, int bugId, String defects4jPath) {
		super(path, projectName, bugId, defects4jPath);
	}
	
	public Avatar(String path, String metric, String projectName, int bugId, String defects4jPath) {
		super(path, metric, projectName, bugId, defects4jPath);
	}

	@Override
	public void fixProcess() {
		// Read paths of the buggy project.
		if (!dp.validPaths) return;
		
		// Read suspicious positions.
		List<SuspiciousPosition> suspiciousCodeList = readSuspiciousCodeFromFile();
		if (suspiciousCodeList == null) return;
		
		List<SuspCodeNode> triedSuspNode = new ArrayList<>();
		log.info("=======StaticBugFixer: Start to fix suspicious code======");
		for (SuspiciousPosition suspiciousCode : suspiciousCodeList) {
			SuspCodeNode scn = parseSuspiciousCode(suspiciousCode);
			if (scn == null) continue;

//			log.debug(scn.suspCodeStr);
			if (triedSuspNode.contains(scn)) continue;
			triedSuspNode.add(scn);
	        // Match fix templates for this suspicious code with its context information.
	        fixWithMatchedFixTemplates(scn);
	        
			if (minErrorTest == 0) break;
        }
		log.info("=======StaticBugFixer: Finish off fixing======");
		
		FileHelper.deleteDirectory(Configuration.TEMP_FILES_PATH + this.dataType + "/" + this.buggyProject);
	}
	
	protected void fixWithMatchedFixTemplates(SuspCodeNode scn) {
		
		// Parse context information of the suspicious code.
		List<Integer> contextInfoList = readAllNodeTypes(scn.suspCodeAstNode);
		List<Integer> distinctContextInfo = new ArrayList<>();
		for (Integer contInfo : contextInfoList) {
			if (!distinctContextInfo.contains(contInfo)) {
				distinctContextInfo.add(contInfo);
			}
		}
		
		// generate patches with fix templates.
		FixTemplate ft = null;
		for (int contextInfo : distinctContextInfo) {
			if (Checker.isTypeDeclaration(contextInfo)) {
				ft = new EQDoesNotOverrideEquals();
			} else if (Checker.isCastExpression(contextInfo)) {
				ft = new BCUnconfirmedCast();
			} else if (Checker.isSimpleName(contextInfo) && !Checker.isForStatement(scn.suspCodeAstNode.getType())
					&& !Checker.isWhileStatement(scn.suspCodeAstNode.getType())
					&& !Checker.isDoStatement(scn.suspCodeAstNode.getType())
					&& !Checker.isEnhancedForStatement(scn.suspCodeAstNode.getType())) {
				ft = new DLSDeadLocalStore();
				generatePatches(ft, scn);
				if (this.minErrorTest == 0) break;
				ft = new NPNullOnSomePath();
			} else if (Checker.isIfStatement(contextInfo)
					|| Checker.isWhileStatement(contextInfo) 
					|| Checker.isDoStatement(contextInfo)) {
				ft = new UCUselessCondition_(false);
				generatePatches(ft, scn);
				if (this.minErrorTest == 0) break;
				ft = new UCUselessCondition();
				generatePatches(ft, scn);
				if (this.minErrorTest == 0) break;
				ft = new UCUselessCondition_(true);
				generatePatches(ft, scn);
				if (this.minErrorTest == 0) break;
				ft = new NPAlwaysNull();
			} else if (!Checker.withBlockStatement(scn.suspCodeAstNode.getType()) && Checker.isConditionalExpression(contextInfo)) {
				ft = new UCUselessCondition_(false);
				generatePatches(ft, scn);
				if (this.minErrorTest == 0) break;
				ft = new UCUselessCondition();
				generatePatches(ft, scn);
				if (this.minErrorTest == 0) break;
				ft = new UCUselessCondition_(true);
				generatePatches(ft, scn);
				if (this.minErrorTest == 0) break;
				ft = new NPAlwaysNull();
			} else if (Checker.isMethodInvocation(contextInfo)) {
				ft = new EP1();
				generatePatches(ft, scn);
				if (this.minErrorTest == 0) break;
				ft = new EP2();
			} else if (Checker.isClassInstanceCreation(contextInfo)) {
				ft = new EP3();
			} else if (Checker.isInfixExpression(contextInfo)) {
				ft = new EP4();
			}
			
			if (ft != null) {
				generatePatches(ft, scn);
				if (this.minErrorTest == 0) break;
			}
			ft = null;
		}
		
		for (int contextInfo : distinctContextInfo) {
			if (Checker.isIfStatement(contextInfo)
					|| Checker.isWhileStatement(contextInfo) 
					|| Checker.isDoStatement(contextInfo)
					|| Checker.isSwitchStatement(contextInfo)
					|| Checker.isBreakStatement(contextInfo)
					|| Checker.isContinueStatement(contextInfo)) {
				ft = new UCFUselessControlFlow();
			} else if (Checker.isVariableDeclarationStatement(contextInfo)) {
				ft = new UCUselessObject();
				generatePatches(ft, scn);
				if (this.minErrorTest == 0) break;
				ft = new EP5();
				generatePatches(ft, scn);
				if (this.minErrorTest == 0) break;
				ft = new EP8();
				generatePatches(ft, scn);
				if (this.minErrorTest == 0) break;
				ft = new EP9();
			}
			
			if (ft != null) {
				generatePatches(ft, scn);
				if (this.minErrorTest == 0) break;
			}
			
			ft = new UPMUncalledPrivateMethod();
			generatePatches(ft, scn);
			if (this.minErrorTest == 0) break;
		}
	}
	
	private void generatePatches(FixTemplate ft, SuspCodeNode scn) {
		ft.setSuspiciousCodeStr(scn.suspCodeStr);
		ft.setSuspiciousCodeTree(scn.suspCodeAstNode);
		if (scn.javaBackup == null) ft.setSourceCodePath(dp.srcPath);
		else ft.setSourceCodePath(dp.srcPath, scn.javaBackup);
		ft.generatePatches();
		List<Patch> patchCandidates = ft.getPatches();
		
		// Test generated patches.
		if (patchCandidates.isEmpty()) return;
		testGeneratedPatches(patchCandidates, scn);
	}

	private List<Integer> readAllNodeTypes(ITree suspCodeAstNode) {
		List<Integer> nodeTypes = new ArrayList<>();
		nodeTypes.add(suspCodeAstNode.getType());
		List<ITree> children = suspCodeAstNode.getChildren();
		for (ITree child : children) {
			if (Checker.isFieldDeclaration(child.getType()) || 
					Checker.isMethodDeclaration(child.getType()) ||
					Checker.isStatement(child.getType())) break;
			nodeTypes.addAll(readAllNodeTypes(child));
		}
		return nodeTypes;
	}

}
