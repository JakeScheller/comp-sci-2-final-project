import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

// Created by Jake on 3/20/2017.

// An explicit trie, with a defined root node and some helper functions.
public class Trie {
    private Node root;

    // Initialize the trie with an empty root node.
    public Trie() {
        this.root = new Node('\0');
    }

    // Build and return a new trie filled with words from a plain text file.
    // The file must be a list of newline separated words, with no trailing
    // newline. If there's something wrong with the file, null is returned.
    public static Trie fromFile(String filename) {
        Trie trie = new Trie();
        try {
            Files.lines(Paths.get(filename)).forEach(w -> trie.addWord(w));
        }
        catch (IOException err) {
            System.out.println("OMG YOU DID SOMETHING WRONG");
            System.out.println(err);
            return null;
        }
        return trie;
    }

    public Node getRoot() {
        return this.root;
    }

    // Add the word to the trie.
    public void addWord(String word) {
        this.root.addWord(word);
    }

    // Return the node for the last character in the prefix,
    // or null if the prefix is not in the trie.
    public Node lookupPrefix(String prefix) {
        return this.root.lookupPrefix(prefix);
    }

    // Return the number of times that the word has been
    // added to the trie.
    public int lookupWord(String word) {
        Node word_node = lookupPrefix(word);
        if (word_node != null) {
            return word_node.getFreq();
        }
        else {
            return 0;
        }
    }

    // Return the string representation and frequency of all
    // the words in the trie with the provided prefix.
    public List<Pair<String, Integer>> autocomplete(String prefix) {
        return autocomplete(prefix, -1);
    }

    // Return the first num words in the trie that begin with the given
    // prefix. The words are ordered alphabetically by length. If num is
    // less than zero, returns all the words that begin with the given prefix.
    public List<Pair<String, Integer>> autocomplete(String prefix, int num) {
        List<Pair<String, Integer>> completions = new ArrayList<>();
        Node start = lookupPrefix(prefix);
        if (num == 0 || start == null) {
            return completions;
        }
        else {
            // An implementation of breadth-first search
            ArrayDeque<Node> nodes = new ArrayDeque<>();
            ArrayDeque<String> prfxs = new ArrayDeque<>();
            nodes.add(start);
            prfxs.add(prefix);
            while (nodes.size() > 0 && num != 0) {
                Node cur_node = nodes.remove();
                String cur_prfx = prfxs.remove();
                if (cur_node.isWord()) {
                    completions.add(new Pair<>(cur_prfx, cur_node.getFreq()));
                    num -= 1;
                }
                for (Node child : cur_node.getSortedChildNodes()) {
                    nodes.add(child);
                    prfxs.add(cur_prfx + child.getValue());
                }
            }
            return completions;
        }
    }
}