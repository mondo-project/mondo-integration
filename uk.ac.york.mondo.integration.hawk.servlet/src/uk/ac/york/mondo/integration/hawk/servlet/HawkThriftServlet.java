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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.server.TServlet;
import org.hawk.core.IModelIndexer.ShutdownRequestType;
import org.hawk.core.IVcsManager;
import org.hawk.core.graph.IGraphDatabase;
import org.hawk.core.graph.IGraphTransaction;
import org.hawk.core.query.IQueryEngine;
import org.hawk.core.query.InvalidQueryException;
import org.hawk.core.runtime.LocalHawkFactory;
import org.hawk.graph.FileNode;
import org.hawk.graph.GraphWrapper;
import org.hawk.graph.ModelElementNode;
import org.hawk.neo4j_v2.Neo4JDatabase;
import org.hawk.osgiserver.HManager;
import org.hawk.osgiserver.HModel;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.york.mondo.integration.api.Credentials;
import uk.ac.york.mondo.integration.api.DerivedAttributeSpec;
import uk.ac.york.mondo.integration.api.File;
import uk.ac.york.mondo.integration.api.Hawk;
import uk.ac.york.mondo.integration.api.HawkInstance;
import uk.ac.york.mondo.integration.api.HawkInstanceNotFound;
import uk.ac.york.mondo.integration.api.HawkInstanceNotRunning;
import uk.ac.york.mondo.integration.api.IndexedAttributeSpec;
import uk.ac.york.mondo.integration.api.InvalidDerivedAttributeSpec;
import uk.ac.york.mondo.integration.api.InvalidIndexedAttributeSpec;
import uk.ac.york.mondo.integration.api.InvalidMetamodel;
import uk.ac.york.mondo.integration.api.InvalidPollingConfiguration;
import uk.ac.york.mondo.integration.api.InvalidQuery;
import uk.ac.york.mondo.integration.api.ModelElement;
import uk.ac.york.mondo.integration.api.Repository;
import uk.ac.york.mondo.integration.api.ScalarOrReference;
import uk.ac.york.mondo.integration.api.Subscription;
import uk.ac.york.mondo.integration.api.UnknownQueryLanguage;
import uk.ac.york.mondo.integration.api.UnknownRepositoryType;
import uk.ac.york.mondo.integration.api.VCSAuthenticationFailed;
import uk.ac.york.mondo.integration.artemis.server.Server;
import uk.ac.york.mondo.integration.hawk.servlet.artemis.ArtemisProducerGraphChangeListener;

/**
 * Entry point to the Hawk model indexers. This servlet exposes a Thrift-based
 * API using the Thrift TCompactProtocol, which saves on I/O at the expense of
 * some CPU.
 *
 * The servlet also starts an embedded Apache Artemis messaging server (for
 * notifications). It listens on {@link TransportConstants#DEFAULT_HOST} and
 * port {@link TransportConstants#DEFAULT_PORT} by default, but these can be
 * changed by setting the {@link #ARTEMIS_HOST_PROPERTY} and/or
 * {@link #ARTEMIS_PORT_PROPERTY} system properties.
 *
 * @author Antonio García-Domínguez
 */
public class HawkThriftServlet extends TServlet {

	private static final String ARTEMIS_PORT_PROPERTY = "hawk.artemis.port";
	private static final String ARTEMIS_HOST_PROPERTY = "hawk.artemis.host";
	private static final Logger LOGGER = LoggerFactory.getLogger(HawkThriftServlet.class);

	private static final class Iface implements Hawk.Iface {
		private final HManager manager = HManager.getInstance();
		private Server artemisServer;

		public void setArtemisServer(Server artemisServer) {
			this.artemisServer = artemisServer;
		}

		private HModel getRunningHawkByName(String name) throws HawkInstanceNotFound, HawkInstanceNotRunning {
			HModel model = getHawkByName(name);
			if (model.isRunning()) {
				return model;
			} else {
				throw new HawkInstanceNotRunning();
			}
		}

