package edu.lu.uni.serval.fixpattern;

import java.util.List;

import edu.lu.uni.serval.avatar.Patch;
import edu.lu.uni.serval.jdt.tree.ITree;

/**
 * FixTemplate interface.
 * 
 * @author kui.liu
 *
 */
public interface IFixTemplate {
	
	public void setSuspiciousCodeStr(String suspiciousCodeStr);
	
	public String getSuspiciousCodeStr();
	
	public void setSuspiciousCodeTree(ITree suspiciousCodeTree);
	
	public ITree getSuspiciousCodeTree();
	
	public void generatePatches();
	
	public List<Patch> getPatches();
	
	public String getSubSuspiciouCodeStr(int startPos, int endPos);
}
