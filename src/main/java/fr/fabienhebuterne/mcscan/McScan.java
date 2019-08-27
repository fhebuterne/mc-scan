package fr.fabienhebuterne.mcscan;

import fr.fabienhebuterne.mcscan.domain.ItemSpecial;
import fr.fabienhebuterne.mcscan.domain.Location;
import io.xol.enklume.MinecraftChunk;
import io.xol.enklume.MinecraftWorld;
import org.apache.commons.cli.*;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.ListTag;
import se.llbit.nbt.SpecificTag;
import se.llbit.nbt.Tag;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Map.Entry.comparingByValue;
import static org.fusesource.jansi.Ansi.ansi;

public class McScan {

    public static void main(String[] args) throws IOException {
        HashMap<ItemSpecial, Integer> itemSpecials = new HashMap<>();
        init();
        CommandLine cmd = initOptions(args);

        // TODO : Put this in other class
        if (cmd.getOptionValue("world") == null) {
            System.out.println("world not found");
            System.exit(1);
        }

        MinecraftWorld minecraftWorld = new MinecraftWorld(new File(cmd.getOptionValue("world")));
        analyzeWorld(itemSpecials, minecraftWorld);
        showResults(itemSpecials);
    }

    private static void init() {
        AnsiConsole.systemInstall();
        System.out.println(ansi().eraseLine().fgBrightGreen().a(
                        "  __  __  _____    _____  _____          _   _ \n" +
                        " |  \\/  |/ ____|  / ____|/ ____|   /\\   | \\ | |\n" +
                        " | \\  / | |      | (___ | |       /  \\  |  \\| |\n" +
                        " | |\\/| | |       \\___ \\| |      / /\\ \\ | . ` |\n" +
                        " | |  | | |____   ____) | |____ / ____ \\| |\\  |\n" +
                        " |_|  |_|\\_____| |_____/ \\_____/_/    \\_\\_| \\_|\n" +
                        "                                               \n"
        ).fgGreen().a(" By Fabien HEBUTERNE").newline().a(" Github : https://github.com/fhebuterne/mc-scan").reset().newline());
    }

    private static CommandLine initOptions(String[] args) {
        Options options = new Options();

        // TODO : Config YML file
        Option world = new Option("w", "world", true, "minecraft world path");
        world.setRequired(true);
        options.addOption(world);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        return cmd;
    }

