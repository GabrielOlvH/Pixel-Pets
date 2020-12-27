package me.steven.pixelpets.items;

import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.pets.Age;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PetData {
    private String nickname;
    private Age age;
    private List<Identifier> abilities = new ArrayList<>();
    @Nullable
    private Identifier selected;
    private int cooldown;
    private int ticksUntilGrow;

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

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Nickname", nickname);
        tag.putInt("Age", age.ordinal());
        tag.putInt("TicksUntilGrow", ticksUntilGrow);
        ListTag abilitiesTag = new ListTag();
        abilities.forEach((ability) -> abilitiesTag.add(StringTag.of(ability.toString())));
        tag.put("Abilities", abilitiesTag);
        if (selected != null)
            tag.putString("Selected", selected.toString());
        tag.putInt("Cooldown", cooldown);
        return tag;
    }

    @Nullable
    public static PetData fromTag(CompoundTag parent) {
        PetData data = new PetData();
        if (!parent.contains("PetData")) return null;
        CompoundTag tag = parent.getCompound("PetData");
        data.nickname = tag.getString("Nickname");
        data.age = Age.values()[tag.getInt("Age")];
        data.ticksUntilGrow = tag.getInt("TicksUntilGrow");
        data.abilities = new ArrayList<>();
        tag.getList("Abilities", 8).forEach((t) -> data.abilities.add(new Identifier(t.asString())));
        if (tag.contains("Selected"))
            data.selected = new Identifier(tag.getString("Selected"));
        data.cooldown = tag.getInt("Cooldown");
        return data;
    }
}