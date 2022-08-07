import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Class for all occasions the user is prompted. It seemed cleaner to organize this way.
 */
public class Prompt {

    /**
     * Prompts the user to select year from available years.
     * @return int of selected year
     */
    public int selectYear(){
        /**
         * All years there has been a FRC event.
         */
        Integer[] yearArrayBad = {2022, 2021, 2020, 2019, 2018, 2017, 2016, 2015, 2014, 2013, 2012, 2011, 2010, 2009, 2008, 2007,
        2006, 2005, 2004, 2003, 2002, 2001, 2000, 1999, 1998, 1997, 1996, 1995, 1994, 1993, 1992};
        /**
         * All years where the scoring data (format?) is consistent with the code written. I am lazy.
         */
        Integer[] yearArray = {2022,2021,2020,2019,2018};

        return (int) JOptionPane.showInputDialog(null, "Pick year", "Pick year",
                JOptionPane.QUESTION_MESSAGE, null, yearArray, 2022);
    }

    /**
     * Prompts the user to select country from available countries.
     * @param countries ArrayList of all available countries
     * @return String of selected country
     */
    public String selectCountry(ArrayList<String> countries){
        //JOptionPane is poopoo and only accepts Arrays (I think) so I convert to Array.
        String[] countriesArray = countries.toArray(new String[0]);

        return (String) JOptionPane.showInputDialog(null, "Pick country", "Pick country",
                JOptionPane.QUESTION_MESSAGE, null, countriesArray, "USA");
    }

    /**
     * Prompts the user to select event from available events, including "irrelevant" events.
     * @param events A LinkedHashMap of all event keys (Key) and the event names (Value).
     * @return String of event key for selected event value
     */
    public String selectEvent(LinkedHashMap<String, String> events){
        ArrayList<String> names = new ArrayList<>();

        for (String key : events.keySet()) {
            names.add(events.get(key));
        }

        // JOptionPane is poopoo and only accepts Arrays (I think) so I started with an Arraylist so I could easily sort
        // and then I converted to Array
        Collections.sort(names);
        String[] eventNames = names.toArray(new String[0]);

        String selectedName = (String) JOptionPane.showInputDialog(null, "Pick Event", "Pick Event",
                JOptionPane.QUESTION_MESSAGE, null, eventNames, "USA");

        //selectedName gives the value for the key, so we find the relevant key from the value to return.
        for (String key : events.keySet()) {
            if(events.get(key).equals(selectedName))
                return key;
        }

        //For the compiler since the return is in the for loop and I didn't feel like solving it another way.
        return null;
    }

    /**
     * Prompts user to save spreadsheet to location, with default name of "matches_" + selectedEventKey + ".xlsx"
     * and location of Documents folder.
     * @param selectedEventKey String of selected event key
     * @return A File object containing the spreadsheet
     */
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

    /**
     * Prompts user to choose whether or not to scout another event.
     * @return boolean as to whether or not Yes was selected
     */
    public boolean reRun (){
        return JOptionPane.showConfirmDialog(null,"Do you want to scout another event?")
                == JOptionPane.YES_OPTION;
    }
}