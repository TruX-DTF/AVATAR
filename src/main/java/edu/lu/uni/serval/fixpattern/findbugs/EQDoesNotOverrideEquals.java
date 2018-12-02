package edu.lu.uni.serval.fixpattern.findbugs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lu.uni.serval.fixpattern.FixTemplate;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.utils.Checker;

/**
 * Fix pattern for EQ_DOESNT_OVERRIDE_EQUALS violations.
 * 
 * @author kui.liu
 *
 */
public class EQDoesNotOverrideEquals extends FixTemplate {
	
	private String className = null;
	private ITree classDeclarationAst = null;
	private Map<String, String> fields = new HashMap<>();
	private List<String> fieldNames = new ArrayList<>();
	private List<List<String>> selectedFieldsList = new ArrayList<>();
	
	@Override
	public void generatePatches() {
		readClassNameAndFields(this.getSuspiciousCodeTree());
		if (className == null) return;
		
		StringBuilder fixedCode = new StringBuilder("    public boolean equals(Object obj) {\n");
		fixedCode.append("        if (obj == null) return false;\n");
		fixedCode.append("        if (obj == this) return true;\n");
		fixedCode.append("        if (!(obj instanceof ").append(className).append(")) return false;\n");
		fixedCode.append("        ").append(className).append(" _var = (").append(className).append(") obj;\n");
		
		if (fieldNames.isEmpty()) {
			fixedCode.append("        return super.equals(obj);\n    }");
			this.generatePatch(fixedCode.toString());
			return;
		}
		// How to select the fields?
		String[] fieldNamesArray = new String[fieldNames.size()];
		fieldNamesArray = fieldNames.toArray(fieldNamesArray);
		for (int i = 0, size = fieldNamesArray.length; i < size; i ++) {
			combinations2(fieldNamesArray, size - i, 0, new String[size - i]);
		}
		
		for (List<String> selectedFields : selectedFieldsList) {
			StringBuilder fixedCodeStr1 = generatedComparingCode(selectedFields);
			fixedCode.append(fixedCodeStr1);
			this.generatePatch(0, fixedCode.toString() + "        return true;\n    }");
			this.generatePatch(0, fixedCode.toString() + "        return super.equals(obj);\n    }");
		}
	}
	
	private void combinations2(String[] arr, int len, int startPosition, String[] result){
        if (len == 0){
        	List<String> fieldsList = new ArrayList<String>(result.length);
        	fieldsList.addAll(Arrays.asList(result));
        	selectedFieldsList.add(fieldsList);
            return;
        }       
        for (int i = startPosition; i <= arr.length-len; i++){
            result[result.length - len] = arr[i];
            combinations2(arr, len-1, i+1, result);
        }
    }

	private StringBuilder generatedComparingCode(List<String> selectedFields) {
		StringBuilder fixedCodeStr1 = new StringBuilder();
		for (String fieldName : selectedFields) {
			String fieldType = fields.get(fieldName);
			fixedCodeStr1.append("        if (");
			if (isPrimitiveType(fieldType)) {
				fixedCodeStr1.append(fieldName).append(" != _var.").append(fieldName);
			} else {
				fixedCodeStr1.append("!").append(fieldName).append(".equals(_var.").append(fieldName).append(")");
			}
			fixedCodeStr1.append(") return false;\n");
		}
		return fixedCodeStr1;
	}

	private boolean isPrimitiveType(String fieldType) {
		if ("char".equals(fieldType)) return true;
		if ("byte".equals(fieldType)) return true;
		if ("short".equals(fieldType)) return true;
		if ("int".equals(fieldType)) return true;
		if ("long".equals(fieldType)) return true;
		if ("float".equals(fieldType)) return true;
		if ("double".equals(fieldType)) return true;
		if ("boolean".equals(fieldType)) return true;
		return false;
	}

	/**
	 * Read the class name and fields of the buggy class file.
	 * @param suspCodeTree
	 */
	private void readClassNameAndFields(ITree suspCodeTree) {
		if (this.classDeclarationAst == null) {
			readClassDeclaration(suspCodeTree);
		}
		if (this.classDeclarationAst == null) {
			// FIXME non-type declaration file.
			className = null;
			return;
		}
		List<ITree> classChildren = this.classDeclarationAst.getChildren();
		for (ITree classChild : classChildren) {
			if (Checker.isSimpleName(classChild.getType())) {
				className = classChild.getLabel().substring(10);
			} else if (Checker.isFieldDeclaration(classChild.getType())) {
				List<ITree> children = classChild.getChildren();

				String type = null;
				List<String> varList = new ArrayList<>();
				for (int index = 0, size = children.size(); index < size; index ++) {
					ITree child = children.get(index);
					if (Checker.isModifier(child.getType())) {
						if (child.getLabel().equals("static") || child.getLabel().equals("final")) {
							break;
						}
					} else {
						type = this.readType(child.getLabel());
						for (index = index + 1; index < size; index ++ ) {
							String var = children.get(index).getChild(0).getLabel();
							varList.add(var);
						}
						break;
					} 
				}
				
				if (type != null && !varList.isEmpty()) {
					for (String var : varList) {
						fields.put(var, type);
						fieldNames.add(var);
					}
				}
			}
		}
	}
	
	/**
	 * Read the class declaration AST of the suspicious code.
	 * 
	 * @param suspCodeTree
	 */
	private void readClassDeclaration(ITree suspCodeTree) {
		ITree parent = suspCodeTree;
		while (true) {
			if (Checker.isTypeDeclaration(parent.getType())) {
				this.classDeclarationAst = parent;
				break;
			}
			parent = parent.getParent();
			if (parent == null) break;
		}
	}
	
}
