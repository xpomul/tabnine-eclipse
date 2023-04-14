package com.tabnine.general;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.core.runtime.preferences.InstanceScope;

public final class PreferenceUtils {
	private PreferenceUtils() {
	}

	public static String[] getAdditionalArguments() {
		var additionalArgumentsString = InstanceScope.INSTANCE.getNode("com.tabnine.binary").get("additional_arguments", "");
		var result = Stream.of(additionalArgumentsString.split("\\$\\$\\$")).filter(Predicate.not(String::isBlank)).toArray(String[]::new);
		return result;
	}
	
	public static String[] getAdditionalMetadata() {
		var additionalArgumentsString = InstanceScope.INSTANCE.getNode("com.tabnine.binary").get("additional_metadata", "");
		var result = Stream.of(additionalArgumentsString.split("\\$\\$\\$")).filter(Predicate.not(String::isBlank)).toArray(String[]::new);
		
		return result;
	}
}
