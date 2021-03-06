package miniventure.game.screen;

import javax.swing.JOptionPane;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.client.ClientWorld;
import miniventure.game.client.LevelViewport;
import miniventure.game.screen.InfoScreen.CreditsScreen;
import miniventure.game.screen.InfoScreen.InstructionsScreen;
import miniventure.game.util.VersionInfo;
import miniventure.game.world.DisplayLevel;
import miniventure.game.world.TimeOfDay;
import miniventure.game.world.levelgen.LevelGenerator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.LinkLabel.LinkLabelStyle;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

import org.jetbrains.annotations.NotNull;

public class MainMenu extends MenuScreen {
	
	private boolean dialog = false;
	
	private final Table table;
	
	private final DisplayLevel backgroundLevel;
	private final LevelViewport levelView;
	private final Color lightOverlay;
	private final Vector2 cameraPos, cameraDir;
	
	private static final float PAN_SPEED = 4.5f; // in tiles/second.
	
	public MainMenu() {
		super();
		
		ClientWorld world = ClientCore.getWorld();
		
		table = new Table();
		// table.setDebug(true);
		addLabel("Welcome to Miniventure!", 20);
		addLabel("You are playing version " + GameCore.VERSION, 25);
		
		VisLabel updateLabel = addLabel("Checking for higher versions...", 45);
		setVersionUpdateLabel(updateLabel);
		
		VisTextButton playButton = new VisTextButton("Play");
		
		playButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				if(dialog) return;
				world.createWorld(0, 0);
			}
		});
		
		table.add(playButton).spaceBottom(20);
		table.row();
		
		VisTextButton joinBtn = new VisTextButton("Join Server");
		joinBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				if(dialog) return;
				dialog = true;
				LoadingScreen loader = new LoadingScreen();
				loader.pushMessage("Preparing to connect...");
				ClientCore.setScreen(loader);
				new Thread(() -> {
					String ipAddress = JOptionPane.showInputDialog("Enter the IP Address you want to connect to.");
					Gdx.app.postRunnable(() -> {
						if(ipAddress != null)
							world.createWorld(ipAddress);
						else
							ClientCore.backToParentScreen();
					});
					dialog = false;
				}).start();
			}
		});
		
		table.add(joinBtn).spaceBottom(20).row();
		
		VisTextButton helpBtn = makeButton("Instructions", () -> ClientCore.setScreen(new InstructionsScreen()));
		table.add(helpBtn).spaceBottom(20).row();
		
		VisTextButton creditsBtn = makeButton("Credits", () -> ClientCore.setScreen(new CreditsScreen()));
		table.add(creditsBtn).spaceBottom(20).row();
		
		VisTextButton exitBtn = makeButton("Quit", () -> Gdx.app.exit());
		table.add(exitBtn).row();
		
		addActor(table);
		table.setPosition(getWidth()/2, getHeight()/2, Align.center);
		
		// setup level scrolling in background
		
		levelView = new LevelViewport();
		levelView.zoom(-1);
		TimeOfDay time = TimeOfDay.values[MathUtils.random(TimeOfDay.values.length-1)];
		lightOverlay = TimeOfDay.getSkyColor(time.getStartOffsetSeconds());
		
		LevelGenerator generator = new LevelGenerator(MathUtils.random.nextLong(), 200, 100);
		backgroundLevel = new DisplayLevel(generator);
		
		Vector2 size = new Vector2(levelView.getViewWidth(), levelView.getViewHeight());//.scl(0.5f);
		cameraPos = new Vector2(MathUtils.random(size.x, backgroundLevel.getWidth()-size.x), MathUtils.random(size.y, backgroundLevel.getHeight()-size.y));
		
		cameraDir = new Vector2().setLength(PAN_SPEED).setToRandomDirection().setLength(PAN_SPEED);
		
		// setup background music
		Music song = ClientCore.setMusicTrack(Gdx.files.internal("audio/music/title.mp3"));
		song.setLooping(true);
		song.play();
	}
	
	@Override
	public boolean usesWholeScreen() { return false; }
	
	@Override
	public void draw() {
		// levelView.handleInput();
		levelView.render(cameraPos, lightOverlay, backgroundLevel);
		
		cameraPos.add(cameraDir.cpy().scl(GameCore.getDeltaTime()));
		cameraDir.x = velDir(cameraPos.x, cameraDir.x, levelView.getViewWidth()/2, backgroundLevel.getWidth() - levelView.getViewWidth()/2);
		cameraDir.y = velDir(cameraPos.y, cameraDir.y, levelView.getViewHeight()/2, backgroundLevel.getHeight() - levelView.getViewHeight()/2);
		
		super.draw();
	}
	
	private float velDir(float pos, float vel, float min, float max) {
		if((pos >= max && vel >= 0) || (pos <= min && vel <= 0)) {
			vel += MathUtils.random(-PAN_SPEED/4, PAN_SPEED/4);
			vel = -vel;
		}
		
		return vel;
	}
	
	@NotNull
	private VisLabel addLabel(String msg, int spacing) {
		VisLabel label = makeLabel(msg);
		table.add(label).spaceBottom(spacing);
		table.row();
		return label;
	}
	
	@NotNull
	private VisLabel makeLabel(String text) {
		return new VisLabel(text, new LabelStyle(GameCore.getFont(), Color.WHITE));
	}
	
	private void setVersionUpdateLabel(VisLabel label) {
		if(!GameCore.determinedLatestVersion()) {
			// return a "loading" label that will be replaced once the version check completes.
			new Thread(() -> {
				GameCore.getLatestVersion();
				setVersionUpdateLabel(label);
			}).start();
		}
		else {
			// add a message saying you have the latest version, or a hyperlink message to the newest jar file.
			VersionInfo latestVersion = GameCore.getLatestVersion();
			if(latestVersion.version.compareTo(GameCore.VERSION) > 0) // link new version
				table.getCell(label).setActor(new MyLinkLabel("Miniventure " + latestVersion.releaseName + " Now Available! Click here to download.", latestVersion.assetUrl, new LinkLabelStyle(GameCore.getFont(), Color.SKY, new ColorRect(Color.SKY))));
			else if(latestVersion.releaseName.length() > 0)
				label.setText("You have the latest version.");
			else
				label.setText("Connection failed, could not check for updates.");
			
			table.pack();
			table.setPosition(getWidth()/2, getHeight()/2, Align.center);
		}
	}
}
