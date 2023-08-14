package me.steven.pixelpets.items;

import me.steven.pixelpets.PixelPetsMod;
import me.steven.pixelpets.abilities.Abilities;
import me.steven.pixelpets.abilities.Ability;
import me.steven.pixelpets.abilities.AbilityAction;
import me.steven.pixelpets.abilities.AbilitySource;
import me.steven.pixelpets.extensions.PixelPetsPlayerExtension;
import me.steven.pixelpets.pets.PetData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PixelPetItem extends Item {

    public PixelPetItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        PetData data = PetData.fromTag(stack);
        if (data.getAbilityId() != null) {
            Ability ability = Abilities.REGISTRY.get(data.getAbilityId());
            String key = "item.pixelpets.pet.ability";
            if (data.hasRerolledAbility()) key += ".rerolled";
            tooltip.add(Text.translatable(key, Text.translatable(ability.getTranslationKey().formatted(Formatting.WHITE)).formatted(Formatting.DARK_PURPLE)));
            if (Screen.hasShiftDown()) {
                tooltip.add(Text.translatable(ability.getTranslationKey() + ".usage").formatted(Formatting.GRAY));
                if (data.getCooldown() > 0) {
                    tooltip.add(Text.empty());
                    tooltip.add(Text.translatable("item.pixelpets.pet.cooldown", data.getCooldown()).formatted(Formatting.GRAY, Formatting.ITALIC));
                }
            }
            else {
                tooltip.add(Text.translatable(ability.getTranslationKey() + ".description").formatted(Formatting.YELLOW, Formatting.ITALIC));
                tooltip.add(Text.translatable("item.pixelpets.pet_item.tooltip.shift").formatted(Formatting.GRAY));
            }
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
        if (data.getCooldown() <= 0) {
            AbilityAction ability = data.getAbilityAction();
            if (ability != null && ability.onInteract(data, world, user)) {
                data.setCooldown(ability.getCooldown());
                data.setTotalCooldown(ability.getCooldown());
                data.update(stack);
            }
        } else return TypedActionResult.pass(stack);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        PetData data = PetData.fromTag(stack);
        boolean update = tick(data, stack, world, entity);
        if (update) {
            data.update(stack);
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
        return data.getCooldown() > 0 && Abilities.REGISTRY.containsKey(data.getAbilityId());
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        PetData data = PetData.fromTag(stack);
        return data.getPet().getColor(data.getVariant());
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        PetData data = PetData.fromTag(stack);
        if (!data.hasRerolledAbility() && otherStack.isOf(PixelPetsMod.ABILITY_REROLL_ITEM)) {

            if (!player.getWorld().isClient) {
                data.setRerolledAbility(true);
                data.setAbilityId(null);
                data.update(stack);
                if (!player.isCreative()) otherStack.decrement(1);
                player.sendMessage(Text.literal(data.getNickname()).styled(s -> s.withColor(data.getPet().getColor(data.getVariant()))).append(Text.literal(" has learned a new ability!").formatted(Formatting.WHITE)));
            }
            return true;
        }
        return false;
    }

    public static boolean tick(PetData data, @Nullable ItemStack stack, World world, Entity entity) {
        if (world.isClient()) return false;
        if (data.getAbilityId() == null) {
            List<Identifier> abilities = new ArrayList<>();

            for (Ability ability : data.getPet().getAbilities()) {
                if (ability.source() == AbilitySource.NATURAL || data.hasRerolledAbility()) abilities.add(ability.id());
            }

            Identifier ability = abilities.get(world.random.nextInt(abilities.size()));
            data.setAbilityId(ability);
            return true;
        }

        if (data.getCooldown() <= 0) {
            AbilityAction ability = data.getAbilityAction();
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
}
