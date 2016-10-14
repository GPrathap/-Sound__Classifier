package org.sound.classifier;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PropertyList {
    static Map properties;
    static Map audioFeaturesBuffer;
    PropertyList(){
        properties = new HashMap();
    }
    PropertyList(int capacity){
        audioFeaturesBuffer = new ConcurrentHashMap();
    }

    public void addProperties(String key, String value){
        properties.put(key,value);
    }

    public void addProperties(Object key, Object value){
        audioFeaturesBuffer.put(key,value);
    }

    public void addProperties(String key, Object value){
        audioFeaturesBuffer.put(key,value);
    }

    public Object getProperties(String key){
        if(properties.containsKey(key))
            return properties.get(key);
        return null;
    }

    public Object getProperties(Object key){
        if(audioFeaturesBuffer.containsKey(key))
            return audioFeaturesBuffer.get(key);
        return null;
    }

    public Map getAudioFeaturesBuffer(){
        return audioFeaturesBuffer;
    }
}
