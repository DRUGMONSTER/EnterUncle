import java.util.ArrayList;

public class Question extends QuestionBase{
	public Question(){
		super();
	}

	public Question(String var, int cw, String l, String sl, String ident, String pos, String skipCon, String skipDest, ArrayList<String[]> choices){
		super(var, cw, l, sl, ident, pos, skipCon, skipDest, choices);
	}
}
