package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.graphics.g2d.Batch;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Hands {
	
	/// This is a wrapper class for items, when being currently held by the player. Perhaps *this* should be extended..?
	
	public static class HandItem extends Item {
		HandItem() {
			super(ItemType.Misc, "Hand", GameCore.icons.get("blank"));
		}
		
		@Override
		public String[] save() { return new String[] {ItemType.Misc.name(), MyUtils.encodeStringArray(Hands.class.getCanonicalName().replace(Item.class.getPackage().getName()+".", ""), HandItem.class.getSimpleName())}; }
		
		/** @noinspection Contract*/
		public static Item load(String[] data) { return new HandItem(); }
		
		@Override public Item getUsedItem() { return this; }
		@Override public Item copy() { return new HandItem(); }
		
		@Override public boolean interact(WorldObject obj, Player player) {
			boolean success = obj.interactWith(player, null);
			if(success) use();
			return success;
		}
		@Override public boolean attack(WorldObject obj, Player player) {
			boolean success = obj.attackedBy(player, null, 1);
			if(success) use();
			return success;
		}
		
		@Override
		public void drawItem(int stackSize, Batch batch, float x, float y) {}
	}
	
	@NotNull private Item item;
	private int count = 1;
	final Player player;
	
	public Hands(Player player) {
		this.player = player;
		item = new HandItem();
	}
	
	public void setItem(@NotNull ItemStack stack) { setItem(stack.item, stack.count); }
	public void setItem(@NotNull Item item, int count) {
		//System.out.println("setting "+player+" hands to "+count+" "+item);
		this.item = item;
		this.count = count;
	}
	
	public boolean addItem(@NotNull Item other, @NotNull Inventory inv) {
		if(item instanceof HandItem && inv.canFit(other))
			item = other;
		else if(item.equals(other) && count < item.getMaxStackSize())
			count++;
		else
			return false;
		
		return true;
	}
	
	public void reset() { setItem(new HandItem(), 1); }
	
	public void clearItem(Inventory inv) {
		Item item = this.item;
		int count = this.count;
		reset();
		if(inv != null && count > 0 && !(item instanceof HandItem)) {
			int added = inv.addItem(item, count);
			if(added != count)
				dropStack(new ItemStack(item, count - added));
		}
		
		//reset();
	}
	
	void dropStack(@NotNull ItemStack stack) {}
	
	public void resetItemUsage() {
		if(count <= 0) // this shouldn't happen, generally... unless stuff has been removed from the active item by the crafting menu.
			reset();
	}
	
	public boolean hasUsableItem() { return !(item.isUsed() || count <= 0); }
	
	@NotNull
	public Item getUsableItem() { return item; }
	@Nullable
	public Item getEffectiveItem() { return item instanceof HandItem ? null : item; }
	
	public int getCount() { return count; }
	
	public String[] save() {
		return ItemStack.save(getUsableItem(), getCount());
	}
	
	public void loadItem(String[] data) {
		ItemStack stack = ItemStack.load(data);
		count = stack.count;
		item = stack.item;
	}
}
