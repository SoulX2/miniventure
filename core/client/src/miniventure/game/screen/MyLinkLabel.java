package miniventure.game.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.kotcrab.vis.ui.widget.LinkLabel;

public class MyLinkLabel extends LinkLabel {
	public MyLinkLabel(CharSequence url) {
		super(url);
	}
	
	public MyLinkLabel(CharSequence text, CharSequence url) {
		super(text, url);
	}
	
	public MyLinkLabel(CharSequence text, int alignment) {
		super(text, alignment);
	}
	
	public MyLinkLabel(CharSequence text, Color textColor) {
		super(text, textColor);
	}
	
	public MyLinkLabel(CharSequence text, LinkLabelStyle style) {
		super(text, style);
	}
	
	public MyLinkLabel(CharSequence text, CharSequence url, String styleName) {
		super(text, url, styleName);
	}
	
	public MyLinkLabel(CharSequence text, CharSequence url, LinkLabelStyle style) {
		super(text, url, style);
	}
	
	public MyLinkLabel(CharSequence text, String fontName, Color color) {
		super(text, fontName, color);
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		//Color c = batch.getColor();
		super.draw(batch, parentAlpha);
		batch.setColor(Color.WHITE);
	}
}
