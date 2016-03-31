import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class XML_Get{
	public static void init(){
		BufferedReader stringReader = new BufferedReader(new InputStreamReader(Writer.class.getResourceAsStream("strings.xml")));
		try{
			System.out.println(stringReader.readLine());
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
