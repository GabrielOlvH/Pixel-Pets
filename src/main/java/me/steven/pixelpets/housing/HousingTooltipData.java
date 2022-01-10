package me.steven.pixelpets.housing;

import net.minecraft.client.item.TooltipData;

public record HousingTooltipData(HousingData housingData) implements TooltipData {
}
