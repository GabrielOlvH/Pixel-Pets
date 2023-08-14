package me.steven.pixelpets.items;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AbilityRerollItem extends Item {
    public AbilityRerollItem() {
        super(new Settings().rarity(Rarity.EPIC));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.pixelpets.ability_reroll.tooltip1").formatted(Formatting.YELLOW, Formatting.ITALIC));
        tooltip.add(Text.translatable("item.pixelpets.ability_reroll.tooltip2").formatted(Formatting.YELLOW, Formatting.ITALIC));
    }
}
