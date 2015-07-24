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
package uk.ac.york.mondo.integration.api.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;

import uk.ac.york.mondo.integration.api.Hawk;

/**
 * Utility methods for connecting to the MONDO APIs. These use the optional
 * dependency on Apache HTTP Components.
 */
public class APIUtils {
	private APIUtils() {
	}

	public static Hawk.Client connectToHawk(String url) throws TTransportException {
		final HttpClient httpClient = APIUtils. createGZipAwareHttpClient();
		final THttpClient transport = new THttpClient(url, httpClient);
		return new Hawk.Client(new TTupleProtocol(transport));
	}

	@SuppressWarnings("restriction")
	private static HttpClient createGZipAwareHttpClient() {
		return HttpClientBuilder.create()
				.addInterceptorFirst(new GZipRequestInterceptor())
				.addInterceptorFirst(new GZipResponseInterceptor()).build();
	}

}
