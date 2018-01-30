package miniventure.game.world.entity.mob;

import miniventure.game.world.Level;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PursuePattern implements MovementPattern {
	
	@FunctionalInterface
	interface EntityFollower {
		@Nullable Entity getEntityToFollow(MobAi self);
		
		EntityFollower NEAREST_PLAYER = (self) -> {
			Level level = self.getLevel();
			if(level == null) return null;
			
			return level.getClosestPlayer(self.getBounds().getCenter(new Vector2()));
		};
	}
	
	@NotNull private EntityFollower followBehavior;
	private float maxDist;
	private float followSpeed;
	
	private WanderingPattern idlePattern;
	private boolean wasFollowing = false;
	
	public PursuePattern() { this(EntityFollower.NEAREST_PLAYER); }
	public PursuePattern(EntityFollower followBehavior) { this(followBehavior, 8 * Tile.SIZE, 1f); }
	public PursuePattern(@NotNull EntityFollower behavior, float maxDist, float followSpeed) {
		this.followBehavior = behavior;
		this.maxDist = maxDist;
		this.followSpeed = followSpeed;
		
		idlePattern = new WanderingPattern();
	}
	
	@Override
	public Vector2 move(float delta, MobAi mob) {
		Entity follow = followBehavior.getEntityToFollow(mob);
		if(follow == null) return new Vector2();
		
		Vector2 dist = follow.getBounds().getCenter(new Vector2());
		dist.sub(mob.getBounds().getCenter(new Vector2()));
		
		if(maxDist <= 0 || dist.len() < maxDist) { // move toward the entity
			dist.setLength(followSpeed * delta);
			wasFollowing = true;
		} else {
			if(wasFollowing) idlePattern.reset();
			wasFollowing = false;
			dist.set(idlePattern.move(delta, mob));
		}
		
		return dist;
	}
}