		private HModel getHawkByName(String name) throws HawkInstanceNotFound {
			final HModel model = manager.getHawkByName(name);
			if (model == null) {
				throw new HawkInstanceNotFound();
			}
			return model;
		}

		@Override
		public void registerMetamodels(String name, List<File> metamodels) throws HawkInstanceNotFound, HawkInstanceNotRunning, InvalidMetamodel, TException {
			final HModel model = getRunningHawkByName(name);

			for (File f : metamodels) {
				try {
					// Remove path separators for now (UNIX-style / and Windows-style \)
					final String safeName = f.name.replaceAll("/", "_").replaceAll("\\\\", "_");
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
		public void unregisterMetamodel(String name, String metamodel) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
			//final HModel model = getRunningHawkByName(name);

			// TODO Unregister metamodel not implemented by Hawk yet
			throw new TException(new UnsupportedOperationException());
		}

		@Override
		public List<String> listMetamodels(String name) throws HawkInstanceNotFound, HawkInstanceNotRunning {
			final HModel model = getRunningHawkByName(name);
			return model.getRegisteredMetamodels();
		}

		@Override
		public List<String> listQueryLanguages(String name) throws HawkInstanceNotFound, HawkInstanceNotRunning {
			final HModel model = getRunningHawkByName(name);
			return new ArrayList<String>(model.getKnownQueryLanguages());
		}

		@Override
		public List<ScalarOrReference> query(String name, String query, String language, String repo, String scope) throws HawkInstanceNotFound, UnknownQueryLanguage, InvalidQuery, TException {
			final HModel model = getRunningHawkByName(name);
			Map<String, String> context = new HashMap<>();
			context.put(IQueryEngine.PROPERTY_REPOSITORYCONTEXT, repo);
			context.put(IQueryEngine.PROPERTY_FILECONTEXT, scope);
			try {
				Object ret = model.contextFullQuery(query, language, context);
				// TODO be able to return other things beyond Strings
				final ScalarOrReference v = new ScalarOrReference();
				v.setVString("" + ret);
				return Arrays.asList(v);
			} catch (NoSuchElementException ex) {
				throw new UnknownQueryLanguage();
			} catch (InvalidQueryException ex) {
				throw new InvalidQuery(ex.getMessage());
			} catch (Exception ex) {
				throw new TException(ex);
			}
		}

		@Override
		public List<ModelElement> resolveProxies(String name, List<String> ids, boolean includeAttributes, boolean includeReferences) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
			final HModel model = getRunningHawkByName(name);

			final IGraphDatabase graph = model.getGraph();
			try (IGraphTransaction tx = graph.beginTransaction()) {
				final HawkModelElementEncoder encoder = new HawkModelElementEncoder(new GraphWrapper(graph));
				encoder.setIncludeNodeIDs(true);
				encoder.setUseContainment(false);
				encoder.setIncludeAttributes(includeAttributes);
				encoder.setIncludeReferences(includeReferences);
				for (String id : ids) {
					try {
						encoder.encode(id);
					} catch (Exception ex) {
						LOGGER.error(ex.getMessage(), ex);
					}
				}
				return new ArrayList<ModelElement>(encoder.getElements());
			} catch (Exception ex) {
				throw new TException(ex);
			}
		}

		@Override
		public void addRepository(String name, Repository repo, Credentials credentials) throws HawkInstanceNotFound, HawkInstanceNotRunning, UnknownRepositoryType, VCSAuthenticationFailed {

			// TODO Integrate with centralized repositories API
			final HModel model = getRunningHawkByName(name);
			try {
				model.addVCS(repo.uri, repo.type, credentials.username, credentials.password);
			} catch (NoSuchElementException ex) {
				throw new UnknownRepositoryType();
			}
		}

		@Override
		public void removeRepository(String name, String uri) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
			final HModel model = getRunningHawkByName(name);
			try {
				model.removeRepository(uri);
			} catch (Exception ex) {
				throw new TException(ex);
			}
		}

		@Override
		public void updateRepositoryCredentials(String name, String uri, Credentials cred) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
			final HModel model = getRunningHawkByName(name);
			for (IVcsManager mgr : model.getRunningVCSManagers()) {
				if (mgr.getLocation().equals(uri)) {
					mgr.setCredentials(cred.username, cred.password);
					return;
				}
			}
		}

