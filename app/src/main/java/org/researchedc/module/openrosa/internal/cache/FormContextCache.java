package org.researchedc.module.openrosa.internal.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class FormContextCache {

    private final ConcurrentHashMap<String, Map<String, String>> cache = new ConcurrentHashMap<>();

    public void store(String ecid, Map<String, String> context) {
        cache.put(ecid, Map.copyOf(context));
    }

    public Map<String, String> get(String ecid) {
        return cache.get(ecid);
    }

    public void remove(String ecid) {
        cache.remove(ecid);
    }

    public int size() {
        return cache.size();
    }
}
