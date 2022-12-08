package software.coley.recaf.info.properties.builtin;

import software.coley.recaf.info.properties.BasicProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Built in property to cache decompilation results for {@link software.coley.recaf.info.ClassInfo} instances,
 * reducing wasted duplicate work on decompiling the same code over and over again.
 *
 * @author Matt Coley
 */
public class CachedDecompileProperty extends BasicProperty<CachedDecompileProperty.Cache> {
	public static final String KEY = "cached-decompiled-code";

	/**
	 * New empty cache.
	 */
	public CachedDecompileProperty() {
		super(KEY, new Cache());
	}

	// TODO: Accessors using decompiler interface once that gets implemented

	/**
	 * Basic cache for decompiled code.
	 */
	public static class Cache {
		private final Map<String, String> implToCode = new HashMap<>();

		/**
		 * @param decompilerId
		 * 		Unique ID of decompiler.
		 *
		 * @return Decompiled code output for decompiler.
		 */
		public String get(String decompilerId) {
			return implToCode.get(decompilerId);
		}

		/**
		 * @param decompilerId
		 * 		Unique ID of decompiler.
		 * @param decompiled
		 * 		Decompiled code output for decompiler.
		 */
		public void save(String decompilerId, String decompiled) {
			implToCode.put(decompilerId, decompiled);
		}
	}
}
