package software.coley.recaf.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.*;
import jakarta.inject.Inject;
import software.coley.recaf.workspace.model.Workspace;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension to force creation of {@link EagerInitialization} annotated beans without the need to
 * {@link Inject} and reference them externally.
 *
 * @author Matt Coley
 * @see WorkspaceBeanContext#onWorkspaceOpened(Workspace) for instantiation of {@link WorkspaceScoped} eager beans.
 */
public class EagerInitializationExtension implements Extension {
	private static final EagerInitializationExtension INSTANCE = new EagerInitializationExtension();
	private static final List<Bean<?>> applicationScopedEagerBeans = new ArrayList<>();
	private static final List<Bean<?>> workspaceScopedEagerBeans = new ArrayList<>();
	private static BeanManager beanManager;

	private EagerInitializationExtension() {
	}

	/**
	 * @return Extension singleton.
	 */
	public static EagerInitializationExtension getInstance() {
		return INSTANCE;
	}

	/**
	 * @return Application scoped {@link EagerInitialization} beans.
	 */
	public static List<Bean<?>> getApplicationScopedEagerBeans() {
		return applicationScopedEagerBeans;
	}

	/**
	 * @return Workspace scoped {@link EagerInitialization} beans.
	 */
	public static List<Bean<?>> getWorkspaceScopedEagerBeans() {
		return workspaceScopedEagerBeans;
	}

	/**
	 * Called when a bean is discovered and processed.
	 * We will record eager beans here so we can initialize them later.
	 *
	 * @param event
	 * 		CDI bean process event.
	 */
	public void onProcessBean(@Observes ProcessBean<?> event) {
		Annotated annotated = event.getAnnotated();
		if (annotated.isAnnotationPresent(EagerInitialization.class)) {
			if (annotated.isAnnotationPresent(ApplicationScoped.class))
				applicationScopedEagerBeans.add(event.getBean());
			else if (annotated.isAnnotationPresent(WorkspaceScoped.class))
				workspaceScopedEagerBeans.add(event.getBean());
		}
	}

	/**
	 * Called when the CDI container deploys. Here we can initialize any {@link ApplicationScoped} beans since
	 * their lifecycle is the duration of the application.
	 *
	 * @param event
	 * 		CDI deploy event.
	 * @param beanManager
	 * 		CDI bean manager.
	 */
	public void onDeploy(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
		EagerInitializationExtension.beanManager = beanManager;
		for (Bean<?> bean : applicationScopedEagerBeans)
			create(bean);
	}

	public static void create(Bean<?> bean) {
		// NOTE: Calling toString() triggers the bean's proxy to the real implementation to initialize it.
		beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean)).toString();
	}
}
