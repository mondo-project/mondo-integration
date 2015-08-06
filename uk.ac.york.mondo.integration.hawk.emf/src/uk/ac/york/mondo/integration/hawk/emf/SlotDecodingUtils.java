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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;

import uk.ac.york.mondo.integration.api.AttributeSlot;
import uk.ac.york.mondo.integration.api.Variant;
import uk.ac.york.mondo.integration.api.Variant._Fields;

/**
 * Utility methods for setting {@link EAttribute}s from {@link AttributeSlot}s.
 */
public final class SlotDecodingUtils {

	private SlotDecodingUtils() {}

	public static EStructuralFeature setFromSlot(final EClass eClass, final EObject eObject, AttributeSlot slot) throws IOException {
		final EStructuralFeature feature = eClass.getEStructuralFeature(slot.name);
		final EClassifier eType = feature.getEType();
	
		// isSet=true and many=false means that we should have exactly one value
		if (eType.eContainer() == EcorePackage.eINSTANCE) {
			fromEcoreType(eClass, eObject, slot, feature, eType);
		} else if (eType instanceof EEnum) {
			fromEnum(eClass, eObject, slot, feature, (EEnum)eType);
		} else {
			fromInstanceClass(eClass, eObject, slot, feature, eType);
		}
	
		return feature;
	}

	private static void fromByte(final EClass eClass,
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
	private static void fromEcoreType(final EClass eClass,
			final EObject eObject, AttributeSlot slot,
			final EStructuralFeature feature, final EClassifier eType)
			throws IOException {
		if (eType == EcorePackage.Literals.EBYTE_ARRAY || eType == EcorePackage.Literals.EBYTE) {
			fromByte(eClass, eObject, slot, feature);
		} else if (eType == EcorePackage.Literals.EFLOAT) {
			fromFloat(eClass, eObject, slot, feature);
		} else if (eType == EcorePackage.Literals.EDOUBLE) {
			fromExpectedType(eClass, eObject, slot,	feature, Variant._Fields.V_DOUBLES, Variant._Fields.V_DOUBLE);
		} else if (eType == EcorePackage.Literals.EINT) {
			fromExpectedType(eClass, eObject, slot,	feature, Variant._Fields.V_INTEGERS, Variant._Fields.V_INTEGER);
		} else if (eType == EcorePackage.Literals.ELONG) {
			fromExpectedType(eClass, eObject, slot,	feature, Variant._Fields.V_LONGS, Variant._Fields.V_LONG);
		} else if (eType == EcorePackage.Literals.ESHORT) {
			fromExpectedType(eClass, eObject, slot,	feature, Variant._Fields.V_SHORTS, Variant._Fields.V_SHORT);
		} else if (eType == EcorePackage.Literals.ESTRING) {
			fromExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_STRINGS, Variant._Fields.V_STRING);
		} else if (eType == EcorePackage.Literals.EBOOLEAN) {
			fromExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_BOOLEANS, Variant._Fields.V_BOOLEAN);
		} else {
			throw new IOException(String.format("Unknown ECore data type '%s'", eType));
		}
	}

	private static void fromEnum(final EClass eClass,
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

	private static void fromFloat(final EClass eClass,
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

	private static void fromInstanceClass(
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
			fromByte(eClass, eObject, slot, feature);
		} else if (Float.class.isAssignableFrom(instanceClass)) {
			fromFloat(eClass, eObject, slot, feature);
		} else if (Double.class.isAssignableFrom(instanceClass)) {
			fromExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_DOUBLES, Variant._Fields.V_DOUBLE);
		} else if (Integer.class.isAssignableFrom(instanceClass)) {
			fromExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_INTEGERS, Variant._Fields.V_INTEGER);
		} else if (Long.class.isAssignableFrom(instanceClass)) {
			fromExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_LONGS, Variant._Fields.V_LONG);
		} else if (Short.class.isAssignableFrom(instanceClass)) {
			fromExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_SHORTS, Variant._Fields.V_SHORT);
		} else if (String.class.isAssignableFrom(instanceClass)) {
			fromExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_STRINGS, Variant._Fields.V_STRING);
		} else if (Boolean.class.isAssignableFrom(instanceClass)) {
			fromExpectedType(eClass, eObject, slot, feature, Variant._Fields.V_BOOLEANS, Variant._Fields.V_BOOLEAN);
		} else {
			throw new IOException(String.format(
					"Unknown data type %s with isMany = false and instance class %s",
					eType.getName(), feature.isMany(), instanceClass));
		}
	}

	private static void fromExpectedType(
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

}
