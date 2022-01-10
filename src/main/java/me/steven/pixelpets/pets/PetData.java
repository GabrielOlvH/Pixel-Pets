package me.steven.pixelpets.pets;

import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.abilities.AbilityAction;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PetData {

    public static final int AGE_BABY = 0;
    public static final int AGE_CHILD = 1;
    public static final int AGE_ADULT = 2;

    private Identifier petId;
    private int variant;

    private String nickname;
    private int age;
    private Map<Identifier, Integer> abilities = new HashMap<>();
    @Nullable
    private Identifier selected;
    private int cooldown;
    private int totalCooldown;
    private int ticksUntilGrow;

    public PetData(Identifier id) {
        this.petId = id;
        setVariant(getPet().getRandomVariant().id());
        setNickname(I18n.translate(getPet().getTranslationKey(getVariant())));
        setAge(AGE_BABY);
        setTicksUntilGrow(1200);
    }

    public PetData(Identifier id, int variant) {
        this.petId = id;
        setVariant(variant);
        setNickname(I18n.translate(getPet().getTranslationKey(getVariant())));
        setAge(AGE_BABY);
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Map<Identifier, Integer> getAbilities() {
        return abilities;
    }

    public AbilityAction getCurrentAbilityAction(Identifier id) {
        return Abilities.REGISTRY.get(id).actions()[abilities.get(id)];
    }

    public void addAbility(Ability ability) {
        abilities.put(ability.id(), 0);
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

    public void setTotalCooldown(int totalCooldown) {
        this.totalCooldown = totalCooldown;
    }

    public int getTotalCooldown() {
        return totalCooldown;
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
        int color = getPet().getColor(variant);
        MutableText ageText = new LiteralText(" [").append(new TranslatableText(ageToString(age))).append(new LiteralText("]"));
        return new LiteralText(nickname).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))).append(ageText.formatted(Formatting.DARK_GRAY));
    }

    @Nullable
    public Ability selectNextAbility(ItemStack stack) {
        if (this.getAbilities().isEmpty()) return null;
        Identifier selected = this.getSelected();
        List<Identifier> identifiers = new ArrayList<>(getAbilities().keySet());
        int next = identifiers.indexOf(selected) + 1;
        if (next >= this.getAbilities().size()) {
            next = 0;
        }
        this.setSelected(identifiers.get(next));
        stack.setSubNbt("PetData", this.toTag());
        return Abilities.REGISTRY.get(this.getSelected());
    }


    @Nullable
    public AbilityAction getSelectedAbility() {
        if (this.getAbilities().isEmpty() || this.getSelected() == null) return null;
        return Abilities.REGISTRY.get(this.getSelected()).actions()[abilities.get(selected)];
    }

    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("PetId", petId.toString());
        tag.putString("Nickname", nickname);
        tag.putInt("Age", age);
        tag.putInt("TicksUntilGrow", ticksUntilGrow);
        NbtList abilitiesTag = new NbtList();
        abilities.forEach((ability, level) -> {
            NbtCompound abilityNbt = new NbtCompound();
            abilityNbt.putString("Id", ability.toString());
            abilityNbt.putInt("Level", level);
            abilitiesTag.add(abilityNbt);
        });
        tag.put("Abilities", abilitiesTag);
        if (selected != null)
            tag.putString("Selected", selected.toString());
        tag.putInt("Cooldown", cooldown);
        tag.putInt("TotalCooldown", totalCooldown);
        tag.putInt("Variant", variant);
        return tag;
    }

    public static PetData fromTag(ItemStack stack) {
        return fromTag(stack.getOrCreateSubNbt("PetData"));
    }

    public static PetData fromTag(NbtCompound tag) {
        PetData data = new PetData(new Identifier("pixelpets:pig"));
        if (!tag.contains("PetId"))
            return data;
        data.petId = new Identifier(tag.getString("PetId"));
        data.nickname = tag.getString("Nickname");
        data.age = tag.getInt("Age");
        data.ticksUntilGrow = tag.getInt("TicksUntilGrow");
        data.abilities = new HashMap<>();
        tag.getList("Abilities", 10).forEach((t) -> {
            NbtCompound abilityNbt = (NbtCompound) t;
            Identifier id = new Identifier(abilityNbt.getString("Id"));
            int level = abilityNbt.getInt("Level");
            data.abilities.put(id, level);
        });
        if (tag.contains("Selected"))
            data.selected = new Identifier(tag.getString("Selected"));
        data.cooldown = tag.getInt("Cooldown");
        data.totalCooldown = tag.getInt("TotalCooldown");
        data.variant = tag.getInt("Variant");
        return data;
    }
    
    public static String ageToString(int age) {
        return switch (age) {
            case AGE_BABY -> "item.pixelpets.pet.age.baby";
            case AGE_CHILD -> "item.pixelpets.pet.age.child";
            case AGE_ADULT -> "item.pixelpets.pet.age.adult";
            default -> "Unknown";
        };
    }
}
