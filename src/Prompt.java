import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class Prompt {

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
}