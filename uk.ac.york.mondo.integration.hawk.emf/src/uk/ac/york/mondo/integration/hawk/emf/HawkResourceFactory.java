package uk.ac.york.mondo.integration.hawk.emf;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory;

public class HawkResourceFactory implements Factory {

	public HawkResourceFactory() {
		// TODO get credentials from Eclipse preferences?
	}

	@Override
	public Resource createResource(URI uri) {
		return new HawkResourceImpl(uri);
	}

}
