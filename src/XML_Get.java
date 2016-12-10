import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class XML_Get{
	private static Element docElement;

	public static void init(){
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

	public static String[] getWeightsForToronto(){
		return getWeightsForRegion("cities", "toronto");
	}

	public static String[] getWeightsForOntario(){
		return getWeightsForRegion("provinces", "ontario");
	}

	public static String[] getWeightsForCanada(){
		NodeList weightNodes = getElementOf(getElementOf(docElement, "weights"), "canada").getChildNodes();

		return nodeListToArray(weightNodes);
	}

	public static String[] getWeightsForRegion(String level, String region){
		NodeList weightNodes = getElementOf(getElementOf(getElementOf(docElement, "weights"), level), region).getChildNodes();
		return nodeListToArray(weightNodes);
	}
	
	public static String[][] getOntarioRegionTable205(){
		NodeList choiceLabelNodes = getElementOf(getElementOf(docElement, "ontarioRegionTable205"), "choiceLabels").getChildNodes();
		String[] choiceLabels = nodeListToArray(choiceLabelNodes);
		
		NodeList valueNodes = getElementOf(getElementOf(docElement, "ontarioRegionTable205"), "values").getChildNodes();
		String[] values = nodeListToArray(valueNodes);
		
		return new String[][]{choiceLabels, values};
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
}
