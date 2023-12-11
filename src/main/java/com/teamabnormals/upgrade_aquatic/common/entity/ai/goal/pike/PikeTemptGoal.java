package com.teamabnormals.upgrade_aquatic.common.entity.ai.goal.pike;

import com.teamabnormals.upgrade_aquatic.common.entity.animal.Pike;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

public final class PikeTemptGoal extends Goal {
	private static final TargetingConditions CAN_FOLLOW = TargetingConditions.forNonCombat().range(10.0D).ignoreLineOfSight();
	private final Pike pike;
	private Player tempter;
	private int cooldown;

	public PikeTemptGoal(Pike pike) {
		this.pike = pike;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	public boolean canUse() {
		if (this.cooldown > 0) {
			this.cooldown--;
			return false;
		} else {
			this.tempter = this.pike.level.getNearestPlayer(CAN_FOLLOW, this.pike);
			if (this.tempter == null) {
				return false;
			} else {
				return this.isTemptedBy(this.tempter.getMainHandItem()) || this.isTemptedBy(this.tempter.getOffhandItem());
			}
		}
	}

	public void stop() {
		this.tempter = null;
		this.pike.getNavigation().stop();
		this.cooldown = 100;
	}

	public void tick() {
		this.pike.getLookControl().setLookAt(this.tempter, this.pike.getMaxHeadYRot() + 20.0F, this.pike.getMaxHeadXRot());
		if (this.pike.distanceToSqr(this.tempter) < 6.25D) {
			this.pike.getNavigation().stop();
		} else {
			this.pike.getNavigation().moveTo(this.tempter, 1.0F);
		}
	}

	private boolean isTemptedBy(ItemStack stack) {
		return stack.is(ItemTags.FISHES);
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}
}