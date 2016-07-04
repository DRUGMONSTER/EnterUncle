import java.util.ArrayList;

public class DemoQuestion extends QuestionBase{

	public DemoQuestion(){
		super();
	}

	public DemoQuestion(String var, int cw, String l, String sl, String ident, String pos, String skipCon, int ifSkipDest, int elseSkipDest, ArrayList<String[]> choices){
		super(var, cw, l, sl, ident, pos, skipCon, ifSkipDest, elseSkipDest, choices);
	}
}
