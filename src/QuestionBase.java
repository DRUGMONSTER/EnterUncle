import java.util.ArrayList;

public class QuestionBase{
	private String variable;
	private int codeWidth;
	private String label;
	private String questionIdentifier;
	private String position;
	private ArrayList<String[]> choices = new ArrayList<String[]>();//[0]=code; [1]=label;

	public QuestionBase(){
		variable = "";
		codeWidth = -1;
		label = "";
		questionIdentifier = "";
		position = "";
	}

	public QuestionBase(String var, int cw, String l, String ident, String pos, ArrayList<String[]> choices){
		variable = var;
		codeWidth = cw;
		label = l;
		questionIdentifier = ident;
		position = pos;
		for(String[] choice : choices){
			addChoice(choice[0], choice[1]);
		}
	}

	public void setAll(String var, int cw, String l, String ident, String pos, ArrayList<String[]> choices){
		variable = var;
		codeWidth = cw;
		label = l;
		questionIdentifier = ident;
		position = pos;
		for(String[] choice : choices){
			addChoice(choice[0], choice[1]);
		}
	}

	public void setVariable(String var){
		variable = var;
	}

	public void setCodeWidth(int aCodeWidth){
		codeWidth = aCodeWidth;
	}
	
	public void setLable(String l){
		label = l;
	}
	
	public void setIdentifier(String qIdent){
		questionIdentifier = qIdent;
	}

	public void setPosition(String pos){
		position = pos;
	}
	
	public void addChoice(String code, String label){
		choices.add(new String[] {code, label});
	}
	
	public String getVariable(){
		return variable;
	}

	public int getCodeWidth(){
		return codeWidth;
	}
	
	public String getLabel(){
		return label;
	}

	public String getIdentifier(){
		return questionIdentifier;
	}

	public String getPosition(){
		return position;
	}

	public ArrayList<String[]> getChoices(){
		return choices;
	}
}
