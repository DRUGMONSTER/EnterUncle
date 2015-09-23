import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;

//Key       -> DiEntry
//IDENT_NUM -> (IDENT_STRING, REGEXs)

@SuppressWarnings("unused")
public class TriMap extends LinkedHashMap<Integer, TriMap.DiEntry>{
	public TriMap(){
		super();
	}

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
}
