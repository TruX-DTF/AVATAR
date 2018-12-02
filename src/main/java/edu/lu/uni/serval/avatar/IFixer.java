package edu.lu.uni.serval.avatar;

import java.util.List;

import edu.lu.uni.serval.avatar.AbstractFixer.SuspCodeNode;
import edu.lu.uni.serval.utils.SuspiciousPosition;

/**
 * Fixer Interface.
 * 
 * @author kui.liu
 *
 */
public interface IFixer {

	public List<SuspiciousPosition> readSuspiciousCodeFromFile();
	
	public SuspCodeNode parseSuspiciousCode(SuspiciousPosition suspiciousCode);

	public void fixProcess();
	
}
