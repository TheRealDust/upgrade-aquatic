package com.teamabnormals.upgrade_aquatic.common.block;

import com.teamabnormals.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import com.teamabnormals.upgrade_aquatic.core.registry.UABlocks;
import com.teamabnormals.upgrade_aquatic.core.registry.UABlocks.KelpType;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.KelpBlock;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class UAKelpBlock extends KelpBlock {
	private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.KELP);
	private final KelpType kelpType;

	public UAKelpBlock(KelpType kelpType, Properties props) {
		super(props);
		this.kelpType = kelpType;
	}

	@Override
	public Block getBodyBlock() {
		return switch (this.kelpType) {
			case TONGUE -> UABlocks.TONGUE_KELP_PLANT.get();
			case THORNY -> UABlocks.THORNY_KELP_PLANT.get();
			case OCHRE -> UABlocks.OCHRE_KELP_PLANT.get();
			case POLAR -> UABlocks.POLAR_KELP_PLANT.get();
		};
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		FILLER.fillItem(this.asItem(), group, items);
	}
}
