import java.util.LinkedHashMap;
import java.util.Map;

//Key       -> DiEntry
//REGEXs -> (IDENT_STRING, DEMO_Q)


public class DemoMap{
	private  static Map<String, DiEntry> map = new LinkedHashMap<String, DiEntry>();

	public static void init(){
		map.put("(?i).*\\bgender\\b.*",							new DiEntry("GENDER"		, null));
		map.put("(?i).*\\bchildren\\b.*",						new DiEntry("CHILDREN"		, null));
		map.put("(?i).*\\bage\\?$",								new DiEntry("AGE"			, null));
		map.put("(?i).*\\bold\\b.*",							new DiEntry("AGE"			, null));
		map.put("(?i).*\\bproperty\\b.*",						new DiEntry("PROPERTY"		, null));
		map.put("(?i).*\\beducation\\b.*",						new DiEntry("EDU"			, null));
		map.put("(?i).*\\bwork or school\\b.*",					new DiEntry("TRANSIT"		, null));
		map.put("(?i).*\\bethnic\\b.*", 						new DiEntry("ETHNIC"		, null));
		map.put("(?i).*\\breligion\\b.*",						new DiEntry("RELIGION"		, null));
		map.put("(?i).*\\bincome\\b.*", 						new DiEntry("INCOME"		, null));
		map.put("(?i).*\\bpart\\b.*\\bof\\b.*\\bcity\\b.*", 	new DiEntry("COMMUNITY"		, null));
		map.put("(?i).*\\baddition\\b.*\\bcell phone\\b.*", 	new DiEntry("AlSO_LANDLINE"	, null));
		map.put("(?i).*\\blandline\\b.*",						new DiEntry("REACHED"		, null));
		map.put("(?i).*\\bborn in canada\\b.*",					new DiEntry("CANADA_BORN"	, null));
	}

	//use this in parser
	public String[] getAllEntries(){
		//return super.get(K).regex;
		return null;
	}

	static class DiEntry{
		public String name;
		public DemoQuestion dq;

		public DiEntry(String aName, DemoQuestion aDQ){
			name = aName;
			dq = aDQ;
		}
	}
}
