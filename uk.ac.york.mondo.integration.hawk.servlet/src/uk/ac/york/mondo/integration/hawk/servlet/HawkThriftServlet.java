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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TServlet;
import org.hawk.core.graph.IGraphDatabase;
import org.hawk.core.graph.IGraphIterable;
import org.hawk.core.graph.IGraphNode;
import org.hawk.core.graph.IGraphNodeIndex;
import org.hawk.core.graph.IGraphTransaction;
import org.hawk.core.query.InvalidQueryException;
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

import uk.ac.york.mondo.integration.api.AttributeSlot;
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
import uk.ac.york.mondo.integration.api.ReferenceSlot;
import uk.ac.york.mondo.integration.api.ScalarList;
import uk.ac.york.mondo.integration.api.ScalarOrReference;
import uk.ac.york.mondo.integration.api.UnknownQueryLanguage;
import uk.ac.york.mondo.integration.api.UnknownRepositoryType;
import uk.ac.york.mondo.integration.api.VCSAuthenticationFailed;

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
		private final HManager manager = HManager.getInstance();
		
		private HModel getRunningHawkByName(String name) throws HawkInstanceNotFound, HawkInstanceNotRunning {
			HModel model = getHawkByName(name);
			if (model.isRunning()) {
				return model;
			} else {
				throw new HawkInstanceNotRunning();
			}
		}

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
			final HModel model = getRunningHawkByName(name);

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
		public List<ScalarOrReference> query(String name, String query, String language, String scope) throws HawkInstanceNotFound, UnknownQueryLanguage, InvalidQuery, TException {
			final HModel model = getRunningHawkByName(name);
			Map<String, String> context = new HashMap<>();
			context.put(org.hawk.core.query.IQueryEngine.PROPERTY_FILECONTEXT, scope);
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
		public List<ModelElement> resolveProxies(String name, List<String> ids) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
			final HModel model = getRunningHawkByName(name);

			final IGraphDatabase graph = model.getGraph();
			final GraphWrapper gw = new GraphWrapper(graph);
			final List<ModelElement> resolved = new ArrayList<ModelElement>();
			try (IGraphTransaction tx = graph.beginTransaction()) {
				for (String id : ids) {
					try {
						ModelElementNode me = gw.getModelElementNodeById(id);
						resolved.add(encodeModelElement(me));
					} catch (Exception ex) {
						LOGGER.error(ex.getMessage(), ex);
					}
				}
			} catch (Exception ex) {
				throw new TException(ex);
			}

			return resolved;
		}

		@Override
		public void addRepository(String name, String uri, String type,
				Credentials credentials) throws HawkInstanceNotFound, HawkInstanceNotRunning, UnknownRepositoryType, VCSAuthenticationFailed {

			// TODO Integrate with centralized repositories API
			final HModel model = getRunningHawkByName(name);
			try {
				model.addVCS(uri, type, credentials.username, credentials.password);
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
		public List<String> listRepositories(String name) throws HawkInstanceNotFound, HawkInstanceNotRunning {
			final HModel model = getRunningHawkByName(name);
			return new ArrayList<String>(model.getLocations());
		}

		@Override
		public List<String> listRepositoryTypes() {
			return new ArrayList<String>(manager.getVCSTypes());
		}

		@Override
		public List<String> listFiles(String name, String repository) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
			final HModel model = getRunningHawkByName(name);

			final IGraphDatabase graph = model.getGraph();
			try (IGraphTransaction t = graph.beginTransaction()) {
				final IGraphNodeIndex fileIndex = graph.getFileIndex();
				final IGraphIterable<IGraphNode> contents = fileIndex.query("id", "*");

				final List<String> files = new ArrayList<>();
				for (IGraphNode node : contents) {
					files.add("" + node.getProperty("id"));
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
		public List<ModelElement> getModel(String name, String repositoryUri, List<String> filePath) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
			final HModel model = getRunningHawkByName(name);
			final GraphWrapper gw = new GraphWrapper(model.getGraph());

			// TODO filtering by repository
			final List<ModelElement> elems = new ArrayList<>();
			try (IGraphTransaction tx = model.getGraph().beginTransaction()) {
				for (FileNode fileNode : gw.getFileNodes(filePath)) {
					LOGGER.info("Retrieving elements from {}", filePath);

					int i = 0;
					for (ModelElementNode meNode : fileNode.getModelElements()) {
						elems.add(encodeModelElement(meNode));
						i++;
						if (i % 1000 == 0) {
							LOGGER.info("Retrieved {} model elements", i);
						}
					}
				}
				tx.success();
				return elems;
			} catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
				throw new TException(ex);
			}
		}

		@Override
		public void createInstance(String name, String adminPassword) throws TException {
			try {
				HModel.create(name, storageFolder(name), Neo4JDatabase.class.getName(), null, manager, adminPassword.toCharArray());
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
			model.stop();
		}

		private ModelElement encodeModelElement(ModelElementNode meNode) throws Exception {
			ModelElement me = new ModelElement();
			me.id = meNode.getNode().getId().toString();
			me.typeName = meNode.getTypeNode().getTypeName();
			me.metamodelUri = meNode.getTypeNode().getMetamodelName();

			final Map<String, Object> attrs = new HashMap<>();
			final Map<String, Object> refs = new HashMap<>();
			meNode.getSlotValues(attrs, refs);

			for (Map.Entry<String, Object> attr : attrs.entrySet()) {
				// to save bandwidth, we do not send unset attributes
				if (attr.getValue() == null) continue;
				me.addToAttributes(encodeAttributeSlot(attr));
			}
			for (Map.Entry<String, Object> ref : refs.entrySet()) {
				// to save bandwidth, we do not send unset references
				if (ref.getValue() == null) continue;
				me.addToReferences(encodeReferenceSlot(ref));
			}
			return me;
		}

		private ReferenceSlot encodeReferenceSlot(Entry<String, Object> slotEntry) {
			assert slotEntry.getValue() != null;

			ReferenceSlot s = new ReferenceSlot();
			s.name = slotEntry.getKey();

			final Object value = slotEntry.getValue();
			s.ids = new ArrayList<>();
			if (value instanceof Collection) {
				for (Object o : (Collection<?>)value) {
					s.ids.add(o.toString());
				}
			} else {
				s.ids.add(value.toString());
			}

			return s;
		}

		private AttributeSlot encodeAttributeSlot(Entry<String, Object> slotEntry) {
			assert slotEntry.getValue() != null;

			AttributeSlot s = new AttributeSlot();
			s.name = slotEntry.getKey();

			final Object value = slotEntry.getValue();
			s.values = new ScalarList();

			if (value instanceof Collection) {
				final Collection<?> cValue = (Collection<?>) value;
				if (!cValue.isEmpty()) {
					s.values = new ScalarList();
					encodeNonEmptyListAttributeSlot(s, value, cValue);
				} else {
					// empty list <-> isSet=true and s.values=null
					s.values = null;
				}
			} else if (value instanceof Byte) {
				s.values.setVBytes(new byte[] { (byte) value });
			} else if (value instanceof Float) {
				s.values.setVDoubles(Arrays.asList((double) value));
			} else if (value instanceof Double) {
				s.values.setVDoubles(Arrays.asList((double) value));
			} else if (value instanceof Integer) {
				s.values.setVIntegers(Arrays.asList((int) value));
			} else if (value instanceof Long) {
				s.values.setVLongs(Arrays.asList((long) value));
			} else if (value instanceof Short) {
				s.values.setVShorts(Arrays.asList((short) value));
			} else if (value instanceof String) {
				s.values.setVStrings(Arrays.asList((String) value));
			} else if (value instanceof Boolean) {
				s.values.setVBooleans(Arrays.asList((Boolean) value));
			} else {
				throw new IllegalArgumentException(String.format(
						"Unsupported value type '%s'", value.getClass()
								.getName()));
			}

			assert s.values.getFieldValue() != null : "The union field should have a value";
			return s;
		}

		@SuppressWarnings("unchecked")
		private void encodeNonEmptyListAttributeSlot(AttributeSlot s,
				final Object value, final Collection<?> cValue) {
			final Iterator<?> it = cValue.iterator();
			final Object o = it.next();
			if (o instanceof Byte) {
				final ByteBuffer bbuf = ByteBuffer.allocate(cValue.size());
				bbuf.put((byte)o);
				while (it.hasNext()) {
					bbuf.put((byte)it.next());
				}
				bbuf.flip();
				s.values.setVBytes(bbuf);
			} else if (o instanceof Float) {
				final ArrayList<Double> l = new ArrayList<Double>(cValue.size());
				l.add((double)o);
				while (it.hasNext()) {
					l.add((double)it.next());
				}
				s.values.setVDoubles(l);
			} else if (o instanceof Double) {
				s.values.setVDoubles(new ArrayList<Double>((Collection<Double>)cValue));
			} else if (o instanceof Integer) {
				s.values.setVIntegers(new ArrayList<Integer>((Collection<Integer>)cValue));
			} else if (o instanceof Long) {
				s.values.setVLongs(new ArrayList<Long>((Collection<Long>)cValue));
			} else if (o instanceof Short) {
				s.values.setVShorts(new ArrayList<Short>((Collection<Short>)cValue));
			} else if (o instanceof String) {
				s.values.setVStrings(new ArrayList<String>((Collection<String>)cValue));
			} else if (o instanceof Boolean) {
				s.values.setVBooleans(new ArrayList<Boolean>((Collection<Boolean>)cValue));
			} else if (o != null) {
				throw new IllegalArgumentException(String.format("Unsupported element type '%s'", value.getClass().getName()));
			} else {
				throw new IllegalArgumentException("Null values inside collections are not allowed");
			}
		}

		private ScalarOrReference encodeScalarOrReferenceValue(Object o) {
			final ScalarOrReference encoded = new ScalarOrReference();

			if (o instanceof Byte) {
				encoded.setVByte((byte)o);
			} else if (o instanceof Float || o instanceof Double) {
				encoded.setVDouble((double)o);
			} else if (o instanceof Integer) {
				encoded.setVInteger((int)o);
			} else if (o instanceof Long) {
				encoded.setVLong((long)o);
			} else if (o instanceof IGraphNode) {
				encoded.setVReference(((IGraphNode)o).getId().toString());
			} else if (o instanceof Short) {
				encoded.setVShort((short)o);
			} else if (o instanceof String) {
				encoded.setVString(o.toString());
			} else if (o instanceof Boolean) {
				encoded.setVBoolean((boolean)o);
			}

			return encoded;
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
