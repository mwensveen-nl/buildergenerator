package nl.mwensveen.buildergenerator.data;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class GeneratorData holds all data needed for the generation of a Builder.
 */
public class GeneratorData {

	public static final String FORMAT_SOURCE = "FormatSource";
	public static final String CLASS_CONSTRUCTOR = "ClassConstructor";
	public static final String GENERATE_BUILD_FACTORY_METHOD_ON_BEAN = "GenerateBuildFactoryMethodOnBean";
	public static final String GENERATE_COPY_CONSTRUCTOR = "GenerateCopyConstructor";
	public static final String GENERATE_VARARG_METHODS_FOR_COLLECTIONS = "GenerateVarargMethodsForCollections";
	public static final String GENERATE_ADD_METHODS_FOR_COLLECTIONS = "GenerateAddMethodsForCollections";
	public static final String USE_WITH_PREFIX = "UseWithPrefix";

	private final List<BuilderField> fields = new ArrayList<>();
	private final List<Option> options;
	// execute or cancel
	private boolean execute;

	public GeneratorData() {
		options = new ArrayList<>();
		options.add(new Option("Use 'with' prefix i/o 'get' for builder method names", USE_WITH_PREFIX));
		options.add(new Option("Generate 'Add' methods for Collection fields", GENERATE_ADD_METHODS_FOR_COLLECTIONS));
		options.add(new Option("Generate vararg methods for Collection fields", GENERATE_VARARG_METHODS_FOR_COLLECTIONS));
		options.add(new Option("Generate copy constructor in builder", GENERATE_COPY_CONSTRUCTOR));
		options.add(new Option("Generate builder factory method on bean", GENERATE_BUILD_FACTORY_METHOD_ON_BEAN));
		options.add(new Option("Generate class constructor with builder on bean", CLASS_CONSTRUCTOR));
		options.add(new Option("Format source (entire file)", FORMAT_SOURCE));
	}

	public void setOption(String key, boolean selected) {
		for (Option option : options) {
			if (option.getPreferenceKey().equals(key)) {
				option.setSelected(selected);
			}
		}
	}

	public boolean isOption(String key) {
		for (Option option : options) {
			if (option.getPreferenceKey().equals(key)) {
				return option.isSelected();
			}
		}
		return false;
	}

	public void addFields(BuilderField field) {
		fields.add(field);
	}

	public boolean isExecute() {
		return execute;
	}

	public void setExecute(boolean execute) {
		this.execute = execute;
	}

	public List<BuilderField> getFields() {
		return fields;
	}

	public List<BuilderField> getSelectedFields() {
		List<BuilderField> selected = new ArrayList<>();
		for (BuilderField fieldSelection : fields) {
			if (fieldSelection.isSelected()) {
				selected.add(fieldSelection);
			}
		}
		return selected;
	}

	public List<Option> getOptions() {
		return options;
	}
}
