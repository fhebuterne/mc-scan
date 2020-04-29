package fr.fabienhebuterne.mcscan;

import fr.fabienhebuterne.mcscan.domain.ItemSpecial;
import fr.fabienhebuterne.mcscan.service.AnalyseWorldService;
import io.xol.enklume.MinecraftWorld;
import org.apache.commons.cli.*;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

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
        new AnalyseWorldService().analyzeWorld(itemSpecials, minecraftWorld);
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
                    if (!hashMap.getKey().getEnchantment().isEmpty()) {
                        System.out.println("Display Enchantment : ");
                        hashMap.getKey().getEnchantment().forEach(specificTag -> {
                            System.out.println("  - " + specificTag);
                        });
                    }
                    hashMap.getKey().getLocations().forEach(location -> System.out.println("Location in Map : " + location));
                    hashMap.getKey().getUuidInventory().forEach(uuid -> System.out.println("Player have item : " + uuid));
                    System.out.println("Count : " + hashMap.getValue());
                });
    }

}
