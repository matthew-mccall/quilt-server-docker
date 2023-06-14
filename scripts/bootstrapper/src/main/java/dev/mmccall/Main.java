package dev.mmccall;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

public class Main {
    public static void main(String[] args) {
        Option minecraftVersionOpt = Option.builder("mcversion")
                .longOpt("minecraft-version")
                .desc("Minecraft version of mods to install (default: 1.20)")
                .hasArg()
                .argName("version")
                .build();

        Option modsToInstallOpt = Option.builder("m")
                .longOpt("mods")
                .desc("Mods to install")
                .hasArgs()
                .optionalArg(true)
                .valueSeparator(',')
                .argName("mods")
                .build();

        Option dataPacksToInstallOpt = Option.builder("d")
                .longOpt("datapacks")
                .desc("Datapacks to install")
                .hasArgs()
                .optionalArg(true)
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

        Option serverRootOpt = Option.builder("r")
                .longOpt("server-root")
                .desc("Server root directory (default: .)")
                .hasArg()
                .argName("directory")
                .build();

        Option printHelp = Option.builder("h")
                .longOpt("help")
                .desc("Print help")
                .build();

        Options options = new Options();

        options.addOption(minecraftVersionOpt);
        options.addOption(modsToInstallOpt);
        options.addOption(dataPacksToInstallOpt);
        options.addOption(dataParksWorldName);
        options.addOption(listSupportedDatapacks);
        options.addOption(serverRootOpt);
        options.addOption(printHelp);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        if (cmd.hasOption(printHelp)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("bootstrapper", options);
            return;
        }

        if (cmd.hasOption(listSupportedDatapacks)) {
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

        String minecraftVersion = cmd.getOptionValue(minecraftVersionOpt, "1.20");
        String serverRoot = cmd.getOptionValue(serverRootOpt, System.getProperty("user.dir"));

        if (cmd.hasOption(dataPacksToInstallOpt) && (cmd.getOptionValues(dataPacksToInstallOpt) != null)) {
            DatapacksManager datapacksManager = new DatapacksManager("/datapacks.json");
            String worldName = cmd.getOptionValue(dataParksWorldName, "world");
            String[] datapacks = cmd.getOptionValues(dataPacksToInstallOpt);

            HashSet<String> dataPacksToInstall = new HashSet<>(Arrays.asList(datapacks));
            HashSet<String> failedDatapacks = new HashSet<>();

            dataPacksToInstall.stream().parallel().forEach(datapack -> {
                if (!datapacksManager.getDatapacks().contains(datapack.toLowerCase())) {
                    System.err.println("Datapack " + datapack + " is not supported");
                    return;
                }

                if (!datapacksManager.getAvailableVersions(datapack).contains(minecraftVersion)) {
                    System.err.printf("We don't have a version of %s for Minecraft %s%n", datapack, minecraftVersion);
                    return;
                }

                try {
                    datapacksManager.installDatapack(datapack, minecraftVersion, worldName, serverRoot);
                } catch (IOException e) {
                    failedDatapacks.add(datapack);
                }
            });

            if (failedDatapacks.size() > 0) {
                System.err.println("Failed to install the following datapacks:");
                failedDatapacks.forEach(System.err::println);
            }
        }

        if (cmd.hasOption(modsToInstallOpt) && (cmd.getOptionValues(modsToInstallOpt) != null)) {
            String[] mods = cmd.getOptionValues(modsToInstallOpt);

            HashSet<String> modsToInstall = new HashSet<>(Arrays.asList(mods));
            HashSet<String> failedMods = new HashSet<>();

            modsToInstall.stream().parallel().forEach(mod -> {
                try {
                    if (!ModDownloader.downloadMod(mod.toLowerCase(), minecraftVersion, serverRoot)) {
                        failedMods.add(mod);
                    }
                } catch (IOException | InterruptedException e) {
                    failedMods.add(mod);
                }
            });

            if (failedMods.size() > 0) {
                System.err.println("Failed to install the following mods:");
                failedMods.forEach(System.err::println);
            }
        }
    }
}