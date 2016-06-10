import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class XML_Get{
	private static Document doc;

	public static void init(){
		try{
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			doc = documentBuilder.parse(Writer.class.getResourceAsStream("strings.xml"));
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
		return getWeightsForCity("cities", "toronto");
	}

	public static String[] getWeightsForOntario(){
		return getWeightsForCity("provinces", "ontario");
	}

	public static String[] getWeightsForCanada(){
		NodeList weightNodes = doc.getDocumentElement().getElementsByTagName("canada").item(0).getChildNodes();

		String[] weights = new String[weightNodes.getLength()/2];
		for(int i = 0, j = 1; i < weights.length; i++, j += 2){
			weights[i] = weightNodes.item(j).getFirstChild().getNodeValue();
			System.out.println(weights[i]);
		}
		return weights;
	}

	public static String[] getWeightsForCity(String level, String city){
		NodeList weightNodes = ((Element)doc.getDocumentElement().getElementsByTagName(level).item(0)).getElementsByTagName(city).item(0).getChildNodes();

		String[] weights = new String[weightNodes.getLength()/2];
		for(int i = 0, j = 1; i < weights.length; i++, j += 2){
			weights[i] = weightNodes.item(j).getFirstChild().getNodeValue();
		}
		return weights;
	}
}
