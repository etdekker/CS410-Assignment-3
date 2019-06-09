package refactor;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

public class SimpleNotePad extends JFrame implements ActionListener{
    JMenuBar menu = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenu editMenu = new JMenu("Edit");
    JTextPane textPane = new JTextPane();
    JMenuItem newFile = new JMenuItem("New File");
    JMenuItem saveFile = new JMenuItem("Save File");
    JMenuItem printFile = new JMenuItem("Print File");
    JMenuItem copy = new JMenuItem("Copy");
    JMenuItem paste = new JMenuItem("Paste");
    
    //JMenus/JMenuItems for new features
    JMenuItem openFile = new JMenuItem("Open"); 
    JMenu recentMenu = new JMenu("Recent"); 
    JMenuItem simpleReplace = new JMenuItem("Replace");
    
    LinkedList<File> recentFiles = new LinkedList<File>(); //holds recent files in order
    
    public SimpleNotePad() {
        setTitle("A Simple Notepad Tool");
  
        fileMenu.add(openFile); //new feature
        fileMenu.add(newFile);
        fileMenu.add(saveFile);
        fileMenu.add(printFile);
        fileMenu.add(recentMenu); //new feature
        
        editMenu.add(simpleReplace); //new feature 
        editMenu.add(copy);
        editMenu.add(paste);
        
        //consolidated action listener and action command
        newFile.addActionListener(this::action_newFile); 
        saveFile.addActionListener(this::action_saveFile);       
        printFile.addActionListener(this::action_printFile);        
        copy.addActionListener(this::action_copy);        
        paste.addActionListener(this::action_paste);        
        openFile.addActionListener(this::action_openFile);        
        simpleReplace.addActionListener(this::action_simpleReplace);

        menu.add(fileMenu);
        menu.add(editMenu);
        
        setJMenuBar(menu);
        add(new JScrollPane(textPane));
        setPreferredSize(new Dimension(600,600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        pack();
    }
    
    public static void main(String[] args) {
        SimpleNotePad app = new SimpleNotePad();
    }
    
    //extracted methods for each action command
    @Override
    public void actionPerformed(ActionEvent e) {

    }
       
    //new feature: simple-replace method
    private void action_simpleReplace(ActionEvent e) {
        textPane.replaceSelection(JOptionPane.showInputDialog(new JFrame("Input"), "Replace or insert with:"));
    }
    
    //new feature: openRecent action method
    private void action_openRecent(ActionEvent e) {
    	open(new File(e.getActionCommand())); 
    }
    
    //open method used by action_openRecent and action_openFile
    private void open(File file) {
    	updateRecentFiles(file); //updates recent files
    	try {
			BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			textPane.read(input, "Opening...");    
		}    			
		catch (IOException ex) {
		    JOptionPane.showMessageDialog(null,
		            "Open error" + ex, "Opening error",
		            JOptionPane.ERROR_MESSAGE);
		}
    }
    
    //called by open() to update recent files list
    private void updateRecentFiles(File file) {
    	//removes file from List if it was opened recently to avoid duplicates
    	if(recentFiles.contains(file)) {
    		recentFiles.remove(file);
    	}
    	recentFiles.addFirst(file); //adds file being opened to top of List
    	
    	recentMenu.removeAll(); //clears recent menu 
    	
    	//repopulates recent menu using updated recent file order
    	for(File f: recentFiles) {
    		JMenuItem recent = new JMenuItem(f.getName());
        	recent.addActionListener(this::action_openRecent);
        	recent.setActionCommand(f.getAbsolutePath());
        	recentMenu.add(recent);
    	} 	
    }
    
    //new feature: opens file from system dialog
    private void action_openFile(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			open(fc.getSelectedFile());
		}
    }
    
    //copy method extracted from actionPerformed
	private void action_copy(ActionEvent e) {
		textPane.copy();
	}
	
	//paste method extracted from actionPerformed
	private void action_paste(ActionEvent e) {
		StyledDocument doc = textPane.getStyledDocument();
		Position position = doc.getEndPosition();
		System.out.println("offset"+position.getOffset());
		textPane.paste();
	}
	
	//printFile method extracted from actionPerformed
	private void action_printFile(ActionEvent e) {
		try{
		    PrinterJob pjob = PrinterJob.getPrinterJob();
		    pjob.setJobName("Sample Command Pattern");
		    pjob.setCopies(1);
		    pjob.setPrintable(new Printable() {
		        public int print(Graphics pg, PageFormat pf, int pageNum) {
		            if (pageNum>0)
		                return Printable.NO_SUCH_PAGE;
		            pg.drawString(textPane.getText(), 500, 500);
		            paint(pg);
		            return Printable.PAGE_EXISTS;
		        }
		    });

		    if (pjob.printDialog() == false)
		        return;
		    pjob.print();
		} catch (PrinterException pe) {
		    JOptionPane.showMessageDialog(null,
		            "Printer error" + pe, "Printing error",
		            JOptionPane.ERROR_MESSAGE);
		}
	}
	
	//saveFile method extracted from actionPerformed
	private void action_saveFile(ActionEvent e) {
		File fileToWrite = null;
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		    fileToWrite = fc.getSelectedFile();
		try {
		    PrintWriter out = new PrintWriter(new FileWriter(fileToWrite));
		    out.println(textPane.getText());
		    JOptionPane.showMessageDialog(null, "File is saved successfully...");
		    out.close();
		    updateRecentFiles(fileToWrite);
		}    
		//implement saving error
		catch (IOException ex) {
		    JOptionPane.showMessageDialog(null,
		            "Save error" + ex, "Saving error",
		            JOptionPane.ERROR_MESSAGE);
		}
	}
	
	//newFile method extracted from actionPerformed
	private void action_newFile(ActionEvent e) {
		textPane.setText("");
	}

}