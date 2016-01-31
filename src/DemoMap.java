import java.util.*;

//Key       -> DiEntry
//REGEXs -> (IDENT_STRING, DEMO_Q)


public class DemoMap{
	private static Map<String, DiEntry> map = new LinkedHashMap<String, DiEntry>();

	private static DemoQuestion GENDER_Q = null;
	private static DemoQuestion AGE_Q = null;
	private static DemoQuestion INCOME_Q = null;
	private static DemoQuestion CHILDREN_Q = null;
	private static DemoQuestion COMMUNITY_Q = null;

	//Populates the map
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

	public static String[] getRegexPatterns(){
		return map.keySet().toArray(new String[map.size()]);
	}

	public static String getIdentifier(String key, DemoQuestion dq){
		DiEntry ent = map.get(key);
		ent.dq = dq;
		String ident = ent.name;

		if(ident.equals("GENDER"))
			GENDER_Q = dq;
		else if(ident.equals("AGE"))
			AGE_Q = dq;
		else if(ident.equals("INCOME"))
			INCOME_Q = dq;
		else if(ident.equals("CHILDREN"))
			CHILDREN_Q = dq;
		else if(ident.equals("COMMUNITY"))
			COMMUNITY_Q = dq;

		return ident;
	}

	public static DemoQuestion getGenderQ(){
		return GENDER_Q;
	}

	public static DemoQuestion getAgeQ(){
		return AGE_Q;
	}

	public static DemoQuestion getIncomeQ(){
		return INCOME_Q;
	}

	public static DemoQuestion getChildrenQ(){
		return CHILDREN_Q;
	}

	public static DemoQuestion getCommunityQ(){
		return COMMUNITY_Q;
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
