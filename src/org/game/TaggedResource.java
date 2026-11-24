package org.game;

import java.util.Hashtable;
import java.util.UUID;

public class TaggedResource extends Resource {
    
    private Hashtable<UUID, Object> tags = new Hashtable<>();

    @SuppressWarnings("unchecked")
    public <T extends Object> T get(UUID key) {
        if(tags.containsKey(key)) {
            return (T)tags.get(key);
        }
        return null;
    }

    public void put(UUID key, Object value) {
        tags.put(key, value);
    }
}
