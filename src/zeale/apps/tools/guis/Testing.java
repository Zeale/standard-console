package zeale.apps.tools.guis;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import zeale.apps.tools.api.backgrounds.Background;

public class Testing extends BorderPane {
	private final Background background = new Background(120) {

		{
			autoclear = true;
		}

		private volatile boolean queuePrint;

		private volatile double mouseX, mouseY, scroll, spawnX = 960, spawnY = 1080;

		{

			EventHandler<? super MouseEvent> mouseHandler = event -> {
				mouseX = event.getX();
				mouseY = event.getY();
			};

			EventHandler<? super ScrollEvent> scrollHandler = event -> scroll += event.getDeltaY();

			EventHandler<? super KeyEvent> keyHandler = event -> {

				if (event.getCode() == KeyCode.ESCAPE)
					if (isPaused())
						resume();
					else
						pause();
			};

			EventHandler<? super MouseEvent> clickHandler = event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					spawnX = event.getX();
					spawnY = event.getY();
				} else if (event.getButton() == MouseButton.SECONDARY)
					queuePrint = true;
			};

			canvasProperty().addListener((ChangeListener<Canvas>) (observable, oldValue, newValue) -> {
				if (newValue != null) {
					newValue.addEventHandler(MouseEvent.MOUSE_MOVED, mouseHandler);
					newValue.addEventHandler(ScrollEvent.SCROLL, scrollHandler);
					newValue.addEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
					newValue.addEventHandler(MouseEvent.MOUSE_CLICKED, clickHandler);
					newValue.setFocusTraversable(true);
				}
				if (oldValue != null) {
					oldValue.removeEventHandler(MouseEvent.MOUSE_MOVED, mouseHandler);
					oldValue.removeEventHandler(ScrollEvent.SCROLL, scrollHandler);
					oldValue.removeEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
					oldValue.removeEventHandler(MouseEvent.MOUSE_CLICKED, clickHandler);
				}

			});

		}

		@Override
		protected void draw(GraphicsContext gc) {
			gc.setStroke(Color.WHITE);
			gc.setFill(Color.GOLD);
			gc.setLineWidth(5);

			drawTear(spawnX, spawnY, mouseX, mouseY, 50 + scroll);

		}

		/**
		 * @param startx
		 * @param starty
		 * @param endx
		 * @param endy
		 * @param width  This must be, <i>at the least</i>, equal to
		 *
		 *               <pre>
		 *               0.5 * sqrt((endx - startx) ^ 2 + (endy - starty) ^ 2)
		 *               </pre>
		 *
		 *               where <code>sqrt</code> is the square root function and
		 *               <code>starty</code> and <code>startx</code> are the starting
		 *               positions of this teardrop.
		 *
		 *               More colloquially, this parameter, (<code>width</code>), can
		 *               not be greater than the length of the tear * 2. This is simply
		 *               a logical limitation of drawing a tear, based on the
		 *               definitions of the different parameters. Note that a teardrop
		 *               can be drawn with a width equal to twice its length, but this
		 *               will simply result in a closed semi-circle (since the entire
		 *               length of the tear will be allocated for the head). Drawing
		 *               tears with small lengths like this is allowed, but will likely
		 *               result in visually abominable tears.
		 */
		protected void drawTear(double startx, double starty, double endx, double endy, double width) {

			double xdist = endx - startx, ydist = endy - starty;
			double distance = Math.sqrt(xdist * xdist + ydist * ydist);
			if (width < 1 || distance < width / 2)
				return;

			GraphicsContext gc = getContext();

			// Design...

			// Line directly down the teardrop.
			gc.setStroke(Color.FIREBRICK);
			gc.setLineWidth(3);

			gc.beginPath();
			gc.moveTo(startx, starty);
			// System.out.println("From: (" + startx + ", " + starty + ")");
			gc.lineTo(endx, endy);
			// System.out.println("To: (" + endx + ", " + endy + ")");
			gc.stroke();
			gc.closePath();

			// Circle at the point where the head of the tear begins (but centered on the
			// line directly down the teardrop).

			double bezierPortion = distance - width / 2;
			double frac = bezierPortion / distance;

			double headStartCenterXPos = frac * (endx - startx) + startx;
			double headStartCenterYPos = frac * (endy - starty) + starty;

			gc.setLineWidth(5);
			gc.setStroke(Color.BLUE);
			gc.beginPath();
			// Draws a dot.
			gc.lineTo(headStartCenterXPos, headStartCenterYPos);
			gc.stroke();
			gc.closePath();

			// Time for some crappy variable names.
			double b1endXDist = ydist * width / distance / 2, b1endYDist = -xdist * width / distance / 2;
			if (queuePrint) {
				System.out.println("b1X=" + b1endXDist);
				System.out.println("b1Y=" + b1endYDist);
				queuePrint = false;
			}

			gc.beginPath();
			gc.moveTo(startx, starty);
			gc.bezierCurveTo(headStartCenterXPos + b1endXDist, headStartCenterYPos + b1endYDist, startx + b1endXDist,
					starty + b1endYDist, headStartCenterXPos + b1endXDist, headStartCenterYPos + b1endYDist);
			gc.stroke();
			gc.closePath();

			// TODO Draw the arc here and don't close any paths until the teardrop is
			// closed. There's no point in drawing the bezier curves individually then
			// moving back to draw the arc at their tips. Apart from testing if this method
			// will work or not.

			gc.setStroke(Color.BLUE);
			gc.beginPath();
			gc.moveTo(startx, starty);
			gc.bezierCurveTo(headStartCenterXPos - b1endXDist, headStartCenterYPos - b1endYDist, startx - b1endXDist,
					starty - b1endYDist, headStartCenterXPos - b1endXDist, headStartCenterYPos - b1endYDist);
			gc.stroke();
			gc.closePath();

			gc.setStroke(Color.GOLD);
			gc.beginPath();
			gc.moveTo(headStartCenterXPos + b1endXDist, headStartCenterYPos + b1endYDist);
			gc.arcTo(endx + b1endXDist, endy + b1endYDist, endx, endy, width / 2);
			gc.arcTo(endx - b1endXDist, endy - b1endYDist, headStartCenterXPos - b1endXDist,
					headStartCenterYPos - b1endYDist, width / 2);
			gc.stroke();
			gc.closePath();

			// The head of the tear consists of a semi-circle whose diameter is `width`.
			//
			// Also, control points will be 1/4th of full for a nice looking tear. :)
			// I might make another method for generating tears with specific control point
			// factors.
			//
			// P.S., I know this is very esoteric.
		}
	};

	{
		Canvas canvas = new Canvas();

		setCenter(canvas);

		canvas.widthProperty().bind(widthProperty());
		canvas.heightProperty().bind(heightProperty());

		background.setCanvas(canvas);
		background.show();
	}
}
