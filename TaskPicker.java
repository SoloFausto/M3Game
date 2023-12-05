import java.awt.event.*;
import javax.swing.*;
import java.io.*;


public class TaskPicker  extends JFrame implements ActionListener  {
    

    // JTextField
    static JTextField t;
 
    // JFrame
    static JFrame f;
 
    // JButton
    static JButton b;
 
    // label to display text
    static JLabel l;
    
    private String textFieldInput;
    // default constructor
    TaskPicker(){
        textFieldInput = null;
                // create a new frame to store text field and button
        f = new JFrame("Windows XP Task Manager");
 
        // create a label to display text
        l = new JLabel("Enter the name of a window that you want to stop. (Wait about 5 seconds after clicking the button)");
 
        // create a new button
        b = new JButton("End Task");
        // create a object of the text class
        b.addActionListener(this);
 
        // create a object of JTextField with 45 columns
        t = new JTextField(45);
 
        // create a panel to add buttons and textfield
        JPanel p = new JPanel();
 
        // add buttons and textfield to panel
        p.add(t);
        p.add(b);
        p.add(l);
 
        // add panel to frame
        f.add(p);
 
        // set the size of frame
        f.setSize(500, 100);
 
        f.setVisible(true); 
    }
    // if the button is pressed
        @Override
    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("End Task")) {
            // set the text of the label to the text of the field
            String tempTaskName = t.getText();
            ProcessHandle taskSelected = getWindowProcessHandler(tempTaskName);
            if (taskSelected == null){
                l.setText("Window not found!");
            }
            else{
                l.setText("Window found! You can close the Task Manager now");
                textFieldInput = tempTaskName;
            }
        }
    }

    public String getTextFieldInput(){
        return this.textFieldInput;
    }

    public static ProcessHandle getWindowProcessHandler(String windowTitle) {
        if(windowTitle==null){return null;}
        ProcessHandle programProcess;
        Integer processPid = -1;
        ProcessBuilder processBuilder = new ProcessBuilder("tasklist", "/v", "/fo", "csv");

        try {
            Process taskListProcess = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(taskListProcess.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(windowTitle)) {
                    String[] parts = line.split(",");

                    processPid = Integer.parseInt(parts[1].replaceAll("\"", "").trim());
                }
            }

            taskListProcess.waitFor(); // Wait for the process to finish

        } catch (IOException | InterruptedException | NumberFormatException e) {
            e.printStackTrace();
        }
        if (processPid != -1 && ProcessHandle.of(processPid).isPresent()) {
            programProcess = ProcessHandle.of(processPid).get();
            return programProcess;
        }
        return null;
    }



}
