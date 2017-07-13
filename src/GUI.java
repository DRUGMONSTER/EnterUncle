import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;


@SuppressWarnings("WeakerAccess")
public class GUI extends JFrame{
	private static final String VERSION = "0.40.2b";
	private static final long serialVersionUID = 1L;
	private static final int FRAME_WIDTH = 720;
	private static final int FRAME_HEIGHT = 480;
	private static File ascFile = null;
	private static boolean written = false;

	private JButton writeOutBTN;
	private JDesktopPane fileArea;
	private JTextField fileToConvPathTF;
	private JCheckBox startPosCB;
	private JTextField startPosTF;
	private JTextField statusTF;

	private JPanel bannerWrap;
	private ButtonGroup governmentLevel = new ButtonGroup();
	private Map<JCheckBox, Question> bannerQuestions = new LinkedHashMap<>();
	private Map<JCheckBox, DemoQuestion> bannerDemoQuestions = new LinkedHashMap<>();
	private JRadioButton[] radioButtons = {new JRadioButton("Municipal", true), new JRadioButton("Provincial"), new JRadioButton("Federal")};
	
	public GUI(){
		setTitle("Enter Uncle v" + VERSION);
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		governmentLevel.add(radioButtons[0]);
		governmentLevel.add(radioButtons[1]);
		governmentLevel.add(radioButtons[2]);
		
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
					try{
						readFile(ascFile);
					}catch(Exception ex){
						statusTF.setText("Couldn't read file");
					}
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
		buttonWrap.setLayout(new GridLayout(4, 1, 0, 10));

		JPanel governmentLevelWrap = new JPanel();
		governmentLevelWrap.add(radioButtons[0]);
		governmentLevelWrap.add(radioButtons[1]);
		governmentLevelWrap.add(radioButtons[2]);
		governmentLevelWrap.setPreferredSize(new Dimension(0, 24));
		buttonWrap.add(governmentLevelWrap);
		
		
		JPanel startPosWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		startPosCB = new JCheckBox("Start Position Override:");
		startPosCB.addActionListener(new CheckListener());
		startPosTF = new JTextField(String.valueOf(Parser.START_POS), 4);
		startPosTF.setHorizontalAlignment(SwingConstants.RIGHT);
		startPosWrap.add(startPosCB);
		startPosWrap.add(startPosTF);
		startPosWrap.setPreferredSize(new Dimension(0, 26));
		buttonWrap.add(startPosWrap);
		
		
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

				GovernmentLevel govLvl = GovernmentLevel.MUNICIPAL;
				if(governmentLevel.getSelection() == radioButtons[0].getModel())
					govLvl = GovernmentLevel.MUNICIPAL;
				else if(governmentLevel.getSelection() == radioButtons[1].getModel())
					govLvl = GovernmentLevel.PROVINCIAL;
				else if(governmentLevel.getSelection() == radioButtons[2].getModel())
					govLvl = GovernmentLevel.FEDERAL;


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
				Writer.writeFile(ascFile, checked, Qnair.getQuestions(), Qnair.getDemoQuestions(), govLvl, Qnair.getLocation());

				written = true;
				statusTF.setText("Conversion Complete");
				System.out.println("DONE");
			}
		}
	}
	
	private class CheckListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			System.out.println("hi");
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
			JCheckBox jcb = new JCheckBox(dq.variable);
			jcb.setSelected(true);
			bannerWrap.add(jcb);
			bannerDemoQuestions.put(jcb, dq);
		}
		for(Question q : Qnair.getQuestions()){
			JCheckBox jcb = new JCheckBox(q.variable);
			bannerWrap.add(jcb);
			bannerQuestions.put(jcb, q);
		}
		getRootPane().revalidate();
	}
}