package dev.mmccall;

import org.apache.commons.io.FileUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ModDownloader {
    static boolean downloadMod(String modName, String minecraftVersion) throws IOException, InterruptedException {

        URI uri;

        try {
            uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("api.modrinth.com")
                    .setPath(String.format("/v2/project/%s/version", modName))
                    .setParameter("loaders", "[\"fabric\",\"quilt\"]")
                    .setParameter("game_versions", String.format("[\"%s\"]", minecraftVersion))
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> res;

        res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            return false;
        }

        JSONArray versions = new JSONArray(res.body());
        JSONObject firstVersion = versions.getJSONObject(0);
        JSONArray files = firstVersion.getJSONArray("files");

        // Get primary file
        JSONObject primaryFile = null;

        for (int i = 0; i < files.length(); i++) {
            JSONObject file = files.getJSONObject(i);
            if (file.getBoolean("primary")) {
                primaryFile = file;
                break;
            }
        }

        if (primaryFile == null) {
            primaryFile = files.getJSONObject(0);
        }

        File modsFolder = new File("mods");
        FileUtils.forceMkdir(modsFolder);

        File modFile = new File(modsFolder, primaryFile.getString("filename"));
        FileUtils.copyURLToFile(new URL(primaryFile.getString("url")), modFile);


        return true;
    }
}
