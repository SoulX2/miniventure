package miniventure.game.client;

import java.io.IOException;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol;
import miniventure.game.screen.MainMenu;
import miniventure.game.util.FrameBlinker;
import miniventure.game.util.ProgressLogger;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.ClientLevel;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.ClientEntity;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntityRenderer;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.ClientPlayer;
import miniventure.game.world.tile.Tile;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import org.jetbrains.annotations.NotNull;

public class GameClient implements GameProtocol {
	
	private Client client;
	
	public GameClient() {
		client = new Client(writeBufferSize, objectBufferSize);
		
		GameProtocol.registerClasses(client.getKryo());
		
		addListener(new Listener() {
			@Override
			public void received (Connection connection, Object object) {
				ClientWorld world = ClientCore.getWorld();
				
				if(object instanceof LevelData) {
					System.out.println("client received level");
					world.addLevel((LevelData)object);
				}
				
				if(object instanceof ChunkData) {
					//System.out.println("client received chunk");
					world.loadChunk((ChunkData)object);
				}
				
				if(object instanceof SpawnData) {
					System.out.println("client received player");
					SpawnData data = (SpawnData) object;
					world.spawnPlayer(new ClientPlayer(data));
					ClientCore.setScreen(null);
				}
				
				if(object instanceof TileUpdate) {
					// individual tile update
					TileUpdate update = (TileUpdate) object;
					Level level = world.getLevel(update.levelDepth);
					if(level == null) return;
					Tile tile = level.getTile(update.x, update.y);
					if(tile != null)
						update.tileData.apply(tile);
				}
				
				if(object instanceof Hurt) {
					System.out.println("client received object hurt");
					Hurt hurt = (Hurt) object;
					
					WorldObject target = hurt.target.getObject(world);
					WorldObject source = hurt.source.getObject(world);
					
					// TODO later, show health bar
					//target.attackedBy(source, attackItem, hurt.damage);
					if(target instanceof Entity)
						((Entity)target).setBlinker(0.5f, true, new FrameBlinker(5, 1, false));
				}
				
				if(object instanceof EntityAddition) {
					System.out.println("client received entity addition");
					EntityAddition addition = (EntityAddition) object;
					if(addition.positionUpdate.levelDepth == null) return; // no point to it, really.
					
					ClientPlayer player = world.getMainPlayer();
					if(player != null && addition.eid == player.getId()) return; // shouldn't pay attention to trying to set the client player like this.
					ClientLevel level = world.getLevel(addition.positionUpdate.levelDepth);
					if(level == null || (player != null && !level.equals(player.getLevel()))) return;
					
					ClientEntity e = new ClientEntity(addition.eid, EntityRenderer.deserialize(addition.spriteUpdate.rendererData));
					PositionUpdate newPos = addition.positionUpdate;
					e.moveTo(level, newPos.x, newPos.y, newPos.z);
				}
				
				if(object instanceof EntityRemoval) {
					System.out.println("client received entity removal");
					int eid = ((EntityRemoval)object).eid;
					world.deregisterEntity(eid);
				}
				
				if(object instanceof EntityUpdate) {
					//System.out.println("client received entity movement");
					EntityUpdate update = (EntityUpdate) object;
					PositionUpdate newPos = update.positionUpdate;
					SpriteUpdate newSprite = update.spriteUpdate;
					
					ServerEntity e = (ServerEntity) update.tag.getObject(world);
					if(e == null) return;
					
					if(newPos != null) {
						ClientLevel level = newPos.levelDepth == null ? null : world.getLevel(newPos.levelDepth);
						if(level != null)
							e.moveTo(level, newPos.x, newPos.y, newPos.z);
					}
					if(newSprite != null) {
						e.setRenderer(EntityRenderer.deserialize(newSprite.rendererData));
					}
				}
				
				forPacket(object, InventoryUpdate.class, newInv -> {
					ClientPlayer player = world.getMainPlayer();
					player.getInventory().loadItems(newInv.inventory);
					player.getHands().loadItem(newInv.heldItemStack);
				});
			}
			
			@Override
			public void disconnected(Connection connection) {
				System.err.println("client disconnected from server.");
				// TODO make ErrorScreen, which accepts a string to display and has a "back to title screen" button.
				ClientCore.setScreen(new MainMenu(ClientCore.getWorld()));
				//GameCore.setScreen(new ErrorScreen("Lost connection with server."));
			}
		});
		
		client.start();
	}
	
	public void send(Object obj) { client.sendTCP(obj); }
	public void addListener(Listener listener) { client.addListener(listener); }
	
	public boolean connectToServer(@NotNull ProgressLogger logger, String host) { return connectToServer(logger, host, GameProtocol.PORT); }
	public boolean connectToServer(@NotNull ProgressLogger logger, String host, int port) {
		logger.pushMessage("connecting to server at "+host+":"+port+"...");
		
		try {
			client.connect(5000, host, port);
		} catch(IOException e) {
			e.printStackTrace();
			// error screen
			logger.editMessage("failed to connect to server.");
			return false;
		}
		
		logger.editMessage("logging in...");
		
		send(new Login("player", GameCore.VERSION));
		
		logger.editMessage("Loading world from server...");
		
		return true;
	}
}
