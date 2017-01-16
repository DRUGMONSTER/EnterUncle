import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public class Writer{
	private static final ArrayList<String[]> TB_PAIRS = new ArrayList<>();
	private static final ArrayList<String> MEAN_KEYWORDS = new ArrayList<>();
	private static final String MUNICIPAL_SAMPLE_POSITION = "267-";
	private static final String PROVINCIAL_SAMPLE_POSITION = "276-";
	private static final String FEDERAL_SAMPLE_POSITION = "273-";
	private static final String ONTARIO_REGION_POSITION = "271:273";
	private static String projectName;
	private static ArrayList<Question> questions;
	private static ArrayList<DemoQuestion> demoQuestions;
	
	static{
		questions = Qnair.getQuestions();
		demoQuestions = Qnair.getDemoQuestions();
		
		TB_PAIRS.add(new String[]{"(?i).*\\bagree\\b.*",			"(?i).*\\bdisagree\\b.*"});
		TB_PAIRS.add(new String[]{"(?i).*\\bapprove\\b.*",			"(?i).*\\bdisapprove\\b.*"});
		TB_PAIRS.add(new String[]{"(?i).*\\bsupport\\b.*", 			"(?i).*\\boppose\\b.*"});
		TB_PAIRS.add(new String[]{"(?i).*\\bhave\\s+heard\\b.*",	"(?i).*\\bhave\\s+not\\s+heard\\b.*"});
		TB_PAIRS.add(new String[]{"(?i).*\\byes\\b.*",				"(?i).*\\bno\\b.*"});
		
		MEAN_KEYWORDS.add("Very Satisfied");
		MEAN_KEYWORDS.add("Somewhat Satisfied");
		MEAN_KEYWORDS.add("Somewhat Dissatisfied");
		MEAN_KEYWORDS.add("Very Dissatisfied");
	}

	public static void writeFile(File file, ArrayList<QuestionBase> checked, GovernmentLevel govLvl){
		Logg.info("WRITE STARTED");

		PrintWriter writer;
		String originalFilePath = file.getParentFile().toString();
		projectName = file.getName().replace(".ASC", "");
		try{
			writer = new PrintWriter(originalFilePath + "\\" + projectName + "_test" + ".e");
		}catch(FileNotFoundException e){
			Logg.severe("Can't write output file");
			return;
		}

		int tableNum = 1;

		//Print Questions
		for(Question q : questions){
			String variable = q.variable;
			writer.println("TABLE " + tableNum++);

			writer.println("T " + q.label);
			writer.println("T &wt Q" + q.identifier);

			writeChoices(writer, q);

			checkMeansNeeded(q.getChoices());
			int[] positions = checkDichotomy(q.getChoices());
			if(positions[0] != -1){								//Dichotomy found
				writer.println("R ; null");
				int size =  q.getChoices().size();
				writer.println("R T-B; none; ex(RU" + (size - positions[0] + 1) + "-RU" + (size - positions[1] + 1) + ") nofreq");
				Logg.fine("Dichotomy found at " + variable);
				Logg.fine("Positions " + positions[0] + ":" + positions[1]);
			}

			writer.println();
			Logg.info("Wrote question " + variable);
		}

		//Print Demo Questions
		for(DemoQuestion dq : demoQuestions){
			writer.println("TABLE " + tableNum++);

			writer.println("T " + dq.label);
			writer.println("T &wt " + dq.identifier);

			writeChoices(writer, dq);

			writer.println();
			Logg.info("Wrote demo question " + dq.variable);
		}
		writer.println();
		
		write200s(writer, govLvl);
		write600s(writer, govLvl);
		write800s(writer, govLvl);
		write900s(writer, govLvl, checked.size());
		write1000s(writer, checked, govLvl);

		writer.close();
		Logg.info("WRITE FINISHED");
	}
	
	private static void writeChoices(PrintWriter writer, QuestionBase qb){
		String qbPos = qb.position;
		ArrayList<String[]> choices = qb.getChoices();
		String[] means = new String[0];
		if(qb == DemoMap.getAgeDQ()){
			means = new String[6];
			means[0] = "; v20";
			means[1] = "; v29.5";
			means[2] = "; v39.5";
			means[3] = "; v49.5";
			means[4] = "; v59.5";
			means[5] = "; v70";
		}else if(qb == DemoMap.getIncomeDQ()){
			means = new String[7];
			means[0] = "; v17500";
			means[1] = "; v30000";
			means[2] = "; v50000";
			means[3] = "; v70000";
			means[4] = "; v90000";
			means[5] = "; v175000";
			means[6] = "; v300000";
		}

		int tabNum = 0;
		String nullAndMean = "";
		if(means.length != 0){					//if the means ArrayList is not empty
			//calc max length of choice text
			int maxLen = 0;
			for(int i = 0; i < means.length; i++){
				String[] choice = choices.get(i);
				int length = choice[0].length() + choice[1].length() + qbPos.length() + 4;
				if(length > maxLen)
					maxLen = length;
			}
			tabNum = maxLen / 4 + 2;

			nullAndMean = "R ; null\nR Mean; none; mean fdp 0 freq nosgtest\n";
		}

		int i = 0;
		for(; i < means.length; i++){			//if the means ArrayList is not empty
			String[] c = choices.get(i);
			int choiceTabLength = (c[0].length() + c[1].length() + qbPos.length() + 4) / 4;
			int addTab = tabNum - choiceTabLength;

			writer.println("R " + c[1] + "; " + qbPos + c[0] + Tagger.getTabs(addTab) + means[i]);
		}
		for(; i < choices.size(); i++){
			String[] c = choices.get(i);

			writer.println("R " + c[1] + "; " + qbPos + c[0]);
		}
		writer.print(nullAndMean);
	}
	
	private static void write200s(PrintWriter w, GovernmentLevel govLvl){
		if(govLvl == GovernmentLevel.PROVINCIAL){
			String[][] t205 = XML_Get.getOntarioRegionTable250();
			StringBuilder buffer = new StringBuilder();
			
			buffer.append("TABLE 205\n").append("T REGION\n").append("T &wt REGION\n");
			
			for(int i = 0; i < t205[0].length; i++){
				buffer.append(t205[0][i]).append(ONTARIO_REGION_POSITION).append(t205[1][i]).append("\n");
			}
			
			w.println(buffer);
		}
		w.println();
	}

	private static void write600s(PrintWriter w, GovernmentLevel govLvl){
		String[] t601 = XML_Get.get601(projectName);
		
		for(String s : t601){
			w.println(s);
		}
		w.println();
				
		if(govLvl == GovernmentLevel.MUNICIPAL){
			String[] t602 = XML_Get.get602ForLevel("municipal", projectName);
			String[] t699 = XML_Get.get699ForLevel("municipal", projectName);
			for(String s : t602){
				w.println(s);
			}
			w.println();
			for(String s : t699){
				w.println(s);
			}
			w.print("\n\n");
		}
		else if(govLvl == GovernmentLevel.PROVINCIAL){
			String[] t602 = XML_Get.get602ForLevel("provincial", projectName);
			String[] t603 = XML_Get.get602ForLevel("provincial", projectName);
			String[] t699 = XML_Get.get699ForLevel("provincial", projectName);
			
			for(String s : t602){
				w.println(s);
			}
			w.println();
			for(String s : t603){
				w.println(s);
			}
			w.println();
			for(String s : t699){
				w.println(s);
			}
			w.print("\n\n");
		}
	}

	private static void write800s(PrintWriter w, GovernmentLevel govLvl){
		DemoQuestion genderQ = DemoMap.getGenderDQ();
		DemoQuestion ageQ = DemoMap.getAgeDQ();

		if(genderQ == null)
			return;
		if(ageQ == null)
			return;

		String genderPos = genderQ.position;
		String agePos = ageQ.position;

		//0 = Toronto
		if(govLvl == GovernmentLevel.MUNICIPAL){
			w.println(
				"TABLE 802\n" +
				"T Age Gender Weight General Pop - Toronto\n" +
				"R Male < 25;\t\t"		+ genderPos + "1 " + agePos + "1; v 0.0610926254927089\n" +
				"R Male 25 - 34;\t\t"	+ genderPos + "1 " + agePos + "2; v 0.0865217867900710\n" +
				"R Male 35 - 44;\t\t"	+ genderPos + "1 " + agePos + "3; v 0.1062743449869340\n" +
				"R Male 45 - 54;\t\t"	+ genderPos + "1 " + agePos + "4; v 0.0940080833817855\n" +
				"R Male 55 - 64;\t\t"	+ genderPos + "1 " + agePos + "5; v 0.0638166372586212\n" +
				"R Male 65 +;\t\t"		+ genderPos + "1 " + agePos + "6; v 0.0664017332645658\n" +
				"R Female < 25;\t\t"	+ genderPos + "2 " + agePos + "1; v 0.0601363799159130\n" +
				"R Female 25 - 34;\t"	+ genderPos + "2 " + agePos + "2; v 0.0952507479982239\n" +
				"R Female 35 - 44;\t"	+ genderPos + "2 " + agePos + "3; v 0.1118448669616400\n" +
				"R Female 45 - 54;\t"	+ genderPos + "2 " + agePos + "4; v 0.0995811310975810\n" +
				"R Female 55 - 64;\t"	+ genderPos + "2 " + agePos + "5; v 0.0681558604517945\n" +
				"R Female 65 +;\t\t"	+ genderPos + "2 " + agePos + "6; v 0.0869158024001612\n");

			//If region demo exists
			DemoQuestion communityQ = DemoMap.getCommunityDQ();
			if(communityQ != null){
				String communityPos = communityQ.position;
				w.println(
					"TABLE 803\n" +
					"T Region Weight (Toronto)\n" +
					"R The former City of Toronto or East York;\t"			+ communityPos + "1,2;\t" + "v 0.2970\n" +
					"R North York;\t\t\t\t\t\t\t\t"							+ communityPos + "3;\t\t" + "v 0.2524\n" +
					"R Etobicoke or York;\t\t\t\t\t\t"						+ communityPos + "4,5;\t" + "v 0.2358\n" +
					"R Scarborough;\t\t\t\t\t\t\t\t"						+ communityPos + "6;\t\t" + "v 0.2148\n");
			}
		}

		w.println(
			"TABLE 804\n" +
			"T weight execute\n" +
			"X set qual off\n" +
			"X weight unweight\n" +
			"X set qual (not " + genderPos + "3)\n" +
			"X weight 802 803\n" +
			"X set qual (" + genderPos + "3)\n" +
			"X cw(1)\n" +
			"X set qual off\n\n");
	}

	private static void write900s(PrintWriter w, GovernmentLevel govLvl, int checked){
		//Calc how many Copy-Paste Tables there will be
		//Offset is -2 because age and gender are merged, and also_landline is removed
		int copyPasteTablesNum = checked - 2;
		
		String copyPasteTables = "";
		for(int i = 0; i < copyPasteTablesNum; i++){
			String table = 1002 + i + "";				//checked could be zero
			copyPasteTables += table + " ";
		}

		String partyPreference200s = "";
		if(govLvl == GovernmentLevel.PROVINCIAL)
			partyPreference200s = "2 201 202 3 ";
		int size = questions.size() + demoQuestions.size();
		String excel = "excel(name'" + projectName + " - __NAME__ - " + getDate();
		w.println(
			"TABLE 901\n" +
			"X run 1 " + partyPreference200s + "thru " + size + " b1001 nofreq pdp 0 " + excel + "' sheet'&r')\n" +
			"X run 1 " + partyPreference200s + "thru " + size + " b1001 nofreq pdp 0 " + excel + " nosgtest' sheet'&r') nosgtest nottest\n" +
			"X run 1 " + partyPreference200s + "thru " + size + " b1001 novp pdp 0 " + excel + " novp' sheet'&r') nosgtest nottest\n" +
			"X run 1 " + partyPreference200s + "thru " + size + " b" + copyPasteTables + "nofreq pdp 0 " + excel + " copy-paste' sheet'&r &b') nosgtest nottest\n");

		if(govLvl == GovernmentLevel.PROVINCIAL || govLvl == GovernmentLevel.FEDERAL)
			w.println(
				"TABLE 902\n" +
				"X run 1 " + partyPreference200s + "b1001 nofreq pdp 0 " + excel + "' sheet'&r')\n" +
				"X run 1 " + partyPreference200s + "b1001 nofreq pdp 0 " + excel + " nosgtest' sheet'&r') nosgtest nottest\n" +
				"X run 1 " + partyPreference200s + "b1001 novp pdp 0 " + excel + " novp' sheet'&r') nosgtest nottest\n" +
				"X run 1 " + partyPreference200s + "b" + copyPasteTables + "nofreq pdp 0 " + excel + " copy-paste' sheet'&r &b') nosgtest nottest\n");

		w.println();
	}

	private static void write1000s(PrintWriter w, ArrayList<QuestionBase> checked, GovernmentLevel govLvl){
		Logg.info("Begin writing 1000s");
		
		if(checked.isEmpty()){
			Logg.severe("No banner questions were selected");
			return;
		}
		
		//Add commands to uncleCommands, remember length of longest line
		int maxLen = 0;
		ArrayList<QuestionBase> modified = modify(checked, govLvl);
		ArrayList<ArrayList<String>> uncleCommands = new ArrayList<>();
		for(QuestionBase qb : modified){
			String qbPos = qb.position;
			ArrayList<String> lines = new ArrayList<>();
			if(qb.getChoices().isEmpty()){
				String line = "C " + qb.identifier + "; " + qbPos;
				lines.add(line);
			}
			for(String choice[] : qb.getChoices()){
				String line = "C " + qb.identifier + " - " + choice[1] + "; " + qbPos + choice[0];
				
				if(line.length() > maxLen)
					maxLen = line.length();
				
				lines.add(line);
			}
			uncleCommands.add(lines);
		}
		int tabNum = maxLen / 4 + 3;
		
		//In uncleCommands, merge children with moms
		DemoQuestion childrenDQ = DemoMap.getChildrenDQ();
		if(childrenDQ != null){
			int childrenDQpos = modified.indexOf(childrenDQ);
			int momsDQpos = childrenDQpos + 1;
			uncleCommands.get(childrenDQpos).add(uncleCommands.get(momsDQpos).get(0));
			uncleCommands.remove(momsDQpos);
		}
		
		//add tags to the commands
		StringBuilder tags = new StringBuilder();
		String bufferWithTags = Tagger.tag(uncleCommands, tabNum, tags);
		
		w.println(
			"TABLE 1001\n" +
			"O sgtest sgcomp ttcomp .99 high 1 cc(red) .95 high 2 cc(green) '" + tags + "autotag (below paren center)\n" +
			"R TOTAL (u//w); all; novp nor now space 1 freq\n" +
			"R TOTAL (w//t); all; novp nor freq noprint\n" +
			"C TOTAL; all\n" +
			bufferWithTags + "\n"
		);
	
		
		//=== Copy-Paste tables, T1002 and so on ===
		
		//Replace first three questions and replace with a modified version
		uncleCommands.remove(0);
		uncleCommands.remove(0);
		uncleCommands.remove(0);

		String genPos = DemoMap.getGenderDQ().position;
		String agePos = DemoMap.getAgeDQ().position;
		ArrayList<String> ageAndGen = new ArrayList<>();
		ageAndGen.add("C 18-34;\t" + agePos + "1,2");
		ageAndGen.add("C 35-44;\t" + agePos + "3");
		ageAndGen.add("C 45-54;\t" + agePos + "4");
		ageAndGen.add("C 55-64;\t" + agePos + "5");
		ageAndGen.add("C 65+;\t\t" + agePos + "6");
		ageAndGen.add("C Male;\t\t\t" + genPos + "1");
		ageAndGen.add("C Female;\t\t" + genPos + "2");

		String incomePos = DemoMap.getIncomeDQ().position;
		ArrayList<String> income = new ArrayList<>();
		income.add("C <20K;\t\t"	 + incomePos + "1");
		income.add("C 20-40K;\t"	 + incomePos + "2");
		income.add("C 40-60K;\t"	 + incomePos + "3");
		income.add("C 60-80K;\t"	 + incomePos + "4");
		income.add("C 80-100K;\t"	 + incomePos + "5");
		income.add("C 100-250K;\t"	 + incomePos + "6");

		uncleCommands.add(0, income);
		uncleCommands.add(0, ageAndGen);
		
		//Merge Sample and Reached
		//Since uncleCommands was modified (two merges) an offset of -2 is needed
		int posOfReached = modified.indexOf(DemoMap.getReachedDq()) - 2;
		int posOfSample = posOfReached - 1;
		for(String s: uncleCommands.get(posOfReached)){
		    uncleCommands.get(posOfSample).add(s);
		}
		uncleCommands.remove(posOfReached);
		
		
		int tableNum = 2;
		for(ArrayList<String> set : uncleCommands){
			w.println("TABLE 1" + String.format("%03d", tableNum));
			w.println("R Sample; all; novp nor now space 1 freq");
			w.println("C Total; all");
			tableNum++;

			for(String line : set){
				w.println(line);
			}
			w.println();
		}
	}
	
	//Makes several modifications to the questions and adds them to a new ArrayList
	private static ArrayList<QuestionBase> modify(ArrayList<QuestionBase> unmodifiedQuestions, GovernmentLevel govLvl){
		ArrayList<QuestionBase> questions = new ArrayList<>();
		questions.addAll(unmodifiedQuestions);

		DemoQuestion ageDQ = DemoMap.getAgeDQ();
		DemoQuestion genderDQ = DemoMap.getGenderDQ();
		DemoQuestion incomeDQ = DemoMap.getIncomeDQ();
		DemoQuestion communityDQ = DemoMap.getCommunityDQ();
		DemoQuestion childrenDQ = DemoMap.getChildrenDQ();
		DemoQuestion alsoLandlineDQ = DemoMap.getAlsoLandlineDQ();

		//Begin Reorder
		if(communityDQ != null){
			communityDQ.identifier += " 2";
			ArrayList<String[]> community2Choices = communityDQ.getChoices();
			
			DemoQuestion community1 = new DemoQuestion();
			community1.identifier = "COMMUNITY 1";
			community1.addChoice("1", community2Choices.get(0)[1] + " + " + community2Choices.get(1)[1]);
			community1.addChoice("2", community2Choices.get(2)[1]);
			community1.addChoice("3", community2Choices.get(3)[1] + " + " + community2Choices.get(4)[1]);
			community1.addChoice("4", community2Choices.get(5)[1]);
			
			
			questions.add(0, community1);
			Logg.fine("Community Demo Question found, created Community 1 and added to front");
		}

		if(incomeDQ != null){
			questions.remove(incomeDQ);
			questions.add(0, incomeDQ);
			Logg.fine("Income Demo Question found, moved to front");
		}

		if(genderDQ != null){
			questions.remove(genderDQ);
			questions.add(0, genderDQ);
			Logg.fine("Gender Demo Question found, moved to front");
		}

		if(ageDQ != null){
			questions.remove(ageDQ);
			questions.add(0, ageDQ);
			Logg.fine("Age Demo Question found, moved to front");
		}
		Logg.fine("Reorder Finished");
		//Finished Reorder

		
		//If children demo question is found, inserts a "MOMS" dummy question
		if(childrenDQ != null && genderDQ != null){
			int childrenPos = questions.indexOf(childrenDQ);
			DemoQuestion dq = new DemoQuestion();
			dq.identifier = "MOMS";
			dq.position = "(" + childrenDQ.position + "1 " + genderDQ.position + "2)";
			questions.add(childrenPos + 1, dq);//inserts after children question
			Logg.info("\"Mom's\" added");
		}
		
		
		//If age demo question is found, merges < 24 with < 34
		if(ageDQ != null){
			ArrayList<String[]> choices = ageDQ.getChoices();
			if(choices.get(0)[1].contains("25"))
				choices.remove(0);
			
			String[] choice = choices.get(0);
			choice[0] = "1," + choice[0];
			choice[1] = "< 34";
			Logg.info("Age Merged");
		}
		
		
		//If income demo question is found, Removes "Prefer not to answer" and "> 200'000"
		if(incomeDQ != null){
			ArrayList<String[]> choices = incomeDQ.getChoices();
			if(choices.size() > 1){
				choices.remove(choices.size() - 1);
				choices.remove(choices.size() - 1);
			}
			Logg.info("Income DQ shortened");
		}
		
			
		//If alsoLandline demo question is found, removes "also landline", and adds a sample mock-question
		if(alsoLandlineDQ != null){
			int alsoLandlinePos = questions.indexOf(alsoLandlineDQ);
			DemoQuestion sampleMock = new DemoQuestion();
			sampleMock.identifier = "SAMPLE";
			switch(govLvl){
				case MUNICIPAL:
					sampleMock.position = MUNICIPAL_SAMPLE_POSITION;
					break;
				case PROVINCIAL:
					sampleMock.position = PROVINCIAL_SAMPLE_POSITION;
					break;
				case FEDERAL:
					sampleMock.position = FEDERAL_SAMPLE_POSITION;
					break;
			}
			sampleMock.addChoice("0", "Landline");
			sampleMock.addChoice("1", "Cellphone");
			
			questions.remove(alsoLandlineDQ);
			questions.add(alsoLandlinePos - 1, sampleMock);//inserts before landline question
			Logg.info("Added Sample DQ");
		}
		
		return questions;
	}

	private static int[] checkDichotomy(ArrayList<String[]> choices){
		int[] dichotomyPositions = {-1, 0};
		for(byte i = 0; i < choices.size(); i++){
			String choiceLabel = choices.get(i)[1];
			for(String[] tbPair : TB_PAIRS){
				//CHECK THIS!!! //Might not work //reorder the loops - for(pairs) first, then for(choices)
				if(Pattern.matches(tbPair[0], choiceLabel))
					dichotomyPositions[0] = i;
				else if(Pattern.matches(tbPair[1], choiceLabel))
					dichotomyPositions[1] = i;
			}
		}

		return dichotomyPositions;
	}

	//Very Satisfied, Somewhat Satisfied, Somewhat Dissatisfied, Very Dissatisfied
	private static boolean checkMeansNeeded(ArrayList<String[]> choices){
		for(int i = 0; i < MEAN_KEYWORDS.size(); i++){
			if(!MEAN_KEYWORDS.get(i).equalsIgnoreCase(choices.get(i)[1])){
				return false;
			}
		}
		return true;
	}

	private static String getDate(){
		Date date = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("MMMM dd yyyy");
		return sf.format(date);
	}
}
