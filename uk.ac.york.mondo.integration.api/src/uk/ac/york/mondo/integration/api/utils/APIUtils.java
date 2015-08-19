/*******************************************************************************
 * Copyright (c) 2015 University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Antonio Garcia-Dominguez - initial API and implementation
 *    Abel G�mez - Generic methods
 *******************************************************************************/
package uk.ac.york.mondo.integration.api.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;

import uk.ac.york.mondo.integration.api.File;
import uk.ac.york.mondo.integration.api.Hawk;

/**
 * Utility methods for connecting to the MONDO APIs. These use the optional
 * dependency on Apache HTTP Components.
 */
public class APIUtils {
	private APIUtils() {
	}

	public static Hawk.Client connectToHawk(String url) throws TTransportException {
		return connectTo(Hawk.Client.class, url);
	}
	
	public static <T extends TServiceClient> T connectTo(Class<T> clazz, String url) throws TTransportException {
		try {
			final HttpClient httpClient = APIUtils.createGZipAwareHttpClient();
			final THttpClient transport = new THttpClient(url, httpClient);
			Constructor<T> constructor = clazz.getDeclaredConstructor(org.apache.thrift.protocol.TProtocol.class);
			return constructor.newInstance(new TTupleProtocol(transport));
		} catch (InstantiationException 
				| IllegalAccessException 
				| IllegalArgumentException 
				| InvocationTargetException 
				| NoSuchMethodException
				| SecurityException e) {
			throw new TTransportException(e);
		}
	}

	@SuppressWarnings({ "restriction", "deprecation" })
	private static HttpClient createGZipAwareHttpClient() {
		/*
		 * Apache HttpClient 4.3 and later deprecate DefaultHttpClient in favour
		 * of HttpClientBuilder, but Hadoop 2.7.x (used by CloudATL) uses Apache
		 * HttpClient 4.2.5. Until Hadoop upgrades to HttpClient 4.3+, we'll
		 * have to keep using this deprecated API. After that, we'll be able to
		 * replace this bit of code with something like:
		 *
		 * <pre>
		 *  return HttpClientBuilder.create()
		 *      .addInterceptorFirst(new GZipRequestInterceptor())
		 *   .addInterceptorFirst(new GZipResponseInterceptor())
		 *   .build();
		 * </pre>
		 */
		final DefaultHttpClient client = new DefaultHttpClient();
		client.addRequestInterceptor(new GZipRequestInterceptor());
		client.addResponseInterceptor(new GZipResponseInterceptor());
		return client;
	}

	public static File convertJavaFileToThriftFile(java.io.File rawFile) throws FileNotFoundException, IOException {
		try (FileInputStream fIS = new FileInputStream(rawFile)) {
			FileChannel chan = fIS.getChannel();

			/* Note: this cast limits us to 2GB files - this shouldn't
			 be a problem, but if it were we could use FileChannel#map
			 and call Hawk.Client#registerModels one file at a time. */ 
			ByteBuffer buf = ByteBuffer.allocate((int) chan.size());
			chan.read(buf);
			buf.flip();

			File mmFile = new File();
			mmFile.name = rawFile.getName();
			mmFile.contents = buf;
			return mmFile;
		}
	}

}
