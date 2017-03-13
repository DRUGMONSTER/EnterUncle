import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

class XML_Get{
	private static final String canadaName = "canada";
	private static final String ontarioName = "ontario";
	private static final String torontoName = "toronto";
	private static final String ottawaName = "ottawa";
	
	private static Element docElement;

	static{
		try{
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			docElement = documentBuilder.parse(Writer.class.getResourceAsStream("strings.xml")).getDocumentElement();
		}catch(SAXException e){
			System.out.println("SAX");
			e.printStackTrace();
		}catch(ParserConfigurationException e){
			System.out.println("Parser Config");
			e.printStackTrace();
		}catch(IOException e){
			System.out.println("IO");
			e.printStackTrace();
		}
	}
	
	static String[][] getOntarioRegionTable250(){
		//TODO: Don't use two calls to get, use nodeListToArrayWithReplace
		
		NodeList choiceLabelNodes = getElementOf(getElementOf(docElement, "ontarioRegionTable250"), "choiceLabels").getChildNodes();
		String[] choiceLabels = nodeListToArrayWithReplace(choiceLabelNodes, "\\t", "\t");
		
		NodeList valueNodes = getElementOf(getElementOf(docElement, "ontarioRegionTable250"), "values").getChildNodes();
		String[] values = nodeListToArray(valueNodes);
		
		return new String[][]{choiceLabels, values};
	}
	
	static String[] get601(String projectName){
		NodeList nodes = getElementOf(getElementOf(docElement, "tables600"), "T601").getChildNodes();
		return nodeListToArrayWithReplace(nodes, "$$$projectNameTwice$$$", projectName + "\\" + projectName);
	}
	
	static String[] get602ForLevel(String level, String projectName){
		NodeList nodes = getElementOf(getElementOf(getElementOf(docElement, "tables600"), level), "T602").getChildNodes();
		return nodeListToArrayWithReplace(nodes, "$$$projectNameTwice$$$", projectName + "\\" + projectName);
	}
	
	static String[] get603ForLevel(String level, String projectName){
		NodeList nodes = getElementOf(getElementOf(getElementOf(docElement, "tables600"), level), "T603").getChildNodes();
		return nodeListToArrayWithReplace(nodes, "$$$projectNameTwice$$$", projectName + "\\" + projectName);
	}
	
	static String[] get699ForLevel(String level, String projectName){
		NodeList nodes = getElementOf(getElementOf(getElementOf(docElement, "tables600"), level), "T699").getChildNodes();
		return nodeListToArrayWithReplace(nodes, "$$$projectNameTwice$$$", projectName + "\\" + projectName);
	}
	
	static String[] getWeights(String location){
		if(location.equalsIgnoreCase(torontoName)){
			return getWeightsForLocation(torontoName);
		}else if(location.equalsIgnoreCase(ontarioName)){
			return getWeightsForLocation(ontarioName);
		}else if(location.equalsIgnoreCase(canadaName)){
			return getWeightsForLocation(canadaName);
		}else if(location.equalsIgnoreCase(ottawaName)){
			return getWeightsForLocation(ottawaName);
		}
		
		return null;
	}
	
	private static String[] getWeightsForLocation(String location){
		NodeList weightNodes = getElementOf(getElementOf(docElement, "weights"), location).getChildNodes();
		return nodeListToArray(weightNodes);
	}
	
	private static Element getElementOf(Element e, String tag){
		return (Element)e.getElementsByTagName(tag).item(0);
	}
	
	private static String[] nodeListToArray(NodeList nl){
		String[] strings = new String[nl.getLength()/2];
		for(int i = 0, j = 1; i < strings.length; i++, j += 2){
			strings[i] = nl.item(j).getFirstChild().getNodeValue();
		}
		return strings;
	}
	
	private static String[] nodeListToArrayWithReplace(NodeList nl, String find, String replaceWith){
		String[] strings = new String[nl.getLength()/2];
		for(int i = 0, j = 1; i < strings.length; i++, j += 2){
			strings[i] = nl.item(j).getFirstChild().getNodeValue().replace(find, replaceWith);
		}
		return strings;
	}
}
