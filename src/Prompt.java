import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class Prompt {

    public int selectYear(){
        /*Integer[] yearArray = {2022, 2021, 2020, 2019, 2018, 2017, 2016, 2015, 2014, 2013, 2012, 2011, 2010, 2009, 2008, 2007,
        2006, 2005, 2004, 2003, 2002, 2001, 2000, 1999, 1998, 1997, 1996, 1995, 1994, 1993, 1992};*/
        Integer[] yearArray = {2022,2021,2020,2019,2018};

        return (int) JOptionPane.showInputDialog(null, "Pick year", "Pick year",
                JOptionPane.QUESTION_MESSAGE, null, yearArray, 2022);
    }

    public String selectCountry(ArrayList<String> countries){
        String[] countriesArray = countries.toArray(new String[0]);

        return (String) JOptionPane.showInputDialog(null, "Pick country", "Pick country",
                JOptionPane.QUESTION_MESSAGE, null, countriesArray, "USA");
    }

    public String selectEvent(LinkedHashMap<String, String> events){
        ArrayList<String> names = new ArrayList<String>();

        for (String key : events.keySet()) {
            names.add(events.get(key));
        }

        Collections.sort(names);
        String[] eventNames = names.toArray(new String[0]);

        String selectedName = (String) JOptionPane.showInputDialog(null, "Pick Event", "Pick Event",
                JOptionPane.QUESTION_MESSAGE, null, eventNames, "USA");

        for (String key : events.keySet()) {
            if(events.get(key) == selectedName)
                return key;
        }

        return null;
    }

    public File compileFile(String selectedEventKey){
        JFrame parentFrame = new JFrame();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save File");

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("spreadsheet", "xlsx"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("spreadsheet", "xlsx"));
        fileChooser.setSelectedFile(new File("matches_" + selectedEventKey + ".xlsx"));

        if (fileChooser.showSaveDialog(parentFrame) != JFileChooser.APPROVE_OPTION) {
            System.err.println("You fool");
            System.exit(-10);
        }

        return fileChooser.getSelectedFile();
    }
}