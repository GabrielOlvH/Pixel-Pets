package me.steven.pixelpets.items;

import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.abilities.AbilityRarity;
import me.steven.pixelpets.extensions.PixelPetsPlayerExtension;
import me.steven.pixelpets.pets.Age;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PixelPetItem extends Item {

    public PixelPetItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        PetData data = PetData.fromTag(stack);

        tooltip.add(LiteralText.EMPTY);
        if (data.getAbilities().isEmpty()) {
            tooltip.add(new TranslatableText("item.pixelpets.pet.noabilities", data.toText()));
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

    @Override
    public Text getName(ItemStack stack) {
        PetData petData = PetData.fromTag(stack);
        return petData.toText();
    }

    @Nullable
    public Ability getSelected(ItemStack stack) {
        PetData data = PetData.fromTag(stack);
        if (data.getAbilities().isEmpty() || data.getSelected() == null) return null;
        return Abilities.REGISTRY.get(data.getSelected());
    }

    @Nullable
    private Ability next(ItemStack stack) {
        PetData data = PetData.fromTag(stack);
        if (data.getAbilities().isEmpty() || data.getSelected() == null) return null;
        Identifier selected = data.getSelected();
        int next = data.getAbilities().indexOf(selected) + 1;
        if (next >= data.getAbilities().size()) {
            next = 0;
        }
        data.setSelected(data.getAbilities().get(next));
        stack.setSubNbt("PetData", data.toTag());
        return Abilities.REGISTRY.get(data.getSelected());
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) return TypedActionResult.pass(stack);
        PetData data = PetData.fromTag(stack);
        if (user.isSneaking()) {
            Ability next = next(stack);
            //TODO use translatable text
            if (next != null)
                user.sendMessage(data.toText().shallowCopy().append(new LiteralText(" selected ability ").formatted(Formatting.WHITE)).append(new TranslatableText(next.getTranslationKey())), false);
        } else if (data.getCooldown() <= 0) {
            Ability ability = getSelected(stack);
            if (ability != null && ability.onInteract(stack, world, user)) {
                data.setCooldown(ability.getCooldown());
                data.setTotalCooldown(ability.getCooldown());
                stack.setSubNbt("PetData", data.toTag());
            }
        }
        else return TypedActionResult.pass(stack);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient()) return;
        PetData data = PetData.fromTag(stack);

       if (data.getAge() != Age.ADULT) {
            data.setTicksUntilGrow(data.getTicksUntilGrow() - 1);
            if (data.getTicksUntilGrow() <= 0)
                grow(data);
            stack.setSubNbt("PetData", data.toTag());
        }

        if (data.getAge() == Age.CHILD && world.random.nextDouble() > 0.97) {
            Ability[] unusual = Arrays.stream(data.getPet().getAbilities()).filter(ability -> ability.getRarity() == AbilityRarity.UNUSUAL && !data.getAbilities().contains(ability.getId())).toArray(Ability[]::new);
            if (unusual.length > 0) {
                int toLearn = world.random.nextInt(unusual.length);
                data.addAbility(unusual[toLearn]);
                stack.setSubNbt("PetData", data.toTag());
            }
        }

        if (data.getCooldown() <= 0) {
            Ability ability = getSelected(stack);
            if (ability != null) {
                if (entity instanceof PixelPetsPlayerExtension) {
                    ((PixelPetsPlayerExtension) entity).getTickingAbilities().computeIfAbsent(data.getPet(), (a) -> new HashMap<>()).put(ability, data);
                }
                if (ability.inventoryTick(stack, world, (LivingEntity) entity)) {
                    data.setCooldown(ability.getCooldown());
                    data.setTotalCooldown(ability.getCooldown());
                    stack.setSubNbt("PetData", data.toTag());
                }
            }
        } else {
            data.setCooldown(data.getCooldown() - 1);
            stack.setSubNbt("PetData", data.toTag());
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

    @Override
    public int getItemBarStep(ItemStack itemStack) {
        PetData data = PetData.fromTag(itemStack);
        return (int)((data.getTotalCooldown() - data.getCooldown()) * 13.0f / data.getTotalCooldown());
    }

    @Override
    public boolean isItemBarVisible(ItemStack itemStack) {
        PetData data = PetData.fromTag(itemStack);
        return data.getCooldown() > 0 &&  Abilities.REGISTRY.containsKey(data.getSelected());
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        PetData data = PetData.fromTag(stack);
        return data.getPet().getColor(data.getVariant());
    }
}
