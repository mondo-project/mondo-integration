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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
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
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.york.mondo.integration.api.AttributeSlot;
import uk.ac.york.mondo.integration.api.ContainerSlot;
import uk.ac.york.mondo.integration.api.Hawk;
import uk.ac.york.mondo.integration.api.ModelElement;
import uk.ac.york.mondo.integration.api.ReferenceSlot;
import uk.ac.york.mondo.integration.api.Variant;
import uk.ac.york.mondo.integration.api.Variant._Fields;
import uk.ac.york.mondo.integration.api.utils.APIUtils;

/**
 * EMF driver that reads a remote model from a Hawk index.
 */
public class HawkResourceImpl extends ResourceImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(HawkResourceImpl.class);

	/**
	 * Accumulator during the loading process, to be discarded after
	 * reconstructing the model. Keeps track of IDs, positions,
	 * {@link ModelElement}s and {@link EObject}s.
	 */
	private static class LoaderState {
		private final Registry packageRegistry;
		private final Map<String, EObject> nodeIdToEObjectMap = new HashMap<>();
		private final List<EObject> allEObjects = new ArrayList<>();
		private final Map<ModelElement, EObject> meToEObject = new IdentityHashMap<>();

	private String lastTypename, lastMetamodelURI;

		public LoaderState(Registry registry) {
			this.packageRegistry = registry;
	}

		public EObject get(int position) {
			return allEObjects.get(position);
	}

		public EObject get(String id) {
			return nodeIdToEObjectMap.get(id);
		}

		public EObject get(ModelElement me) {
			return meToEObject.get(me);
		}

		public EClass getEClass(ModelElement me) throws IOException {
		final EPackage pkg = packageRegistry.getEPackage(me.metamodelUri);
		if (pkg == null) {
			throw new IOException(String.format(
					"Could not find EPackage with URI '%s' in the registry %s",
					me.metamodelUri, packageRegistry));
		}

		final EClassifier eClassifier = pkg.getEClassifier(me.typeName);
		if (!(eClassifier instanceof EClass)) {
			throw new IOException(String.format(
					"Received an element of type '%s', which is not an EClass",
					eClassifier));
		}
		final EClass eClass = (EClass) eClassifier;
		return eClass;
	}

		public EFactory getEFactory(ModelElement me) {
			return packageRegistry.getEFactory(me.metamodelUri);
		}

		public EObject create(ModelElement me) throws IOException {
			if (!me.isSetTypeName()) {
				me.setTypeName(lastTypename);
			}
			if (!me.isSetMetamodelUri()) {
				me.setMetamodelUri(lastMetamodelURI);
			}
			lastTypename      = me.getTypeName();
			lastMetamodelURI  = me.getMetamodelUri();

			final EObject obj = getEFactory(me).create(getEClass(me));
			if (me.isSetId()) {
				nodeIdToEObjectMap.put(me.id, obj);
			}
			allEObjects.add(obj);
			meToEObject.put(me, obj);

			return obj;
		}
	}

	public HawkResourceImpl() {
	}

	public HawkResourceImpl(URI uri) {
		super(uri);
	}

	private void setStructuralFeatureFromByte(final EClass eClass,
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

	private void setStructuralFeatureFromEcoreType(final EClass eClass,
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

	private void setStructuralFeatureFromEnum(final EClass eClass,
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

	private void setStructuralFeatureFromFloat(final EClass eClass,
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
				floats.add((float)slot.value.getVDouble());
			}
			eObject.eSet(feature, floats);
		} else {
			final double d = slot.value.getVDouble();
			eObject.eSet(feature, (float) d);
		}
	}

	private void setStructuralFeatureFromInstanceClass(
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

	private EStructuralFeature setStructuralFeatureFromSlot(
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

	private void setStructuralFeatureWithExpectedType(
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

	@Override
	public void load(Map<?, ?> options) throws IOException {
		if (uri.hasAbsolutePath() && uri.scheme() != null && uri.scheme().startsWith("hawk+")) {
			// construct HawkModelDescriptor from URI on the fly
			final HawkModelDescriptor descriptor = new HawkModelDescriptor();
			final String instanceURL = uri.trimQuery().toString().replaceFirst("hawk[+]",  "");
			descriptor.setHawkURL(instanceURL);

			final List<NameValuePair> pairs = URLEncodedUtils.parse(uri.query(), Charset.forName("UTF-8"));
			for (NameValuePair pair : pairs) {
				switch (pair.getName()) {
				case "instance":
					descriptor.setHawkInstance(pair.getValue()); break;
				case "filePatterns":
					descriptor.setHawkFilePatterns(pair.getValue().split(",")); break;
				case "repository":
					descriptor.setHawkRepository(pair.getValue()); break;
				}
			}

			doLoad(descriptor);
		} else {
			super.load(options);
		}
	}

	@Override
	protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
		HawkModelDescriptor descriptor = new HawkModelDescriptor();
		descriptor.load(inputStream);
		doLoad(descriptor);
	}

	private void doLoad(HawkModelDescriptor descriptor) throws IOException {
		try {
			final Hawk.Client client = APIUtils.connectToHawk(descriptor.getHawkURL());
			final List<ModelElement> elems = client.getModel(
					descriptor.getHawkInstance(),
					descriptor.getHawkRepository(),
					Arrays.asList(descriptor.getHawkFilePatterns()));

			// Do a first pass, creating all the objects with their attributes
			// and containers and saving their graph IDs into the One Map to bind them.
			final Registry packageRegistry = getResourceSet().getPackageRegistry();
			final LoaderState state = new LoaderState(packageRegistry);
			final List<EObject> rootEObjects = createEObjects(elems, state);
			getContents().addAll(rootEObjects);

			// On the second pass, fill in the references.
			fillInReferences(elems, state);
		} catch (TException e) {
			LOGGER.error(e.getMessage(), e);
			throw new IOException(e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
	}

	private void fillInReferences(final List<ModelElement> elems, final LoaderState state) throws IOException {
		for (ModelElement me : elems) {
			final EObject sourceObj = state.get(me);

			if (me.isSetReferences()) {
				for (ReferenceSlot s : me.references) {
					final EClass eClass = state.getEClass(me);
					final EStructuralFeature feature = eClass.getEStructuralFeature(s.name);
						final EList<EObject> value = new BasicEList<>();

					if (s.isSetIds()) {
						for (String targetId : s.ids) {
							final EObject target = state.get(targetId);
							if (target == null) {
								LOGGER.warn(
										"Could not find ModelElement with id {} for feature {} of class {}, skipping",
										targetId, feature, eClass);
								continue;
							}
							value.add(target);
						}
					}
					if (s.isSetPositions()) {
						for (Integer targetPos : s.positions) {
							value.add(state.get(targetPos));
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
					fillInReferences(s.elements, state);
				}
			}
		}
	}

	private List<EObject> createEObjects(final List<ModelElement> elems, final LoaderState state) throws IOException {
		final List<EObject> eObjects = new ArrayList<>();
		for (ModelElement me : elems) {
			final EObject parent = createEObject(me, state);
			eObjects.add(parent);

			if (me.isSetContainers()) {
				for (ContainerSlot s : me.containers) {
					final EStructuralFeature sf = parent.eClass().getEStructuralFeature(s.name);
					final List<EObject> children = createEObjects(s.elements, state);
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

	private EObject createEObject(final ModelElement me, final LoaderState state) throws IOException {
		final EObject obj = state.create(me);

		if (me.isSetAttributes()) {
			final EClass eClass = state.getEClass(me);
			for (AttributeSlot s : me.attributes) {
				setStructuralFeatureFromSlot(eClass, obj, s);
			}
		}

		return obj;
	}

	@Override
	protected void doSave(OutputStream outputStream, Map<?, ?> options)
			throws IOException {
		throw new UnsupportedOperationException();
	}
}
