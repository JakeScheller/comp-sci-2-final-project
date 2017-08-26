import java.util.*;

// Created by Jake on 4/18/2017.

// This class provides a minimal implementation of an autocompleter. Despite its
// methods, it is not incremental. It is included simply to show that the Trie
// class's autocomplete method works correctly.
public class StrictAutocompleter extends Autocompleter {
    private Trie trie;
    private String prefix;

    public StrictAutocompleter(Trie trie) {
        this.trie = trie;
        this.prefix = "";
    }

    public int getMaxError() {
        return 0;
    }

    public Trie getTrie() {
        return this.trie;
    }

    public void addChar(char input) {
        this.prefix += input;
    }

    public void delChar() {
        this.prefix = this.prefix.substring(0, this.prefix.length()-1);
    }

    // Returns a list of completions, with their frequencies included.
    public List<String> getWords(int num) {
        List<String> words = new ArrayList<>();
        for (Pair<String, Integer> entry : this.trie.autocomplete(this.prefix, num)) {
            String word = entry.getFirst();
            Integer freq = entry.getSecond();
            words.add("(" + freq.toString() + ") " + word);
        }
        return words;
    }
}
