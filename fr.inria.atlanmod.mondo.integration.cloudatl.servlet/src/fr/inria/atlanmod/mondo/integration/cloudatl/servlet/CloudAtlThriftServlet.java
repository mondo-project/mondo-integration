/*******************************************************************************
 * Copyright (c) 2015 Atlanmod.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Abel Gómez - initial API and implementation
 *******************************************************************************/
package fr.inria.atlanmod.mondo.integration.cloudatl.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.server.TServlet;

import uk.ac.york.mondo.integration.api.CloudATL;
import uk.ac.york.mondo.integration.api.InvalidModelSpec;
import uk.ac.york.mondo.integration.api.InvalidTransformation;
import uk.ac.york.mondo.integration.api.ModelSpec;
import uk.ac.york.mondo.integration.api.TransformationStatus;
import uk.ac.york.mondo.integration.api.TransformationTokenNotFound;

/**
 * Entry point to the Cloud-based ATL engine. This servlet exposes a
 * Thrift-based API.
 *
 * @author agomez
 *
 */
public class CloudAtlThriftServlet extends TServlet {

	/**
	 * Dummy implementation of the class controlling the CloudATL cluster
	 * 
	 * @author agomez
	 *
	 */
	private static class DummyIface implements CloudATL.Iface {

		@SuppressWarnings("unused")
		private class TransformationInformation {
			private String transformation;
			private List<ModelSpec> source;
			private List<ModelSpec> target;
			private TransformationStatus status;
		}

		/**
		 * Transient {@link Map} that mimics the behavior of a Job Registry
		 */
		private Map<String, TransformationInformation> transformations = new ConcurrentHashMap<>();

		@Override
		public String launch(String transformation, List<ModelSpec> source, List<ModelSpec> target) throws InvalidTransformation, InvalidModelSpec, TException {
			String id = UUID.randomUUID().toString();

			TransformationInformation information = new TransformationInformation();

			information.transformation = transformation;
			information.source = source;
			information.target = target;

			information.status = new TransformationStatus();
			information.status.setElapsed(0);
			information.status.setFinished(false);
			information.status.setError("");

			transformations.put(id, information);

			return id;
		}

		@Override
		public List<String> getJobs() throws TException {
			List<String> ids = new ArrayList<String>();
			ids.addAll(transformations.keySet());
			return ids;
		}

		@Override
		public TransformationStatus getStatus(String token) throws TransformationTokenNotFound, TException {
			return getTransformationInformation(token).status;
		}

		@Override
		public void kill(String token) throws TransformationTokenNotFound, TException {
			getTransformationInformation(token).status.finished = true;
		}

		/**
		 * Returns the {@link TransformationInformation}
		 * 
		 * @param token
		 *            The transformation Id
		 * @return
		 * @throws TransformationTokenNotFound
		 *             Transformation id not found
		 */
		private TransformationInformation getTransformationInformation(String token) throws TransformationTokenNotFound {
			TransformationInformation information = transformations.get(token);
			if (information != null) {
				return information;
			}
			throw new TransformationTokenNotFound(token);
		}
	}

	private static final long serialVersionUID = 1L;

	public CloudAtlThriftServlet() {
		super(new CloudATL.Processor<CloudATL.Iface>(new DummyIface()), new TTupleProtocol.Factory());
	}
}
