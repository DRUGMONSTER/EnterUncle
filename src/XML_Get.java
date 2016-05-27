import org.w3c.dom.Document;
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

	public static String getWeightsFromCity(String city){

		return doc.getDocumentElement().getElementsByTagName(city).toString();
	}
}
