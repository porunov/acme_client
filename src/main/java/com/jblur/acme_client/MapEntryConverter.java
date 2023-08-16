package com.jblur.acme_client;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.IStringConverter;

/**
 * A converter to obtain a pseudo map of elements.
 * 
 * @see <a href="https://github.com/cbeust/jcommander/issues/218">https://github.com/cbeust/jcommander/issues/218</a>
 * @author Darian
 */
public class MapEntryConverter implements IStringConverter<List<Map.Entry<String, String>>> {
    
    @Override
    public List<Map.Entry<String, String>> convert(final String value) {
        
        List<Map.Entry<String, String>> result = new ArrayList<Map.Entry<String, String>>();
        
        String[] items = value.split("[\\s]*[,;]+[\\s]*");
        
        for (String item : items) {
            String[] pair = item.split("[\\s]*[=:]+[\\s]*", 2);
            
            String key = pair[0];
            String val = pair.length > 1 ? pair[1] : null;
            
            result.add(new AbstractMap.SimpleEntry<String, String>(key, val));
        }
        
        return result;
    }
}
