import javax.swing.UIManager;
import java.io.File;
import java.util.ArrayList;

public class UncleConvert_DRV{
	public static void main(String[] args){
		Logg.init();
		test();
		//launchGUI();
	}

	private static void launchGUI(){
		javax.swing.SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				try{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				}catch(Exception e){
					Logg.severe("Failed to set Look and Feel of system UI");
				}
				new GUI();
				Logg.fine("GUI launched successfully");
			}
		});
	}

	private static void test(){
		String fname = "G:\\FORUM\\FODF\\Tabs\\FODF.ASC";
		//String fname = "C:\\Users\\Plaxx\\Desktop\\Work\\Tabs\\ASC\\FODF.ASC";

		Parser.parseASCFile(fname);

		if(Qnair.isEmpty()){
			System.out.println("Empty Qnair, file no exist?");
			return;
		}

		ArrayList<DemoQuestion> demoQuestions = Qnair.getDemoQuestions();
		ArrayList<QuestionBase> questionBases = new ArrayList<QuestionBase>();
		questionBases.addAll(demoQuestions);
		questionBases.add(Qnair.getQuestions().get(0));									// get first non-demo question
		questionBases.add(Qnair.getQuestions().get(Qnair.getQuestions().size() - 1));		// get last non-demo question

		Writer.writeFile(new File(fname), questionBases);

		System.out.println("DONE");
	}
}
