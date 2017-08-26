import java.util.*;
import java.util.stream.Collectors;

// Created by Jake on 3/20/2017.

// A Node is an element of a trie. Since this is an autocomplete program,
// each node represents a character in one or more words. I didn't have
// the nodes store a reference to their parent because I figure I can
// incrementally build and cache the words as I'm traversing down the trie.
public class Node implements Comparable<Node> {
    private char val;
    private int freq;
    private Map<Character, Node> children;

    // Initialize the node with a character value. Any char may be used
    // except '\0', which is a special value that indicates the root node.
    public Node(char val) {
        this.val = val;
        this.freq = 0;
        this.children = new HashMap<>();
    }

    public char getValue() {
        return this.val;
    }

    public int getFreq() {
        return this.freq;
    }

    // True if this node is the last character of some word in the trie.
    public boolean isWord() {
        return (this.freq > 0);
    }

    // True if this node is the root of the trie. The character value of
    // the root node should be ignored. It is not part of any word.
    public boolean isRoot() {
        return (this.val == '\0');
    }

    // Return the child with the provided value, or null if it does not exist.
    public Node getChildNode(char val) {
        return this.children.get(val);
    }

    // The child nodes of this node. The keys of the children map are redundant
    // because each child node also stores its own character value.
    public Collection<Node> getChildNodes() {
        return this.children.values();
    }

    // Same as getChildNodes, but returns the nodes sorted alphabetically.
    public Collection<Node> getSortedChildNodes() {
        return this.children.values().stream().sorted().collect(Collectors.toCollection(ArrayList::new));
    }

    // Recursively add the word to the node's subtrie.
    public void addWord(String word) {
        if (word.length() == 0) {
            this.freq += 1;
        }
        else {
            char first = word.charAt(0);
            Node child = this.children.computeIfAbsent(first, k -> new Node(first));
            child.addWord(word.substring(1));
        }
    }

    // Recursively lookup the prefix and return the node corresponding to the
    // prefix's last character, or null if it does not exist.
    public Node lookupPrefix(String word) {
        if (word.length() == 0) {
            return this;
        }
        else {
            char first = word.charAt(0);
            Node child = this.children.get(first);
            if (child == null) {
                return null;
            }
            else {
                return child.lookupPrefix(word.substring(1));
            }
        }
    }

    // Comparison is based solely on value. Frequency is not factored in.
    public int compareTo(Node other) {
        return this.val - other.getValue();
    }
}