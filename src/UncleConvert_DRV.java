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
		String fname = "C:\\Users\\Plaxx\\Desktop\\FOCX.ASC";
		Parser.parseASCFile(fname);

		if(Qnair2.isEmpty()){
			System.out.println("Empty Qnair, file no exist?");
			return;
		}


		System.out.println("Empty Qnair, file no exist?");

		ArrayList<DemoQuestion> demoQuestions = Qnair2.getDemoQuestions();
		ArrayList<QuestionBase> questionBases = new ArrayList<QuestionBase>();
		questionBases.addAll(demoQuestions);
		questionBases.add(Qnair2.getQuestions().get(Qnair2.getQuestions().size() - 1)); // get last non-demo question

		Writer.writeFile(new File(fname), questionBases);
	}
}
