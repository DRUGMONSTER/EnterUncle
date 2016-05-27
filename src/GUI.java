import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;


public class GUI extends JFrame{
	public static final int MUNICIPAL = 0;
	public static final int PROVINCIAL = 1;
	public static final int FEDERAL = 2;

	private static final String VERSION = "0.32.3b";
	private static final long serialVersionUID = 1L;
	private static final int FRAME_WIDTH = 720;
	private static final int FRAME_HEIGHT = 480;
	private static File ascFile = null;
	private static boolean written = false;

	private JButton writeOutBTN;
	private JDesktopPane fileArea;
	private JTextField fileToConvPathTF;
	private JTextField statusTF;

	private JPanel bannerWrap;
	private ButtonGroup governmentLevel;
	private Map<JCheckBox, Question> bannerQuestions = new LinkedHashMap<>();
	private Map<JCheckBox, DemoQuestion> bannerDemoQuestions = new LinkedHashMap<>();
	private JRadioButton[] radioButtons = {new JRadioButton("Municipal", true), new JRadioButton("Provincial"), new JRadioButton("Federal")};

	public GUI(){
		setTitle("Uncle Convert beta v" + VERSION);
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(0, 2, 15, 0));
		loadComponents(mainPanel);
		
		ButtonListener convertListener = new ButtonListener();
		writeOutBTN.addActionListener(convertListener);
		
		DropTarget qaxDropTarget = new DropTarget(){
			public synchronized void drop(DropTargetDropEvent evt){
				try{
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					@SuppressWarnings("unchecked")
					List<File> droppedFiles = (List<File>)evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
					ascFile = droppedFiles.get(0);
					readFile(ascFile);
					//statusTF.setText("File Read or Parse Error");
				}catch(Exception ex){
					statusTF.setText("Drag and Drop Error");
				}
			}
		};
		fileArea.setDropTarget(qaxDropTarget);
		
		getContentPane().add(mainPanel);
		setVisible(true);

		System.out.println("LOADED GUI");
	}

	private void loadComponents(JPanel mainPanel){
		mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout(0, 15));
		
		bannerWrap = new JPanel();
		bannerWrap.setLayout(new GridLayout(0, 2));
		JScrollPane scrollPane = new JScrollPane(bannerWrap);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		leftPanel.add(scrollPane, BorderLayout.CENTER);
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout(0, 0));
		
		fileToConvPathTF = new JTextField();
		fileToConvPathTF.setForeground(Color.GRAY);
		fileToConvPathTF.setText("ASC input file");
		rightPanel.add(fileToConvPathTF, BorderLayout.NORTH);
		
		fileArea = new JDesktopPane();
		fileArea.setToolTipText("Drag files onto here");
		fileArea.setBackground(Color.LIGHT_GRAY);
		rightPanel.add(fileArea, BorderLayout.CENTER);

		JPanel buttonWrap = new JPanel();
		buttonWrap.setLayout(new GridLayout(3, 1, 0, 10));

		JPanel governmentLevelWrap = new JPanel();
		governmentLevel = new ButtonGroup();
		for(JRadioButton jrb : radioButtons){
			governmentLevelWrap.add(jrb);
			governmentLevel.add(jrb);
		}
		governmentLevelWrap.setPreferredSize(new Dimension(0, 23));
		buttonWrap.add(governmentLevelWrap);

		statusTF = new JTextField();
		statusTF.setForeground(Color.BLACK);
		statusTF.setText("Status");
		buttonWrap.add(statusTF);

		writeOutBTN = new JButton("Write!");
		buttonWrap.add(writeOutBTN);

		rightPanel.add(buttonWrap, BorderLayout.SOUTH);
		
		mainPanel.add(leftPanel);
		mainPanel.add(rightPanel);
	}
	
	private class ButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if(e.getSource() == writeOutBTN){
				if(written)
					readFile(ascFile);

				int govLvl = -1;
				if(governmentLevel.getSelection() == radioButtons[MUNICIPAL].getModel())
					govLvl = MUNICIPAL;
				else if(governmentLevel.getSelection() == radioButtons[PROVINCIAL].getModel())
					govLvl = PROVINCIAL;
				else if(governmentLevel.getSelection() == radioButtons[FEDERAL].getModel())
					govLvl = FEDERAL;


				if(Qnair.isEmpty()){
					statusTF.setText("Can't Write - Empty Qnair");
					return;
				}
				ArrayList<QuestionBase> checked = new ArrayList<>();
				for(Map.Entry entry : bannerDemoQuestions.entrySet()){
					if(((JCheckBox) entry.getKey()).isSelected())
						checked.add((QuestionBase)entry.getValue());
				}
				for(Map.Entry entry : bannerQuestions.entrySet()){
					if(((JCheckBox) entry.getKey()).isSelected())
						checked.add((QuestionBase)entry.getValue());
				}
				Writer.writeFile(ascFile, checked, govLvl);

				written = true;
				statusTF.setText("Conversion Complete");
				System.out.println("DONE");
			}
		}
	}

	private void readFile(File ascFile){
		Qnair.clearQuestions();

		bannerDemoQuestions.clear();
		bannerQuestions.clear();
		bannerWrap.removeAll();
		getRootPane().revalidate();

		fileToConvPathTF.setText(ascFile.getAbsolutePath());

		boolean success = Parser.parseASCFile(fileToConvPathTF.getText());
		if(!success){
			Logg.severe("Qnair Buffers are empty");
			statusTF.setText("Bad file, no questions loaded. Try converting to UTF-8?");
		}else
			statusTF.setText("Read Successfully");


		//populate banner pane
		for(DemoQuestion dq : Qnair.getDemoQuestions()){
			JCheckBox jcb = new JCheckBox(dq.getVariable());
			jcb.setSelected(true);
			bannerWrap.add(jcb);
			bannerDemoQuestions.put(jcb, dq);
		}
		for(Question q : Qnair.getQuestions()){
			JCheckBox jcb = new JCheckBox(q.getVariable());
			bannerWrap.add(jcb);
			bannerQuestions.put(jcb, q);
		}
		getRootPane().revalidate();
	}
}