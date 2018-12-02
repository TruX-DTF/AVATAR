package edu.lu.uni.serval.findbugs.xml.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.lu.uni.serval.findbugs.info.BugCollection;
import edu.lu.uni.serval.findbugs.info.BugInstance;
import edu.lu.uni.serval.utils.ListSorter;

/**
 * 
 * Method, Type, Field
 * 
 * @author kui.liu
 *
 */
public class XmlParser implements IXmlParser {

	private BugCollection bugCollection;

	public BugCollection getBugCollection() {
		return bugCollection;
	}

	public XmlParser() {
		super();
	}

	@Override
	public void parserXml(String fileName) {
		File file = new File(fileName);
		parserXml(file);
	}

	/**
	 * Element: SourceLine
	 * @param xmlFile
	 */
	public void parserXml(File xmlFile) {
		String projectName = xmlFile.getName();
		projectName = projectName.substring(0, projectName.lastIndexOf(".")).replace("-", "_");
//		Date releasedTime = null;
		List<BugInstance> bugInstances = new ArrayList<>();
		SAXReader saxReader = new SAXReader();

		try {
			Document document = saxReader.read(xmlFile);
			Element rootElement = document.getRootElement();
			Iterator<?> elementIterator = rootElement.elementIterator();

			while (elementIterator.hasNext()) {
				Element element = (Element) elementIterator.next();

				if ("BugInstance".equals(element.getName())) {
					String violationType = parseViolationType(element.attributes());
					if (violationType == null) continue;

					List<?> sourceLineElements = element.elements("SourceLine");
					
					if (sourceLineElements == null || sourceLineElements.size() == 0) {
//						List<?> subElements = element.elements();
//						Element subElement = (Element) subElements.get(subElements.size() - 1);
//						sourceLineElements = subElement.elements("SourceLine");
						continue;
					}
					for (Object obj : sourceLineElements) {
						Element sourceLineElement = (Element) obj;
						
						List<String> sourceLineAttribute = parseSourceLineAttributes(sourceLineElement);
						String sourceFile = sourceLineAttribute.get(2);
						
						int start = Integer.parseInt(sourceLineAttribute.get(0));
						int end = Integer.parseInt(sourceLineAttribute.get(1));
						if (start == 0 || end == 0 || "".equals(sourceFile)) {
							continue;
						} else  {
							BugInstance bugInstance = new BugInstance();
							bugInstance.setType(violationType);
							bugInstance.setStartOfSourceLine(start);
							bugInstance.setEndOfSourceLine(end);
							bugInstance.setSourcePath(sourceFile);
							bugInstances.add(bugInstance);
						}
					}
//				} else if ("Project".equals(element.getName())) {
//					Element jarElement = (Element) element.elements("Jar").get(0);
//					projectName = jarElement.getText();
//					projectName = projectName.substring(0, projectName.indexOf("/buggy"));
//					projectName = projectName.replace("/", "_");
//				} else {
//					break;
				}
			}
		} catch (DocumentException e) {
			System.err.println(e.getMessage());
		}
		// System.out.println(bugInstances.size());

		ListSorter<BugInstance> als = new ListSorter<BugInstance>(bugInstances);
		bugInstances = als.sortAscending();

		this.bugCollection = new BugCollection(projectName, bugInstances);
	}
	
	/**
	 * Element: Class, Method, Type
	 * @param xmlFile
	 */
	public void parserXml(File xmlFile, String type) {
		String projectName = xmlFile.getName();
		projectName = projectName.substring(0, projectName.lastIndexOf(".")).replace("-", "_");
//		Date releasedTime = null;
		List<BugInstance> bugInstances = new ArrayList<>();
		SAXReader saxReader = new SAXReader();

		try {
			Document document = saxReader.read(xmlFile);
			Element rootElement = document.getRootElement();
			Iterator<?> elementIterator = rootElement.elementIterator();

			while (elementIterator.hasNext()) {
				Element element = (Element) elementIterator.next();

				if ("BugInstance".equals(element.getName())) {
					String violationType = parseViolationType(element.attributes());
					if (violationType == null) continue;

					List<?> typeElements = element.elements(type);
					
					if (typeElements == null || typeElements.size() == 0) {
						continue;
					} else {
						for (Object obj : typeElements) {
							Element typeElement = (Element) obj;
							List<?> sourceLineElements = typeElement.elements("SourceLine");
							
							if (sourceLineElements == null || sourceLineElements.size() == 0) {
								continue;
							} else {
								for (Object obj1 : sourceLineElements) {
									Element sourceLineElement = (Element) obj1;
									
									List<String> sourceLineAttribute = parseSourceLineAttributes(sourceLineElement);
									String sourceFile = sourceLineAttribute.get(2);
									
									int start = Integer.parseInt(sourceLineAttribute.get(0));
									int end = Integer.parseInt(sourceLineAttribute.get(1));
									if (start == 0 || end == 0 || "".equals(sourceFile)) {
										continue;
									} else  {
										BugInstance bugInstance = new BugInstance();
										bugInstance.setType(violationType);
										bugInstance.setStartOfSourceLine(start);
										bugInstance.setEndOfSourceLine(end);
										bugInstance.setSourcePath(sourceFile);
										bugInstances.add(bugInstance);
									}
								}
							}
						}
					}
//				} else if ("Project".equals(element.getName())) {
//					Element jarElement = (Element) element.elements("Jar").get(0);
//					projectName = jarElement.getText();
//					projectName = projectName.substring(0, projectName.indexOf("/buggy"));
//					projectName = projectName.replace("/", "_");
//				} else {
//					break;
				}
			}
		} catch (DocumentException e) {
			System.err.println(e.getMessage());
		}
		// System.out.println(bugInstances.size());

		ListSorter<BugInstance> als = new ListSorter<BugInstance>(bugInstances);
		bugInstances = als.sortAscending();

		this.bugCollection = new BugCollection(projectName, bugInstances);
	}

