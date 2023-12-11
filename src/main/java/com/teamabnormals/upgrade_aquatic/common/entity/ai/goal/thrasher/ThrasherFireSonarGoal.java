package com.teamabnormals.upgrade_aquatic.common.entity.ai.goal.thrasher;

import com.teamabnormals.blueprint.core.util.EntityUtil;
import com.teamabnormals.blueprint.core.util.NetworkUtil;
import com.teamabnormals.upgrade_aquatic.common.entity.monster.Thrasher;
import com.teamabnormals.upgrade_aquatic.common.entity.projectile.SonarWave;
import com.teamabnormals.upgrade_aquatic.core.registry.UAEntityTypes;
import com.teamabnormals.upgrade_aquatic.core.registry.UAPlayableEndimations;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.BiPredicate;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class ThrasherFireSonarGoal extends Goal {
	public Thrasher thrasher;
	private int turnTicks;
	private int sonarTicks;
	private int sonarFireDuration;
	private float originalYaw, originalPitch;
	@Nullable
	private SonarPhase sonarPhase;

	public ThrasherFireSonarGoal(Thrasher thrasher) {
		this.thrasher = thrasher;
		this.setFlags(EnumSet.of(Flag.LOOK, Flag.TARGET));
	}

	@Override
	public boolean canUse() {
		return SonarPhase.shouldContinueExecutingPhase(null, this.thrasher, this.sonarTicks) && this.thrasher.getTicksSinceLastSonarFire() > 55 && this.thrasher.isNoEndimationPlaying();
	}

	@Override
	public boolean canContinueToUse() {
		boolean shouldContinue = SonarPhase.shouldContinueExecutingPhase(this.sonarPhase, this.thrasher, this.sonarTicks);
		return shouldContinue && (this.sonarPhase != SonarPhase.FIRE || (this.thrasher.getTarget() == null && (this.sonarTicks == 0 || this.sonarTicks == this.sonarFireDuration) || this.sonarTicks < this.sonarFireDuration));
	}

	@Override
	public void start() {
		this.sonarPhase = SonarPhase.TURN;
		this.sonarFireDuration = this.thrasher.getRandom().nextInt(3) * 5 + 30;
	}

	@Override
	public void stop() {
		this.sonarFireDuration = 0;
		this.sonarTicks = 0;
		this.turnTicks = 0;
		this.sonarPhase = null;
		this.thrasher.setPossibleDetectionPoint(null);
		((Thrasher.ThrasherLookController) this.thrasher.getLookControl()).setTurningForSonar(false);
	}

	@Override
	public void tick() {
		this.thrasher.getNavigation().stop();

		if (this.sonarPhase == SonarPhase.TURN) {
			this.turnTicks++;
			BlockPos pos = this.thrasher.getPossibleDetectionPoint();
			((Thrasher.ThrasherLookController) this.thrasher.getLookControl()).setTurningForSonar(true);
			this.thrasher.getLookControl().setLookAt(pos.getX(), pos.getY(), pos.getZ(), 90.0F, 90.0F);

			if (this.turnTicks > 50) {
				this.sonarPhase = SonarPhase.FIRE;
			}
		} else {
			if (this.sonarTicks == 0 && SonarPhase.shouldContinueExecutingPhase(SonarPhase.FIRE, this.thrasher, this.sonarTicks)) {
				this.originalYaw = this.thrasher.getYRot();
				this.originalPitch = this.thrasher.getXRot();
				NetworkUtil.setPlayingAnimation(this.thrasher, UAPlayableEndimations.THRASHER_SONAR_FIRE);
				this.thrasher.playSound(this.thrasher.getSonarFireSound(), 3.5F, 1.0F);
			}

			this.sonarTicks++;

			this.stablilizeDirection();

			if (this.sonarTicks % 5 == 0 && this.sonarTicks < this.sonarFireDuration) {
				SonarWave sonarWave = UAEntityTypes.SONAR_WAVE.get().create(this.thrasher.level);
				sonarWave.fireSonarWave(this.thrasher);
				this.thrasher.level.addFreshEntity(sonarWave);
			}
		}
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	private void stablilizeDirection() {
		this.thrasher.yRotO = this.originalYaw;
		this.thrasher.xRotO = this.originalPitch;
		this.thrasher.setYRot(this.originalYaw);
		this.thrasher.setXRot(this.originalPitch);
	}

	enum SonarPhase {
		TURN(null),
		FIRE((thrasher, sonarTicks) -> sonarTicks <= 15 || EntityUtil.rayTrace(thrasher, 32.0D, 1.0F).getType() == HitResult.Type.MISS);

		@Nullable
		private final BiPredicate<Thrasher, Integer> phaseCondition;

		SonarPhase(@Nullable BiPredicate<Thrasher, Integer> phaseCondition) {
			this.phaseCondition = phaseCondition;
		}

		public static boolean shouldContinueExecutingPhase(@Nullable SonarPhase phase, Thrasher thrasher, int sonarTicks) {
			boolean defaultCondition = !thrasher.isStunned() && thrasher.isInWater() && thrasher.getPassengers().isEmpty() && thrasher.getTarget() == null && thrasher.getPossibleDetectionPoint() != null && thrasher.level.getBlockState(thrasher.blockPosition().below()).getBlock() == Blocks.WATER;
			if (phase == null) {
				return defaultCondition;
			}
			return defaultCondition && (phase.phaseCondition == null || phase.phaseCondition.test(thrasher, sonarTicks));
		}
	}
}