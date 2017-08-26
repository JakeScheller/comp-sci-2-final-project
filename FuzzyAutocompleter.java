import java.util.*;
import java.util.stream.Collectors;

// Created by Jake on 4/18/2017.

// Given a trie, incrementally performs autocomplete for a given input prefix. This is
// accomplished by building a frontier of nodes that represent prefixes with an edit
// distance less than or equal to some maximum from the input prefix, and using the previously
// built frontier to build a new frontier every time the user enters a new character. The
// algorithm is a slightly modified version of the algorithm described in section 3 of
// the paper included in this project's folder. I omitted the steps relating to nodes
// that represent character "insertions," because I take care of those in the getWords method.
public class FuzzyAutocompleter extends Autocompleter {
    private Trie trie;
    private int max_error;
    private Frontier frontier;
    private ArrayDeque<Frontier> past_frontiers;

    // Initialize autocompleter with a trie and a max error, which is the maximum allowed
    // edit distance between the input prefix and any node in the frontier. The class starts
    // out with an empty frontier, to which the root node is immediately added. This program
    // omits the step from the paper where you add some sublevels of the root node to the
    // frontier. Past frontiers is a cache of the frontier for each prefix of the input prefix.
    public FuzzyAutocompleter(Trie trie, int max_error) {
        this.trie = trie;
        this.max_error = max_error;
        this.frontier = new Frontier();
        this.past_frontiers = new ArrayDeque<>();

        // The input prefix is initially the empty string, which the root node matches.
        this.frontier.addItem(new FrontierItem(trie.getRoot(), Action.MATCH));
    }

    public int getMaxError() {
        return this.max_error;
    }

    public Trie getTrie() {
        return this.trie;
    }

    // Add a character to the input prefix, building a new frontier based on nodes in the old one.
    public void addChar(char input) {
        Frontier old_frontier = this.frontier;
        // Cache the old frontier
        this.past_frontiers.push(old_frontier);
        this.frontier = new Frontier();

        for (FrontierItem item : old_frontier.getItems()) {
            Node node = item.getNode();
            int cost = item.getCost();

            if (cost + Action.DELETE.getCost() <= this.max_error) {
                // Since we added a character, if a node is in the old frontier, it
                // now corresponds to deleting a character from the new input prefix.
                this.frontier.addItem(item.makeNext(node, Action.DELETE));
            }

            for (Node child : node.getChildNodes()) {
                if (child.getValue() == input) {
                    if (cost + Action.MATCH.getCost() <= this.max_error) {
                        // The child node matches the added char. This program omits the step from
                        // the paper where you add some sublevels of the child node to the frontier,
                        // corresponding to inserting characters after the added char. Storing insertions
                        // is irrelevant since *any* node under this node is a possible word completion.
                        this.frontier.addItem(item.makeNext(child, Action.MATCH));
                    }
                }
                else {
                    if (cost + Action.SUBSTITUTE.getCost() <= this.max_error) {
                        // The child node does not match the added char, so it corresponds
                        // to substituting the added char with a different character.
                        this.frontier.addItem(item.makeNext(child, Action.SUBSTITUTE));
                    }
                }
            }
        }
    }

    // Delete a character from the input prefix. Since every past frontier is cached, this is simply
    // achieved by popping the most recent frontier and using it as the new current frontier, with
    // no further calculation necessary.
    public void delChar() {
        this.frontier = this.past_frontiers.pop();
    }

    // Get the 'num' most likely autocompetions for the input prefix, along with their frequencies.
    // The way I rank completions sucks, but it gets the job done. Unfortunately, I ran out of time
    // to implement frequency-based ordering of completions.
    public ArrayList<String> getWords(int num) {
        ArrayList<String> words = new ArrayList<>(num);

        HashSet<Node> seen_words = new HashSet<>();
        for (FrontierItem item : this.frontier.getSortedItems()) {
            ArrayDeque<Node> nodes = new ArrayDeque<>();
            ArrayDeque<String> prfxs = new ArrayDeque<>();
            nodes.add(item.getNode());
            prfxs.add(item.getPrefix());

            while (words.size() < num && nodes.size() > 0) {
                Node node = nodes.remove();
                String prfx = prfxs.remove();

                if (node.isWord() && !seen_words.contains(node)) {
                    words.add("(" + Integer.toString(node.getFreq()) + ") " + prfx);
                    seen_words.add(node);
                }

                for (Node child : node.getChildNodes()) {
                    nodes.add(child);
                    prfxs.add(prfx + child.getValue());
                }
            }
        }
        return words;
    }
}

// A Frontier stores nodes the the autocompleter is considering as possible prefixes for completions.
class Frontier {
    private Map<Node, FrontierItem> frontier;

    public Frontier() {
        this.frontier = new HashMap<>();
    }

    // Add the item to the frontier, but only if there is no item in the frontier with the same node,
    // or if there is, the new item has a lower cost than the item currently in the frontier.
    public void addItem(FrontierItem item) {
        Node node = item.getNode();
        FrontierItem old_item = this.frontier.get(node);
        if (old_item == null || item.getCost() < old_item.getCost()) {
            this.frontier.put(node, item);
        }
    }

    public Collection<FrontierItem> getItems() {
        return this.frontier.values();
    }

    // Same as getItems, but returns sorts them first.
    public Collection<FrontierItem> getSortedItems() {
        return getItems().stream().sorted().collect(Collectors.toCollection(ArrayList::new));
    }
}

// A FrontierItem essentially represents a node, but also contains metadata, such as the node's cost,
// the prefix that the node represents, and the series of edit actions that led to this node.
class FrontierItem implements Comparable<FrontierItem> {
    private Node node;
    private int cost;
    private String actions;
    private String prefix;

    // Build a brand new item with the given node and action.
    public FrontierItem(Node node, Action action) {
        this.node = node;
        this.cost = action.getCost();
        this.actions = "" + action.getToken();
        this.prefix = "";
        if (!node.isRoot()) {
            this.prefix += node.getValue();
        }
    }

    // Build a new item, based on the data in an old item. This is used for
    // incrementally building prefixes and such without having to store the
    // prefixes for every node above this one.
    public FrontierItem(Node node, Action action, FrontierItem old_item) {
        this.node = node;
        this.cost = old_item.getCost() + action.getCost();
        this.actions = old_item.getActions() + action.getToken();
        this.prefix = old_item.getPrefix();
        if (action != Action.DELETE) {
            this.prefix += node.getValue();
        }
    }

    // See the second constructor
    public FrontierItem makeNext(Node node, Action action) {
        return new FrontierItem(node, action, this);
    }

    public Node getNode() {
        return this.node;
    }

    public int getCost() {
        return this.cost;
    }

    public String getActions() {
        return this.actions;
    }

    public String getPrefix() {
        return this.prefix;
    }

    // Items are sorted by edit action (nodes that match the input prefix exactly are sorted first, then
    // deletions, then substitutions). Then ties are broken alphabetically by the node's prefix.
    public int compareTo(FrontierItem other) {
        int compval = this.actions.compareTo(other.getActions());
        if (compval == 0) {
            compval = this.prefix.compareTo(other.getPrefix());
        }
        return compval;
    }
}

// Edit actions and their associated costs. It's pretty cool that enums are classes.
enum Action {
    MATCH (0, 'a'),
    DELETE (1, 'd'),
    SUBSTITUTE (1, 's');

    private final int cost;
    private final char token;

    Action(int cost, char token) {
        this.cost = cost;
        this.token = token;
    }

    int getCost() {
        return this.cost;
    }

    // Token is a single char that represents the action.
    char getToken() {
        return this.token;
    }
}