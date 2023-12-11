package com.teamabnormals.upgrade_aquatic.common.item;

import com.teamabnormals.blueprint.core.util.item.filling.TargetedItemCategoryFiller;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import net.minecraft.world.item.Item.Properties;

public class SquidBucketItem extends BucketItem {
	private static final TargetedItemCategoryFiller FILLER = new TargetedItemCategoryFiller(() -> Items.TROPICAL_FISH_BUCKET);

	public SquidBucketItem(Supplier<? extends Fluid> supplier, Properties builder) {
		super(supplier, builder);
	}

	@Override
	public void checkExtraContent(@Nullable Player player, Level level, ItemStack stack, BlockPos pos) {
		if (level instanceof ServerLevel) {
			this.placeEntity((ServerLevel) level, stack, pos);
			level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
		}
	}

	@Override
	protected void playEmptySound(@Nullable Player player, LevelAccessor world, BlockPos pos) {
		world.playSound(player, pos, SoundEvents.BUCKET_EMPTY_FISH, SoundSource.NEUTRAL, 1.0F, 1.0F);
	}

	protected void placeEntity(ServerLevel level, ItemStack stack, BlockPos pos) {
		Entity entity = EntityType.SQUID.spawn(level, stack, null, pos, MobSpawnType.BUCKET, true, false);
		if (entity instanceof Squid) {
			((Squid) entity).setPersistenceRequired();
		}
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		FILLER.fillItem(this, group, items);
	}
}