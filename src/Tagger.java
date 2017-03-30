import java.util.ArrayList;

class Tagger{
	//add tags to the lines in bufferOfLines
	static String tag(ArrayList<ArrayList<String>> bufferOfLines, int tabNum, StringBuilder tags){
		StringBuilder bufferWithTags = new StringBuilder();
		String lastTag = "B";
		for(int i = 0; i < bufferOfLines.size(); i++){
			ArrayList<String> linesBuf = bufferOfLines.get(i);

			int tabOffset = (i % 2) + tabNum;
			for(String str : linesBuf){
				int tabsNeeded = tabOffset - str.length() / 4;
				String currentTag = Tagger.getNextTag(false, lastTag, tags);
				assert currentTag != null;
				bufferWithTags.append(str).append(getTabs(tabsNeeded)).append("; tag '").append(currentTag).append(",").append(currentTag.toLowerCase()).append("'\n");
				lastTag = currentTag;
			}
			Tagger.getNextTag(true, null, tags);
		}
		tags.deleteCharAt(tags.length() - 1); //deletes apostrophe

		return bufferWithTags.toString();
	}

	//side effect: updates tags
	private static String getNextTag(boolean end, String lastTag, StringBuilder tags){
		if(end){
			tags.deleteCharAt(tags.length() - 1); //Deletes comma
			tags.append("' '");
			return null;
		}

		String next = getNextTag(lastTag);

		tags.append(next).append(",");

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

	static String getTabs(int x){
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < x; i++)
			s.append("\t");
		return s.toString();
	}
}
