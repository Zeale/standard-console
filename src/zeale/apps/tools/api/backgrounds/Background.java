package zeale.apps.tools.api.backgrounds;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 * <code>0,0</code> is the top left of the background.
 *
 * @author Zeale
 *
 */
public abstract class Background {

	protected boolean autoclear = true;

	/**
	 * <p>
	 * Determines whether draw calls will be run on the Application thread (like
	 * they're supposed to be).
	 * </p>
	 * <p>
	 * {@link Canvas} documentation declares that drawing to a Canvas should be done
	 * on the JavaFX Application thread, however, it can be done elsewhere.
	 * Sometimes, invoking draw calls on other threads can cause tearing issues and
	 * other anomalies (such as {@link InternalError}s) that tend to lag
	 * {@link Background} animations, or even completely stop them. This can be
	 * solved by posting a {@link Runnable} to the application thread's run queue
	 * ({@link Platform#runLater(Runnable)}) for <b>each individual frame</b>.
	 * Running a background that draws multiple complex shapes, at 60 FPS, with
	 * effects, colors, and other graphics will likely block up the run queue on
	 * some systems, causing the application to freeze up or lag. For this reason,
	 * running draw calls on the application thread is left optional.
	 * </p>
	 * <p>
	 * Note: The entire background couldn't simply be executed entirely on the app
	 * thread because it contains a while loop with sleep calls. This would
	 * completely steal the application thread (which is why it runs on a private
	 * thread), causing an almost completely unresponsive app. (The only thing that
	 * would run would be the background.)
	 * </p>
	 * <p>
	 * <b>The default value of this property is <code>true</code>, to avoid tearing
	 * and other anomalies.</b>
	 * </p>
	 */
	private BooleanProperty runOnApplicationThread = new SimpleBooleanProperty(true);

	private Thread runner = new Thread(new Runnable() {

		@Override
		public void run() {
			try {

				while (run) {
					Thread.sleep((long) (1000 / targetFramerate.doubleValue()));
					synchronized (runner) {

						if (runOnApplicationThread.get())
							Platform.runLater(() -> {
								if (autoclear)
									clearCanvas();
								draw(canvas.get().getGraphicsContext2D());
							});
						else {
							if (autoclear)
								clearCanvas();
							draw(canvas.get().getGraphicsContext2D());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			runner = new Thread(this);
			runner.setDaemon(true);
		}
	});

	{
		runner.setDaemon(true);
	}

	private final ObjectProperty<Canvas> canvas = new SimpleObjectProperty<>();

	private DoubleProperty width, height;

	private GraphicsContext cachedContext;

	private boolean run;
	{
		ChangeListener<Number> widthListener = (observable, oldValue, newValue) -> resized(newValue.doubleValue(),
				getHeight(), oldValue.doubleValue(), getHeight()),
				heightListener = (observable, oldValue, newValue) -> resized(getWidth(), newValue.doubleValue(),
						getWidth(), oldValue.doubleValue());
		canvas.addListener((ChangeListener<Canvas>) (observable, oldValue, newValue) -> {
			synchronized (runner) {
				if (oldValue != null) {
					clearCanvas();
					oldValue.widthProperty().removeListener(widthListener);
					oldValue.heightProperty().removeListener(heightListener);
				}
				if (newValue != null) {
					cachedContext = newValue.getGraphicsContext2D();
					width = newValue.widthProperty();
					height = newValue.heightProperty();
					width.addListener(widthListener);
					height.addListener(heightListener);
				} else {
					run = false;
					runner.interrupt();
				}
			}
		});
	}

	private final DoubleProperty targetFramerate = new SimpleDoubleProperty(120);

	{
		int refreshRate = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode()
				.getRefreshRate();
		if (refreshRate != DisplayMode.REFRESH_RATE_UNKNOWN)
			setTargetFramerate(refreshRate);
	}

	public Background() {
	}

	protected Background(boolean autoclear) {
		autoclear = false;
	}

	public Background(Canvas canvas) {
		setCanvas(canvas);
	}

	protected Background(Canvas canvas, boolean autoclear) {
		this(canvas);
		autoclear = false;
	}

	public Background(Canvas canvas, double targetFramerate) {
		this(canvas);
		setTargetFramerate(targetFramerate);
	}

	protected Background(Canvas canvas, double targetFramerate, boolean autoclear) {
		this(canvas, targetFramerate);
		autoclear = false;
	}

	public Background(double targetFramerate) {
		setTargetFramerate(targetFramerate);
	}

	public final ObjectProperty<Canvas> canvasProperty() {
		return canvas;
	}

	private void clearCanvas() {
		cachedContext.clearRect(0, 0, getWidth(), getHeight());
	}

	protected abstract void draw(GraphicsContext gc);

	protected void fillFullRect() {
		getContext().fillRect(0, 0, getWidth(), getHeight());
	}

	public final Canvas getCanvas() {
		return canvasProperty().get();
	}

	protected final GraphicsContext getContext() {
		return cachedContext;
	}

	public double getHeight() {
		return height == null ? 0 : height.get();
	}

	public final double getTargetFramerate() {
		return targetFramerateProperty().get();
	}

	public double getWidth() {
		return width == null ? 0 : width.get();
	}

	public void hide() {
		pause();
		while (runner.isAlive())
			;// Wait for draw loop to stop its last execution.
		clearCanvas();
	}

	public boolean isPaused() {
		return !run;
	}

	public void pause() {
		run = false;
	}

	protected void resized(double width, double height, double oldWidth, double oldHeight) {
	}

	public void resume() {
		show();
	}

	public final void setCanvas(final Canvas canvas) {
		canvasProperty().set(canvas);
	}

	public final void setTargetFramerate(final double targetFramerate) {
		targetFramerateProperty().set(targetFramerate);
	}

	public void show() {
		// Start the thread if it isn't already running.
		run = true;
		if (!runner.isAlive())
			runner.start();
	}

	public final DoubleProperty targetFramerateProperty() {
		return targetFramerate;
	}

}
