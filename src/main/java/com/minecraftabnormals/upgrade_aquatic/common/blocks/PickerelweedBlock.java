package com.minecraftabnormals.upgrade_aquatic.common.blocks;

import com.minecraftabnormals.upgrade_aquatic.common.entities.pike.PikeEntity;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public class PickerelweedBlock extends DirectionalBlock implements IBucketPickupHandler, ILiquidContainer {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final VoxelShape[] SHAPES = new VoxelShape[] {
		Block.box(0.0D, 5.0D, 0.0D, 16.0D, 16.0D, 16.0D),
		Block.box(0.0D, 0.0D, 0.0D, 16.0D, 11.0D, 16.0D),
		Block.box(0.0D, 0.0D, 16.0D, 16.0D, 16.0D, 5.0D),
		Block.box(0.0D, 0.0D, 11.0D, 16.0D, 16.0D, 0.0D),
		Block.box(16.0D, 0.0D, 0.0D, 5.0D, 16.0D, 16.0D),
		Block.box(11.0D, 0.0D, 0.0D, 0.0D, 16.0D, 16.0D),
	};
	private final boolean isBoiled;
	
	public PickerelweedBlock(Properties properties, boolean isBoiled) {
		super(properties);
		this.isBoiled = isBoiled;
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACING, Direction.UP)
			.setValue(WATERLOGGED, false)
		);
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPES[state.getValue(FACING).get3DDataValue()];
	}
	
	public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return false;
	}
	
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
		return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(ifluidstate.getType() == Fluids.WATER)).setValue(FACING, context.getNearestLookingDirection().getOpposite());
	}
	
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		if(stateIn.getValue(WATERLOGGED)) {
			worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
		}
		return stateIn;
	}

	@Override
	public boolean canPlaceLiquid(IBlockReader worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
		return !state.getValue(WATERLOGGED) && fluidIn == Fluids.WATER;
	}

	@Override
	public boolean placeLiquid(IWorld worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn) {
		if(!state.getValue(WATERLOGGED) && fluidStateIn.getType() == Fluids.WATER) {
			if(!worldIn.isClientSide()) {
				worldIn.setBlock(pos, state.setValue(WATERLOGGED, Boolean.valueOf(true)), 3);
				worldIn.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Fluid takeLiquid(IWorld worldIn, BlockPos pos, BlockState state) {
		if(state.getValue(WATERLOGGED)) {
			worldIn.setBlock(pos, state.setValue(WATERLOGGED, Boolean.valueOf(false)), 3);
			return Fluids.WATER;
		} else {
			return Fluids.EMPTY;
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}
	
	@Override
	public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entity) {
		if(!(entity instanceof PikeEntity)) {
			if(!this.isBoiled) {
				entity.makeStuckInBlock(state, new Vector3d(0.6D, 1.0, 0.6D));
			}
		}
	}
	
	@Override
	public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
}