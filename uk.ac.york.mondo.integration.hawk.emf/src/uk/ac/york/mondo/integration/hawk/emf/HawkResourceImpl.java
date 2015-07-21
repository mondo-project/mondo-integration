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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.THttpClient;
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

	@Override
	protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
		this.descriptor = new HawkModelDescriptor();
		this.descriptor.load(inputStream);

		try {
			final Hawk.Client client = new Hawk.Client(new TCompactProtocol(new THttpClient(descriptor.getHawkURL())));
			final List<ModelElement> elems = client.getModel(
					descriptor.getHawkInstance(),
					descriptor.getHawkRepository(),
					Arrays.asList(descriptor.getHawkFilePatterns()));

			// Do a first pass, creating all the objects with their attributes
			// and saving their graph IDs
			// into the One Map to bind them.
			final Registry packageRegistry = getResourceSet()
					.getPackageRegistry();
			final Map<String, EObject> nodeIdToEObjectMap = new HashMap<>();
			for (ModelElement me : elems) {
				final EClass eClass = getEClass(packageRegistry, me);
				final EFactory factory = packageRegistry
						.getEFactory(me.metamodelUri);
				final EObject obj = factory.create(eClass);
				nodeIdToEObjectMap.put(me.id, obj);

				for (AttributeSlot s : me.attributes) {
					if (!s.isSet)
						continue;
					setStructuralFeatureFromSlot(eClass, obj, s);
				}
			}

			// On the second pass, fill in the references
			for (ModelElement me : elems) {
				final EObject sourceObj = nodeIdToEObjectMap.get(me.id);

				for (ReferenceSlot s : me.references) {
					if (!s.isSet)
						continue;

					final EClass eClass = getEClass(packageRegistry, me);
					final EStructuralFeature feature = eClass
							.getEStructuralFeature(s.name);
					if (feature.isMany()) {
						final EList<EObject> value = new BasicEList<>();
						for (String targetId : s.ids) {
							final EObject targets = nodeIdToEObjectMap
									.get(targetId);
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
						final String targetId = s.ids.get(0);
						final EObject target = nodeIdToEObjectMap.get(targetId);
						if (target == null) {
							LOGGER.warn(
									"Could not find ModelElement with id {} for feature {} of class {}, skipping",
									targetId, feature, eClass);
							continue;
						}
						sourceObj.eSet(feature, target);
					}

					// Root level: only objects without containers
					if (sourceObj.eContainer() == null) {
						getContents().add(sourceObj);
					}
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

	private EStructuralFeature setStructuralFeatureFromSlot(
			final EClass eClass, final EObject eObject, AttributeSlot slot)
			throws IOException {
		assert slot.isSet : "This method should only be called for slots with set values";

		final EStructuralFeature feature = eClass
				.getEStructuralFeature(slot.name);
		final EClassifier eType = feature.getEType();

		if (feature.isMany()) {
			switch (eType.getClassifierID()) {
			case EcorePackage.EBYTE:
				// TODO not sure, need to test
				setByteEListStructuralFeature(eClass, eObject, slot, feature);
				break;

			case EcorePackage.EFLOAT:
				if (!slot.values.isSetVDoubles()) {
					throw new IOException(
							String.format(
									"Expected to receive doubles for feature '%s' in type '%s', but did not",
									feature.getName(), eClass.getName()));

				} else {
					final EList<Float> floats = new BasicEList<Float>(
							slot.values.getVDoubles().size());
					for (double d : slot.values.getVDoubles()) {
						floats.add((float) d);
					}
					eObject.eSet(feature, floats);
				}
				break;

			case EcorePackage.EDOUBLE:
				setManyStructuralFeatureWithExpectedType(eClass, eObject, slot,
						feature, _Fields.V_DOUBLES);
				break;
			case EcorePackage.EINT:
				setManyStructuralFeatureWithExpectedType(eClass, eObject, slot,
						feature, _Fields.V_INTEGERS);
				break;
			case EcorePackage.ELONG:
				setManyStructuralFeatureWithExpectedType(eClass, eObject, slot,
						feature, _Fields.V_LONGS);
				break;
			case EcorePackage.ESHORT:
				setManyStructuralFeatureWithExpectedType(eClass, eObject, slot,
						feature, _Fields.V_SHORTS);
				break;
			case EcorePackage.ESTRING:
				setManyStructuralFeatureWithExpectedType(eClass, eObject, slot,
						feature, _Fields.V_STRINGS);
				break;
			default:
				throw new IOException(String.format(
						"Unknown data type %s with isMany = true",
						eType.getName()));
			}
		} else {
			// isSet=true and many=false means that we should have exactly one
			// value
			switch (eType.getClassifierID()) {
			case EcorePackage.EBYTE_ARRAY:
				// TODO not sure, need to test
				setByteEListStructuralFeature(eClass, eObject, slot, feature);
				break;
			case EcorePackage.EBYTE:
				if (!slot.values.isSetVBytes()) {
					throw new IOException(
							String.format(
									"Expected to receive bytes for feature '%s' in type '%s', but did not",
									feature.getName(), eClass.getName()));
				} else {
					final byte b = slot.values.getVBytes()[0];
					eObject.eSet(feature, b);
				}
				break;
			case EcorePackage.EFLOAT:
				if (!slot.values.isSetVDoubles()) {
					throw new IOException(
							String.format(
									"Expected to receive doubles for feature '%s' in type '%s', but did not",
									feature.getName(), eClass.getName()));

				} else {
					final double d = slot.values.getVDoubles().get(0);
					eObject.eSet(feature, (float) d);
				}
				break;
			case EcorePackage.EDOUBLE:
				setSingleStructuralFeatureWithExpectedType(eClass, eObject,
						slot, feature, _Fields.V_DOUBLES);
				break;
			case EcorePackage.EINT:
				setSingleStructuralFeatureWithExpectedType(eClass, eObject,
						slot, feature, _Fields.V_INTEGERS);
				break;
			case EcorePackage.ELONG:
				setSingleStructuralFeatureWithExpectedType(eClass, eObject,
						slot, feature, _Fields.V_LONGS);
				break;
			case EcorePackage.ESHORT:
				setSingleStructuralFeatureWithExpectedType(eClass, eObject,
						slot, feature, _Fields.V_SHORTS);
				break;
			case EcorePackage.ESTRING:
				setSingleStructuralFeatureWithExpectedType(eClass, eObject,
						slot, feature, _Fields.V_STRINGS);
				break;
			default:
				throw new IOException(String.format(
						"Unknown data type %s with isMany = false",
						eType.getName(), feature.isMany()));
			}
		}
		return feature;
	}

	private void setSingleStructuralFeatureWithExpectedType(
			final EClass eClass, final EObject eObject, AttributeSlot slot,
			final EStructuralFeature feature, final _Fields expectedType)
			throws IOException {
		if (!slot.values.isSet(expectedType)) {
			throw new IOException(
					String.format(
							"Expected to receive '%s' for feature '%s' in type '%s' with many='%s', but did not",
							expectedType, feature.getName(), eClass.getName(),
							feature.isMany()));
		} else {
			final Iterable<?> slotValues = (Iterable<?>) slot.values
					.getFieldValue(expectedType);
			final Object first = slotValues.iterator().next();
			eObject.eSet(feature, first);
		}
	}

	private void setByteEListStructuralFeature(final EClass eClass,
			final EObject eObject, AttributeSlot slot,
			final EStructuralFeature feature) throws IOException {
		if (!slot.values.isSetVBytes()) {
			throw new IOException(
					String.format(
							"Expected to receive bytes for feature '%s' in type '%s', but did not",
							feature.getName(), eClass.getName()));
		} else {
			final EList<Byte> bytes = new BasicEList<Byte>(
					slot.values.getVBytes().length);
			for (byte b : slot.values.getVBytes()) {
				bytes.add(b);
			}
			eObject.eSet(feature, bytes);
		}
	}

	private void setManyStructuralFeatureWithExpectedType(final EClass eClass,
			final EObject eObject, AttributeSlot slot,
			final EStructuralFeature feature, final _Fields expectedType)
			throws IOException {
		if (!slot.values.isSet(expectedType)) {
			throw new IOException(
					String.format(
							"Expected to receive '%s' for feature '%s' in type '%s' with many=%s, but did not",
							expectedType, feature.getName(), eClass.getName(),
							feature.isMany()));
		} else {
			eObject.eSet(feature, ECollections
					.toEList((Iterable<?>) slot.values
							.getFieldValue(expectedType)));
		}
	}
}
