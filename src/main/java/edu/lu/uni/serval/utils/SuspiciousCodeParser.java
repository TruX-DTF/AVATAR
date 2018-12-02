package edu.lu.uni.serval.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.lu.uni.serval.AST.ASTGenerator;
import edu.lu.uni.serval.AST.ASTGenerator.TokenType;
import edu.lu.uni.serval.jdt.tree.ITree;

/**
 * Parse the suspicious code into an AST.
 * 
 * @author kui.liu
 *
 */
public class SuspiciousCodeParser {

	private File javaFile;
	private CompilationUnit unit = null;
	private ITree suspiciousCodeAstNode = null;
	private String suspiciousCodeStr = null;
	
	private BuggyMethod buggyMethod = null;
	
	public void parseSuspiciousCode(File javaFile, int suspLineNum) {
		this.javaFile = javaFile;
		unit = new MyUnit().createCompilationUnit(javaFile);
		ITree rootTree = new ASTGenerator().generateTreeForJavaFile(javaFile, TokenType.EXP_JDT);
		identifySuspiciousCodeAst(rootTree, suspLineNum);
	}

	public void parseSuspiciousMethod(File javaFile, int buggyLine) {
		this.javaFile = javaFile;
		unit = new MyUnit().createCompilationUnit(javaFile);
		ITree rootTree = new ASTGenerator().generateTreeForJavaFile(javaFile, TokenType.EXP_JDT);
		identifySuspiciousMethodAst(rootTree, buggyLine);
	}

	private void identifySuspiciousCodeAst(ITree tree, int suspLineNum) {
		List<ITree> children = tree.getChildren();
		
		for (ITree child : children) {
			int startPosition = child.getPos();
			int endPosition = startPosition + child.getLength();
			int startLine = this.unit.getLineNumber(startPosition);
			int endLine = this.unit.getLineNumber(endPosition);
			if (endLine == -1) endLine = this.unit.getLineNumber(endPosition - 1);
			if (startLine <= suspLineNum && suspLineNum <= endLine) {
				if (startLine == suspLineNum || endLine == suspLineNum) {
					if (!isRequiredAstNode(child)) {
						child = traverseParentNode(child);
						if (child == null) break;
					}
					this.suspiciousCodeAstNode = child;
					this.suspiciousCodeStr = readSuspiciousCode();
					break;// FIXME: one code line might contain several statements.
				} else {
					identifySuspiciousCodeAst(child, suspLineNum);
				}
				break;
			} else if (startLine > suspLineNum) {
				break;
			}
		}
	}

	private void identifySuspiciousMethodAst(ITree tree, int buggyLine) {
		List<ITree> children = tree.getChildren();
		
		for (ITree child : children) {
			
			int startPosition = child.getPos();
			int endPosition = startPosition + child.getLength();
			int startLine = this.unit.getLineNumber(startPosition);
			int endLine = this.unit.getLineNumber(endPosition);
			if (endLine == -1) endLine = this.unit.getLineNumber(endPosition - 1);
			if (startLine <= buggyLine && buggyLine <= endLine) {
				if (Checker.isMethodDeclaration(child.getType())) {
					buggyMethod = new BuggyMethod();
					buggyMethod.classPath = this.javaFile.getPath();
					buggyMethod.startLine = startLine;
					buggyMethod.endLine = endLine;
					break;
				} else {
					identifySuspiciousMethodAst(child, buggyLine);
				}
			} else if (startLine > buggyLine) {
				break;
			}
		}
	}
	
	private boolean isRequiredAstNode(ITree tree) {
		int astNodeType = tree.getType();
		if (Checker.isStatement(astNodeType) 
				|| Checker.isFieldDeclaration(astNodeType)
				|| Checker.isMethodDeclaration(astNodeType)
				|| Checker.isTypeDeclaration(astNodeType)) {
			return true;
		}
		return false;
	}

	private ITree traverseParentNode(ITree tree) {
		ITree parent = tree.getParent();
		if (parent == null) return null;
		if (!isRequiredAstNode(parent)) {
			parent = traverseParentNode(parent);
		}
		return parent;
	}

	private String readSuspiciousCode() {
		String javaFileContent = FileHelper.readFile(this.javaFile);
		int startPos = this.suspiciousCodeAstNode.getPos();
		int endPos = startPos + this.suspiciousCodeAstNode.getLength();
		return javaFileContent.substring(startPos, endPos);
	}

	public ITree getSuspiciousCodeAstNode() {
		return suspiciousCodeAstNode;
	}

	public String getSuspiciousCodeStr() {
		return suspiciousCodeStr;
	}
	
	public BuggyMethod getBuggMethod() {
		return buggyMethod;
	}

	public class BuggyMethod {
		public String classPath;
//		public String methodName;
		public int startLine;
		public int endLine;
	}
	
	private class MyUnit {
		
		public CompilationUnit createCompilationUnit(File javaFile) {
			char[] javaCode = readFileToCharArray(javaFile);
			ASTParser parser = createASTParser(javaCode);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			CompilationUnit unit = (CompilationUnit) parser.createAST(null);
			
			return unit;
		}

		private ASTParser createASTParser(char[] javaCode) {
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setSource(javaCode);

			return parser;
		}
		
		private char[] readFileToCharArray(File javaFile) {
			StringBuilder fileData = new StringBuilder();
			BufferedReader br = null;
			
			char[] buf = new char[10];
			int numRead = 0;
			try {
				FileReader fileReader = new FileReader(javaFile);
				br = new BufferedReader(fileReader);
				while ((numRead = br.read(buf)) != -1) {
					String readData = String.valueOf(buf, 0, numRead);
					fileData.append(readData);
					buf = new char[1024];
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (br != null) {
						br.close();
						br = null;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (fileData.length() > 0)
				return fileData.toString().toCharArray();
			else return new char[0];
		}
	}
	
}
