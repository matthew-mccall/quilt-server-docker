package dev.mmccall;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

public class DatapacksManager {
    public DatapacksManager(String filename) {
        datapacks = new HashMap<>();

        StringBuilder datapacksJSON = new StringBuilder();

        try {
            // Get resource file
            File datapacksFile = new File(Objects.requireNonNull(Main.class.getResource(filename)).getFile());
            Scanner scanner = new Scanner(datapacksFile);

            while (scanner.hasNextLine()) {
                datapacksJSON.append(scanner.nextLine());
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        JSONObject object = new JSONObject(datapacksJSON.toString());
        JSONObject datapacksObject = object.getJSONObject("datapacks");

        for (String datapackKey : datapacksObject.keySet()) {

            String datapack = datapackKey.toLowerCase();
            datapacks.put(datapack, new HashMap<>());

            JSONObject datapackObject = datapacksObject.getJSONObject(datapackKey);
            for (String version : datapackObject.keySet()) {
                datapacks.get(datapack).put(version, datapackObject.getString(version));
            }
        }
    }

    public Set<String> getDatapacks() {
        return datapacks.keySet();
    }

    public Set<String> getAvailableVersions(String datapack) {
        return datapacks.get(datapack).keySet();
    }

    public String getDownloadURL(String datapack, String version) {
        return datapacks.get(datapack).get(version);
    }

    public void installDatapack(String datapack, String version, String worldName) throws IOException {
        // Create worldName/datapacks directory if it doesn't exist using Commons IO
        File datapacksDirectory = new File(worldName + "/datapacks");
        FileUtils.forceMkdir(datapacksDirectory);

        // Download zip into worldName/datapacks directory
        URL downloadURL = new URL(getDownloadURL(datapack, version));
        FileUtils.copyURLToFile(downloadURL, new File(datapacksDirectory + "/" + datapack + ".zip"));
    }

    private final HashMap<String, HashMap<String, String>> datapacks;
}