	@SuppressWarnings("unused")
	private List<Element> extractAllSourceLineElements(List<?> subElements) {
		List<Element> sourceLineElements = new ArrayList<Element>();

		for (Object obj : subElements) {
			Element element = (Element) obj;

			if ("SourceLine".equals(element.getName())) {
				sourceLineElements.add(element);
			} else {
				List<?> subElementsList = element.elements();
				if (subElementsList.size() > 0) {
					sourceLineElements.addAll(extractAllSourceLineElements(subElementsList));
				}
			}
		}

		return sourceLineElements;
	}

	@SuppressWarnings("unused")
	private List<List<String>> parseSourceLineElement(List<?> sourceLineElements) {
		List<List<String>> sourceLineAttributes = new ArrayList<>();
		for (Object obj : sourceLineElements) {
			Element sourceLineElement = (Element) obj;
			List<String> sourceLineAttribute = parseSourceLineAttributes(sourceLineElement);
			int start = Integer.parseInt(sourceLineAttribute.get(0));
			int end = Integer.parseInt(sourceLineAttribute.get(1));

			if (start == 0 && end == 0) {
			} else {
				sourceLineAttributes.add(sourceLineAttribute);
			}
		}
		return sourceLineAttributes;
	}

	@SuppressWarnings("unused")
	private boolean parseClassElement(List<?> classElements, BugInstance bugInstance) {
		Element firstClassElement = (Element) classElements.get(0);
		Element sourceLine = firstClassElement.element("SourceLine");

		List<String> sourceLineAttributes = parseSourceLineAttributes(sourceLine);
		int start = Integer.parseInt(sourceLineAttributes.get(0));
		int end = Integer.parseInt(sourceLineAttributes.get(1));
		String sourcePath = sourceLineAttributes.get(2);

		if (start == 0 && end == 0) {
			return false;
		} else {
			bugInstance.setStartOfSourceLine(start);
			bugInstance.setEndOfSourceLine(end);
			bugInstance.setSourcePath(sourcePath);
			return true;
		}
	}

	private List<String> parseSourceLineAttributes(Element sourceLineElement) {
		List<String> attributes = new ArrayList<>();
		List<?> attributesList = sourceLineElement.attributes();
		String start = "0";
		String end = "0";
		String sourcePath = "";

		for (int index = 0, size = attributesList.size(); index < size; index++) {
			Attribute attribute = (Attribute) attributesList.get(index);
			String attributeName = attribute.getName();
			if ("start".equals(attributeName)) {
				start = attribute.getValue();
			} else if ("end".equals(attributeName)) {
				end = attribute.getValue();
			} else if ("sourcepath".equals(attributeName)) {
				sourcePath = attribute.getValue();
				if (sourcePath.toLowerCase(Locale.ENGLISH).contains("test")) {
					sourcePath = "";
				}
			}
		}

		attributes.add(start);
		attributes.add(end);
		attributes.add(sourcePath);

		return attributes;
	}
	
	/**
	 * Read the violation type of the current instance.
	 * @param attributesList
	 * @return
	 */
	private String parseViolationType(List<?> attributesList) {
		for (int index = 0, size = attributesList.size(); index < size; index++) {
			Attribute attribute = (Attribute) attributesList.get(index);
			String attributeName = attribute.getName();
			if ("type".equals(attributeName)) {
				String alarmType = attribute.getValue();
				return alarmType;
			}
		}
		return null;
	}

	@SuppressWarnings("unused")
	private void parseBugInstanceElement(List<?> attributesList, BugInstance bugInstance) {
		String type = "";
		int priority = 0;
		int rank = 0;
		String abbrev = "";
		String category = "";

		for (int index = 0, size = attributesList.size(); index < size; index++) {
			Attribute attribute = (Attribute) attributesList.get(index);
			String attributeName = attribute.getName();
			if ("type".equals(attributeName)) {
				type = attribute.getValue();
			} else if ("priority".equals(attributeName)) {
				priority = Integer.parseInt(attribute.getValue());
			} else if ("rank".equals(attributeName)) {
				rank = Integer.parseInt(attribute.getValue());
			} else if ("abbrev".equals(attributeName)) {
				abbrev = attribute.getValue();
			} else if ("category".equals(attributeName)) {
				category = attribute.getValue();
			}
		}

		bugInstance.setType(type);
		bugInstance.setPriority(priority);
		bugInstance.setRank(rank);
		bugInstance.setAbbrev(abbrev);
		bugInstance.setCategory(category);
	}
}
