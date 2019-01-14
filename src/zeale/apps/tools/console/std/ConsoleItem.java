package zeale.apps.tools.console.std;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * <p>
 * If any of the properties of this class has an "empty" value, use that instead
 * of <code>null</code>. A few of the properties in this class may throw
 * exceptions if <code>null</code> isn't permitted, and they are set to
 * <code>null</code>. (Of course, these exceptions should get caught during
 * testing, since, as soon as the properties are set to <code>null</code>
 * values, the exception will be thrown.)
 * <p>
 * For example, the {@link #weight} property should be set to
 * {@link FontWeight#NORMAL}, rather than <code>null</code>. If a property
 * doesn't have a "normal" or "empty" value, <code>null</code> should be
 * permitted. More specific documentation, (on each individual property), will
 * be provided later.
 *
 * @author Zeale
 *
 */
public class ConsoleItem {

	public static final FontPosture convertToValidPosture(boolean italicized) {
		return italicized ? FontPosture.ITALIC : FontPosture.REGULAR;
	}

	public static final FontWeight convertToValidWeight(boolean bold) {
		return bold ? FontWeight.BOLD : FontWeight.NORMAL;
	}

	private final StringProperty text = new SimpleStringProperty();
	private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.BLACK),
			stroke = new SimpleObjectProperty<>(null);
	private final DoubleProperty strokeWidth = new SimpleDoubleProperty(1);

	private final BooleanProperty disable = new SimpleBooleanProperty(), isObfuscated = new SimpleBooleanProperty();
	private final DoubleProperty opacity = new SimpleDoubleProperty(1);

	private final StringProperty fontFamily = new SimpleStringProperty();
	private final DoubleProperty fontSize = new SimpleDoubleProperty(-1);

	private final ObjectProperty<FontWeight> weight = new SimpleObjectProperty<>(FontWeight.NORMAL);
	private final ObjectProperty<FontPosture> posture = new SimpleObjectProperty<>(FontPosture.REGULAR);
	private final BooleanProperty strikethrough = new SimpleBooleanProperty(), underline = new SimpleBooleanProperty();

	public ConsoleItem() {
	}

	public final ObjectProperty<Color> colorProperty() {
		return color;
	}

	public final BooleanProperty disableProperty() {
		return disable;
	}

	public final StringProperty fontFamilyProperty() {
		return fontFamily;
	}

	public final DoubleProperty fontSizeProperty() {
		return fontSize;
	}

	public final Color getColor() {
		return colorProperty().get();
	}

	public final String getFontFamily() {
		return fontFamilyProperty().get();
	}

	public final double getFontSize() {
		return fontSizeProperty().get();
	}

	public Text getNode() {
		// TODO Deal with obfuscation.
		Text text = new Text();
		text.textProperty().bind(this.text);
		text.fillProperty().bind(color);
		text.strokeProperty().bind(stroke);
		text.strokeWidthProperty().bind(strokeWidth);
		text.disableProperty().bind(disable);
		text.opacityProperty().bind(opacity);
		text.fontProperty()
				.bind(Bindings.createObjectBinding(
						() -> Font.font(fontFamily.get(), weight.get(), posture.get(), fontSize.get()), fontFamily,
						weight, posture, fontSize));
		text.strikethroughProperty().bind(strikethrough);
		text.underlineProperty().bind(underline);
		return text;
	}

	public final double getOpacity() {
		return opacityProperty().get();
	}

	public final FontPosture getPosture() {
		return postureProperty().get();
	}

	public final Color getStroke() {
		return strokeProperty().get();
	}

	public final double getStrokeWidth() {
		return strokeWidthProperty().get();
	}

	public final String getText() {
		return textProperty().get();
	}

	public final FontWeight getWeight() {
		return weightProperty().get();
	}

	public final boolean isDisable() {
		return disableProperty().get();
	}

	public final boolean isIsObfuscated() {
		return isObfuscatedProperty().get();
	}

	public final BooleanProperty isObfuscatedProperty() {
		return isObfuscated;
	}

	public final boolean isStrikethrough() {
		return strikethroughProperty().get();
	}

	public final boolean isUnderline() {
		return underlineProperty().get();
	}

	public final DoubleProperty opacityProperty() {
		return opacity;
	}

	public final ObjectProperty<FontPosture> postureProperty() {
		return posture;
	}

	public final ConsoleItem setColor(final Color color) {
		colorProperty().set(color);
		return this;
	}

	public final ConsoleItem setDisable(final boolean disabled) {
		disableProperty().set(disabled);
		return this;
	}

	public final ConsoleItem setFontFamily(final String fontFamily) {
		fontFamilyProperty().set(fontFamily);
		return this;
	}

	public final ConsoleItem setFontSize(final double fontSize) {
		fontSizeProperty().set(fontSize);
		return this;
	}

	public final ConsoleItem setIsObfuscated(final boolean isObfuscated) {
		isObfuscatedProperty().set(isObfuscated);
		return this;
	}

	public final ConsoleItem setOpacity(final double opacity) {
		opacityProperty().set(opacity);
		return this;
	}

	public final ConsoleItem setPosture(final FontPosture posture) {
		postureProperty().set(posture);
		return this;
	}

	public final ConsoleItem setStrikethrough(final boolean strikethrough) {
		strikethroughProperty().set(strikethrough);
		return this;
	}

	public final ConsoleItem setStroke(final Color stroke) {
		strokeProperty().set(stroke);
		return this;
	}

	public final ConsoleItem setStrokeWidth(final double strokeWidth) {
		strokeWidthProperty().set(strokeWidth);
		return this;
	}

	public final ConsoleItem setText(final String text) {
		textProperty().set(text);
		return this;
	}

	public final ConsoleItem setUnderline(final boolean underline) {
		underlineProperty().set(underline);
		return this;
	}

	public final ConsoleItem setWeight(final FontWeight weight) {
		weightProperty().set(weight);
		return this;
	}

	public final BooleanProperty strikethroughProperty() {
		return strikethrough;
	}

	public final ObjectProperty<Color> strokeProperty() {
		return stroke;
	}

	public final DoubleProperty strokeWidthProperty() {
		return strokeWidth;
	}

	public final StringProperty textProperty() {
		return text;
	}

	@Override
	public String toString() {
		return "ConsoleItem [getColor()=" + getColor() + ", getFontFamily()=" + getFontFamily() + ", getFontSize()="
				+ getFontSize() + ", getNode()=" + getNode() + ", getOpacity()=" + getOpacity() + ", getPosture()="
				+ getPosture() + ", getStroke()=" + getStroke() + ", getStrokeWidth()=" + getStrokeWidth()
				+ ", getText()=" + getText() + ", getWeight()=" + getWeight() + ", isDisable()=" + isDisable()
				+ ", isIsObfuscated()=" + isIsObfuscated() + ", isStrikethrough()=" + isStrikethrough()
				+ ", isUnderline()=" + isUnderline() + "]";
	}

	public final BooleanProperty underlineProperty() {
		return underline;
	}

	public final ObjectProperty<FontWeight> weightProperty() {
		return weight;
	}

}
