/*******************************************************************************
 * Copyright (c) 2015 University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Antonio Garcia-Dominguez - initial API and implementation
 *******************************************************************************/
package uk.ac.york.mondo.integration.hawk.emf;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory;

import uk.ac.york.mondo.integration.api.SubscriptionDurability;
import uk.ac.york.mondo.integration.api.utils.APIUtils.ThriftProtocol;
import uk.ac.york.mondo.integration.hawk.emf.HawkModelDescriptor.LoadingMode;

public class HawkResourceFactoryImpl implements Factory {

	public HawkResourceFactoryImpl() {
		// TODO get credentials from Eclipse preferences?
	}

	@Override
	public Resource createResource(URI uri) {
		if (isHawkURL(uri)) {
			final HawkModelDescriptor descriptor = parseHawkURL(uri);
			return new HawkResourceImpl(descriptor);
		} else {
			return new HawkResourceImpl(uri);
		}
	}

	private HawkModelDescriptor parseHawkURL(URI uri) {
		// construct HawkModelDescriptor from URI on the fly
		final HawkModelDescriptor descriptor = new HawkModelDescriptor();
		final String instanceURL = uri.trimQuery().toString().replaceFirst("hawk[+]",  "");
		descriptor.setHawkURL(instanceURL);

		final List<NameValuePair> pairs = URLEncodedUtils.parse(uri.query(), Charset.forName("UTF-8"));
		for (NameValuePair pair : pairs) {
			final String v = pair.getValue();
			switch (pair.getName()) {
			case "instance":
				descriptor.setHawkInstance(v); break;
			case "filePatterns":
				descriptor.setHawkFilePatterns(v.split(",")); break;
			case "tprotocol":
				descriptor.setThriftProtocol(ThriftProtocol.valueOf(v.toUpperCase())); break;
			case "repository":
				descriptor.setHawkRepository(v); break;
			case "loadingMode":
				descriptor.setLoadingMode(LoadingMode.valueOf(v.toUpperCase())); break;
			case "subscribe":
				descriptor.setSubscribed(Boolean.valueOf(v)); break;
			case "durability":
				descriptor.setSubscriptionDurability(SubscriptionDurability.valueOf(v.toUpperCase())); break;
			case "clientID":
				descriptor.setSubscriptionClientID(v); break;
			}
		}
		return descriptor;
	}

	private boolean isHawkURL(URI uri) {
		return uri.hasAbsolutePath() && uri.scheme() != null && uri.scheme().startsWith("hawk+");
	}

}
