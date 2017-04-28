import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public class Parser{
	private static final int START_POS = 248;

	public static boolean parseASCFile(String filepath){
		ArrayList<RawQuestion> rawQuestions = new ArrayList<>();

		try{
			boolean success = readAndParseQuestions(filepath, rawQuestions);
			if(!success)
				return false;
		}catch(Exception e){//if an exception is triggered somewhere in readAndParseQuestions()
			e.printStackTrace();
			Logg.severe("Failed to read file");
			return false;
		}

		formatAndAddQuestions(rawQuestions);

		ArrayList questions = Qnair.getQuestions();
		ArrayList demoQuestions = Qnair.getDemoQuestions();

		Logg.fine(questions.size() + " questions were added");
		Logg.fine(demoQuestions.size() + " demographic questions were added");
		Logg.fine(questions.size() + demoQuestions.size() + " questions total");

		Qnair.removeBadQuestions();
		return true;
	}

	private static boolean readAndParseQuestions(String filepath, ArrayList<RawQuestion> rawQuestions){
		Scanner sc;
		try{
			sc = new Scanner(new File(filepath), "UTF-8");
		}catch(Exception e){
			Logg.severe("Can't open asc file " + e.getMessage());
			return false;
		}

		sc.nextLine();
		sc.nextLine();
		sc.nextLine();
		sc.nextLine();
		sc.nextLine();
		String line = sc.nextLine();
		while(true){
			if(line.startsWith("---")){
				Logg.fine("Stopped Parsing");
				break;                //Stop Parsing
			}

			RawQuestion rq = new RawQuestion();

			//This is the order these commands appear in .ASC files
			if(line.startsWith("*ME")){
				Logg.info("\"*ME\" Message to the Interviewer Found");
				Logg.info("Line: " + line);

				sc.nextLine();//do nothing
				line = sc.nextLine();
			}
			if(line.startsWith("*LL")){
				Logg.info("\"*LL\" Long Label Found");
				Logg.info("Line: " + line);

				rq.variableRaw = line;
				StringBuilder label = new StringBuilder(sc.nextLine());
				while(!label.toString().endsWith("]")){		//if multiple lines
					line = sc.nextLine();

					if(line.length() < 8)
						label.append(line);
					else
						label.append("\n").append(line);
				}
				rq.label = label.toString();
				
				line = sc.nextLine();
			}
			if(line.startsWith("*SL")){
				Logg.fine("\"*SL\" Short Label Found");
				Logg.info("Line: " + line);
				
				rq.shortLabel = sc.nextLine();
				line = sc.nextLine();
			}
			if(line.startsWith("*MA")){
				Logg.info("\"*MA\" Mask Found");
				Logg.info("Line: " + line);

				sc.nextLine();//do nothing
				line = sc.nextLine();
			}
			if(line.startsWith("*SK")){
				Logg.info("\"*SK\" Skip Found");
				Logg.info("Line: " + line);

				rq.skipDestination = sc.nextLine();
				rq.skipCondition = sc.nextLine();

				line = sc.nextLine();
			}
			if(line.startsWith("*CL")){
				Logg.info("\"*CL\" Code List Found");
				Logg.info("Line: " + line);

				String choice = sc.nextLine();
				do{
					rq.choices.add(choice);
					choice = sc.nextLine();
				}while(!choice.startsWith("---"));
				line = sc.nextLine();
			}
			rawQuestions.add(rq);
		}

		sc.close();
		return true;
	}

	//reads from rawQuestions and adds questions and demo questions to Qnair
	private static void formatAndAddQuestions(ArrayList<RawQuestion> rawQuestions){
		int pos = START_POS;
		
		//Set VariableName and Position of RawQuestions first
		for(int i = 0; i < rawQuestions.size(); i++){
			RawQuestion rq = rawQuestions.get(i);
			String[] rawVariableParts = rq.variableRaw.split(" ");
			String variableName = rawVariableParts[1];
			Logg.fine("Variable read: " + variableName);
			
			int codeWidth = Integer.parseInt(rawVariableParts[2].substring(2));
			String position = pos + "-";
			
			rq.variable = variableName;
			rq.codeWidth = codeWidth;
			rq.position = position;
			rq.quePosition = i;
			
			pos += codeWidth;
			
		}
		
		//Look at Intro question and try to identify the location
		for(RawQuestion rq : rawQuestions){
		    if(rq.variable.equals("INTRO")){
				String rawShortLabel = rq.shortLabel;
				if(rawShortLabel != null)
					Qnair.setLocation(rawShortLabel.substring(1, rawShortLabel.length() - 1));
				break;
			}
		}

		for(RawQuestion rq : rawQuestions){
			boolean demoQ = false;
			String variableName = rq.variable;

			//Mark demographic questions
			if(variableName.charAt(0) == 'D'){
				Logg.fine("Variable " + variableName + " was marked as demographic");
				demoQ = true;
			}

			//Replace tabs and remove square brackets around label
			String rawLabel = rq.label.replace('\t', ' ');
			String label = rawLabel.substring(1, rawLabel.length() - 1).trim();

			//Find and capitalize first letter
			int periodIndex = label.indexOf('.');
			if(periodIndex < 0)
				periodIndex = 0;
			for(int i = periodIndex; i < label.length(); i++){
				if(Character.isLetter(label.charAt(i))){
					label = label.substring(0, i) + label.substring(i, i + 1).toUpperCase() + label.substring(i + 1);
					break;
				}
			}

			//Add shortLabel
			String shortLabel = "";
			if(rq.shortLabel != null)
				shortLabel = rq.shortLabel.substring(1, rq.shortLabel.length() - 1);

			//Determine Question Identifier from label
			String identifier = "";
			if(!demoQ){
				if(!label.isEmpty()){
					int delimiter = label.indexOf(".");		//find first period

					if(delimiter == -1 || delimiter > 9)	//period not found or too far away
						delimiter = label.indexOf(" ");		//use first space instead

					if(delimiter == -1){					//space not found either
						identifier = variableName;
						Logg.warning(variableName + " - question identifier not found and label not empty");
					}else
						identifier = label.substring(0, delimiter).toUpperCase();
				}
			}

			//Add Skips
			String skipCondition = rq.skipCondition;
			int skipDestinationIf = 0;
			int skipDestinationElse = 0;
			
			if(skipCondition != null){
				String skipDestinationIfStr;
				String skipDestinationElseStr = "";
				
				boolean elseExists = false;
				int offSet = 2;
				int elseSkipStartPos = rq.skipDestination.indexOf(' ');
				if(elseSkipStartPos != -1){
					skipDestinationElseStr = rq.skipDestination.substring(elseSkipStartPos + 6);
					skipDestinationIfStr = rq.skipDestination.substring(offSet, elseSkipStartPos);
					elseExists = true;
				}else{
					skipDestinationIfStr = rq.skipDestination.substring(offSet);
				}
				
				//skip destination may be a variable name, if it is, find it, then calculate it's relative position
				skipDestinationIf = findQuePosition(skipDestinationIfStr, rq.quePosition, rawQuestions);
				if(elseExists)
					skipDestinationElse = findQuePosition(skipDestinationElseStr, rq.quePosition, rawQuestions);
			}
			
			
			//Add Choices
			ArrayList<String[]> choices = new ArrayList<>();//[0]=code; [1]=label; [2]=skipDestination;
			for(int i = 0; i < rq.choices.size(); i++){
				String rawChoiceStr = rq.choices.get(i);

				int choiceLabelEndPos = rawChoiceStr.indexOf(']');
				String choiceLabel = rawChoiceStr.substring(1, choiceLabelEndPos);

				if(choiceLabel.length() > 0)		//capitalize first letter
					choiceLabel = choiceLabel.substring(0, 1).toUpperCase() + choiceLabel.substring(1);

				int codeStartPos = rawChoiceStr.indexOf('[', choiceLabelEndPos) + 1;
				String code = rawChoiceStr.substring(codeStartPos, codeStartPos + rq.codeWidth);

				String skipToQuestion = "";
				int skipStartPos = rawChoiceStr.indexOf('>', codeStartPos);
				if(skipStartPos != -1)
					skipToQuestion = rawChoiceStr.substring(skipStartPos + 1, rawChoiceStr.length());

				choices.add(new String[]{code, choiceLabel, skipToQuestion});
			}

			if(demoQ){
				Qnair.addDemoQuestion(variableName, rq.codeWidth, label, shortLabel, identifier, rq.position, skipCondition, skipDestinationIf, skipDestinationElse, choices);
				Logg.fine("Question " + variableName + " was added as demographic");
			}else{
				Qnair.addQuestion(variableName, rq.codeWidth, label, shortLabel, identifier, rq.position, skipCondition, skipDestinationIf, skipDestinationElse, choices);
				Logg.fine("Question " + variableName + " was added");
			}
		}
		//Set Identifiers for DemoQuestions
		for(DemoQuestion dq : Qnair.getDemoQuestions()){
			boolean set = false;
			for(String regex : DemoMap.getRegexPatterns()){
				if(Pattern.matches(regex, dq.label)){
					dq.identifier = DemoMap.getIdentifier(regex, dq);
					set = true;
					break;
				}
			}
			if(!set)
				dq.identifier = "DEMO_QUESTION_ID_NOT_FOUND";
		}
	}
	
	private static int findQuePosition(String relativeSkipDestination, int quePosition, ArrayList<RawQuestion> rawQuestions){
		int position = -1;
		try{
			position = quePosition + Integer.parseInt(relativeSkipDestination);
		}catch(NumberFormatException e){
			
			//If a slash is present, just ignore it
			if(relativeSkipDestination.charAt(0) == '/')
				relativeSkipDestination = relativeSkipDestination.substring(1);
			
			//Find the variable that the skip points to
			for(RawQuestion rq2 : rawQuestions){
				if(rq2.variable.equals(relativeSkipDestination)){
					position = rq2.quePosition;
					break;
				}
			}
		}
		return position;
	}

	private static class RawQuestion{
		String variableRaw;
		String variable;
		int codeWidth;
		int quePosition;
		String label;
		String position;
		String shortLabel;
		String skipCondition;
		String skipDestination;
		ArrayList<String> choices = new ArrayList<>();
	}
}
