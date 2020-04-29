package fr.fabienhebuterne.mcscan.service;

import fr.fabienhebuterne.mcscan.domain.ItemSpecial;
import io.xol.enklume.MinecraftChunk;
import io.xol.enklume.MinecraftWorld;
import org.fusesource.jansi.Ansi;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.SpecificTag;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.HashMap;

import static org.fusesource.jansi.Ansi.ansi;

public class AnalyseWorldService {

    private CountItemService countItemService;

    public AnalyseWorldService() {
        this.countItemService = new CountItemService();
    }

    public void analyzeWorld(HashMap<ItemSpecial, Integer> itemSpecials, MinecraftWorld minecraftWorld) {
        analysePlayerData(itemSpecials, minecraftWorld);
        analyseRegion(itemSpecials, minecraftWorld);
    }

    public void analyseRegion(HashMap<ItemSpecial, Integer> itemSpecials, MinecraftWorld minecraftWorld) {
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

                        countItemService.treatmentTileEntities(itemSpecials, specificTag);
                    }
                }
                minecraftRegion.close();
            });

            System.out.println(ansi().fg(Ansi.Color.YELLOW).a(counterLimit + " on " + filesNumberRegionFolder + " regions analyzed...").reset());
        }
    }

    public void analysePlayerData(HashMap<ItemSpecial, Integer> itemSpecials, MinecraftWorld minecraftWorld) {
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
                countItemService.parseItems(itemSpecials, (SpecificTag) specificTag.get(""), null, minecraftPlayerData.getUUID(), "Inventory");
            });

            System.out.println(ansi().fg(Ansi.Color.YELLOW).a(counterLimit + " on " + filesNumberPlayerData + " playersdata analyzed...").reset());
        }
    }

}
