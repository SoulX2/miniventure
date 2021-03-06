package miniventure.game.item;

import org.jetbrains.annotations.NotNull;

public enum ResourceItem {
	
	Log;
	
	@NotNull
	public Item get() {
		return new Item(ItemType.Resource, name()) {
			@Override
			public String[] save() {
				return new String[] {getType().name(), ResourceItem.this.name()};
			}
			
			@Override public Item copy() { return this; }
		};
	}
}
