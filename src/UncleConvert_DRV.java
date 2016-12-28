import javax.swing.UIManager;
import java.io.File;
import java.util.ArrayList;

public class UncleConvert_DRV{
	public static void main(String[] args){
		test();
		//launchGUI();
	}

	@SuppressWarnings("unused")
	private static void launchGUI(){
		javax.swing.SwingUtilities.invokeLater(() -> {
			try{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}catch(Exception e){
				Logg.severe("Failed to set Look and Feel of system UI");
			}
			new GUI();
			Logg.fine("GUI launched successfully");
		});
	}

	private static void test(){
		//String fname = "G:\\Public Works\\Tabs\\PWG1.ASC";
		//String fname = "G:\\FORUM\\FOJT\\Tabs\\FOJT.ASC";
		String fname = "E:\\MAN_SON\\Dropbox\\Work\\Tabs\\ASC\\FOJT.ASC";
		GovernmentLevel govLvl = GovernmentLevel.MUNICIPAL;

		Parser.parseASCFile(fname);

		if(Qnair.isEmpty()){
			System.out.println("Empty Qnair, file no exist?");
			return;
		}

		ArrayList<DemoQuestion> demoQuestions = Qnair.getDemoQuestions();
		ArrayList<QuestionBase> questionBases = new ArrayList<>();
		questionBases.addAll(demoQuestions);
		questionBases.add(Qnair.getQuestions().get(0));										// get first non-demo question
		questionBases.add(Qnair.getQuestions().get(Qnair.getQuestions().size() - 1));		// get last non-demo question

		Writer.writeFile(new File(fname), questionBases, govLvl);

		System.out.println("DONE");
	}
}
