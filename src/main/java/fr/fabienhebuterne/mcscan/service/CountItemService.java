package fr.fabienhebuterne.mcscan.service;

import fr.fabienhebuterne.mcscan.domain.ItemSpecial;
import fr.fabienhebuterne.mcscan.domain.Location;
import se.llbit.nbt.ListTag;
import se.llbit.nbt.SpecificTag;
import se.llbit.nbt.Tag;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CountItemService {

    public void treatmentTileEntities(HashMap<ItemSpecial, Integer> itemSpecials, SpecificTag read) {
        Tag tileEntities = read.asCompound().get("").get("Level").get("TileEntities");

        Pattern patternContainers = Pattern.compile("^((minecraft:)?([Cc]hest))$|^((minecraft:)?([Tt]rapped_chest))$|^((minecraft:)?([Ff]urnace))$|^((minecraft:)?([Dd]ropper))$|^((minecraft:)?([Dd]ispenser))$|^((minecraft:)?([Tt]rap))$|^((minecraft:)?([Hh]opper))$|^((minecraft:)?([Cc]hest_minecart))$|^((minecraft:)?([Ff]urnace_minecart))$|^((minecraft:)?([Hh]opper_minecart))$|^minecraft:(.*)shulker_box$");

        Stream.of(tileEntities.asList()).forEach(tileEntity -> {
            tileEntity.items.stream()
                    .filter(specificTag -> patternContainers.matcher(specificTag.get("id").stringValue().toLowerCase()).find())
                    .forEach(specificTag -> {
                        int x = specificTag.get("x").intValue();
                        int y = specificTag.get("y").intValue();
                        int z = specificTag.get("z").intValue();

                        Location location = new Location(x, y, z);

                        parseItems(itemSpecials, specificTag, location, null, "Items");
                    });
        });
    }

    public void treatmentEntities(HashMap<ItemSpecial, Integer> itemSpecials, SpecificTag read) {
        Tag entities = read.asCompound().get("").get("Level").get("Entities");

        Pattern patternContainers = Pattern.compile("^itemframe$");

        Stream.of(entities.asList()).forEach(entity -> {
            entity.items.stream()
                    .filter(specificTag -> patternContainers.matcher(specificTag.get("id").stringValue().toLowerCase()).find())
                    .forEach(specificTag -> {
                        int x = specificTag.get("TileX").intValue();
                        int y = specificTag.get("TileY").intValue();
                        int z = specificTag.get("TileZ").intValue();

                        Location location = new Location(x, y, z);

                        parseItems(itemSpecials, specificTag, location, null, "Item");
                    });
        });
    }

    public void parseItems(HashMap<ItemSpecial, Integer> itemSpecials, SpecificTag specificTag, Location location, String uuid, String baseTag) {
        List<SpecificTag> items;
        if (baseTag.equals("Item")) {
            items = Collections.singletonList(specificTag.get("Item").asCompound());
        } else {
            items = specificTag.get(baseTag).asList().items;
        }

        items.stream()
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
                    ListTag enchantment = item.get("tag").get("ench").asList();
                    ListTag listTag = item.get("tag").get("display").get("Lore").asList();

                    // We ignore shulker_box item that considered like special item because have lore
                    if (id.contains("shulker_box") && listTag.size() == 0) {
                        return;
                    }

                    if (!name.isEmpty() || listTag.size() > 0) {
                        ItemSpecial itemSpecialProcessing = new ItemSpecial(id, name, listTag, enchantment);

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
