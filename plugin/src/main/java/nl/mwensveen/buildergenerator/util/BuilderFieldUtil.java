package nl.mwensveen.buildergenerator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.mwensveen.buildergenerator.data.BuilderField;
import nl.mwensveen.buildergenerator.data.GeneratorData;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

/**
 * The Class BuilderFieldUtil has some convenience methods for the BuilderFields.
 */
public class BuilderFieldUtil {
	private static final String COLLECTIONS_REGEX = "\\w*(Collection|List|Set)(\\s)?(<(\\w+)>)?";
	private static final Pattern COLLECTIONS_PATTERN = Pattern.compile(COLLECTIONS_REGEX);
	private static final String SET_REGEX = "\\w*(Set)(\\s)?(<(\\w+)>)?";
	private static final Pattern SET_PATTERN = Pattern.compile(SET_REGEX);
	private static final String LIST_REGEX = "\\w*(List)(\\s)?(<(\\w+)>)?";
	private static final Pattern LIST_PATTERN = Pattern.compile(LIST_REGEX);

	public static String getName(BuilderField field) {
		return field.getField().getElementName();
	}

	public static String getType(final BuilderField field) {
		try {
			return Signature.toString(field.getField().getTypeSignature());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void findAllFields(final ICompilationUnit compilationUnit, GeneratorData data) {
		try {
			IType mainType = compilationUnit.getTypes()[0];

			for (IField field : mainType.getFields()) {
				if (!Flags.isStatic(field.getFlags())) {
					data.addFields(new BuilderField(field));
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	public static String findParameterTypeForCollection(String fieldType) {
		Matcher matcher = COLLECTIONS_PATTERN.matcher(fieldType);
		String parameterType = null;
		if (matcher.matches()) {
			parameterType = matcher.group(4);
			if (parameterType == null) {
				parameterType = "Object";
			}
		}
		return parameterType;
	}

	public static String findCollectionType(String fieldType) {
		Matcher matcher = SET_PATTERN.matcher(fieldType);
		if (matcher.matches()) {
			return "Set";
		}

		matcher = LIST_PATTERN.matcher(fieldType);
		if (matcher.matches()) {
			return "List";
		}
		return "Collection";
	}

	public static boolean isCollection(String fieldType) {
		Matcher matcher = COLLECTIONS_PATTERN.matcher(fieldType);
		return matcher.matches();
	}
}
