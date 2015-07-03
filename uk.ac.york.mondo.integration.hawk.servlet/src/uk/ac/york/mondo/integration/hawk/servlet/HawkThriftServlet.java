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
package uk.ac.york.mondo.integration.hawk.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TServlet;
import org.eclipse.core.runtime.CoreException;
import org.hawk.core.graph.IGraphIterable;
import org.hawk.core.graph.IGraphNode;
import org.hawk.core.graph.IGraphNodeIndex;
import org.hawk.core.util.HawkConfig;

import uk.ac.york.mondo.integration.api.Credentials;
import uk.ac.york.mondo.integration.api.DerivedAttributeSpec;
import uk.ac.york.mondo.integration.api.File;
import uk.ac.york.mondo.integration.api.Hawk;
import uk.ac.york.mondo.integration.api.HawkInstance;
import uk.ac.york.mondo.integration.api.HawkInstanceNotFound;
import uk.ac.york.mondo.integration.api.IndexedAttributeSpec;
import uk.ac.york.mondo.integration.api.InvalidDerivedAttributeSpec;
import uk.ac.york.mondo.integration.api.InvalidIndexedAttributeSpec;
import uk.ac.york.mondo.integration.api.InvalidMetamodel;
import uk.ac.york.mondo.integration.api.InvalidPollingConfiguration;
import uk.ac.york.mondo.integration.api.InvalidQuery;
import uk.ac.york.mondo.integration.api.ModelElement;
import uk.ac.york.mondo.integration.api.ModelElementChange;
import uk.ac.york.mondo.integration.api.ModelElementChangeType;
import uk.ac.york.mondo.integration.api.UnknownQueryLanguage;
import uk.ac.york.mondo.integration.api.UnknownRepositoryType;
import uk.ac.york.mondo.integration.api.VCSAuthenticationFailed;
import uk.ac.york.mondo.integration.hawk.servlet.util.HManager;
import uk.ac.york.mondo.integration.hawk.servlet.util.HModel;

/**
 * Entry point to the Hawk model indexers. This servlet exposes a Thrift-based
 * API using the Thrift TCompactProtocol, which saves on I/O at the expense of
 * some CPU.
 *
 * @author Antonio García-Domínguez
 */
public class HawkThriftServlet extends TServlet {

	private static final class Iface implements Hawk.Iface {
		
		private final HManager manager = Activator.getInstance().getHawkManager();
		
		private HModel getHawkByName(String name) throws HawkInstanceNotFound {
			HModel model;
			try {
				model = manager.getHawkByName(name);
			} catch (NoSuchElementException ex) {
				throw new HawkInstanceNotFound();
			}
			return model;
		}

		@Override
		public void registerMetamodels(String name, List<File> metamodels) throws HawkInstanceNotFound, InvalidMetamodel, TException {
			final HModel model = getHawkByName(name);

			for (File f : metamodels) {
				try {
					// Remove path separators for now (UNIX-style / and Windows-style \)
					final String safeName = f.name.replaceAll("/", "_").replaceAll("\\", "_");
					final java.io.File dataFile = Activator.getInstance().writeToDataFile(safeName, f.contents);
					// TODO No way to report a bad file?
					model.registerMeta(dataFile);
				} catch (FileNotFoundException ex) {
					throw new TException(ex);
				} catch (IOException ex) {
					throw new TException(ex);
				}
			}
		}

		@Override
		public void unregisterMetamodel(String name, String metamodel) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);

