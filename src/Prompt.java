import javax.swing.*;
import java.util.ArrayList;

public class Prompt {

    public String selectCountry(ArrayList<String> countries){
        String[] countriesArray = countries.toArray(new String[0]);
        return (String) JOptionPane.showInputDialog(null, "Pick country", "Pick country",
                JOptionPane.QUESTION_MESSAGE, null, countriesArray, "USA");
    }
}