package chess;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

public class PointWhite extends Canvas {
	Pad padBelonged;
	public PointWhite(Pad padBelonged) {
		setSize(20,20);
		this.padBelonged=padBelonged;
	}
//	»­°×Æå
	public void paint(Graphics g) {
		g.setColor(Color.white);
		g.fillOval(0, 0, 14, 14);
	}
}
