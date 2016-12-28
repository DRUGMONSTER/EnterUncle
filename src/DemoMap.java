import java.util.LinkedHashMap;
import java.util.Map;

//Key       -> DiEntry
//REGEXs -> (IDENT_STRING, DEMO_Q)


class DemoMap{
	private static Map<String, DiEntry> map = new LinkedHashMap<>();

	private static DemoQuestion GENDER_DQ = null;
	private static DemoQuestion AGE_DQ = null;
	private static DemoQuestion INCOME_DQ = null;
	private static DemoQuestion CHILDREN_DQ = null;
	private static DemoQuestion COMMUNITY_DQ = null;
	private static DemoQuestion ALSO_LANDLINE_DQ = null;
	private static DemoQuestion REACHED_DQ = null;
	
	//Populates the map
	static{
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

	static String[] getRegexPatterns(){
		return map.keySet().toArray(new String[map.size()]);
	}

	static String getIdentifier(String questionLabel, DemoQuestion dq){
		DiEntry ent = map.get(questionLabel);
		ent.dq = dq;
		String ident = ent.name;
		
		switch(ident){
			case "GENDER":
				GENDER_DQ = dq;
				break;
			case "AGE":
				AGE_DQ = dq;
				break;
			case "INCOME":
				INCOME_DQ = dq;
				break;
			case "CHILDREN":
				CHILDREN_DQ = dq;
				break;
			case "COMMUNITY":
				COMMUNITY_DQ = dq;
				break;
			case "AlSO_LANDLINE":
				ALSO_LANDLINE_DQ = dq;
				break;
			case "REACHED":
				REACHED_DQ = dq;
				break;
		}

		return ident;
	}

	static DemoQuestion getGenderDQ(){
		return GENDER_DQ;
	}

	static DemoQuestion getAgeDQ(){
		return AGE_DQ;
	}

	static DemoQuestion getIncomeDQ(){
		return INCOME_DQ;
	}

	static DemoQuestion getChildrenDQ(){
		return CHILDREN_DQ;
	}

	static DemoQuestion getCommunityDQ(){
		return COMMUNITY_DQ;
	}

	static DemoQuestion getAlsoLandlineDQ(){
		return ALSO_LANDLINE_DQ;
	}
	
	static DemoQuestion getReachedDq(){
		return REACHED_DQ;
	}
	
	static class DiEntry{
		private String name;
		private DemoQuestion dq;

		DiEntry(String aName, DemoQuestion aDQ){
			name = aName;
			dq = aDQ;
		}
	}
}
