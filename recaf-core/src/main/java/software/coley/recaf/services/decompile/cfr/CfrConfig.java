package software.coley.recaf.services.decompile.cfr;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.util.getopt.OptionDecoderParam;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;
import software.coley.recaf.config.BasicConfigContainer;
import software.coley.recaf.util.StringUtil;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

/**
 * @author Matt Coley
 * @see OptionsImpl CFR options
 */
@ApplicationScoped
public class CfrConfig extends BasicConfigContainer {
	@Inject
	public CfrConfig() {
		super("decompiler-cfr" + CONFIG_SUFFIX);
		// TODO: Mirror important options from OptionsImpl
	}

	/**
	 * Fetch default value from configuration parameter.
	 *
	 * @param param
	 * 		Parameter.
	 *
	 * @return Default value as string, may be {@code null}.
	 */
	private static String getOptValue(PermittedOptionProvider.ArgumentParam<?, ?> param) {
		try {
			Field fn = PermittedOptionProvider.ArgumentParam.class.getDeclaredField("fn");
			fn.setAccessible(true);
			OptionDecoderParam<?, ?> decoder = (OptionDecoderParam<?, ?>) fn.get(param);
			return decoder.getDefaultValue();
		} catch (ReflectiveOperationException ex) {
			throw new IllegalStateException("Failed to fetch default value from Cfr parameter, did" +
					" the backend change?");
		}
	}

	/**
	 * Fetch help description from configuration parameter.
	 *
	 * @param param
	 * 		Parameter.
	 *
	 * @return Help description string, may be {@code null}.
	 */
	private static String getOptHelp(PermittedOptionProvider.ArgumentParam<?, ?> param) {
		try {
			Field fn = PermittedOptionProvider.ArgumentParam.class.getDeclaredField("help");
			fn.setAccessible(true);
			String value = (String) fn.get(param);
			if (StringUtil.isNullOrEmpty(value))
				value = "";
			return value;
		} catch (ReflectiveOperationException ex) {
			throw new IllegalStateException("Failed to fetch description from Cfr parameter, did" +
					" the backend change?");
		}
	}

	/**
	 * @return CFR compatible string map for {@link CfrDriver.Builder#withOptions(Map)}.
	 */
	public Map<String, String> toMap() {
		return Collections.emptyMap();
	}
}
