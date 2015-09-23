import java.util.ArrayList;

public class Tagger{
	//add tags to the lines in bufferOfLines
	public static String tag(ArrayList<ArrayList<String>> bufferOfLines, int tabNum, StringBuilder tags){
		String bufferWithTags = "";
		String lastTag = "B";
		for(int i = 0; i < bufferOfLines.size(); i++){
			ArrayList<String> linesBuf = bufferOfLines.get(i);

			int tabOffset = (i % 2) + tabNum;
			for(String str : linesBuf){
				int tabsNeeded = tabOffset - str.length() / 4;
				String currentTag = Tagger.getNextTag(false, lastTag, tags);
				bufferWithTags += str + getTabs(tabsNeeded) + "; tag '" + currentTag + "," + currentTag.toLowerCase() + "'\n";
				lastTag = currentTag;
			}
			Tagger.getNextTag(true, null, tags);
		}
		tags.deleteCharAt(tags.length() - 1); //deletes apostrophe

		return bufferWithTags;
	}

	//side effect: updates tags
	public static String getNextTag(boolean end, String lastTag, StringBuilder tags){
		if(end){
			tags.deleteCharAt(tags.length() - 1); //Deletes comma
			tags.append("' '");
			return null;
		}

		String next = getNextTag(lastTag);

		tags.append(next + ",");

		return next;
	}

	private static String getNextTag(String last){
		int asciiCode = last.charAt(last.length() - 1);            //get char at end
		String rest = last.substring(0, last.length() - 1);        //string without last char
		asciiCode++;

		if(asciiCode > 90){			//if past 'Z'
			if(rest.isEmpty())
				return "AA";
			return getNextTag(rest) + "A";
		}
		return rest + String.valueOf((char) asciiCode);
	}

	public static String getTabs(int x){
		String s = "";
		for(int i = 0; i < x; i++)
			s += "\t";
		return s;
	}
}
