import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

//Key       -> DiEntry
//IDENT_NUM -> (IDENT_STRING, REGEXs)




@SuppressWarnings("unused")
public class DemoMap extends LinkedHashMap<Integer, DemoMap.DiEntry>{
	public void put(int i, String name, ArrayList<String> regexs){
		super.put(i, new DiEntry(name, regexs));
	}

	//use this in parser
	public String[] getAllEntries(){
		//return super.get(K).regex;
		return null;
	}

	class DiEntry{
		public ArrayList<String> regexs;
		public String name;

		public DiEntry(String aName, ArrayList<String> aRregexList){
			regexs = aRregexList;
			name = aName;
		}
	}

	private static void populateDemoMap(Map<String, String> demographicKeywords){
		demographicKeywords.put("(?i).*\\bgender\\b.*",							"GENDER");
		demographicKeywords.put("(?i).*\\bchildren\\b.*",						"CHILDREN");
		demographicKeywords.put("(?i).*\\bage\\?$",								"AGE");
		demographicKeywords.put("(?i).*\\bold\\b.*",							"AGE");
		demographicKeywords.put("(?i).*\\bproperty\\b.*",						"PROPERTY");
		demographicKeywords.put("(?i).*\\beducation\\b.*",						"EDU");
		demographicKeywords.put("(?i).*\\bwork or school\\b.*",					"TRANSIT");
		demographicKeywords.put("(?i).*\\bethnic\\b.*", 						"ETHNIC");
		demographicKeywords.put("(?i).*\\breligion\\b.*",						"RELIGION");
		demographicKeywords.put("(?i).*\\bincome\\b.*", 						"INCOME");
		demographicKeywords.put("(?i).*\\bpart\\b.*\\bof\\b.*\\bcity\\b.*", 	"COMMUNITY");
		demographicKeywords.put("(?i).*\\baddition\\b.*\\bcell phone\\b.*", 	"AlSO_LANDLINE");
		demographicKeywords.put("(?i).*\\blandline\\b.*",						"REACHED");
		demographicKeywords.put("(?i).*\\bborn in canada\\b.*",					"CANADA_BORN");
	}
}
