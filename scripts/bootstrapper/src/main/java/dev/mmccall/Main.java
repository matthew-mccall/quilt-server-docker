package dev.mmccall;

import org.apache.commons.cli.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Option minecraftVersionOpt = Option.builder("mcversion")
                .longOpt("minecraft-version")
                .desc("Minecraft version of mods to install (default: 1.20)")
                .hasArg()
                .argName("version")
                .build();

        Option modsToInstall = Option.builder("m")
                .longOpt("mods")
                .desc("Mods to install")
                .hasArgs()
                .valueSeparator(',')
                .argName("mods")
                .build();

        Option dataPacksToInstall = Option.builder("d")
                .longOpt("datapacks")
                .desc("Datapacks to install")
                .hasArgs()
                .valueSeparator(',')
                .argName("datapacks")
                .build();

        Option dataParksWorldName = Option.builder("w")
                .longOpt("world")
                .desc("World name to install datapacks to (default: world)")
                .hasArg()
                .argName("world")
                .build();

        Option listSupportedDatapacks = Option.builder("listdatapacks")
                .longOpt("list-datapacks")
                .desc("List supported datapacks")
                .build();

        Option printHelp = Option.builder("h")
                .longOpt("help")
                .desc("Print help")
                .build();

        Options options = new Options();

        options.addOption(minecraftVersionOpt);
        options.addOption(modsToInstall);
        options.addOption(dataPacksToInstall);
        options.addOption(listSupportedDatapacks);
        options.addOption(printHelp);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            return;
        }

        if (cmd.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("bootstrapper", options);
            return;
        }

        if (cmd.hasOption("listdatapacks")) {
            System.out.println("Supported datapacks:");
            DatapacksManager datapacksManager = new DatapacksManager("/datapacks.json");

            for (String datapack : datapacksManager.getDatapacks()) {
                System.out.print(datapack + " [");

                Iterator<String> versions = datapacksManager.getAvailableVersions(datapack).iterator();
                while (versions.hasNext()) {
                    System.out.print(versions.next());
                    if (versions.hasNext()) {
                        System.out.print(", ");
                    }
                }

                System.out.println("]");
            }

            return;
        }

        if (cmd.hasOption(dataPacksToInstall)) {
            DatapacksManager datapacksManager = new DatapacksManager("/datapacks.json");
            String worldName = cmd.getOptionValue("world", "world");
            String[] datapacks = cmd.getOptionValues("datapacks");

            for (String datapack : datapacks) {
                if (!datapacksManager.getDatapacks().contains(datapack)) {
                    System.err.println("Datapack " + datapack + " is not supported");
                    return;
                }

                String minecraftVersion = cmd.getOptionValue("mcversion", "1.20");

                if (!datapacksManager.getAvailableVersions(datapack).contains(minecraftVersion)) {
                    System.err.printf("We don't have a version of %s for Minecraft %s%n", datapack, minecraftVersion);
                    return;
                }

                System.out.printf("Installing %s for Minecraft %s to %s%n", datapack, minecraftVersion, worldName);

                try {
                    datapacksManager.installDatapack(datapack, minecraftVersion, worldName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}