package com.jblur.acme_client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.ParameterException;

/**
 * A default provider that reads its default values from a property file on the file system.
 * 
 * @author Darian
 */
public class PropertyFileDefaultProvider implements IDefaultProvider {
    private Properties properties;

    public PropertyFileDefaultProvider(String fileName) {
        init(fileName);
    }

    private void init(String fileName) {
        File propertyFile = new File(fileName);
        if (propertyFile.isFile()) {
            try (FileReader reader = new FileReader(propertyFile)) {
                properties = new Properties();
                properties.load(reader);
            }
            catch (IOException ex) {
                throw new ParameterException("Could not open property file: " + fileName);
            }
        } else {
            throw new ParameterException("Could not find property file: " + fileName);
        }
    }
    
    public String getDefaultValueFor(String optionName) {
        int index = 0;
        while (index < optionName.length() && ! Character.isLetterOrDigit(optionName.charAt(index))) {
            index++;
        }
        String key = optionName.substring(index);
        return properties.getProperty(key);
    }
}