			// TODO Unregister metamodel not implemented by Hawk UI yet?
			throw new TException(new UnsupportedOperationException());
		}

		@Override
		public List<String> listMetamodels(String name)
				throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);
			return model.getRegisteredMetamodels();
		}

		@Override
		public List<String> query(String name, String query, String language, String scope) throws HawkInstanceNotFound, UnknownQueryLanguage, InvalidQuery, TException {
			final HModel model = getHawkByName(name);
			Map<String, String> context = new HashMap<>();
			context.put(org.hawk.core.query.IQueryEngine.PROPERTY_FILECONTEXT, scope);
			try {
				// TODO How to detect invalid queries?
				return Arrays.asList(model.contextFullQuery(query, language, context));
			} catch (NoSuchElementException ex) {
				throw new UnknownQueryLanguage();
			}
		}

		@Override
		public List<ModelElement> resolveProxies(String name, List<ModelElement> proxies) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);
			
			List<ModelElement> resolved = new ArrayList<ModelElement>();
			for (ModelElement proxy : proxies) {
				final IGraphNode node = model.getGraph().getNodeById(proxy.id);
				// TODO How to recover ModelElement from proxy?
			}
			
			return resolved;
		}

		@Override
		public void addRepository(String name, String uri, String type,
				Credentials credentials) throws HawkInstanceNotFound,
				UnknownRepositoryType, VCSAuthenticationFailed, TException {

			// TODO Integrate with centralized repositories API
			final HModel model = getHawkByName(name);
			try {
				model.addEncryptedVCS(uri, type, credentials.username, credentials.password);
			} catch (NoSuchElementException ex) {
				throw new UnknownRepositoryType();
			} catch (CoreException ex) {
				throw new TException(ex);
			} catch (Exception ex) {
				// TODO Need more detailed exceptions from IVcsManager.run
				throw new VCSAuthenticationFailed();
			}
		}

		@Override
		public void deleteRepository(String name, String uri) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);

			// TODO Delete repository not implemented by Hawk UI yet?
			throw new TException(new UnsupportedOperationException());
		}

		@Override
		public List<String> listRepositories(String name) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);

			return new ArrayList<String>(model.getLocations());
		}

		@Override
		public List<String> listFiles(String name, String repository) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);

			final IGraphNodeIndex fileIndex = model.getGraph().getFileIndex();
			final IGraphIterable<IGraphNode> contents = fileIndex.get("id", "*");

			final List<String> files = new ArrayList<>();
			for (IGraphNode node : contents) {
				files.add("" + node.getProperty("id"));
			}
			return files;
		}

		@Override
		public void configurePolling(String name, int base, int max) throws HawkInstanceNotFound, InvalidPollingConfiguration, TException {
			final HModel model = getHawkByName(name);

			// TODO Hawk has a fixed polling interval now: need API to change that
			throw new TException(new UnsupportedOperationException());
		}

		@Override
		public void addDerivedAttribute(String name, DerivedAttributeSpec spec)
				throws HawkInstanceNotFound, InvalidDerivedAttributeSpec,
				TException {
			final HModel model = getHawkByName(name);

			// TODO No syntax validation by Hawk?
			model.addDerivedAttribute(
					spec.metamodelUri, spec.typeName, spec.attributeName, spec.attributeType,
					spec.isMany, spec.isOrdered, spec.isUnique,
					spec.derivationLanguage, spec.derivationLogic);
		}

		@Override
		public void removeDerivedAttribute(String name,
				DerivedAttributeSpec spec) throws HawkInstanceNotFound,
				TException {
			final HModel model = getHawkByName(name);

			// TODO Removing derived attributes not available in Hawk UI yet?
			throw new TException(new UnsupportedOperationException());
		}

		@Override
		public List<String> listDerivedAttributes(String name)
				throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);
			// TODO Change Hawk API to use DerivedAttribute POJO in add/remove/list instead of raw strings
			return new ArrayList<String>(model.getDerivedAttributes());
		}

		@Override
		public void addIndexedAttribute(String name, IndexedAttributeSpec spec)
				throws HawkInstanceNotFound, InvalidIndexedAttributeSpec,
				TException {
			final HModel model = getHawkByName(name);
			// TODO No exceptions from Hawk at all?
			model.addIndexedAttribute(spec.metamodelUri, spec.typename, spec.attributename);
		}

		@Override
		public void removeIndexedAttribute(String name,
				IndexedAttributeSpec spec) throws HawkInstanceNotFound,
				TException {
			final HModel model = getHawkByName(name);
			// TODO Removing indexed attributes not available in Hawk UI yet?
			throw new TException(new UnsupportedOperationException());
		}

		@Override
		public List<String> listIndexedAttributes(String name)
				throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);
			// TODO Change Hawk API to use IndexedAttribute POJO instead of raw Strings in add/del/list
			return new ArrayList<String>(model.getIndexedAttributes());
		}

		@Override
		public List<ModelElement> getModel(String name, String repositoryUri, String filePath) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);
			// TODO Ask Kostas for help with this (extra method for HModel would be great)
			return Collections.emptyList();
		}

		//@Override
		public List<ModelElementChange> watchModelChanges(String name,
				String repositoryUri, String filePath,
				ModelElementChangeType changeType, String modelElementType)
				throws TException {
			/** 
			 * TODO Need to think about how to transform stream methods into Thrift + Artemis:
			 * maybe generate notification struct + Artemis wrappers (skipping the operation)?
			 */
			return null;
		}

		@Override
		public void createInstance(String name) throws TException {
			try {
				final HawkConfig config = new HawkConfig();
				config.setName(name);
				manager.addHawk(new HModel(manager, config, false));
			} catch (Exception ex) {
				throw new TException(ex);
			}
		}

		@Override
		public List<HawkInstance> listInstances() throws TException {
			final List<HawkInstance> instances = new ArrayList<>();
			for (HModel m : manager.getHawks()) {
				final HawkInstance instance = new HawkInstance();
				instance.name = m.getName();
				instance.running = m.isRunning();
				instances.add(instance);
			}
			return instances;
		}

		@Override
		public void deleteInstance(String name) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);
			model.delete();
		}

		@Override
		public void startInstance(String name) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);
			model.start(manager);
		}

		@Override
		public void stopInstance(String name) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);
			model.stop();
		}
	}

	private static final long serialVersionUID = 1L;

	public HawkThriftServlet() {
		super(new Hawk.Processor<Hawk.Iface>(new Iface()),
				new TCompactProtocol.Factory());
	}

}
