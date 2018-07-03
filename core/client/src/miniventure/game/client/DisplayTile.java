package miniventure.game.client;

import miniventure.game.world.tile.RenderTile;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

class DisplayTile extends RenderTile {
	
	DisplayTile(@NotNull DisplayLevel level, int x, int y, @NotNull TileTypeEnum[] types) {
		super(level, x, y, types, null);
	}
	
	@Override
	public String toString() { return getType().getEnumType()+" DisplayTile"; }
	
}
