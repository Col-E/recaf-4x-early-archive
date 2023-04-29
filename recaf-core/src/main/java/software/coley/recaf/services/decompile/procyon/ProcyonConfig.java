package software.coley.recaf.services.decompile.procyon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.recaf.config.BasicConfigContainer;
import software.coley.recaf.config.ConfigGroups;
import software.coley.recaf.services.decompile.DecompilerConfig;

/**
 * Config for {@link ProcyonDecompiler}
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class ProcyonConfig extends BasicConfigContainer implements DecompilerConfig {
	private int hash;

	@Inject
	public ProcyonConfig() {
		super(ConfigGroups.SERVICE_DECOMPILE, "decompiler-procyon" + CONFIG_SUFFIX);
	}

	@Override
	public int getConfigHash() {
		return hash;
	}

	@Override
	public void setConfigHash(int hash) {
		this.hash = hash;
	}
}
