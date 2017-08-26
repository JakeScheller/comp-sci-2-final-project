// Created by Jake on 3/21/2017.

public class Tester {
    public static void main(String[] args) {
        String dataset_path = String.join(" ", args);

        // Strip wrapping quotes
        if (dataset_path.length() > 1 && dataset_path.charAt(0) == dataset_path.charAt(dataset_path.length() - 1)) {
            if (dataset_path.charAt(0) == '"' || dataset_path.charAt(0) == '\'') {
                dataset_path = dataset_path.substring(1, dataset_path.length()-1);
            }
        }

        if (dataset_path.length() == 0) {
            new AutocompleteGUI();
        }
        else {
            new AutocompleteGUI(dataset_path);
        }
    }
}