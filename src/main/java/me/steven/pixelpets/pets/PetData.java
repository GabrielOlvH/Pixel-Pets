package me.steven.pixelpets.pets;

import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.AbilityAction;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class PetData {
    public static final String PET_DATA_ID = "PetData";
    private Identifier petId;
    private int variant;
    private String nickname;
    private Identifier abilityId;
    private int cooldown;
    private int totalCooldown;

    public PetData(Identifier id) {
        this.petId = id;
        setVariant(getPet().getRandomVariant().id());
        setNickname(I18n.translate(getPet().getTranslationKey(getVariant())));
    }

    public PetData(Identifier id, int variant) {
        this.petId = id;
        setVariant(variant);
        setNickname(I18n.translate(getPet().getTranslationKey(getVariant())));
    }

    public Identifier getPetId() {
        return petId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Identifier getAbilityId() {
        return abilityId;
    }

    public void setAbilityId(Identifier abilityId) {
        this.abilityId = abilityId;
    }

    public AbilityAction getAbilityAction() {
        return Abilities.REGISTRY.get(abilityId).action();
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public void setTotalCooldown(int totalCooldown) {
        this.totalCooldown = totalCooldown;
    }

    public int getTotalCooldown() {
        return totalCooldown;
    }

    public int getVariant() {
        return variant;
    }

    public void setVariant(int variant) {
        this.variant = variant;
    }

    public PixelPet getPet() {
        if (petId == null) petId = new Identifier("pixelpets:pig");
        return PixelPets.REGISTRY.get(petId);
    }

    public Text toText() {
        int color = getPet().getColor(variant);
        return Text.literal(nickname).styled(s -> s.withColor(TextColor.fromRgb(color)));
    }

    public ItemStack update(ItemStack stack) {
        stack.getOrCreateNbt().put(PET_DATA_ID, toTag());
        return stack;
    }

    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("PetId", petId.toString());
        tag.putString("Nickname", nickname);
        if (abilityId != null)
            tag.putString("AbilityId", abilityId.toString());
        tag.putInt("Cooldown", cooldown);
        tag.putInt("TotalCooldown", totalCooldown);
        tag.putInt("Variant", variant);
        return tag;
    }

    public static PetData fromTag(ItemStack stack) {
        return fromTag(stack.getOrCreateSubNbt(PET_DATA_ID));
    }

    public static PetData fromTag(NbtCompound tag) {
        PetData data = new PetData(new Identifier("pixelpets:pig"));
        if (!tag.contains("PetId"))
            return data;
        data.petId = new Identifier(tag.getString("PetId"));
        data.nickname = tag.getString("Nickname");
        if (tag.contains("AbilityId"))
            data.abilityId = new Identifier(tag.getString("AbilityId"));
        data.cooldown = tag.getInt("Cooldown");
        data.totalCooldown = tag.getInt("TotalCooldown");
        data.variant = tag.getInt("Variant");
        return data;
    }

    @Nullable
    public static PetData createFromGroupId(Identifier eggGroupId, Random random) {
        Identifier[] pets = PixelPets.REGISTRY.values().stream()
                .filter(pet -> pet.getEggGroupId().equals(eggGroupId))
                .map(PixelPet::getId)
                .toArray(Identifier[]::new);
        if (pets.length == 0) {
            return null;
        }
        Identifier pet = pets[random.nextInt(pets.length)];
        return new PetData(pet);
    }
}
