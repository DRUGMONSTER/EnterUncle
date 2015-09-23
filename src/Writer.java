import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class Writer{
	private static final int GENDER_IDENTIFIER = 0; //also pretty smelly
	private static final int CHILDREN_IDENTIFIER = 1;
	private static final int AGE_IDENTIFIER = 2;
	private static final int INCOME_IDENTIFIER = 8;

	public static ArrayList<String> identifiers;

	private static final ArrayList<String[]> TB_PAIRS = new ArrayList<String[]>();
	private static String projectName;
	private static ArrayList<Question> questions;
	private static ArrayList<DemoQuestion> demoQuestions;

	public static void writeFile(File file, ArrayList<QuestionBase> checked){
		init();

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
			String variable = q.getVariable();
			writer.println("TABLE " + tableNum++);

			writer.println("T " + q.getLabel());
			writer.println("T &wt Q" + q.getIdentifier());

			writeChoices(writer, q);

			int[] positions = checkDichotomy(q.getChoices());
			if(positions[0] != -1){								//Dichotomy found
				writer.println("R ; null");
				int size =  q.getChoices().size();
				writer.println("R T-B; none; ex(RU" + (size - positions[0] + 1) + "-RU" + (size - positions[1] + 1) + ") nofreq");
				Logg.fine("Dichotomy found at " + variable);
				Logg.fine("Positions " + positions[0] + ":" + positions[1]);
			}

			writer.println();
			Logg.info("Wrote question " + q.getVariable());
		}

		//Print Demo Questions
		for(DemoQuestion dq : demoQuestions){
			writer.println("TABLE " + tableNum++);

			writer.println("T " + dq.getLabel());
			writer.println("T &wt " + dq.getIdentifier());

			writeChoices(writer, dq);

			writer.println();
			Logg.info("Wrote demo question " + dq.getVariable());
		}

		writer.println();
		write600s(writer);
		write800s(writer, demoQuestions, 0);
		write900s(writer, checked.size());
		write1000s(writer, checked);

		writer.close();
	}

	private static void writeChoices(PrintWriter writer, QuestionBase qb){
		String qbPos = qb.getPosition();
		ArrayList<String[]> choices = qb.getChoices();
		String[] means = new String[0];
		if(qb.getIdentifier().equals(identifiers.get(AGE_IDENTIFIER))){//smelly
			means = new String[6];
			means[0] = "; v20";
			means[1] = "; v29.5";
			means[2] = "; v39.5";
			means[3] = "; v49.5";
			means[4] = "; v59.5";
			means[5] = "; v70";
		}else if(qb.getIdentifier().equals(identifiers.get(INCOME_IDENTIFIER))){
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
			if(c[1].length() > 0)
				writer.println("R " + c[1].substring(0, 1).toUpperCase() + c[1].substring(1) +
				"; " + qbPos + c[0] + Tagger.getTabs(addTab) + means[i]);
			else{//rare case that choice label length is zero
				writer.println("R " + "; " + qbPos + c[0] + Tagger.getTabs(addTab) + means[i]);
				Logg.warning("Choice Lable with length 0 printed");
			}
		}
		for(; i < choices.size(); i++){
			String[] c = choices.get(i);
			if(c[1].length() > 0)
				writer.println("R " + c[1].substring(0,1).toUpperCase() + c[1].substring(1) + "; " + qbPos + c[0]);
			else{//rare case that choice label length is zero
				writer.println("R " + "; " + qbPos + c[0]);
				Logg.warning("Choice Lable with length 0 printed");
			}
		}
		writer.print(nullAndMean);
	}

	private static void write600s(PrintWriter w){
		w.println(
				"TABLE 601\n" +
						"T Load data, run all tabs for checking purposes\n" +
						"X load rep char from 'Y:\\" + projectName + "\\" + projectName + "_COMP.TXT'\n" +
						"X justify 89:93 right\n" +
						"X ex 901\n\n" +

						"TABLE 602\n" +
						"T Load data, weight by age, gender, region\n" +
						"X load rep char from 'Y:\\" + projectName + "\\" + projectName + "_COMP.TXT'\n" +
						"X justify 89:93 right\n" +
						"X if (r(89:93,0:XXX)) DEL\n" +
						"X ex 804\n" +
						"X ex 901\n\n" +

						"TABLE 699\n" +
						"T Load data, weight by voter turnout, run initial analysis tabs\n" +
						"X load rep char from 'Y:\\" + projectName + "\\" + projectName + "_COMP.TXT'\n" +
						"X justify 89:93 right\n" +
						"X if (r(89:93,0:XXX)) DEL\n" +
						"X ex 804\n" +
						"X calc F310:325 12 (W)\n\n");
	}

	private static void write800s(PrintWriter w, ArrayList<DemoQuestion> dqs, int region){
		DemoQuestion genderDQ = null;
		DemoQuestion ageDQ = null;


		for(DemoQuestion dq : dqs){
			if(dq.getIdentifier().equals(identifiers.get(GENDER_IDENTIFIER)))	//Smelly
				genderDQ = dq;
			else if(dq.getIdentifier().equals(identifiers.get(AGE_IDENTIFIER)))
				ageDQ = dq;
		}

		String genderPos = "0";
		String  agePos = "0";
		if(genderDQ != null)
			genderPos = genderDQ.getPosition();
		if(ageDQ != null)
			agePos = ageDQ.getPosition();

		//0 = Toronto
		if(region == 0){
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
			w.println(
				"TABLE 803\n" +
				"T Region Weight (Toronto)\n"	+
				"R The former City of Toronto or East York;\t"	+ "999-1,2;\t"	+ "v 0.2970\n" + //needs edit
				"R North York;\t\t\t\t\t\t\t\t"				 	+ "999-3;\t\t"	+ "v 0.2524\n" +
				"R Etobicoke or York;\t\t\t\t\t\t"				+ "999-4,5;\t"	+ "v 0.2358\n" +
				"R Scarborough;\t\t\t\t\t\t\t\t"				+ "999-6;\t\t"	+ "v 0.2148\n");
		}

		w.println(
				"TABLE 804\n" +
						"T weight execute\n" +
						"X set qual off\n" +
						"X weight unweight\n" +
						"X weight 802 803\n" +
						"X set qual off\n\n");
	}

	private static void write900s(PrintWriter w, int checked){
		String copyPasteTables = "";
		for(int i = 0; i < checked; i++){
			String table = "" + (1002 + i);
			copyPasteTables += table + " ";
		}

		int size = questions.size() + demoQuestions.size();
		String excel = "excel(name'" + projectName + " - __NAME__ - " + getDate();
		w.println(
				"TABLE 901\n" +
						"X run 1 thru " + size + " b1001 nofreq pdp 0 " + excel + "' sheet'&r')\n" +
						"X run 1 thru " + size + " b1001 nofreq pdp 0 " + excel + " nosgtest' sheet'&r') nosgtest nottest\n" +
						"X run 1 thru " + size + " b1001 novp pdp 0 " + excel + " novp' sheet'&r') nosgtest nottest\n" +
						"X run 1 thru " + size + " b" + copyPasteTables + "nofreq pdp 0 " + excel + " copy-paste' sheet'&r &b') nosgtest nottest\n\n");
	}

	private static void write1000s(PrintWriter w, ArrayList<QuestionBase> checked){
		Logg.info("Begin writing 1000s");

		if(checked.isEmpty()){
			Logg.severe("No banner questions were selected");
			return;
		}

		//add lines to bufferLines without tags, remember length of longest line
		int tabNum;
		int maxLen = 0;
		ArrayList<ArrayList<String>> bufferOfLines = new ArrayList<ArrayList<String>>();
		ArrayList<QuestionBase> newOrder = reorderQuestions(checked);
		addMoms(newOrder);
		mergeAge(newOrder);
		cropIncomeDQ(newOrder);
		for(QuestionBase qb : newOrder){
			String qbPos = qb.getPosition();
			ArrayList<String> lines = new ArrayList<String>();
			for(String choice[] : qb.getChoices()){
				String line = "C " + qb.getIdentifier() + " - " + choice[1] + "; " + qbPos + choice[0];

				if(line.length() > maxLen)
					maxLen = line.length();

				lines.add(line);
			}
			bufferOfLines.add(lines);
		}
		tabNum = maxLen / 4 + 3;


		//add tags to the lines in bufferOfLines
		StringBuilder tags = new StringBuilder();
		String bufferWithTags = Tagger.tag(bufferOfLines, tabNum, tags);

		w.println(
				"TABLE 1001\n" +
						"O sgtest sgcomp ttcomp .99 high 1 cc(red) .95 high 2 cc(green) '" + tags + "autotag (below paren center)\n" +
						"R TOTAL (u//w); all; novp nor now space 1 freq\n" +
						"R TOTAL (w//t); all; novp nor freq noprint\n" +
						"C TOTAL; all\n" +
						bufferWithTags + "\n"
		);
	}

	private static ArrayList<QuestionBase> reorderQuestions(ArrayList<QuestionBase> unorderedQuestions){
		Logg.info("Begin Reorder");

		ArrayList<QuestionBase> ordered = new ArrayList<QuestionBase>();
		ordered.addAll(unorderedQuestions);

		DemoQuestion ageDQ = null;
		DemoQuestion genderDQ = null;
		DemoQuestion incomeDQ = null;
		DemoQuestion communityDQ = null;

		for(QuestionBase qb : unorderedQuestions){
			String ident = qb.getIdentifier();

			if(ident.equals("AGE"))            //Smelly
				ageDQ = (DemoQuestion)qb;
			else if(ident.equals("GENDER"))
				genderDQ = (DemoQuestion)qb;
			else if(ident.equals("INCOME"))
				incomeDQ = (DemoQuestion)qb;
			else if(ident.equals("COMMUNITY"))
				communityDQ = (DemoQuestion)qb;
		}

		if(communityDQ != null){
			ordered.remove(communityDQ);
			ordered.add(0, communityDQ);
			Logg.fine("Community Demo Question found, moved to front");
		}

		if(incomeDQ != null){
			ordered.remove(incomeDQ);
			ordered.add(0, incomeDQ);
			Logg.fine("Income Demo Question found, moved to front");
		}

		if(genderDQ != null){
			ordered.remove(genderDQ);
			ordered.add(0, genderDQ);
			Logg.fine("Gender Demo Question found, moved to front");
		}

		if(ageDQ != null){
			ordered.remove(ageDQ);
			ordered.add(0, ageDQ);
			Logg.fine("Age Demo Question found, moved to front");
		}

		Logg.info("Exit Reorder");
		return ordered;
	}

	//If children demo question is found, inserts a "MOMS" dummy question
	private static void addMoms(ArrayList<QuestionBase> questionBases){
		QuestionBase genderQ = new QuestionBase();
		QuestionBase childrenQ = new QuestionBase();
		int childrenPos = 0;

		for(int i = 0; i < questionBases.size(); i++){
			QuestionBase qb = questionBases.get(i);

			if(qb.getIdentifier().equals(identifiers.get(GENDER_IDENTIFIER))){			//smelly
				genderQ = qb;
				continue;
			}
			if(qb.getIdentifier().equals(identifiers.get(CHILDREN_IDENTIFIER))){	//smelly
				childrenQ = qb;
				childrenPos = i;
			}
		}

		DemoQuestion dq = new DemoQuestion();
		dq.setIdentifier("MOMS");
		dq.setPosition("(" + childrenQ.getPosition() + "1 " + genderQ.getPosition() + "2)");
		dq.addChoice("", "");
		questionBases.add(childrenPos + 1, dq);//inserts after children question
	}

	//If age demo question is found, merges < 24 with < 34
	private static void mergeAge (ArrayList<QuestionBase> questionBases){
		QuestionBase ageDQ = null;

		for(QuestionBase qb : questionBases){
			if(qb.getIdentifier().equals(identifiers.get(AGE_IDENTIFIER)))            //smelly
				ageDQ = qb;
		}

		if(ageDQ == null)//Age not found
			return;

		ArrayList<String[]> choices = ageDQ.getChoices();
		if(choices.get(0)[1].contains("25"))
			choices.remove(0);

		String[] choice = choices.get(0);
		choice[0] = "1," + choice[0];
		choice[1] = "< 34";
	}

	//Removes "Prefer not to answer" and "> 200'000"
	private static void cropIncomeDQ(ArrayList<QuestionBase> questionBases){
		QuestionBase incomeDQ = null;

		for(QuestionBase qb : questionBases){
			if(qb.getIdentifier().equals(identifiers.get(INCOME_IDENTIFIER)))            //smelly
				incomeDQ = qb;
		}

		if(incomeDQ == null)//Income not found
			return;

		ArrayList<String[]> choices = incomeDQ.getChoices();
		if(choices.size() > 1){
			choices.remove(choices.size() - 1);
			choices.remove(choices.size() - 1);
		}
	}

	private static void init(){
		questions = Qnair2.getQuestions();
		demoQuestions = Qnair2.getDemoQuestions();

		TB_PAIRS.add(new String[]{"(?i).*\\bagree\\b.*",			"(?i).*\\bdisagree\\b.*"});
		TB_PAIRS.add(new String[]{"(?i).*\\bapprove\\b.*",			"(?i).*\\bdisapprove\\b.*"});
		TB_PAIRS.add(new String[]{"(?i).*\\bsupport\\b.*", 			"(?i).*\\boppose\\b.*"});
		TB_PAIRS.add(new String[]{"(?i).*\\bhave\\s+heard\\b.*",	"(?i).*\\bhave\\s+not\\s+heard\\b.*"});
		TB_PAIRS.add(new String[]{"(?i).*\\byes\\b.*",				"(?i).*\\bno\\b.*"});
	}

	private static int[] checkDichotomy(ArrayList<String[]> choices){
		int[] dichotomyPositions = {-1, 0};
		for(byte i = 0; i < choices.size(); i++){
			String choiceLabel = choices.get(i)[1];
			for(String[] tbPair : TB_PAIRS){
				//dichotomyPositions[0] = -1;
				//CHECK THIS!!! //Might not work //reorder the loops - for(pairs) first, then for(choices)
				if(Pattern.matches(tbPair[0], choiceLabel))
					dichotomyPositions[0] = i;
				else if(Pattern.matches(tbPair[1], choiceLabel))
					dichotomyPositions[1] = i;
			}
		}

		return dichotomyPositions;
	}

	private static String getDate(){
		Date date = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("MMMM dd yyyy");
		return sf.format(date);
	}
}