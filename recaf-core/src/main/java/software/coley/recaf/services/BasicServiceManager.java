package software.coley.recaf.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import software.coley.recaf.Bootstrap;
import software.coley.recaf.Recaf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Basic implementation of {@link ServiceManager} by using the {@link Recaf} singleton instance.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicServiceManager implements ServiceManager {
	private static final Recaf recaf = Bootstrap.get();

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Service> getAllServices() {
		Map<String, Service> serviceMap = new HashMap<>();
		Set<Bean<Service>> beans = (Set<Bean<Service>>) (Object)
				recaf.getContainer().getBeanManager().getBeans(Service.class);
		for (Bean<Service> bean : beans) {
			Instance<Service> instance = (Instance<Service>) recaf.instance(bean.getBeanClass());
			Service serviceInstance = instance.get();
			serviceMap.put(serviceInstance.getServiceId(), serviceInstance);
		}
		return serviceMap;
	}
}
