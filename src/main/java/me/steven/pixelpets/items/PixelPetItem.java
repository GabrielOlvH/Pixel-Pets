package me.steven.pixelpets.items;

import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.abilities.AbilityAction;
import me.steven.pixelpets.extensions.PixelPetsPlayerExtension;
import me.steven.pixelpets.pets.PetData;
import me.steven.pixelpets.utils.Utils;
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
            data.getAbilities().forEach((id, tier) -> {
                Ability ability = Abilities.REGISTRY.get(id);
                if (ability == null) return;
                MutableText text = new TranslatableText(ability.getTranslationKey());
                if (data.getSelected() != null && data.getSelected().equals(id))
                    text = new LiteralText(" [*] ").append(text).append(" ").append(Utils.toRomanNumeral(tier));
                else
                    text = new LiteralText(" [ ] ").append(text).append(" ").append(Utils.toRomanNumeral(tier));
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

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) return TypedActionResult.pass(stack);
        PetData data = PetData.fromTag(stack);
        if (user.isSneaking()) {
            Ability next = data.selectNextAbility(stack);
            //TODO use translatable text
            if (next != null)
                user.sendMessage(data.toText().shallowCopy().append(new LiteralText(" selected ability ").formatted(Formatting.WHITE)).append(new TranslatableText(next.getTranslationKey())), false);
        } else if (data.getCooldown() <= 0) {
            AbilityAction ability = data.getSelectedAbility();
            if (ability != null && ability.onInteract(data, world, user)) {
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
        PetData data = PetData.fromTag(stack);
        boolean update = tick(data, stack, world, entity);
        if (update) {
            stack.setSubNbt("PetData", data.toTag());
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
        return data.getCooldown() > 0 && Abilities.REGISTRY.containsKey(data.getSelected());
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        PetData data = PetData.fromTag(stack);
        return data.getPet().getColor(data.getVariant());
    }

    public static boolean tick(PetData data, @Nullable ItemStack stack, World world, Entity entity) {
        if (world.isClient()) return false;

        if (data.getAge() != PetData.AGE_ADULT) {
            data.setTicksUntilGrow(data.getTicksUntilGrow() - 1);
            if (data.getTicksUntilGrow() <= 0)
                grow(data);
            return true;
        }

        if (data.getCooldown() <= 0) {
            AbilityAction ability = data.getSelectedAbility();
            if (ability != null && stack != null) {
                if (entity instanceof PixelPetsPlayerExtension) {
                    ((PixelPetsPlayerExtension) entity).getTickingAbilities().computeIfAbsent(data.getPet(), (a) -> new HashMap<>()).put(ability, data);
                }
                if (ability.inventoryTick(data, world, (LivingEntity) entity)) {
                    data.setCooldown(ability.getCooldown());
                    data.setTotalCooldown(ability.getCooldown());
                    return true;
                }
            }
        } else {
            data.setCooldown(data.getCooldown() - 1);
            return true;
        }

        return false;
    }

    private static void grow(PetData data) {
        data.setAge(data.getAge() + 1);
        data.setTicksUntilGrow(1200);
        if (data.getAge() == PetData.AGE_CHILD) {
           Ability[] initials = data.getPet().getAbilities();
            if (initials.length > 0) {
                Ability initial = initials[ThreadLocalRandom.current().nextInt(initials.length - 1)];
                data.addAbility(initial);
                data.setSelected(initial.id());
            }
        }
    }
}
