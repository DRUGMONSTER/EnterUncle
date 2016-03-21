import java.util.ArrayList;

public class Question extends QuestionBase{
	public Question(){
		super();
	}

	public Question(String var, int cw, String l, String ident, String pos, String skipCon, ArrayList<String[]> choices){
		super(var, cw, l, ident, pos, skipCon, choices);
	}
}
