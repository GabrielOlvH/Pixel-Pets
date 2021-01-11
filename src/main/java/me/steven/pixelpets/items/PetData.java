package me.steven.pixelpets.items;

import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.pets.Age;
import me.steven.pixelpets.pets.PixelPet;
import me.steven.pixelpets.pets.PixelPets;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class PetData {
    private Identifier petId;
    private String nickname;
    private Age age;
    private List<Identifier> abilities = new ArrayList<>();
    @Nullable
    private Identifier selected;
    private int cooldown;
    private int ticksUntilGrow;
    private int variant;

    public PetData(Identifier id) {
        this.petId = id;
        List<PixelPet.Variant> variants = getPet().getVariants();
        setVariant(variants.get(ThreadLocalRandom.current().nextInt(variants.size())).getId());
        setNickname(I18n.translate(getPet().getTranslationKey(getVariant())));
        setAge(Age.BABY);
        setTicksUntilGrow(1200);
    }

    public PetData(Identifier id, int variant) {
        this.petId = id;
        setVariant(variant);
        setNickname(I18n.translate(getPet().getTranslationKey(getVariant())));
        setAge(Age.BABY);
        setTicksUntilGrow(1200);
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

    public Age getAge() {
        return age;
    }

    public void setAge(Age age) {
        this.age = age;
    }

    public List<Identifier> getAbilities() {
        return abilities;
    }

    public void addAbility(Ability ability) {
        abilities.add(ability.getId());
    }

    @Nullable
    public Identifier getSelected() {
        return selected;
    }

    public void setSelected(Identifier selected) {
        this.selected = selected;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getTicksUntilGrow() {
        return ticksUntilGrow;
    }

    public void setTicksUntilGrow(int ticksUntilGrow) {
        this.ticksUntilGrow = ticksUntilGrow;
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
        int color = getPet().getCooldownDisplayColor();
        MutableText ageText = new LiteralText(" [").append(new TranslatableText("item.pixelpets.pet.age." + getAge().toString().toLowerCase(Locale.ROOT))).append(new LiteralText("]"));
        return new LiteralText(nickname).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))).append(ageText.formatted(Formatting.DARK_GRAY));
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("PetId", petId.toString());
        tag.putString("Nickname", nickname);
        tag.putInt("Age", age.ordinal());
        tag.putInt("TicksUntilGrow", ticksUntilGrow);
        ListTag abilitiesTag = new ListTag();
        abilities.forEach((ability) -> abilitiesTag.add(StringTag.of(ability.toString())));
        tag.put("Abilities", abilitiesTag);
        if (selected != null)
            tag.putString("Selected", selected.toString());
        tag.putInt("Cooldown", cooldown);
        tag.putInt("Variant", variant);
        return tag;
    }

    public static PetData fromTag(ItemStack stack) {
        return fromTag(stack.getOrCreateSubTag("PetData"));
    }

    public static PetData fromTag(CompoundTag tag) {
        PetData data = new PetData(new Identifier("pixelpets:pig"));
        if (!tag.contains("PetId"))
            return data;
        data.petId = new Identifier(tag.getString("PetId"));
        data.nickname = tag.getString("Nickname");
        data.age = Age.values()[tag.getInt("Age")];
        data.ticksUntilGrow = tag.getInt("TicksUntilGrow");
        data.abilities = new ArrayList<>();
        tag.getList("Abilities", 8).forEach((t) -> data.abilities.add(new Identifier(t.asString())));
        if (tag.contains("Selected"))
            data.selected = new Identifier(tag.getString("Selected"));
        data.cooldown = tag.getInt("Cooldown");
        data.variant = tag.getInt("Variant");
        return data;
    }
}
