package com.minecraftabnormals.upgrade_aquatic.client.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpectralConsumeParticle extends SpriteTexturedParticle {
	protected final IAnimatedSprite animatedSprite;
	private final float scale;
	private final int MAX_FRAME_ID = 3;
	protected int currentFrame = 0;
	private boolean directionRight = true;
	private int lastTick = 0;

	public SpectralConsumeParticle(IAnimatedSprite animatedSprite, ClientWorld world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
		super(world, posX, posY, posZ, motionX, motionY, motionZ);
		this.scale = this.quadSize = this.random.nextFloat() * 0.6F + 0.2F;
		this.rCol = 1f;
		this.gCol = 1f;
		this.bCol = 1f;
		this.xd = motionX * (double)0.2F + (Math.random() * 2.0D - 1.0D) * (double)0.01F;
		this.yd = motionY * (double)0.2F + (Math.random() * 2.0D - 1.0D) * (double)0.01F;
		this.zd = motionZ * (double)0.2F + (Math.random() * 2.0D - 1.0D) * (double)0.01F;
		this.lifetime = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
		this.animatedSprite = animatedSprite;
		this.setSpriteFromAge(animatedSprite);
	}
	
	@Override
	public void render(IVertexBuilder p_225606_1_, ActiveRenderInfo activeInfo, float partialTicks) {
		Entity entity = activeInfo.getEntity();
        if (entity.tickCount >= this.lastTick + 5) {
            if (this.currentFrame == MAX_FRAME_ID) {
                this.directionRight = false;
            } else if (currentFrame == 0) {
                this.directionRight = true;
            }
            this.currentFrame = this.currentFrame + (directionRight ? 1 : -1);
            this.lastTick = entity.tickCount;
        }
        float f = ((float) this.age + partialTicks) / (float) this.lifetime;
        this.quadSize = this.scale * (1f - f * f * 0.5f);
		super.render(p_225606_1_, activeInfo, partialTicks);
	}
	
	@Override
    public void tick() {
		super.tick();
		this.oRoll = this.roll;
		
		if(this.isAlive()) {
            this.setSpriteFromAge(this.animatedSprite);
        }
    }
	
	@Override
    public IParticleRenderType getRenderType() {
    	return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }
	
	@Override
    public int getLightColor(float partialTick) {
		float f = ((float) this.age + partialTick) / (float) this.lifetime;
		f = MathHelper.clamp(f, 0f, 1f);
		int i = super.getLightColor(partialTick);
		int j = i & 255;
		int k = i >> 16 & 255;
		j = j + (int) (f * 15f * 16f);
		if (j > 240) {
			j = 240;
		}
		return j | k << 16;
    }
	
	public static class Factory implements IParticleFactory<BasicParticleType> {
		private IAnimatedSprite animatedSprite;

    	public Factory(IAnimatedSprite animatedSprite) {
    		this.animatedSprite = animatedSprite;
    	}
    	
        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SpectralConsumeParticle(this.animatedSprite, world, x, y, z, xSpeed, ySpeed, zSpeed);
        }
	}

}
