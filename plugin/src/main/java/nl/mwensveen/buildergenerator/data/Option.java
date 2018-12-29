package nl.mwensveen.buildergenerator.data;

/**
 * The Class Option holds the data for the options that can be selected for the Generation.
 */
public class Option {
	private boolean selected;
	private final String description;
	private final String preferenceKey;

	public Option(String description, String preferenceKey) {
		this.description = description;
		this.preferenceKey = preferenceKey;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public void switchSelected() {
		selected = !selected;
	}

	public String getDescription() {
		return description;
	}

	public String getPreferenceKey() {
		return preferenceKey;
	}

}
