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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import org.apache.thrift.TException;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.DynamicEStoreEObjectImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.york.mondo.integration.api.AttributeSlot;
import uk.ac.york.mondo.integration.api.ContainerSlot;
import uk.ac.york.mondo.integration.api.Hawk.Client;
import uk.ac.york.mondo.integration.api.HawkInstanceNotFound;
import uk.ac.york.mondo.integration.api.HawkInstanceNotRunning;
import uk.ac.york.mondo.integration.api.MixedReference;
import uk.ac.york.mondo.integration.api.ModelElement;
import uk.ac.york.mondo.integration.api.ReferenceSlot;
import uk.ac.york.mondo.integration.api.utils.APIUtils;
import uk.ac.york.mondo.integration.hawk.emf.HawkModelDescriptor.LoadingMode;

/**
 * EMF driver that reads a remote model from a Hawk index.
 */
public class HawkResourceImpl extends ResourceImpl {

	/**
	 * Internal state used only while loading a tree of {@link ModelElement}s. It's
	 * kept separate so Java can reclaim the memory as soon as we're done with that
	 * tree.
	 */
	private final class TreeLoadingState {
		// Only for the initial load (allEObjects is cleared afterwards)
		public String lastTypename, lastMetamodelURI;
		public final List<EObject> allEObjects = new ArrayList<>();

		// Only until references are filled in
		public final Map<ModelElement, EObject> meToEObject = new IdentityHashMap<>();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(HawkResourceImpl.class);

	private static EClass getEClass(String metamodelUri, String typeName,
			final Registry packageRegistry) {
		final EPackage pkg = packageRegistry.getEPackage(metamodelUri);
		if (pkg == null) {
			throw new NoSuchElementException(String.format(
					"Could not find EPackage with URI '%s' in the registry %s",
					metamodelUri, packageRegistry));
		}

		final EClassifier eClassifier = pkg.getEClassifier(typeName);
		if (!(eClassifier instanceof EClass)) {
			throw new NoSuchElementException(String.format(
					"Received an element of type '%s', which is not an EClass",
					eClassifier));
		}
		final EClass eClass = (EClass) eClassifier;
		return eClass;
	}


	private HawkModelDescriptor descriptor;
	private Client client;

	private final Map<String, EObject> nodeIdToEObjectMap = new HashMap<>();
	private Map<Class<?>, Enhancer> enhancers = null;
	private LazyResolver lazyResolver;

	public HawkResourceImpl() {
	}

