import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class QuestionBase{
	public String variable;
	private int codeWidth;
	public String label;
	public String identifier;
	public String position;
	private String shortLabel;
	private String skipCondition;
	private int ifSkip;
	private int elseSkip;
	private ArrayList<String[]> choices = new ArrayList<>();//[0]=code; [1]=label; [2]=skipDestination

	public QuestionBase(){
		variable = "";
		codeWidth = -1;
		label = "";
		identifier = "";
		position = "";
		shortLabel = "";
		skipCondition = "";
		ifSkip = 0;
		elseSkip = 0;
	}

	public QuestionBase(String var, int cw, String l, String sl, String ident, String pos, String skipCon, int is, int es, ArrayList<String[]> ch){
		variable = var;
		codeWidth = cw;
		label = l;
		identifier = ident;
		position = pos;
		shortLabel = sl;
		skipCondition = skipCon;
		ifSkip = is;
		elseSkip = es;

		choices = ch;
	}

	public void setAll(String var, int cw, String l, String ident, String pos, String skipCon, ArrayList<String[]> choices){
		variable = var;
		codeWidth = cw;
		label = l;
		identifier = ident;
		position = pos;
		skipCondition = skipCon;
		for(String[] choice : choices){
			addChoice(choice[0], choice[1]);
		}
	}
	
	public void addChoice(String code, String label){
		choices.add(new String[] {code, label});
	}

	public ArrayList<String[]> getChoices(){
		return choices;
	}
}
