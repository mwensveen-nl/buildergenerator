package nl.mwensveen.buildergenerator.prefs;

import java.util.List;
import nl.mwensveen.buildergenerator.data.GeneratorData;
import nl.mwensveen.buildergenerator.data.Option;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The Class BuilderPreferencesManager stores and retrieves preferences from/to the eclipse preference store.
 * This class converts between Options and preferences.
 */
public class BuilderPreferencesManager {
	private static final String PACKAGE = "nl.mwensveen.buildergenerator";

	public static GeneratorData loadGeneratorData() {
		GeneratorData data = new GeneratorData();
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(PACKAGE);
		List<Option> options = data.getOptions();

		for (Option option : options) {
			option.setSelected(prefs.getBoolean(option.getPreferenceKey(), true));
		}

		return data;
	}

	public static void saveGeneratorData(GeneratorData data) {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(PACKAGE);
		List<Option> options = data.getOptions();
		for (Option option : options) {
			prefs.putBoolean(option.getPreferenceKey(), option.isSelected());
		}
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
		}
	}
}
