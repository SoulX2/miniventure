package miniventure.game.screen;

import miniventure.game.GameCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

public class MainMenuScreen implements Screen {
	
	private final GameCore game;
	
	private SpriteBatch batch;
	
	private OrthographicCamera camera;
	
	private boolean clicked = false, rendered = false;
	
	public MainMenuScreen(final GameCore game) {
		this.game = game;
		
		batch = game.getBatch();
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, GameCore.SCREEN_WIDTH, GameCore.SCREEN_HEIGHT);
	}
	
	@Override
	public void dispose() {}
	
	@Override
	public void render(float delta) {
		
		if(rendered) {
			game.setScreen(new GameScreen(game));
			dispose();
			return;
		}
		
		Gdx.gl.glClearColor(0, 0, 0.2f, 1); // these are floats from 0 to 1.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		
		batch.setProjectionMatrix(camera.combined); // tells the batch to use the camera's coordinate system.
		batch.begin();
		BitmapFont font = GameCore.getFont();
		
		if(clicked) {
			font.draw(batch, "Loading...", camera.viewportWidth / 2, camera.viewportHeight / 2, 0, Align.center, false);
			rendered = true;
		} else {
			font.draw(batch, "Welcome to Miniventure! You're playing Version " + GameCore.VERSION, 100, 200);
			font.draw(batch, "Use the arrow keys or the mouse to move, the C key to attack, and the V key to", 100, 150);
			font.draw(batch, "interact with things and place items.", 100, 130);
			font.draw(batch, "Use the Q and E keys to cycle through your inventory.", 100, 80);
			font.draw(batch, "Click anywhere, or press space, to begin.", 100, 30);
		}
		batch.end();
		
		if (Gdx.input.isTouched() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
			clicked = true;
	}
	
	@Override public void resize(int width, int height) {}
	
	@Override public void pause() {}
	@Override public void resume() {}
	
	@Override public void show() {}
	@Override public void hide() {}
}
