package com.teamabnormals.upgrade_aquatic.common.block;

import com.teamabnormals.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CoralFanBlock;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class UACoralFanBlock extends CoralFanBlock {
	private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.HORN_CORAL_FAN);

	public UACoralFanBlock(Block deadState, Properties props) {
		super(deadState, props);
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		FILLER.fillItem(this.asItem(), group, items);
	}
}