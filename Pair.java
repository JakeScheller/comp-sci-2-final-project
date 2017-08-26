// Created by Jake on 4/20/2017.

// Only used in the autocomplete method in Trie.
// I have no idea if there's a better way to return
// a sorted list of key, value pairs.
public class Pair<Type1, Type2> {
    private Type1 first;
    private Type2 second;

    Pair(Type1 first, Type2 second) {
        this.first = first;
        this.second = second;
    }

    public Type1 getFirst() {
        return this.first;
    }

    public Type2 getSecond() {
        return this.second;
    }
}
