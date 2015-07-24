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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import uk.ac.york.mondo.integration.api.Hawk;
import uk.ac.york.mondo.integration.api.ModelElement;
import uk.ac.york.mondo.integration.api.ReferenceSlot;
import uk.ac.york.mondo.integration.api.ScalarList._Fields;
import uk.ac.york.mondo.integration.api.utils.APIUtils;

/**
 * EMF driver that reads a remote model from a Hawk index.
 */
public class HawkResourceImpl extends ResourceImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(HawkResourceImpl.class);
	private HawkModelDescriptor descriptor;

	public HawkResourceImpl() {
	}

	public HawkResourceImpl(URI uri) {
		super(uri);
	}

	private EClass getEClass(final Registry packageRegistry, ModelElement me)
			throws IOException {
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

	private void setStructuralFeatureFromByte(final EClass eClass,
			final EObject eObject, AttributeSlot slot,
			final EStructuralFeature feature) throws IOException {
		// TODO not sure, need to test

		if (!slot.values.isSetVBytes()) {
			throw new IOException(
					String.format(
							"Expected to receive bytes for feature '%s' in type '%s', but did not",
							feature.getName(), eClass.getName()));
		} else if (feature.isMany() || feature.getEType() == EcorePackage.Literals.EBYTE_ARRAY) {
			final EList<Byte> bytes = new BasicEList<Byte>(
					slot.values.getVBytes().length);
			for (byte b : slot.values.getVBytes()) {
				bytes.add(b);
			}
			eObject.eSet(feature, bytes);
		} else {
			final byte b = slot.values.getVBytes()[0];
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
			setStructuralFeatureWithExpectedType(eClass, eObject, slot,	feature, _Fields.V_DOUBLES);
		} else if (eType == EcorePackage.Literals.EINT) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot,	feature, _Fields.V_INTEGERS);
		} else if (eType == EcorePackage.Literals.ELONG) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot,	feature, _Fields.V_LONGS);
		} else if (eType == EcorePackage.Literals.ESHORT) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot,	feature, _Fields.V_SHORTS);
		} else if (eType == EcorePackage.Literals.ESTRING) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, _Fields.V_STRINGS);
		} else if (eType == EcorePackage.Literals.EBOOLEAN) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, _Fields.V_BOOLEANS);
		} else {
			throw new IOException(String.format("Unknown ECore data type '%s'", eType));
		}
	}

	private void setStructuralFeatureFromEnum(final EClass eClass,
			final EObject eObject, AttributeSlot slot,
			final EStructuralFeature feature, final EEnum enumType)
			throws IOException {
		if (!slot.values.isSetVStrings()) {
			throw new IOException(
					String.format(
							"Expected to receive strings for feature '%s' in type '%s' with many='%s', but did not",
							feature.getName(), eClass.getName(), feature.isMany()));
		} else if (feature.isMany()) {
			List<EEnumLiteral> literals = new ArrayList<>();
			for (String s : slot.values.getVStrings()) {
				literals.add(enumType.getEEnumLiteral(s));
			}
			eObject.eSet(feature, literals);
		} else {
			final EEnumLiteral enumLiteral = enumType.getEEnumLiteral(slot.values.getVStrings().get(0));
			eObject.eSet(feature, enumLiteral);
		}
	}

	private void setStructuralFeatureFromFloat(final EClass eClass,
			final EObject eObject, AttributeSlot slot,
			final EStructuralFeature feature) throws IOException {
		if (!slot.values.isSetVDoubles()) {
			throw new IOException(
					String.format(
							"Expected to receive doubles for feature '%s' in type '%s', but did not",
							feature.getName(), eClass.getName()));

		} else if (feature.isMany()) {
			final EList<Float> floats = new BasicEList<Float>(slot.values.getVDoubles().size());
			for (double d : slot.values.getVDoubles()) {
				floats.add((float) d);
			}
			eObject.eSet(feature, floats);
		} else {
			final double d = slot.values.getVDoubles().get(0);
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
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, _Fields.V_DOUBLES);
		} else if (Integer.class.isAssignableFrom(instanceClass)) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, _Fields.V_INTEGERS);
		} else if (Long.class.isAssignableFrom(instanceClass)) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, _Fields.V_LONGS);
		} else if (Short.class.isAssignableFrom(instanceClass)) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, _Fields.V_SHORTS);
		} else if (String.class.isAssignableFrom(instanceClass)) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, _Fields.V_STRINGS);
		} else if (Boolean.class.isAssignableFrom(instanceClass)) {
			setStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, _Fields.V_BOOLEANS);
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
			final EStructuralFeature feature, final _Fields expectedType)
			throws IOException {
		if (!slot.values.isSet(expectedType)) {
			throw new IOException(
					String.format(
							"Expected to receive '%s' for feature '%s' in type '%s' with many='%s', but did not",
							expectedType, feature.getName(), eClass.getName(),
							feature.isMany()));
		} else if (feature.isMany()) {
			eObject.eSet(feature, ECollections
					.toEList((Iterable<?>) slot.values
							.getFieldValue(expectedType)));
		} else {
			final Iterable<?> slotValues = (Iterable<?>) slot.values
					.getFieldValue(expectedType);
			final Object first = slotValues.iterator().next();
			eObject.eSet(feature, first);
		}
	}

	@Override
	protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
		this.descriptor = new HawkModelDescriptor();
		this.descriptor.load(inputStream);

		try {
			final Hawk.Client client = APIUtils.connectToHawk(descriptor.getHawkURL());
			final List<ModelElement> elems = client.getModel(
					descriptor.getHawkInstance(),
					descriptor.getHawkRepository(),
					Arrays.asList(descriptor.getHawkFilePatterns()));

			// Do a first pass, creating all the objects with their attributes
			// and saving their graph IDs into the One Map to bind them.
			final Registry packageRegistry = getResourceSet().getPackageRegistry();
			final Map<Long, EObject> nodeIdToEObjectMap = new HashMap<>();
			for (ModelElement me : elems) {
				final EClass eClass = getEClass(packageRegistry, me);
				final EFactory factory = packageRegistry.getEFactory(me.metamodelUri);
				final EObject obj = factory.create(eClass);
				nodeIdToEObjectMap.put(me.id, obj);

				if (me.isSetAttributes()) {
					for (AttributeSlot s : me.attributes) {
						setStructuralFeatureFromSlot(eClass, obj, s);
					}
				}
			}

			// On the second pass, fill in the references.
			for (ModelElement me : elems) {
				final EObject sourceObj = nodeIdToEObjectMap.get(me.id);

				if (me.isSetReferences()) {
					for (ReferenceSlot s : me.references) {
						final EClass eClass = getEClass(packageRegistry, me);
						final EStructuralFeature feature = eClass.getEStructuralFeature(s.name);
						if (feature.isMany()) {
							final EList<EObject> value = new BasicEList<>();
							for (Long targetId : s.ids) {
								final EObject targets = nodeIdToEObjectMap.get(targetId);
								if (targets == null) {
									LOGGER.warn(
											"Could not find ModelElement with id {} for feature {} of class {}, skipping",
											targetId, feature, eClass);
									continue;
								}
								value.add(targets);
							}

							sourceObj.eSet(feature, value);
						} else {
							final Long targetId = s.ids.get(0);
							final EObject target = nodeIdToEObjectMap.get(targetId);
							if (target == null) {
								LOGGER.warn(
										"Could not find ModelElement with id {} for feature {} of class {}, skipping",
										targetId, feature, eClass);
								continue;
							}
							sourceObj.eSet(feature, target);
						}
					}
				}
			}

			// On the third pass, add elements which still don't have containers as root elements
			for (EObject eo : nodeIdToEObjectMap.values()) {
				if (eo.eContainer() == null) {
					getContents().add(eo);
				}
			}
		} catch (TException e) {
			LOGGER.error(e.getMessage(), e);
			throw new IOException(e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected void doSave(OutputStream outputStream, Map<?, ?> options)
			throws IOException {
		throw new UnsupportedOperationException();
	}
}
