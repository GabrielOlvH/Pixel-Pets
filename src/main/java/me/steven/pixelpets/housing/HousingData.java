package me.steven.pixelpets.housing;

import me.steven.pixelpets.pets.PetData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class HousingData {
    private final Identifier id;
    private final List<PetData> storedPets = new ArrayList<>();
    private final List<PetData> eggs = new ArrayList<>();

    public HousingData(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }

    public List<PetData> getStoredPets() {
        return storedPets;
    }

    public List<PetData> getEggs() {
        return eggs;
    }

    public Housing getHousing() {
        return Housing.REGISTRY.get(id);
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("HousingID", id.toString());
        NbtList petList = new NbtList();

        for (PetData pet : storedPets) {
            petList.add(pet.toTag());
        }

        nbt.put("Stored", petList);

        NbtList eggList = new NbtList();

        for (PetData pet : eggs) {
            eggList.add(pet.toTag());
        }

        nbt.put("StoredEggs", eggList);
        return nbt;
    }

    public static HousingData fromTag(NbtCompound nbt) {
        Identifier id;
        if (nbt.contains("HousingID")) id = new Identifier(nbt.getString("HousingID"));
        else id = new Identifier("pixelpets:aquarium");
        NbtList stored = nbt.getList("Stored", 10);
        HousingData housing = new HousingData(id);
        for (NbtElement element : stored) {
            NbtCompound petNbt = (NbtCompound) element;
            housing.storedPets.add(PetData.fromTag(petNbt));
        }

        NbtList eggs = nbt.getList("StoredEggs", 10);
        for (NbtElement element : eggs) {
            NbtCompound petNbt = (NbtCompound) element;
            housing.eggs.add(PetData.fromTag(petNbt));
        }
        return housing;
    }

    public static HousingData fromTag(ItemStack stack) {
        NbtCompound nbt = stack.getSubNbt("HousingData");
        if (nbt == null) return new HousingData(new Identifier("pixelpets:aquarium"));
        else return fromTag(nbt);
    }
}