		@Override
		public List<Repository> listRepositories(String name) throws HawkInstanceNotFound, HawkInstanceNotRunning {
			final HModel model = getRunningHawkByName(name);
			final List<Repository> repos = new ArrayList<Repository>();
			for (IVcsManager mgr : model.getRunningVCSManagers()) {
				repos.add(new Repository(mgr.getLocation(), mgr.getType()));
			}
			return repos;
		}

		@Override
		public List<String> listRepositoryTypes() {
			return new ArrayList<String>(manager.getVCSTypes());
		}

		@Override
		public List<String> listFiles(String name, List<String> repository, List<String> filePatterns) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
			final HModel model = getRunningHawkByName(name);

			final IGraphDatabase graph = model.getGraph();
			try (IGraphTransaction t = graph.beginTransaction()) {
				final GraphWrapper gw = new GraphWrapper(graph);

				final Set<FileNode> fileNodes = gw.getFileNodes(repository, filePatterns);
				final List<String> files = new ArrayList<>(fileNodes.size());
				for (FileNode node : fileNodes) {
					files.add(node.getFileName());
				}

				return files;
			} catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
				throw new TException(ex);
			}
		}

		@Override
		public void configurePolling(String name, int base, int max) throws HawkInstanceNotFound, HawkInstanceNotRunning, InvalidPollingConfiguration {
			final HModel model = getRunningHawkByName(name);
			model.configurePolling(base, max);
		}

		@Override
		public void addDerivedAttribute(String name, DerivedAttributeSpec spec)
				throws HawkInstanceNotFound, HawkInstanceNotRunning, InvalidDerivedAttributeSpec,
				TException {
			final HModel model = getRunningHawkByName(name);

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
		public void removeDerivedAttribute(String name, DerivedAttributeSpec spec) throws HawkInstanceNotFound, HawkInstanceNotRunning {
			final HModel model = getRunningHawkByName(name);
			model.removeDerivedAttribute(
				spec.metamodelUri, spec.typeName, spec.attributeName);
		}

		@Override
		public List<DerivedAttributeSpec> listDerivedAttributes(String name) throws HawkInstanceNotFound, HawkInstanceNotRunning {
			final HModel model = getRunningHawkByName(name);

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
				throws HawkInstanceNotFound, HawkInstanceNotRunning, InvalidIndexedAttributeSpec, TException {
			final HModel model = getRunningHawkByName(name);
			try {
				model.addIndexedAttribute(spec.metamodelUri, spec.typeName, spec.attributeName);
			} catch (Exception e) {
				throw new TException(e);
			}
		}

		@Override
		public void removeIndexedAttribute(String name, IndexedAttributeSpec spec) throws HawkInstanceNotFound, HawkInstanceNotRunning {
			final HModel model = getRunningHawkByName(name);
			model.removeIndexedAttribute(spec.metamodelUri, spec.typeName, spec.attributeName);
		}

		@Override
		public List<IndexedAttributeSpec> listIndexedAttributes(String name) throws HawkInstanceNotFound, HawkInstanceNotRunning {
			final HModel model = getRunningHawkByName(name);

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
		public List<ModelElement> getModel(String name, List<String> repositories, List<String> filePath, boolean includeAttributes, boolean includeReferences, boolean includeNodeIDs) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
			return collectElements(name, repositories, filePath, CollectElements.ALL, includeAttributes, includeReferences, includeNodeIDs);
		}

		@Override
		public List<ModelElement> getRootElements(String name, List<String> repositories, List<String> filePath, boolean includeAttributes, boolean includeReferences) throws TException {
			return collectElements(name, repositories, filePath, CollectElements.ONLY_ROOTS, includeAttributes, includeReferences, true);
		}

		private List<ModelElement> collectElements(String name,
				List<String> repositories, List<String> filePath, final CollectElements collectType,
				boolean includeAttributes, boolean includeReferences, boolean includeNodeIDs)
				throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
			final HModel model = getRunningHawkByName(name);
			final GraphWrapper gw = new GraphWrapper(model.getGraph());

			// TODO filtering by repository
			try (IGraphTransaction tx = model.getGraph().beginTransaction()) {
				final HawkModelElementEncoder encoder = new HawkModelElementEncoder(new GraphWrapper(model.getGraph()));
				encoder.setIncludeAttributes(includeAttributes);
				encoder.setIncludeReferences(includeReferences);
				encoder.setIncludeNodeIDs(includeNodeIDs);
				for (FileNode fileNode : gw.getFileNodes(repositories, filePath)) {
					LOGGER.info("Retrieving elements from {}", filePath);

					if (collectType == CollectElements.ALL) {
						for (ModelElementNode meNode : fileNode.getModelElements()) {
							encoder.encode(meNode);
						}
					} else {
						encoder.setUseContainment(false);
						for (ModelElementNode meNode : fileNode.getRootModelElements()) {
							encoder.encode(meNode);
						}
					}
				}

				return new ArrayList<>(encoder.getElements());
			} catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
				throw new TException(ex);
			}
		}

		@Override
		public void createInstance(String name, String adminPassword) throws TException {
			try {
				HModel.create(new LocalHawkFactory(), name, storageFolder(name), null, Neo4JDatabase.class.getName(), null, manager, adminPassword.toCharArray());
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
			try {
				manager.delete(model, true);
			} catch (BackingStoreException e) {
				throw new TException(e.getMessage(), e);
			}
		}

		@Override
		public void startInstance(String name, String adminPassword) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);
			model.start(manager, adminPassword.toCharArray());
		}

		@Override
		public void stopInstance(String name) throws HawkInstanceNotFound, TException {
			final HModel model = getHawkByName(name);
			model.stop(ShutdownRequestType.ALWAYS);
		}

		@Override
		public Subscription watchModelChanges(String name,
				String repositoryUri, List<String> filePaths,
				boolean durableEvents)
				throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
			final HModel model = getHawkByName(name);

			// TODO keep track of existing subscriptions and save/restore/list/delete them
			// TODO allow for filtering by repository/path/change type/model element type
			try {
				final ArtemisProducerGraphChangeListener listener =
						new ArtemisProducerGraphChangeListener(model.getName(), repositoryUri, filePaths, durableEvents);
				model.addGraphChangeListener(listener);
				return new Subscription(artemisServer.getHost(), artemisServer.getPort(), listener.getQueueAddress());
			} catch (Exception e) {
				LOGGER.error("Could not register the new listener", e);
				throw new TException(e);
			}
		}

		private java.io.File storageFolder(String instanceName) throws IOException {
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

	private static enum CollectElements { ALL, ONLY_ROOTS; }
	private static final long serialVersionUID = 1L;
	private final Iface implementation;

	private Server artemis;

	public HawkThriftServlet() throws Exception {
		/*
		 * We use this double-constructor pattern so we can capture the inner Iface and
		 * tell it the server and port Artemis is listening at later on.
		 */
		this(new Iface());
	}

	private HawkThriftServlet(Iface iface) {
		super(new Hawk.Processor<Hawk.Iface>(iface), new TTupleProtocol.Factory());
		this.implementation = iface;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		String artemisHost = System.getProperty(ARTEMIS_HOST_PROPERTY);
		if (artemisHost == null) {
			artemisHost = TransportConstants.DEFAULT_HOST;
		}

		String sArtemisPort = System.getProperty(ARTEMIS_PORT_PROPERTY);
		int artemisPort;
		if (sArtemisPort == null) {
			artemisPort = TransportConstants.DEFAULT_PORT;
		} else {
			artemisPort = Integer.valueOf(sArtemisPort);
		}

		artemis = new Server(artemisHost, artemisPort);
		try {
			artemis.start();
			implementation.setArtemisServer(artemis);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		try {
			artemis.stop();
			artemis = null;
		} catch (Exception e) {
			LOGGER.error("Failed to stop Artemis", e);
		}
	}

}
