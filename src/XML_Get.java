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
	private static final String londonName = "london";
	
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
	
	static String[] getOntarioRegionTable250(String position){
		NodeList labelAndValueNodes = getElementOf(docElement, "ontarioRegionTable250").getChildNodes();
		return nodeListToArrayWithReplaceTwice(labelAndValueNodes, "$$$position$$$", position, "\\t", "\t");
	}
	
	static String[] get601(String level, String projectName){
		NodeList nodes = getElementOf(getElementOf(getElementOf(docElement, "tables600"), level), "T601").getChildNodes();
		return nodeListToArrayWithReplace(nodes, "$$$projectNameTwice$$$", projectName + "\\" + projectName);
	}
	
	static String[] get602(String level, String projectName){
		NodeList nodes = getElementOf(getElementOf(getElementOf(docElement, "tables600"), level), "T602").getChildNodes();
		return nodeListToArrayWithReplace(nodes, "$$$projectNameTwice$$$", projectName + "\\" + projectName);
	}
	
	static String[] get603(String level, String projectName){
		NodeList nodes = getElementOf(getElementOf(getElementOf(docElement, "tables600"), level), "T603").getChildNodes();
		return nodeListToArrayWithReplaceWithNull(nodes, "$$$projectNameTwice$$$", projectName + "\\" + projectName);
	}
	
	static String[] get604(String projectName){
		NodeList nodes = getElementOf(getElementOf(getElementOf(docElement, "tables600"), "federal"), "T603").getChildNodes();
		return nodeListToArrayWithReplaceWithNull(nodes, "$$$projectNameTwice$$$", projectName + "\\" + projectName);
	}
	
	static String[] get699(String level, String projectName){
		NodeList nodes = getElementOf(getElementOf(getElementOf(docElement, "tables600"), level), "T699").getChildNodes();
		return nodeListToArrayWithReplace(nodes, "$$$projectNameTwice$$$", projectName + "\\" + projectName);
	}
	
	static String[] get802Base(){
		NodeList nodes = getElementOf(getElementOf(docElement, "tables800"), "T802Base").getChildNodes();
		return nodeListToArrayWithReplace(nodes, "\\t", "\t");
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
		}else if(location.equalsIgnoreCase(londonName)){
			return getWeightsForLocation(londonName);
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
	
	private static String[] nodeListToArrayWithReplaceWithNull(NodeList nl, String find, String replaceWith){
		String[] strings = new String[nl.getLength()/2];
		for(int i = 0, j = 1; i < strings.length; i++, j += 2){
			org.w3c.dom.Node n = nl.item(j).getFirstChild();
			if(n != null){
				strings[i] = n.getNodeValue().replace(find, replaceWith);
			}else{
				strings[i] = "";
			}
		}
		return strings;
	}
	
	private static String[] nodeListToArrayWithReplaceTwice(NodeList nl, String find1, String replaceWith1, String find2, String replaceWith2){
		String[] strings = new String[nl.getLength()/2];
		for(int i = 0, j = 1; i < strings.length; i++, j += 2){
			strings[i] = nl.item(j).getFirstChild().getNodeValue().replace(find1, replaceWith1);
		}
		for(int i = 0; i < strings.length; i++){
			strings[i] = strings[i].replace(find2, replaceWith2);
		}
		return strings;
	}
}