    private static void showResults(HashMap<ItemSpecial, Integer> itemSpecials) {
        itemSpecials.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(comparingByValue()))
                .forEach(hashMap -> {
                    System.out.println("____________________");
                    System.out.println("ID : " + hashMap.getKey().getId());
                    System.out.println("Display Name : " + hashMap.getKey().getName());
                    System.out.println("Display Lore : " + hashMap.getKey().getLore());
                    hashMap.getKey().getLocations().forEach(location -> System.out.println("Location in Map : " + location));
                    hashMap.getKey().getUuidInventory().forEach(uuid -> System.out.println("Player have item : " + uuid));
                    System.out.println("Count : " + hashMap.getValue());
                });
    }

    private static void analyzeWorld(HashMap<ItemSpecial, Integer> itemSpecials, MinecraftWorld minecraftWorld) {
        analysePlayerData(itemSpecials, minecraftWorld);
        analyseRegion(itemSpecials, minecraftWorld);
    }

    private static void analyseRegion(HashMap<ItemSpecial, Integer> itemSpecials, MinecraftWorld minecraftWorld) {
        Long filesNumberRegionFolder = minecraftWorld.getRegionsSize();

        for (int counter = 0; counter < filesNumberRegionFolder; counter = counter + 75) {
            int counterLimit;

            if (counter + 75 < filesNumberRegionFolder) {
                counterLimit = counter + 75;
            } else {
                counterLimit = filesNumberRegionFolder.intValue();
            }

            minecraftWorld.getRegionsFrom(counter, counterLimit).forEach(minecraftRegion -> {
                for (int x = 0; x < 32; x++) {
                    for (int z = 0; z < 32; z++) {
                        MinecraftChunk chunk = minecraftRegion.getChunk(x, z);
                        ByteArrayInputStream data = chunk.getData();

                        if (data == null) {
                            continue;
                        }

                        DataInputStream dataInputStream = new DataInputStream(data);
                        SpecificTag specificTag = CompoundTag.read(dataInputStream);

                        treatmentTileEntities(itemSpecials, specificTag);
                    }
                }
                minecraftRegion.close();
            });

            System.out.println(ansi().fg(Ansi.Color.YELLOW).a(counterLimit + " on " + filesNumberRegionFolder + " regions analyzed...").reset());
        }
    }

    private static void analysePlayerData(HashMap<ItemSpecial, Integer> itemSpecials, MinecraftWorld minecraftWorld) {
        Long filesNumberPlayerData = minecraftWorld.getPlayerDataSize();

        for (int counter = 0; counter < filesNumberPlayerData; counter = counter + 10000) {

            int counterLimit;

            if (counter + 10000 < filesNumberPlayerData) {
                counterLimit = counter + 10000;
            } else {
                counterLimit = filesNumberPlayerData.intValue();
            }

            minecraftWorld.getPlayersDataFrom(counter, counterLimit).forEach(minecraftPlayerData -> {
                SpecificTag specificTag = CompoundTag.read(minecraftPlayerData.getData());
                parseItems(itemSpecials, (SpecificTag) specificTag.get(""), null, minecraftPlayerData.getUUID(), "Inventory");
            });

            System.out.println(ansi().fg(Ansi.Color.YELLOW).a(counterLimit + " on " + filesNumberPlayerData + " playersdata analyzed...").reset());
        }
    }

    private static void treatmentTileEntities(HashMap<ItemSpecial, Integer> itemSpecials, SpecificTag read) {
        Tag tileEntities = read.asCompound().get("").get("Level").get("TileEntities");

        Pattern patternContainers = Pattern.compile("^((minecraft:)?([Cc]hest))$|^((minecraft:)?([Hh]opper))$|^minecraft:(.*)shulker_box$");

        Stream.of(tileEntities.asList()).forEach(tileEntity -> {
            tileEntity.items.stream()
                    .filter(specificTag -> patternContainers.matcher(specificTag.get("id").stringValue()).find())
                    .forEach(specificTag -> {
                        int x = specificTag.get("x").intValue();
                        int y = specificTag.get("y").intValue();
                        int z = specificTag.get("z").intValue();

                        Location location = new Location(x, y, z);

                        parseItems(itemSpecials, specificTag, location, null, "Items");
                    });
        });
    }

    private static void parseItems(HashMap<ItemSpecial, Integer> itemSpecials, SpecificTag specificTag, Location location, String uuid, String baseTag) {
        specificTag.get(baseTag).asList().items.stream()
                .filter(item -> item.get("tag").get("display").asCompound() != null)
                .filter(item -> item.get("tag").get("display").get("Lore").asList().size() > 0
                        || item.get("tag").get("display").get("Name").asList() != null
                        || item.get("tag").get("BlockEntityTag").asCompound() != null)
                .forEach(item -> {
                    if (item.get("tag").get("BlockEntityTag").isCompoundTag()) {
                        parseItems(itemSpecials, (SpecificTag) item.get("tag").get("BlockEntityTag"), location, uuid, "Items");
                    }

                    String id = item.get("id").stringValue();
                    String name = item.get("tag").get("display").get("Name").stringValue();
                    ListTag listTag = item.get("tag").get("display").get("Lore").asList();

                    // We ignore shulker_box item that considered like special item because have lore
                    if (id.contains("shulker_box") && listTag.size() == 0) {
                        return;
                    }

                    if (!name.isEmpty() || listTag.size() > 0) {
                        ItemSpecial itemSpecialProcessing = new ItemSpecial(id, name, listTag);

                        if (!itemSpecials.containsKey(itemSpecialProcessing)) {
                            itemSpecialProcessing.addLocation(location);
                            itemSpecialProcessing.addUuidInventory(uuid);
                        }

                        // Add location / uuid player have item to existing object
                        if (location != null || uuid != null) {
                            itemSpecials.keySet()
                                    .stream()
                                    .filter(itemSpecial -> itemSpecial.equals(itemSpecialProcessing))
                                    .forEach(itemSpecial -> {
                                        itemSpecial.addLocation(location);
                                        itemSpecial.addUuidInventory(uuid);
                                    });
                        }

                        itemSpecials.computeIfPresent(itemSpecialProcessing, (itemSpecial, integer) -> integer + 1);
                        itemSpecials.putIfAbsent(itemSpecialProcessing, 1);
                    }
                });
    }

}
