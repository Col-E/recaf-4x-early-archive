package software.coley.recaf.plugin;

/**
 * Object containing necessary information about a plugin.
 *
 * @author xDark
 * @see PluginInformation Annotation containing this information applied to {@link Plugin} implementations.
 */
public final class PluginInfo {
	private final String name;
	private final String version;
	private final String author;
	private final String description;

	/**
	 * @param name
	 * 		Name of the plugin.
	 * @param version
	 * 		Plugin version.
	 * @param author
	 * 		Author of the plugin.
	 * @param description
	 * 		Plugin description.
	 */
	public PluginInfo(String name, String version, String author, String description) {
		this.name = name;
		this.version = version;
		this.author = author;
		this.description = description;
	}

	/**
	 * @return Plugin name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Plugin version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return Author of the plugin.
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @return Plugin description.
	 */
	public String getDescription() {
		return description;
	}
}
