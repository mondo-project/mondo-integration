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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.thrift.TException;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.DynamicEStoreEObjectImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.york.mondo.integration.api.AttributeSlot;
import uk.ac.york.mondo.integration.api.ContainerSlot;
import uk.ac.york.mondo.integration.api.Hawk.Client;
import uk.ac.york.mondo.integration.api.ModelElement;
import uk.ac.york.mondo.integration.api.ReferenceSlot;
import uk.ac.york.mondo.integration.api.Variant;
import uk.ac.york.mondo.integration.api.Variant._Fields;
import uk.ac.york.mondo.integration.api.utils.APIUtils;

/**
 * EMF driver that reads a remote model from a Hawk index.
 */
public class HawkResourceImpl extends ResourceImpl {

	private final class LazyEStore implements InternalEObject.EStore {
		private String id;
		private ModelElement me;
		private EObject obj;

		public LazyEStore(String id) {
			this.id = id;
		}

		@Override
		public void unset(InternalEObject object, EStructuralFeature feature) {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] toArray(InternalEObject object, EStructuralFeature feature, T[] array) {
			final Object value = get(object, feature, -1);
			if (value instanceof Collection) {
				final Collection<?> c = (Collection<?>)value;
				if (array.length != c.size()) {
					array = (T[]) Array.newInstance(array.getClass().getComponentType(), c.size());
				}
				
				final Iterator<?> it = c.iterator();
				for (int i = 0; i < array.length && it.hasNext(); i++) {
					array[i] = (T)it.next();
				}
			}
			return array;
		}

		@Override
		public Object[] toArray(InternalEObject object, EStructuralFeature feature) {
			return toArray(object, feature, new Object[0]);
		}

		@Override
		public int size(InternalEObject object, EStructuralFeature feature) {
			final Object value = get(object, feature, -1);
			if (value instanceof Collection) {
				return ((Collection<?>)value).size();
			}
			return 0;
		}

