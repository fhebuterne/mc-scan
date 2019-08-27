package fr.fabienhebuterne.mcscan.domain;

import se.llbit.nbt.ListTag;

import java.util.HashSet;
import java.util.Objects;

public class ItemSpecial {

    private String id;
    private String name;
    private ListTag lore;
    private HashSet<Location> locations;
    private HashSet<String> uuidInventory;

    public ItemSpecial(String id, String name, ListTag lore) {
        this.locations = new HashSet<>();
        this.uuidInventory = new HashSet<>();
        this.id = id;
        this.name = name;
        this.lore = lore;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ListTag getLore() {
        return lore;
    }

    public void setLore(ListTag lore) {
        this.lore = lore;
    }

    public HashSet<Location> getLocations() {
        return locations;
    }

    public void addLocation(Location l) {
        if (l != null) {
            locations.add(l);
        }
    }

    public HashSet<String> getUuidInventory() {
        return uuidInventory;
    }

    public void addUuidInventory(String uuidInventory) {
        if (uuidInventory != null) {
            this.uuidInventory.add(uuidInventory);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemSpecial that = (ItemSpecial) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getLore(), that.getLore());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getLore());
    }

    @Override
    public String toString() {
        return "ItemSpecial{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", lore=" + lore +
                ", location=" + locations +
                '}';
    }
}
