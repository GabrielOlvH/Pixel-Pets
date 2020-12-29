package me.steven.pixelpets.items;

import me.shedaniel.cloth.api.durability.bar.DurabilityBarItem;
import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.abilities.AbilityRarity;
import me.steven.pixelpets.pets.Age;
import me.steven.pixelpets.pets.PixelPet;
import me.steven.pixelpets.player.PixelPetsPlayerExtension;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class PixelPetItem extends Item implements DurabilityBarItem {

    public PixelPetItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        PetData data = PetData.fromTag(stack.getOrCreateTag());
        if (data != null) {
            tooltip.add(LiteralText.EMPTY);
            if (data.getAbilities().isEmpty()) {
                tooltip.add(new TranslatableText("item.pixelpets.pet.noabilities", getName(stack)));
            } else {
                tooltip.add(new TranslatableText("item.pixelpets.pet.abilities").formatted(Formatting.AQUA));
                data.getAbilities().forEach((id) -> {
                    Ability ability = Abilities.REGISTRY.get(id);
                    if (ability == null) return;
                    MutableText text = new TranslatableText(ability.getTranslationKey());
                    if (data.getSelected() != null && data.getSelected().equals(id))
                        text = new LiteralText(" [*] ").append(text);
                    else
                        text = new LiteralText(" [ ] ").append(text);
                    tooltip.add(text);
                    if (Screen.hasShiftDown()) {
                        tooltip.add(new LiteralText("     ").append(new TranslatableText(ability.getTranslationKey() + ".description")).formatted(Formatting.ITALIC, Formatting.GRAY));
                    }
                });
            }
            if (data.getCooldown() > 0) {
                tooltip.add(LiteralText.EMPTY);
                tooltip.add(new TranslatableText("item.pixelpets.pet.cooldown", data.getCooldown()).formatted(Formatting.GRAY, Formatting.ITALIC));
            }
        }
    }

    @Override
    public Text getName(ItemStack stack) {
        PetData petData = PetData.fromTag(stack.getOrCreateTag());
        if (petData == null) return super.getName(stack);
        int color = petData.getPet().getCooldownDisplayColor();
        MutableText ageText = new LiteralText(" [").append(new TranslatableText("item.pixelpets.pet.age." + petData.getAge().toString().toLowerCase(Locale.ROOT))).append(new LiteralText("]"));
        return new LiteralText(petData.getNickname()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))).append(ageText.formatted(Formatting.DARK_GRAY));
    }

    @Nullable
    public Ability getSelected(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        PetData data = PetData.fromTag(tag);
        if (data == null || data.getAbilities().isEmpty() || data.getSelected() == null) return null;
        return Abilities.REGISTRY.get(data.getSelected());
    }

    @Nullable
    private Ability next(ItemStack stack) {
        PetData data = PetData.fromTag(stack.getOrCreateTag());
        if (data == null || data.getAbilities().isEmpty() || data.getSelected() == null) return null;
        Identifier selected = data.getSelected();
        int next = data.getAbilities().indexOf(selected) + 1;
        if (next >= data.getAbilities().size()) {
            next = 0;
        }
        data.setSelected(data.getAbilities().get(next));
        stack.putSubTag("PetData", data.toTag());
        return Abilities.REGISTRY.get(data.getSelected());
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) return TypedActionResult.pass(stack);
        PetData data = PetData.fromTag(stack.getOrCreateTag());
        if (data == null) return TypedActionResult.pass(stack);
        if (user.isSneaking()) {
            Ability next = next(stack);
            //TODO use translatable text
            if (next != null)
                user.sendMessage(getName(stack).shallowCopy().append(new LiteralText(" selected ability ").formatted(Formatting.WHITE)).append(new TranslatableText(next.getTranslationKey())), false);
        } else if (data.getCooldown() <= 0) {
            Ability ability = getSelected(stack);
            if (ability != null && ability.onInteract(stack, world, user)) {
                data.setCooldown(ability.getCooldown());
                stack.putSubTag("PetData", data.toTag());
            }
        }
        else return TypedActionResult.pass(stack);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient()) return;
        PetData data = PetData.fromTag(stack.getOrCreateTag());

        if (data == null) {
            data = new PetData();
            initialize(data);
            stack.putSubTag("PetData", data.toTag());
        } else if (data.getAge() != Age.ADULT) {
            data.setTicksUntilGrow(data.getTicksUntilGrow() - 1);
            if (data.getTicksUntilGrow() <= 0)
                grow(data);
            stack.putSubTag("PetData", data.toTag());
        }

        if (data.getAge() == Age.CHILD && world.random.nextDouble() > 0.97) {
            PetData finalData = data;
            Ability[] unusual = Arrays.stream(data.getPet().getAbilities()).filter(ability -> ability.getRarity() == AbilityRarity.UNUSUAL && !finalData.getAbilities().contains(ability.getId())).toArray(Ability[]::new);
            if (unusual.length > 0) {
                int toLearn = world.random.nextInt(unusual.length);
                data.addAbility(unusual[toLearn]);
                stack.putSubTag("PetData", data.toTag());
            }
        }

        if (data.getCooldown() <= 0) {
            Ability ability = getSelected(stack);
            if (ability != null) {
                if (entity instanceof PixelPetsPlayerExtension) {
                    ((PixelPetsPlayerExtension) entity).getInventoryPets().put(data.getPet(), data);
                }
                if (ability.inventoryTick(stack, world, (LivingEntity) entity)) {
                    data.setCooldown(ability.getCooldown());
                    stack.putSubTag("PetData", data.toTag());
                }
            }
        } else {
            data.setCooldown(data.getCooldown() - 1);
            stack.putSubTag("PetData", data.toTag());
        }
    }

    private void grow(PetData data) {
        data.setAge(Age.values()[data.getAge().ordinal() + 1]);
        data.setTicksUntilGrow(1200);
        if (data.getAge() == Age.CHILD) {
            Ability[] initials = Arrays.stream(data.getPet().getAbilities()).filter(ability -> ability.getRarity() == AbilityRarity.COMMON).toArray(Ability[]::new);
            if (initials.length > 0) {
                Ability initial = initials[ThreadLocalRandom.current().nextInt(initials.length)];
                data.addAbility(initial);
                data.setSelected(initial.getId());
            }
        }
    }

    public static void initialize(PetData data) {
        data.setNickname(I18n.translate(data.getPet().getTranslationKey()));
        data.setAge(Age.BABY);
        data.setTicksUntilGrow(1200);
    }

    @Override
    public double getDurabilityBarProgress(ItemStack itemStack) {
        PetData data = PetData.fromTag(itemStack.getOrCreateTag());
        return (double) data.getCooldown() / (double) Abilities.REGISTRY.get(data.getSelected()).getCooldown();
    }

    @Override
    public boolean hasDurabilityBar(ItemStack itemStack) {
        PetData data = PetData.fromTag(itemStack.getOrCreateTag());
        return data != null && data.getCooldown() > 0 &&  Abilities.REGISTRY.containsKey(data.getSelected());
    }

    @Override
    public int getDurabilityBarColor(ItemStack stack) {
        PetData data = PetData.fromTag(stack.getTag());
        return data.getPet().getCooldownDisplayColor();
    }
}
