package de.militaermiltz.tdv.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author Alexander Ley
 * @version 1.0
 *
 * This class can read the server.properties.
 */
public class ServerPropertiesManager {

    private final HashMap<String, String> properties = new HashMap<>();

    public ServerPropertiesManager() throws IOException {
        final Path propertiesFiles = Paths.get("server.properties");
        final List<String> lines = Files.readAllLines(propertiesFiles);

        for (int i = 2; i < lines.size(); i++){
            final List<String> keyValue = Arrays.stream(lines.get(i).split("=")).toList();

            final String key = keyValue.get(0);
            final String value = (keyValue.size() == 1) ? null : keyValue.get(1);

            properties.put(key, value);
        }
    }

    public String getProperty(String key){
        return properties.get(key);
    }
}