	public HawkResourceImpl(HawkModelDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	public HawkResourceImpl(URI uri) {
		super(uri);
	}

	@Override
	public void load(Map<?, ?> options) throws IOException {
		if (descriptor != null) {
			doLoad(descriptor);
		} else {
			super.load(options);
		}
	}

	public HawkModelDescriptor getDescriptor() {
		return descriptor;
	}

	public void doLoad(HawkModelDescriptor descriptor) throws IOException {
		try {
			this.descriptor = descriptor;
			this.client = APIUtils.connectToHawk(descriptor.getHawkURL());

			// TODO allow for multiple repositories
			final LoadingMode mode = descriptor.getLoadingMode();
			List<ModelElement> elems;
			if (mode.isGreedyElements()) {
				elems = client.getModel(descriptor.getHawkInstance(),
					Arrays.asList(descriptor.getHawkRepository()),
					Arrays.asList(descriptor.getHawkFilePatterns()), mode.isGreedyAttributes(), true, !mode.isGreedyAttributes());
			} else {
				elems = client.getRootElements(descriptor.getHawkInstance(),
						Arrays.asList(descriptor.getHawkRepository()),
						Arrays.asList(descriptor.getHawkFilePatterns()), mode.isGreedyAttributes(), true);
			}

			final TreeLoadingState state = new TreeLoadingState();
			final List<EObject> rootEObjects = createEObjectTree(elems, state);
			getContents().addAll(rootEObjects);
			fillInReferences(elems, state);
		} catch (TException e) {
			LOGGER.error(e.getMessage(), e);
			throw new IOException(e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
	}

	public EList<EObject> fetchNodes(List<String> ids)
			throws HawkInstanceNotFound, HawkInstanceNotRunning,
			TException, IOException {
		// Filter the objects that need to be retrieved
		final List<String> toBeFetched = new ArrayList<>();
		for (String id : ids) {
			if (!nodeIdToEObjectMap.containsKey(id)) {
				toBeFetched.add(id);
			}
		}
	
		// Fetch the eObjects, decode them and resolve references
		if (!toBeFetched.isEmpty()) {
			List<ModelElement> elems = client.resolveProxies(
					descriptor.getHawkInstance(), toBeFetched,
					descriptor.getLoadingMode().isGreedyAttributes(),
					true);
			final TreeLoadingState state = new TreeLoadingState();
			createEObjectTree(elems, state);
			fillInReferences(elems, state);
		}

		// Rebuild the real EList now
		final EList<EObject> finalList = new BasicEList<EObject>(ids.size());
		for (String id : ids) {
			final EObject eObject = nodeIdToEObjectMap.get(id);
			finalList.add(eObject);
		}
		return finalList;
	}

	public void fetchAttributes(InternalEObject object, final List<String> ids) throws IOException, HawkInstanceNotFound, HawkInstanceNotRunning, TException {
		final List<ModelElement> elems = client.resolveProxies(
			descriptor.getHawkInstance(), ids,
			true, false);
		if (elems.isEmpty()) {
			LOGGER.warn("While retrieving attributes, resolveProxies returned an empty list");
		} else {
			final ModelElement me = elems.get(0);
			final EFactory eFactory = getResourceSet().getPackageRegistry().getEFactory(me.getMetamodelUri());
			final EClass eClass = getEClass(
					me.getMetamodelUri(), me.getTypeName(),
					getResourceSet().getPackageRegistry());
			for (AttributeSlot s : me.attributes) {
				SlotDecodingUtils.setFromSlot(eFactory, eClass, object, s);
			}
		}
	}

	private EObject createEObject(ModelElement me) throws IOException {
		final Registry registry = getResourceSet().getPackageRegistry();
		final EClass eClass = getEClass(me.metamodelUri, me.typeName, registry);

		final EFactory factory = registry.getEFactory(me.metamodelUri);
		final LoadingMode mode = descriptor.getLoadingMode();

		EObject obj = factory.create(eClass);
		if (!mode.isGreedyAttributes() || !mode.isGreedyElements()) {
			// We need to create a proxy to implement the lazy loading bits, but
			// we need to use a subclass of the *real* implementation, or we'll
			// have all kinds of issues with some advanced metamodels (especially UML).
			if (enhancers == null) {
				enhancers = new HashMap<>();
			}

			Enhancer enh = enhancers.get(obj.getClass());
			if (enh == null) {
				enh = new Enhancer();
				CallbackHelper helper = new CallbackHelper(obj.getClass(), new Class[0]) {
					@Override
					protected Object getCallback(Method m) {
						if ("eGet".equals(m.getName())) {
							return getLazyResolver().getMethodInterceptor();
						} else {
							return NoOp.INSTANCE;
						}
					}
				};
				enh.setSuperclass(obj.getClass());

				// We need both classloaders: the classloader of the class to be
				// enhanced, and the classloader of this plugin (which includes CGLIB)
				enh.setClassLoader(new BridgeClassLoader(
					obj.getClass().getClassLoader(),
					this.getClass().getClassLoader()));

				enh.setCallbackFilter(helper);
				enh.setCallbacks(helper.getCallbacks());
				enhancers.put(obj.getClass(), enh);
			}

			obj = (EObject) enh.create();
		}

		if (me.isSetId()) {
			nodeIdToEObjectMap.put(me.id, obj);
		}

		if (me.isSetAttributes()) {
			for (AttributeSlot s : me.attributes) {
				SlotDecodingUtils.setFromSlot(factory, eClass, obj, s);
			}
		} else if (!mode.isGreedyAttributes()) {
			getLazyResolver().addLazyAttributes(me.id, obj);
		}

		return obj;
	}

	private List<EObject> createEObjectTree(final List<ModelElement> elems, final TreeLoadingState state) throws IOException {
		final List<EObject> eObjects = new ArrayList<>();
		for (ModelElement me : elems) {
			if (me.isSetMetamodelUri()) {
				state.lastMetamodelURI = me.getMetamodelUri();
			} else {
				me.setMetamodelUri(state.lastMetamodelURI);
			}
			
			if (me.isSetTypeName()) {
				state.lastTypename = me.getTypeName();
			} else {
				me.setTypeName(state.lastTypename);
			}
			
			final EObject obj = createEObject(me);
			state.allEObjects.add(obj);
			state.meToEObject.put(me, obj);
			eObjects.add(obj);

			if (me.isSetContainers()) {
				for (ContainerSlot s : me.containers) {
					final EStructuralFeature sf = obj.eClass().getEStructuralFeature(s.name);
					final List<EObject> children = createEObjectTree(s.elements, state);
					if (sf.isMany()) {
						obj.eSet(sf, ECollections.toEList(children));
					} else if (!children.isEmpty()) {
						obj.eSet(sf, children.get(0));
					}
				}
			}
		}
		return eObjects;
	}

	private void fillInReferences(final List<ModelElement> elems, TreeLoadingState state) throws IOException {
		final Registry packageRegistry = getResourceSet().getPackageRegistry();

		for (ModelElement me : elems) {
			final EObject sourceObj = state.meToEObject.remove(me);
			fillInReferences(packageRegistry, me, sourceObj, state);
		}
	}

	private void fillInReferences(final Registry packageRegistry, ModelElement me, final EObject sourceObj, final TreeLoadingState state) throws IOException {
		if (me.isSetReferences()) {
			for (ReferenceSlot s : me.references) {
				final EClass eClass = getEClass(me.getMetamodelUri(), me.getTypeName(), packageRegistry);
				final EReference feature = (EReference) eClass.getEStructuralFeature(s.name);

				// We always start from the roots, so we always set things from the containment side
				if (feature.isContainer()) {
					continue;
				}

				fillInReference(sourceObj, s, feature, state);
			}
		}

		if (me.isSetContainers()) {
			for (ContainerSlot s : me.getContainers()) {
				fillInReferences(s.elements, state);
			}
		}
	}

	private void fillInReference(final EObject sourceObj, final ReferenceSlot s, final EReference feature, final TreeLoadingState state) {
		if (!feature.isChangeable() || feature.isDerived() && !(sourceObj instanceof DynamicEStoreEObjectImpl)) {
			// we don't set unchangeable features, and we don't derived references on real objects
			return;
		}

		final boolean greedyElements = descriptor.getLoadingMode().isGreedyElements();

		// This variable will be set to a non-null value if we need to call eSet
		EList<Object> eSetValues = null;

		if (s.isSetId()) {
			if (greedyElements) {
				eSetValues = createEList(nodeIdToEObjectMap.get(s.id));
			} else {
				final EList<Object> value = new BasicEList<Object>();
				value.add(s.id);
				getLazyResolver().addLazyReferences(sourceObj, feature, value);
			}
		}
		else if (s.isSetIds()) {
			if (greedyElements) {
				eSetValues = createEList();
				for (String targetId : s.ids) {
					eSetValues.add(nodeIdToEObjectMap.get(targetId));
				}
			} else {
				final EList<Object> lazyIds = new BasicEList<>();
				lazyIds.addAll(s.ids);
				getLazyResolver().addLazyReferences(sourceObj, feature, lazyIds);
			}
		}
		else if (s.isSetPosition()) {
			eSetValues = createEList(state.allEObjects.get(s.position));
		}
		else if (s.isSetPositions()) {
			eSetValues = createEList();
			for (Integer position : s.positions) {
				eSetValues.add(state.allEObjects.get(position));
			}
		}
		else if (s.isSetMixed()) {
			final EList<Object> value = createEList();

			for (MixedReference mixed : s.mixed) {
				if (mixed.isSetId()) {
					if (greedyElements) {
						// normally, if we fetch all elements we won't have mixed ReferenceSlots,
						// but we handle it here, just in case.
						value.add(nodeIdToEObjectMap.get(mixed.getId()));
					} else {
						value.add(mixed.getId());
					}
				} else if (mixed.isSetPosition()) {
					value.add(state.allEObjects.get(mixed.getPosition()));
				} else {
					LOGGER.warn("Unknown mixed reference in {}", mixed);
				}
			}
			if (greedyElements) {
				eSetValues = value;
			} else {
				getLazyResolver().addLazyReferences(sourceObj, feature, value);
			}
		}
		else {
			LOGGER.warn("No known reference field was set in {}", s);
		}

		if (eSetValues != null) {
			if (feature.isMany()) {
				sourceObj.eSet(feature, eSetValues);
			} else if (!eSetValues.isEmpty()) {
				sourceObj.eSet(feature, eSetValues.get(0));
			}
		}
	}

	private EList<Object> createEList(final EObject... objects) {
		EList<Object> values = new BasicEList<Object>();
		values.addAll(Arrays.asList(objects));
		return values;
	}

	private LazyResolver getLazyResolver() {
		if (lazyResolver == null) {
			lazyResolver = new LazyResolver(this);
		}
		return lazyResolver;
	}

	@Override
	protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
		HawkModelDescriptor descriptor = new HawkModelDescriptor();
		descriptor.load(inputStream);
		doLoad(descriptor);
	}

	@Override
	protected void doSave(OutputStream outputStream, Map<?, ?> options) throws IOException {
		throw new UnsupportedOperationException();
	}

}
