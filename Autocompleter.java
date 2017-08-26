import java.util.*;

// Created by Jake on 3/22/2017.

// This class supports incremental autocomplete over a trie. See FuzzyAutocompleter
// for a description of this. StrictAutocompleter only exists for compatibility with
// the rubric. When an Autocompleter is built with a maximum error of 0, a
// StrictAutocompleter is returned. Otherwise, a FuzzyAutocompleter is returned.
public abstract class Autocompleter {
    public static Autocompleter build(Trie trie, int max_error) {
        if (max_error < 1) {
            return new StrictAutocompleter(trie);
        }
        else {
            return new FuzzyAutocompleter(trie, max_error);
        }
    }

    public abstract Trie getTrie();

    public abstract int getMaxError();

    public abstract void addChar(char input);

    public abstract void delChar();

    public abstract List<String> getWords(int num);
}