package net.skillbase.fsbm.util;

public class Pair<T, K>
{
    private final T key;
    private final K value;
    
    public Pair(final T key, final K value) {
        this.key = key;
        this.value = value;
    }
    
    public K getValue() {
        return this.value;
    }
    
    public T getKey() {
        return this.key;
    }
}