		@Override
		public Object set(InternalEObject object, EStructuralFeature feature, int index, Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object remove(InternalEObject object, EStructuralFeature feature, int index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object move(InternalEObject object, EStructuralFeature feature, int targetIndex, int sourceIndex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int lastIndexOf(InternalEObject object, EStructuralFeature feature, Object value) {
			final Object featureValue = get(object, feature, -1);
			if (featureValue instanceof List) {
				final List<?> l = (List<?>)featureValue;
				for (int i = l.size() - 1; i > 0; i--) {
					if (l.get(i).equals(value)) {
						return i; 
					}
				}
			}
			return -1;
		}

		@Override
		public int indexOf(InternalEObject object, EStructuralFeature feature, Object value) {
			final Object featureValue = get(object, feature, -1);
			if (featureValue instanceof List) {
				final List<?> l = (List<?>)featureValue;
				for (int i = 0; i < l.size(); i++) {
					if (l.get(i).equals(value)) {
						return i; 
					}
				}
			}
			return -1;
		}

		@Override
		public boolean isSet(InternalEObject object, EStructuralFeature feature) {
			try {
				return retrieveEObject().eIsSet(feature);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				return false;
			}
		}

		@Override
		public boolean isEmpty(InternalEObject object, EStructuralFeature feature) {
			return size(object, feature) == 0;
		}

		@Override
		public int hashCode(InternalEObject object, EStructuralFeature feature) {
			final Object value = get(object, feature, -1);
			return value != null ? value.hashCode() : 0;
		}

		@Override
		public EStructuralFeature getContainingFeature(InternalEObject object) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public InternalEObject getContainer(InternalEObject object) {
			try {
				return null; //(InternalEObject) retrieveEObject().eContainer();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				return null;
			}
		}

		@Override
		public Object get(InternalEObject object, EStructuralFeature feature, int index) {
			try {
				final Object value = retrieveEObject().eGet(feature);
				if (index >= 0 && value instanceof List) {
					return ((List<?>)value).get(index);
				}
				return value;
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				return null;
			}
		}

		private EObject retrieveEObject() throws Exception {
			if (obj == null) {
				try {
					obj = nodeIdToEObjectMap.get(id);
					if (obj != null  && !(obj instanceof DynamicEStoreEObjectImpl)) {
						return obj;
					}

					ModelElement me = client.resolveProxies(descriptor.getHawkInstance(), Arrays.asList(id)).get(0);
					obj = createEObject(me, IS_NOT_PROXY);
					fillInReferences(getResourceSet().getPackageRegistry(), me, obj);
				} catch (TException|IOException e) {
					LOGGER.error(e.getMessage(), me);
					throw e;
				}
			}
			return obj;
		}

		@Override
		public EObject create(EClass eClass) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean contains(InternalEObject object, EStructuralFeature feature, Object value) {
			return indexOf(object, feature, value) != -1;
		}

		@Override
		public void clear(InternalEObject object, EStructuralFeature feature) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(InternalEObject object, EStructuralFeature feature, int index, Object value) {
			throw new UnsupportedOperationException();
		}
	}

	private static final boolean IS_NOT_PROXY = false;
	private static final boolean IS_PROXY = true;
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

	private static void setStructuralFeatureFromByte(final EClass eClass,
			final EObject eObject, AttributeSlot slot,
			final EStructuralFeature feature) throws IOException {
		// TODO not sure, need to test

		if (!slot.value.isSetVBytes() && !slot.value.isSetVByte()) {
			throw new IOException(
					String.format(
							"Expected to receive bytes for feature '%s' in type '%s', but did not",
							feature.getName(), eClass.getName()));
		} else if (feature.isMany() || feature.getEType() == EcorePackage.Literals.EBYTE_ARRAY) {
			final EList<Byte> bytes = new BasicEList<Byte>();
			if (slot.value.isSetVBytes()) {
				for (byte b : slot.value.getVBytes()) {
					bytes.add(b);
				}
			} else {
				bytes.add(slot.value.getVByte());
			}
			eObject.eSet(feature, bytes);
		} else {
			final byte b = slot.value.getVByte();
			eObject.eSet(feature, b);
		}
	}
	private static void setStructuralFeatureFromEcoreType(final EClass eClass,
			final EObject eObject, AttributeSlot slot,
			final EStructuralFeature feature, final EClassifier eType)
			throws IOException {
		if (eType == EcorePackage.Literals.EBYTE_ARRAY || eType == EcorePackage.Literals.EBYTE) {
			setStructuralFeatureFromByte(eClass, eObject, slot, feature);
		} else if (eType == EcorePackage.Literals.EFLOAT) {
			setStructuralFeatureFromFloat(eClass, eObject, slot, feature);
		} else if (eType == EcorePackage.Literals.EDOUBLE) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot,	feature, Variant._Fields.V_DOUBLES, Variant._Fields.V_DOUBLE);
		} else if (eType == EcorePackage.Literals.EINT) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot,	feature, Variant._Fields.V_INTEGERS, Variant._Fields.V_INTEGER);
		} else if (eType == EcorePackage.Literals.ELONG) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot,	feature, Variant._Fields.V_LONGS, Variant._Fields.V_LONG);
		} else if (eType == EcorePackage.Literals.ESHORT) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot,	feature, Variant._Fields.V_SHORTS, Variant._Fields.V_SHORT);
		} else if (eType == EcorePackage.Literals.ESTRING) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_STRINGS, Variant._Fields.V_STRING);
		} else if (eType == EcorePackage.Literals.EBOOLEAN) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_BOOLEANS, Variant._Fields.V_BOOLEAN);
		} else {
			throw new IOException(String.format("Unknown ECore data type '%s'", eType));
		}
	}

	private static void setStructuralFeatureFromEnum(final EClass eClass,
			final EObject eObject, AttributeSlot slot,
			final EStructuralFeature feature, final EEnum enumType)
			throws IOException {
		if (!slot.value.isSetVStrings() && !slot.value.isSetVString()) {
			throw new IOException(
				String.format(
					"Expected to receive strings for feature '%s' in type '%s' with many='%s', but did not",
					feature.getName(), eClass.getName(), feature.isMany()));
		} else if (feature.isMany()) {
			List<EEnumLiteral> literals = new ArrayList<>();
			if (slot.value.isSetVStrings()) {
				for (String s : slot.value.getVStrings()) {
					literals.add(enumType.getEEnumLiteral(s));
				}
			} else {
				literals.add(enumType.getEEnumLiteral(slot.value.getVString()));
			}
			eObject.eSet(feature, literals);
		} else {
			final EEnumLiteral enumLiteral = enumType.getEEnumLiteral(slot.value.getVString());
			eObject.eSet(feature, enumLiteral);
		}
	}

	private static void setStructuralFeatureFromFloat(final EClass eClass,
			final EObject eObject, AttributeSlot slot,
			final EStructuralFeature feature) throws IOException {
		if (!slot.value.isSetVDoubles() && !slot.value.isSetVDouble()) {
			throw new IOException(
					String.format(
							"Expected to receive doubles for feature '%s' in type '%s', but did not",
							feature.getName(), eClass.getName()));

		} else if (feature.isMany()) {
			final EList<Float> floats = new BasicEList<Float>();
			if (slot.value.isSetVDoubles()) {
				for (double d : slot.value.getVDoubles()) {
					floats.add((float) d);
				}
			} else {
				floats.add((float) slot.value.getVDouble());
			}
			eObject.eSet(feature, floats);
		} else {
			final double d = slot.value.getVDouble();
			eObject.eSet(feature, (float) d);
		}
	}

	private static void setStructuralFeatureFromInstanceClass(
			final EClass eClass, final EObject eObject, AttributeSlot slot,
			final EStructuralFeature feature, final EClassifier eType)
			throws IOException {
		// Fall back on using the Java instance classes
		final Class<?> instanceClass = eType.getInstanceClass();
		if (instanceClass == null) {
			throw new IOException(String.format(
					"Cannot set value for feature '%s' with type '%s', as "
					+ "it is not an Ecore data type and it does not have an instance class",
					feature, eType));
		}

		if (Byte.class.isAssignableFrom(instanceClass)) {
			setStructuralFeatureFromByte(eClass, eObject, slot, feature);
		} else if (Float.class.isAssignableFrom(instanceClass)) {
			setStructuralFeatureFromFloat(eClass, eObject, slot, feature);
		} else if (Double.class.isAssignableFrom(instanceClass)) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_DOUBLES, Variant._Fields.V_DOUBLE);
		} else if (Integer.class.isAssignableFrom(instanceClass)) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_INTEGERS, Variant._Fields.V_INTEGER);
		} else if (Long.class.isAssignableFrom(instanceClass)) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_LONGS, Variant._Fields.V_LONG);
		} else if (Short.class.isAssignableFrom(instanceClass)) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_SHORTS, Variant._Fields.V_SHORT);
		} else if (String.class.isAssignableFrom(instanceClass)) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_STRINGS, Variant._Fields.V_STRING);
		} else if (Boolean.class.isAssignableFrom(instanceClass)) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_BOOLEANS, Variant._Fields.V_BOOLEAN);
		} else {
			throw new IOException(String.format(
					"Unknown data type %s with isMany = false and instance class %s",
					eType.getName(), feature.isMany(), instanceClass));
		}
	}

	private static EStructuralFeature setStructuralFeatureFromSlot(
			final EClass eClass, final EObject eObject, AttributeSlot slot)
			throws IOException {
		final EStructuralFeature feature = eClass.getEStructuralFeature(slot.name);
		final EClassifier eType = feature.getEType();

		// isSet=true and many=false means that we should have exactly one value
		if (eType.eContainer() == EcorePackage.eINSTANCE) {
			setStructuralFeatureFromEcoreType(eClass, eObject, slot, feature, eType);
		} else if (eType instanceof EEnum) {
			setStructuralFeatureFromEnum(eClass, eObject, slot, feature, (EEnum)eType);
		} else {
			setStructuralFeatureFromInstanceClass(eClass, eObject, slot, feature, eType);
		}

		return feature;
	}

	private static void setStructuralFeatureWithExpectedType(
			final EClass eClass, final EObject eObject, AttributeSlot slot,
			final EStructuralFeature feature, final Variant._Fields expectedMultiType, _Fields expectedSingleType)
			throws IOException {
		if (!slot.value.isSet(expectedMultiType) && !slot.value.isSet(expectedSingleType)) {
			throw new IOException(
					String.format(
							"Expected to receive '%s' for feature '%s' in type '%s' with many='%s', but did not",
							expectedMultiType, feature.getName(), eClass.getName(),
							feature.isMany()));
		} else if (feature.isMany() && slot.value.isSet(expectedMultiType)) {
			eObject.eSet(feature, ECollections.toEList(
				(Iterable<?>) slot.value.getFieldValue(expectedMultiType)));
		} else if (feature.isMany()) {
			eObject.eSet(feature, ECollections.asEList(
				slot.value.getFieldValue(expectedSingleType)));
		} else {
			final Object elem = slot.value.getFieldValue(expectedSingleType);
			eObject.eSet(feature, elem);
		}
	}

	private HawkModelDescriptor descriptor;
	private Client client;

	// Only for the initial load (allEObjects is cleared afterwards)
	private String lastTypename, lastMetamodelURI;
	private final List<EObject> allEObjects = new ArrayList<>();

	// Only until references are filled in
	private final Map<ModelElement, EObject> meToEObject = new IdentityHashMap<>();

	// Persistent (needed to resolve references with lazy loading)
	private final Map<String, EObject> nodeIdToEObjectMap = new HashMap<>();

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

	/**
	 * Adds an EObject representing the model element with <code>targetId</code>
	 * to the indicated list. Greedy loaders may create the EObject on the fly,
	 * while lazy loaders may initially use a proxy (as they won't have a copy
	 * of the real {@link ModelElement}).
	 * 
	 * @throws IOException
	 */
	private void addEObjectWithNodeId(EClass eClass, EReference ref, EList<EObject> value, String targetId) throws IOException {
		final EObject existing = nodeIdToEObjectMap.get(targetId);
		if (existing == null) {
			if (descriptor.isLazy()) {
				final ModelElement me = new ModelElement();
				me.setId(targetId);
				me.setMetamodelUri(eClass.getEPackage().getNsURI());
				me.setTypeName(ref.getEReferenceType().getName());
				final EObject proxy = createEObject(me, IS_PROXY);
				value.add(proxy);
			} else {
				LOGGER.warn(
						"Could not find ModelElement with id {} for feature {} of class {}, skipping",
						targetId, ref, eClass);
			}
		} else {
			value.add(existing);
		}
	}

	private EObject createEObject(ModelElement me, boolean isProxy) throws IOException {
		final Registry registry = getResourceSet().getPackageRegistry();
		final EFactory factory = registry.getEFactory(me.metamodelUri);
		final EClass eClass = getEClass(me.metamodelUri, me.typeName, registry);

		EObject obj;
		if (isProxy) {
			// we might only have the supertype in some cases: fall back on a
			// DynamicEObjectImpl then
			obj = new DynamicEStoreEObjectImpl(eClass, new LazyEStore(me.id));
		} else {
			obj = factory.create(eClass);
		}

		if (me.isSetId()) {
			nodeIdToEObjectMap.put(me.id, obj);
		}

		if (me.isSetAttributes()) {
			for (AttributeSlot s : me.attributes) {
				setStructuralFeatureFromSlot(eClass, obj, s);
			}
		}

		return obj;
	}

	private List<EObject> createEObjectTree(final List<ModelElement> elems) throws IOException {
		final List<EObject> eObjects = new ArrayList<>();
		for (ModelElement me : elems) {
			if (me.isSetMetamodelUri()) {
				lastMetamodelURI = me.getMetamodelUri();
			} else {
				me.setMetamodelUri(lastMetamodelURI);
			}
			
			if (me.isSetTypeName()) {
				lastTypename = me.getTypeName();
			} else {
				me.setTypeName(lastTypename);
			}
			
			final EObject obj = createEObject(me, IS_NOT_PROXY);
			allEObjects.add(obj);
			meToEObject.put(me, obj);
			final EObject parent = obj;
			eObjects.add(parent);

			if (me.isSetContainers()) {
				for (ContainerSlot s : me.containers) {
					final EStructuralFeature sf = parent.eClass().getEStructuralFeature(s.name);
					final List<EObject> children = createEObjectTree(s.elements);
					if (sf.isMany()) {
						parent.eSet(sf, ECollections.toEList(children));
					} else if (!children.isEmpty()) {
						parent.eSet(sf, children.get(0));
					}
				}
			}
		}
		return eObjects;
	}

	private void doLoad(HawkModelDescriptor descriptor) throws IOException {
		try {
			this.descriptor = descriptor;
			this.client = APIUtils.connectToHawk(descriptor.getHawkURL());

			List<ModelElement> elems;
			if (descriptor.isLazy()) {
				elems = client.getRootElements(descriptor.getHawkInstance(),
					descriptor.getHawkRepository(),
					Arrays.asList(descriptor.getHawkFilePatterns()));
			} else {
				elems = client.getModel(descriptor.getHawkInstance(),
					descriptor.getHawkRepository(),
					Arrays.asList(descriptor.getHawkFilePatterns()));
			}

			final List<EObject> rootEObjects = createEObjectTree(elems);
			getContents().addAll(rootEObjects);
			fillInReferences(elems);

			// Position-based references are only supported for the initial load
			// (esp. greedy loading). Clear this list to avoid using up too much
			// memory.
			allEObjects.clear();
		} catch (TException e) {
			LOGGER.error(e.getMessage(), e);
			throw new IOException(e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
	}

	private void fillInReferences(final List<ModelElement> elems) throws IOException {
		final Registry packageRegistry = getResourceSet().getPackageRegistry();

		for (ModelElement me : elems) {
			final EObject sourceObj = meToEObject.remove(me);
			fillInReferences(packageRegistry, me, sourceObj);
		}
	}

	private void fillInReferences(final Registry packageRegistry, ModelElement me, final EObject sourceObj) throws IOException {
		if (me.isSetReferences()) {
			for (ReferenceSlot s : me.references) {
				final EClass eClass = getEClass(me.getMetamodelUri(), me.getTypeName(), packageRegistry);
				final EReference feature = (EReference) eClass.getEStructuralFeature(s.name);
				final EList<EObject> value = new BasicEList<>();

				// We always start from the roots, so we always set things from the containment side
				if (feature.isContainer()) {
					continue;
				}

				if (s.isSetId()) {
					addEObjectWithNodeId(eClass, feature, value, s.id);
				}
				if (s.isSetIds()) {
					for (String targetId : s.ids) {
						addEObjectWithNodeId(eClass, feature, value, targetId);
					}
				}

				// Note: using position-based references after the initial load is not supported
				if (s.isSetPosition()) {
					value.add(allEObjects.get(s.position));
				}
				if (s.isSetPositions()) {
					for (Integer targetPos : s.positions) {
						value.add(allEObjects.get(targetPos));
					}
				}

				if (feature.isMany()) {
					sourceObj.eSet(feature, value);
				} else if (!value.isEmpty()) {
					sourceObj.eSet(feature, value.get(0));
				}
			}
		}

		if (me.isSetContainers()) {
			for (ContainerSlot s : me.getContainers()) {
				fillInReferences(s.elements);
			}
		}
	}

	@Override
	protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
		HawkModelDescriptor descriptor = new HawkModelDescriptor();
		descriptor.load(inputStream);
		doLoad(descriptor);
	}

	@Override
	protected void doSave(OutputStream outputStream, Map<?, ?> options)
			throws IOException {
		throw new UnsupportedOperationException();
	}

}
