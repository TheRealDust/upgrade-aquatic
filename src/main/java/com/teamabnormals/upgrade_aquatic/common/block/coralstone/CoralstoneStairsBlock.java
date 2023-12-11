package com.teamabnormals.upgrade_aquatic.common.block.coralstone;

import com.teamabnormals.upgrade_aquatic.core.registry.UABlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CoralWallFanBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.function.Supplier;
import java.util.Random;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

@SuppressWarnings("deprecation")
public class CoralstoneStairsBlock extends StairBlock {
	public static final BooleanProperty POWERED = BooleanProperty.create("powered");
	@Nullable
	private final Block[] growableCoralBlocks;

	public CoralstoneStairsBlock(Supplier<BlockState> state, Properties properties, @Nullable Block[] growableCoralBlocks) {
		super(state, properties);
		this.growableCoralBlocks = growableCoralBlocks;
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(HALF, Half.BOTTOM)
				.setValue(SHAPE, StairsShape.STRAIGHT)
				.setValue(WATERLOGGED, false)
				.setValue(POWERED, false)
		);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POWERED, FACING, HALF, SHAPE, WATERLOGGED);
	}

	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
		if (!worldIn.isAreaLoaded(pos, 3)) return;

		if (this.growableCoralBlocks == null && state.getBlock() != UABlocks.DEAD_CORALSTONE_STAIRS.get()) {
			CoralstoneBlock.tickConversion(UABlocks.CORALSTONE_STAIRS_CONVERSION_MAP, state, worldIn, pos, random);
		}

		if (this.growableCoralBlocks != null && random.nextFloat() < 0.24F && state.getValue(POWERED)) {
			Direction randDirection = this.growableCoralBlocks.length > 3 ? Direction.getRandom(random) : Direction.from3DDataValue(random.nextInt(5) + 1);
			BlockPos growPos = pos.relative(randDirection);
			FluidState fluidState = worldIn.getBlockState(growPos).getFluidState();
			BlockState coralState;

			if (randDirection.get3DDataValue() > 1) {
				coralState = growableCoralBlocks[2].defaultBlockState().setValue(CoralWallFanBlock.FACING, randDirection);
				if (coralState.canSurvive(worldIn, growPos) && this.isValidPosToGrow(worldIn, growPos, fluidState)) {
					worldIn.setBlock(growPos, coralState, 2);
				}
			} else if (randDirection.get3DDataValue() == 1) {
				coralState = random.nextBoolean() ? growableCoralBlocks[1].defaultBlockState() : growableCoralBlocks[0].defaultBlockState();
				if (coralState.canSurvive(worldIn, growPos) && this.isValidPosToGrow(worldIn, growPos, fluidState)) {
					worldIn.setBlock(growPos, coralState, 2);
				}
			} else {
				coralState = growableCoralBlocks[3].defaultBlockState();
				if (coralState.canSurvive(worldIn, growPos) && this.isValidPosToGrow(worldIn, growPos, fluidState)) {
					worldIn.setBlock(growPos, coralState, 2);
				}
			}
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		if (!worldIn.isClientSide) {
			boolean flag = state.getValue(POWERED);
			if (flag != worldIn.hasNeighborSignal(pos)) {
				worldIn.setBlock(pos, state.cycle(POWERED), 2);
			}
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		ItemStack stack = player.getItemInHand(hand);
		if (stack.getItem() == Items.SHEARS && state.getBlock() != UABlocks.CORALSTONE_STAIRS.get()) {
			BlockState newState = UABlocks.CORALSTONE_STAIRS.get().defaultBlockState()
					.setValue(FACING, state.getValue(FACING))
					.setValue(HALF, state.getValue(HALF))
					.setValue(SHAPE, state.getValue(SHAPE))
					.setValue(WATERLOGGED, state.getValue(WATERLOGGED)
					);
			world.playSound(null, pos, SoundEvents.MOOSHROOM_SHEAR, SoundSource.PLAYERS, 1.0F, 0.8F);
			stack.hurtAndBreak(1, player, (entity) -> entity.broadcastBreakEvent(hand));
			world.setBlock(pos, newState, 2);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.FAIL;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
	}

	private boolean isValidPosToGrow(Level world, BlockPos pos, FluidState fluidState) {
		return world.getBlockState(pos).getMaterial().isReplaceable() && fluidState.getAmount() >= 8 && fluidState.is(FluidTags.WATER);
	}
}
