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
import org.hawk.core.query.IQueryEngine;
import org.hawk.core.query.InvalidQueryException;
import org.hawk.core.util.HawkConfig;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		
		private static final Logger LOGGER = LoggerFactory.getLogger(HawkThriftServlet.class);
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
				Object ret = model.contextFullQuery(query, language, context);
				// TODO convert collections into String
				return Arrays.asList(""+ ret);
			} catch (NoSuchElementException ex) {
				throw new UnknownQueryLanguage();
			} catch (InvalidQueryException ex) {
				throw new InvalidQuery(ex.getMessage());
			} catch (Exception ex) {
				throw new TException(ex);
			}
		}

		@Override
		public List<ModelElement> resolveProxies(String name, List<String> ids) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);

			List<ModelElement> resolved = new ArrayList<ModelElement>();
			for (uk.ac.york.mondo.integration.hawk.servlet.util.ModelElement me : model.resolveProxies(ids)) {
				// TODO convert me to Thrift ModelElement (wait for Kostas to implement resolveProxies)
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
		public void removeRepository(String name, String uri) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);
			try {
				model.removeRepository(uri);
			} catch (Exception ex) {
				throw new TException(ex);
			}
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
			model.configurePolling(base, max);
		}

		@Override
		public void addDerivedAttribute(String name, DerivedAttributeSpec spec)
				throws HawkInstanceNotFound, InvalidDerivedAttributeSpec,
				TException {
			final HModel model = getHawkByName(name);

			try {
				model.addDerivedAttribute(
					spec.metamodelUri, spec.typeName, spec.attributeName, spec.attributeType,
					spec.isMany, spec.isOrdered, spec.isUnique,
					spec.derivationLanguage, spec.derivationLogic);
			} catch (Exception ex) {
				throw new TException(ex);
			}
		}

		@Override
		public void removeDerivedAttribute(String name, DerivedAttributeSpec spec) throws HawkInstanceNotFound,
				TException {
			final HModel model = getHawkByName(name);
			model.removeDerivedAttribute(
				spec.metamodelUri, spec.typeName, spec.attributeName);
		}

		@Override
		public List<DerivedAttributeSpec> listDerivedAttributes(String name) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);

			final List<DerivedAttributeSpec> specs = new ArrayList<>();
			for (String sIndexedAttr : model.getDerivedAttributes()) {
				String[] parts = sIndexedAttr.split("##", 3);
				if (parts.length != 3) {
					LOGGER.warn("Expected {} to have 3 parts, but had {} instead: skipping", sIndexedAttr, parts.length);
					continue;
				}

				final DerivedAttributeSpec spec = new DerivedAttributeSpec();
				spec.metamodelUri = parts[0];
				spec.typeName = parts[1];
				spec.attributeName = parts[2];
				specs.add(spec);
			}
			return specs;
		}

		@Override
		public void addIndexedAttribute(String name, IndexedAttributeSpec spec)
				throws HawkInstanceNotFound, InvalidIndexedAttributeSpec,
				TException {
			final HModel model = getHawkByName(name);
			try {
				model.addIndexedAttribute(spec.metamodelUri, spec.typename, spec.attributename);
			} catch (Exception e) {
				throw new TException(e);
			}
		}

		@Override
		public void removeIndexedAttribute(String name, IndexedAttributeSpec spec) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);
			model.removeIndexedAttribute(spec.metamodelUri, spec.typename, spec.attributename);
		}

		@Override
		public List<IndexedAttributeSpec> listIndexedAttributes(String name) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);

			final List<IndexedAttributeSpec> specs = new ArrayList<>();
			for (String sIndexedAttr : model.getIndexedAttributes()) {
				String[] parts = sIndexedAttr.split("##", 3);
				if (parts.length != 3) {
					LOGGER.warn("Expected {} to have 3 parts, but had {} instead: skipping", sIndexedAttr, parts.length);
					continue;
				}

				final IndexedAttributeSpec spec = new IndexedAttributeSpec(parts[0], parts[1], parts[2]);
				specs.add(spec);
			}
			return specs;
		}

		@Override
		public List<ModelElement> getModel(String name, String repositoryUri, String filePath) throws HawkInstanceNotFound, TException {
			final Map<String, String> props = new HashMap<>();
			props.put(IQueryEngine.PROPERTY_FILECONTEXT, filePath);

			// TODO Change query/contextFullQuery to return model elements (waiting for Kostas)
			return Collections.emptyList();
		}

		@Override
		public List<ModelElement> getAllContents(String name) throws HawkInstanceNotFound, TException {
			// TODO Wait for Kostas to implement object retrieval through queries
			return null;
		}

		@Override
		public void createInstance(String name) throws TException {
			try {
				final HawkConfig config = new HawkConfig();
				config.setName(name);

				config.setLoc(storageFolder(name).getPath());
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
		public void removeInstance(String name) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);
			manager.delete(model, true);
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

		private java.io.File storageFolder(String instanceName) {
			java.io.File dataFile = FrameworkUtil.getBundle(HawkThriftServlet.class).getDataFile("hawk-" + instanceName);
			if (!dataFile.exists()) {
				dataFile.mkdir();
				LOGGER.info("Created storage directory for instance '{}' in '{}'", instanceName, dataFile.getPath());
			} else {
				LOGGER.info("Reused storage directory for instance '{}' in '{}'", instanceName, dataFile.getPath());
			}
			return dataFile;
		}
	}

	private static final long serialVersionUID = 1L;

	public HawkThriftServlet() {
		super(new Hawk.Processor<Hawk.Iface>(new Iface()),
				new TCompactProtocol.Factory());
	}

}
