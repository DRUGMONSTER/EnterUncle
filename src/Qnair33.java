import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class Qnair33{
	private static File ascFile;
	private static final int START_POS = 246;
	private static final Set<String> rejectableVariables = new HashSet<String>(Arrays.asList(new String[]{"TZONE", "LOC", "LDF", "LDE", "AREA", "FSA", "FSA1", "INTRO", "LANG", "IT2", "S1", "INT01", "INT99", "C3"}));
	private static final Map<String, String> demographicKeywords = new LinkedHashMap<String, String>();
	private static final ArrayList<ArrayList<String>> buffers = new ArrayList<ArrayList<String>>();
	private static final ArrayList<Question> questions = new ArrayList<Question>();
	private static final ArrayList<DemoQuestion> demoQuestions = new ArrayList<DemoQuestion>();


	public static void readAndLoadFile(String filepath){
		ascFile = new File(filepath);
		Scanner sc;
		try{
			sc = new Scanner(ascFile, "UTF-8");
		}catch(Exception e){
			Logg.severe("Can't open asc file " + e.getMessage());
			return;
		}

		//Find TZONE
		String line = "";
		boolean foundTZONE = false;
		while(sc.hasNextLine()){
			line = sc.nextLine();
			Logg.info("Read line: " + line);

			String tzone = "*LL TZONE";
			if(line.startsWith(tzone)){
				foundTZONE = true;

				Logg.fine("TZONE found - Start loading question buffers");
				break;
			}
		}

		if(!foundTZONE){
			Logg.severe("TZONE not found - Stopped reading");
			return;//TZONE not found - cut method short
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
	}

	//reads from buffers and adds to questions and demo questions
	public static boolean formatQuestions(){
		//init HashMap
		demographicKeywords.put("(?i).*\\bgender\\b.*",							"GENDER");
		demographicKeywords.put("(?i).*\\bchildren\\b.*",						"CHILDREN");
		demographicKeywords.put("(?i).*\\bage\\?$",								"AGE");
		demographicKeywords.put("(?i).*\\bold\\b.*",							"AGE");
		demographicKeywords.put("(?i).*\\bproperty\\b.*",						"PROPERTY");
		demographicKeywords.put("(?i).*\\beducation\\b.*",						"EDU");
		demographicKeywords.put("(?i).*\\bwork or school\\b.*",					"TRANSIT");
		demographicKeywords.put("(?i).*\\bethnic\\b.*",							"ETHNIC");
		demographicKeywords.put("(?i).*\\breligion\\b.*",						"RELIGION");
		demographicKeywords.put("(?i).*\\bincome\\b.*",							"INCOME");
		demographicKeywords.put("(?i).*\\bpart\\b.*\\bof\\b.*\\bcity\\b.*",		"COMMUNITY");
		demographicKeywords.put("(?i).*\\baddition\\b.*\\bcell phone\\b.*",		"AlSO_LANDLINE");
		demographicKeywords.put("(?i).*\\blandline\\b.*",						"REACHED");

		int pos = START_POS;
		for(ArrayList<String> buffer : buffers){
			boolean demoQ = false;

			String rawVariable = buffer.get(0);
			String variable = rawVariable.substring(4, rawVariable.indexOf(" ", 4));
			Logg.fine("Variable read: " + variable);

			//Mark demographic questions
			if(variable.charAt(0) == 'D'){
				Logg.fine("Variable " + variable + " was marked as demographic");
				demoQ = true;
			}

			QuestionBase qb;
			if(demoQ)
				qb = new DemoQuestion();
			else
				qb = new Question();
			qb.setVariable(variable);
			qb.setPosition(pos + "-");

			int equalsPos = rawVariable.indexOf("=");
			int spacePos = rawVariable.indexOf(" ", equalsPos);
			int codeWidth = Integer.parseInt(rawVariable.substring(equalsPos + 1, spacePos));
			qb.setCodeWidth(codeWidth);

			String rawLabel = buffer.get(1).replace('\t', ' ');
			String label = rawLabel.substring(1, rawLabel.length() - 1);
			qb.setLable(label);

			//set Question Identifiers
			if(demoQ){
				boolean set = false;
				for(int i = 0; i < demographicKeywords.size(); i++){
					String keyword = (String)demographicKeywords.keySet().toArray()[i];
					if(Pattern.matches(keyword, label)){
						qb.setIdentifier(demographicKeywords.get(keyword));
						set = true;
						break;
					}
				}
				if(!set)
					qb.setIdentifier("DEMO_QUESTION_ID_NOT_FOUND");
			}else{
				String questionIdentifier = "";
				if(!label.isEmpty()){
					int delimeter = label.indexOf(".");//find first period

					if(delimeter == -1 || delimeter > 9)//period not found or too far away
						delimeter = label.indexOf(" ");//use first space instead

					if(delimeter == -1){//space not found either
						questionIdentifier = "QUESTION_ID_NOT_FOUND";
						Logg.warning(variable + " - question identifier not found and label not empty");
					}else
						questionIdentifier = label.substring(0, delimeter).toUpperCase();
				}
				qb.setIdentifier(questionIdentifier);
			}

			//Add Choices
			for(int i = 3; i < buffer.size(); i++){
				String line = buffer.get(i);

				if(line.charAt(0) == '['){
					int choiceLabelEndPos = line.indexOf(']');
					String choiceLabel = line.substring(1, choiceLabelEndPos);

					int codeStartPos = line.indexOf('[', choiceLabelEndPos);
					String code = line.substring(codeStartPos + 1, codeStartPos + 1 + codeWidth);

					qb.addChoice(code, choiceLabel);
				}
			}
			pos += codeWidth;

			if(demoQ){
				demoQuestions.add((DemoQuestion) qb);
				Logg.fine("Question " + variable + " was added as demographic");
			}else{
				//noinspection ConstantConditions
				questions.add((Question) qb);
				Logg.fine("Question " + variable + " was added");
			}
		}

		removeBadQuestions();

		Logg.fine(questions.size() + " questions were added");
		Logg.fine(demoQuestions.size() + " demographic questions were added");
		Logg.fine(questions.size() + demoQuestions.size() + " questions total");
		clearBuffers();
		return true;
	}

	//This also removes hear again choices
	private static void removeBadQuestions(){
		Iterator<Question> iter = questions.iterator();
		while(iter.hasNext()){
			Question q = iter.next();
			String variable = q.getVariable();

			//Remove questions with no label
			if(q.getLabel().isEmpty()){
				iter.remove();
				Logg.info("Removed " + variable + " - no label");
				continue;
			}

			//Remove questions with no choices
			if(q.getChoices().isEmpty()){
				iter.remove();
				Logg.info("Removed " + variable + " - no choices");
				continue;
			}

			//Remove questions with rejectable variable names
			if(rejectableVariables.contains(variable)){
				iter.remove();
				Logg.info("Removed " + variable + " - is rejectable");
				continue;
			}

			//Remove recruitment questions (hopefully only recruit)
			if(variable.charAt(0) == 'R'){
				iter.remove();
				Logg.info("Removed " + variable + " is recruit");
				continue;
			}

			removeHearAgain(q);
		}

		//Remove demo questions with no label
		Iterator<DemoQuestion> d_iter = demoQuestions.iterator();
		while(d_iter.hasNext()){
			DemoQuestion dq = d_iter.next();
			String variable = dq.getVariable();

			if(dq.getLabel().isEmpty()){
				d_iter.remove();
				Logg.info("Removed " + variable + " - no label");
				continue;
			}

			removeHearAgain(dq);
		}
	}

	public static void clearBuffers(){
		buffers.clear();
	}

	public static void clearQuestions(){
		questions.clear();
		demoQuestions.clear();
	}

	public static ArrayList<QuestionBase> getAllQuestions(){
		ArrayList<QuestionBase> temp = new ArrayList<QuestionBase>();
		temp.addAll(questions);
		temp.addAll(demoQuestions);
		return temp;
	}

	public static ArrayList<Question> getQuestions(){
		return questions;
	}

	public static ArrayList<DemoQuestion> getDemoQuestions(){
		return demoQuestions;
	}

	public static ArrayList<String> getUniqueIdentifiers(){
		ArrayList<String> idents_r = new ArrayList<String>();
		Set<String> idents = new HashSet<String>();
		for(String s : demographicKeywords.values()){
			if(!idents.contains(s)){
				idents.add(s);
				idents_r.add(s);
			}
		}
		return idents_r;
	}

	public static boolean isBuffEmpty(){
		return buffers.isEmpty();
	}

	public static boolean isEmpty(){
		return questions.isEmpty() && demoQuestions.isEmpty();
	}

	public static void setAscFile(File aAscFile){
		ascFile = aAscFile;
	}

	public static File getAscFile(){
		return ascFile;
	}

	//remove "hear again" or "Repeat answers" choice
	private static void removeHearAgain(QuestionBase bq){
		Iterator<String[]> c_iter = bq.getChoices().iterator();
		while(c_iter.hasNext()){
			String[] choice = c_iter.next();
			if(checkHearAgain(choice[1])){
				c_iter.remove();
				Logg.warning("Removed 'hear again' choice in " + bq.getVariable());
			}
		}
	}

	private static boolean checkHearAgain(String lbl){
		String lowerLable = lbl.toLowerCase();
		return (lowerLable.contains("hear") && lowerLable.contains("again")) || (lowerLable.contains("repeat") && lowerLable.contains("answers"));
	}

	@SuppressWarnings("UnusedDeclaration")
	public static void printAll(){
		for(Question q : questions){
			printQusetion(q);
		}

		for(DemoQuestion dq : demoQuestions){
			printQusetion(dq);
		}
	}

	public static void printQusetion(QuestionBase q){
		System.out.println(q.getVariable());
		if(!q.getLabel().isEmpty())
			System.out.println(q.getLabel());
		else
			System.out.println("EMPTY LABEL");
		System.out.println(q.getIdentifier());
		System.out.println(q.getPosition());
		ArrayList<String[]> choices = q.getChoices();
		for(String[] c : choices)
			System.out.println(c[0] + "|--|" + c[1]);
		System.out.println();
	}
}
