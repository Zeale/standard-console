package zeale.apps.tools.api.backgrounds;

import org.alixia.chatroom.api.QuickList;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class RedVelvetBackground extends Background {

	private class Circle {
		private double x = Math.random() * getWidth(), y = Math.random() * getHeight();
		private double opacity = Math.random() * 2 - 1;
		private final float sizeScale = (float) (1 + (Math.random() - 0.5) / 3);

		public void draw() {

			if (opacity >= 1) {
				opacity = -1;
				x = Math.random() * getWidth();
				y = Math.random() * getHeight();
			}
			opacity += 1d / steps.get();

			GraphicsContext gc = getContext();
			gc.setStroke(new Color(1, 1, 1, Math.abs(Math.abs(opacity) - 1)));
			gc.setLineWidth(1.5);
			gc.setEffect(DEFAULT_CIRCLE_EFFECT);

			double diameter = sizeScale * 100 * (getWidth() + getHeight()) / 3000;// == 100 * sizeScale on a 1920x1080
			gc.strokeOval(x - diameter / 2, y - diameter / 2, diameter, diameter);

			gc.setLineWidth(1);
			gc.setEffect(null);
		}
	}

	private static final LinearGradient velvetGradient = new LinearGradient(0.5, 0, 0.5, 1, true, CycleMethod.NO_CYCLE,
			new Stop(0, Color.DARKRED), new Stop(1, new Color(0.15, 0, 0, 1)));

	private static final DropShadow DEFAULT_CIRCLE_EFFECT = new DropShadow(40, Color.RED);

	private final QuickList<Circle> circles = new QuickList<>();

	{
		for (int i = 0; i < 12; i++)
			circles.add(new Circle());
	}

	private IntegerProperty steps = new SimpleIntegerProperty();

	{
		steps.bind(targetFramerateProperty().multiply(1));
	}

	{
		DEFAULT_CIRCLE_EFFECT.setSpread(0.8);
		DEFAULT_CIRCLE_EFFECT.setBlurType(BlurType.GAUSSIAN);
	}

	public RedVelvetBackground() {
	}

	protected RedVelvetBackground(boolean autoclear) {
		super(autoclear);
	}

	public RedVelvetBackground(Canvas canvas) {
		super(canvas);
	}

	protected RedVelvetBackground(Canvas canvas, boolean autoclear) {
		super(canvas, autoclear);
	}

	public RedVelvetBackground(Canvas canvas, double targetFramerate) {
		super(canvas, targetFramerate);
	}

	protected RedVelvetBackground(Canvas canvas, double targetFramerate, boolean autoclear) {
		super(canvas, targetFramerate, autoclear);
	}

	public RedVelvetBackground(double targetFramerate) {
		super(targetFramerate);
	}

	@Override
	protected void draw(GraphicsContext gc) {
		gc.setFill(velvetGradient);
		fillFullRect();
		for (Circle c : circles)
			c.draw();
	}

	@Override
	protected void resized(double width, double height, double oldWidth, double oldHeight) {
		for (Circle c : circles) {
			c.x *= width / oldWidth;
			c.y *= height / oldHeight;
		}
	}

	/**
	 * Sets the amount of circles being shown by this background. This method
	 * differs from {@link #setCircles(int)} in that it maintains as many circles
	 * that are currently being used, as it can.
	 *
	 * @param count The new amount of circles that this background will manage.
	 */
	public void setCircleCount(int count) {
		if (count < 0)
			count = 0;
		if (count < circles.size())
			circles.setAll(circles.subList(0, count - 1));
		else
			while (count > circles.size())
				circles.add(new Circle());

	}

	/**
	 * Sets the time that it takes for the circle to fade in (or out, since fading
	 * in and out take the same amount of time). A completely invisible circle will
	 * take <code>seconds * 2</code> seconds to become completely visible (fade in),
	 * then fade back out, becoming completely invisible.
	 *
	 * @param seconds The seconds that the fade animation takes.
	 */
	public void setCircleFadeTime(float seconds) {
		steps.unbind();
		steps.bind(targetFramerateProperty().multiply(seconds));
	}

	/**
	 * Sets the circles being shown by this background.
	 *
	 * @param count The amount of circles.
	 */
	public void setCircles(int count) {
		circles.clear();
		for (int i = 0; i < count; i++)
			circles.add(new Circle());
	}

}
