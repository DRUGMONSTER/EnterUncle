import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class Parser{
	private static final int START_POS = 246;
	private static final Map<String, String> demographicKeywords = new LinkedHashMap<String, String>();

	public static boolean parseASCFile(String filepath){
		ArrayList<ArrayList<String>> buffers = new ArrayList<ArrayList<String>>();

		populateDemoMap(demographicKeywords);
		Writer.identifiers = getUniqueIdentifiers();//REALLY SMELLY

		boolean success = readAndLoadFile(filepath, buffers);
		if(!success)
			return false;

		formatAndAddQuestions(buffers, demographicKeywords);

		ArrayList questions = Qnair.getQuestions();
		ArrayList demoQuestions = Qnair.getDemoQuestions();

		Logg.fine(questions.size() + " questions were added");
		Logg.fine(demoQuestions.size() + " demographic questions were added");
		Logg.fine(questions.size() + demoQuestions.size() + " questions total");

		Qnair.removeBadQuestions();
		return true;
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

	private static boolean readAndLoadFile(String filepath, ArrayList<ArrayList<String>> buffers){
		File ascFile = new File(filepath);
		Scanner sc;
		try{
			sc = new Scanner(ascFile, "UTF-8");
		}catch(Exception e){
			Logg.severe("Can't open asc file " + e.getMessage());
			return false;
		}

		//Find *LL (First Variable)
		String line = "";
		boolean foundLL = false;
		while(sc.hasNextLine()){
			line = sc.nextLine();
			Logg.info("Read line: " + line);

			String ll = "*LL";
			if(line.startsWith(ll)){
				foundLL = true;

				Logg.fine("\"*LL\" found - Started loading question buffers");
				break;
			}
		}

		if(!foundLL){
			Logg.severe("\"*LL\" not found - Stopped reading");
			return false;//*LL not found - cut method short
		}

		//Load question lines into buffer
		while(sc.hasNextLine()){
			ArrayList<String> buf = new ArrayList<String>();

			while(true){
				buf.add(line);
				Logg.info("Added line to buffer: " + line);


				line = sc.nextLine();
				if(line.startsWith("*LL "))//next question reached
					break;
			}

			if(line.startsWith("*LL INT L")){
				buf.remove(buf.size() - 1);//remove INT itself
				buffers.add(buf);
				Logg.info("Buffer filled");
				Logg.fine("INT found - Stop reading file");
				break;
			}

			buffers.add(buf);
			Logg.info("Buffer filled");
		}

		sc.close();
		Logg.info("Buffers size: " + buffers.size());

		return true;
	}

	//reads from buffers and adds questions and demo questions to Qnair
	public static boolean formatAndAddQuestions(ArrayList<ArrayList<String>> buffers, Map<String, String> demographicKeywords){
		int pos = START_POS;

		for(ArrayList<String> buffer : buffers){
			boolean demoQ = false;
			String variableName = "";
			String position = pos + "-";
			int codeWidth = 0;
			String label = "";
			String identifier = "";
			ArrayList<String[]> choices = new ArrayList<String[]>();//[0]=code; [1]=label;

			String rawVariable = buffer.get(0);
			variableName = rawVariable.substring(4, rawVariable.indexOf(" ", 4));
			Logg.fine("Variable read: " + variableName);

			//Mark demographic questions
			if(variableName.charAt(0) == 'D'){
				Logg.fine("Variable " + variableName + " was marked as demographic");
				demoQ = true;
			}

			int equalsPos = rawVariable.indexOf("=");
			int spacePos = rawVariable.indexOf(" ", equalsPos);
			codeWidth = Integer.parseInt(rawVariable.substring(equalsPos + 1, spacePos));

			String rawLabel = buffer.get(1).replace('\t', ' ');
			label = rawLabel.substring(1, rawLabel.length() - 1); //remove square brackets around label

			//Find and capitalize first letter
			boolean firstLetter = false;
			for(int i = 0; i < label.length(); i++){
				if(!firstLetter && Character.isLetter(label.charAt(i)))
					firstLetter = true;
				if(Character.isLetter(label.charAt(i))){
					System.out.println(label + " =-=-= " + i);
					label = label.substring(0, i) + label.substring(i, i + 1).toUpperCase() + label.substring(i + 1);
					System.out.println(label);
					break;
				}
			}

			//determine Question Identifier
			if(demoQ){
				boolean set = false;
				for(int i = 0; i < demographicKeywords.size(); i++){
					String keyword = (String)demographicKeywords.keySet().toArray()[i];
					if(Pattern.matches(keyword, label)){
						identifier = demographicKeywords.get(keyword);
						set = true;
						break;
					}
				}
				if(!set)
					identifier = "DEMO_QUESTION_ID_NOT_FOUND";
			}else{
				if(!label.isEmpty()){
					int delimeter = label.indexOf(".");//find first period

					if(delimeter == -1 || delimeter > 9)//period not found or too far away
						delimeter = label.indexOf(" ");//use first space instead

					if(delimeter == -1){//space not found either
						identifier = variableName;
						Logg.warning(variableName + " - question identifier not found and label not empty");
					}else
						identifier = label.substring(0, delimeter).toUpperCase();
				}
			}

			//Add Choices
			for(int i = 3; i < buffer.size(); i++){
				String line = buffer.get(i);

				if(line.charAt(0) == '['){
					int choiceLabelEndPos = line.indexOf(']');
					String choiceLabel = line.substring(1, choiceLabelEndPos);

					if(choiceLabel.length() > 0)		//capitalize first letter
						choiceLabel = choiceLabel.substring(0, 1).toUpperCase() + choiceLabel.substring(1);

					int codeStartPos = line.indexOf('[', choiceLabelEndPos);
					String code = line.substring(codeStartPos + 1, codeStartPos + 1 + codeWidth);

					choices.add(new String[]{code, choiceLabel});
				}
			}
			pos += codeWidth;

			if(demoQ){
				Qnair.addDemoQuestion(variableName, codeWidth, label, identifier, position, choices);
				Logg.fine("Question " + variableName + " was added as demographic");
			}else{
				Qnair.addQuestion(variableName, codeWidth, label, identifier, position, choices);
				Logg.fine("Question " + variableName + " was added");
			}
		}
		return true;
	}

	private static ArrayList<String> getUniqueIdentifiers(){
		ArrayList<String> idents_r = new ArrayList<String>();
		Set<String> idents = new HashSet<String>();
		for(String s : demographicKeywords.values()){
			if(!idents.contains(s)){						//might allready be unique
				idents.add(s);
				idents_r.add(s);
			}
		}
		return idents_r;
	}
}
