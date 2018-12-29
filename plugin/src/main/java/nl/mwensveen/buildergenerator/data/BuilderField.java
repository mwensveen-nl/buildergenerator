package nl.mwensveen.buildergenerator.data;

import org.eclipse.jdt.core.IField;

/**
 * The Class BuilderField holds the data for the fields that can be used in the generation of a workingunit.
 */
public class BuilderField {

	private final IField field;
	private boolean selected = true;

	public BuilderField(IField field) {
		super();
		this.field = field;
	}

	public boolean isSelected() {
		return selected;
	}

	public void switchSelected() {
		selected = !selected;
	}

	public IField getField() {
		return field;
	}

}
