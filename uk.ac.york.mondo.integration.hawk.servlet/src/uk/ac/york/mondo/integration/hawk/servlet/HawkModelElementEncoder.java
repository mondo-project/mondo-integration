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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.graph.ModelElementNode;

import uk.ac.york.mondo.integration.api.AttributeSlot;
import uk.ac.york.mondo.integration.api.ModelElement;
import uk.ac.york.mondo.integration.api.ReferenceSlot;
import uk.ac.york.mondo.integration.api.Variant;

/**
 * Collection of methods for converting Hawk {@link ModelElementNode}s into Thrift {@link ModelElement}s.
 */
public class HawkModelElementEncoder {

	private HawkModelElementEncoder() {}

	public static ModelElement encodeModelElement(ModelElementNode meNode) throws Exception {
		ModelElement me = new ModelElement();
		me.id = (long)meNode.getNode().getId();
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
			// to save bandwidth, we do not send unset or empty references 
			if (ref.getValue() == null) continue;
			final ReferenceSlot encodeReferenceSlot = encodeReferenceSlot(ref);
			if (encodeReferenceSlot.ids.isEmpty()) continue;
			me.addToReferences(encodeReferenceSlot);
		}
		return me;
	}

	public static ReferenceSlot encodeReferenceSlot(Entry<String, Object> slotEntry) {
		assert slotEntry.getValue() != null;
	
		ReferenceSlot s = new ReferenceSlot();
		s.name = slotEntry.getKey();
	
		final Object value = slotEntry.getValue();
		s.ids = new ArrayList<>();
		if (value instanceof Collection) {
			for (Object o : (Collection<?>)value) {
				s.ids.add((long)o);
			}
		} else {
			s.ids.add((long)value);
		}
	
		return s;
	}

	public static AttributeSlot encodeAttributeSlot(Entry<String, Object> slotEntry) {
		assert slotEntry.getValue() != null;
	
		AttributeSlot s = new AttributeSlot();
		s.name = slotEntry.getKey();
	
		final Object value = slotEntry.getValue();
		s.value = new Variant();

		if (value instanceof Collection) {
			final Collection<?> cValue = (Collection<?>) value;
			final int cSize = cValue.size();
			if (cSize == 1) {
				// use the single value attrs if we only have one value (saves
				// 1-5 bytes on TTupleTransport)
				encodeSingleValueAttributeSlot(s, cValue.iterator().next());
			} else if (cSize > 0) {
				s.value = new Variant();
				encodeNonEmptyListAttributeSlot(s, value, cValue);
			} else {
				// empty list <-> isSet=true and s.values=null
				s.value = null;
			}
		} else {
			encodeSingleValueAttributeSlot(s, value);
		}

		if (!s.value.isSet()) {
			throw new IllegalArgumentException(String.format(
					"Unsupported value type '%s'", value.getClass()
							.getName()));
		}

		assert s.value.getFieldValue() != null : "The union field should have a value";
		return s;
	}

	private static void encodeSingleValueAttributeSlot(AttributeSlot s, final Object value) {
		if (value instanceof Byte) {
			s.value.setVByte((byte) value);
		} else if (value instanceof Float) {
			s.value.setVDouble((double) value);
		} else if (value instanceof Double) {
			s.value.setVDouble((double) value);
		} else if (value instanceof Integer) {
			s.value.setVInteger((int) value);
		} else if (value instanceof Long) {
			s.value.setVLong((long) value);
		} else if (value instanceof Short) {
			s.value.setVShort((short) value);
		} else if (value instanceof String) {
			s.value.setVString((String) value);
		} else if (value instanceof Boolean) {
			s.value.setVBoolean((Boolean) value);
		}
	}

	@SuppressWarnings("unchecked")
	private static void encodeNonEmptyListAttributeSlot(AttributeSlot s, final Object value, final Collection<?> cValue) {
		final Iterator<?> it = cValue.iterator();
		final Object o = it.next();
		if (o instanceof Byte) {
			final ByteBuffer bbuf = ByteBuffer.allocate(cValue.size());
			bbuf.put((byte)o);
			while (it.hasNext()) {
				bbuf.put((byte)it.next());
			}
			bbuf.flip();
			s.value.setVBytes(bbuf);
		} else if (o instanceof Float) {
			final ArrayList<Double> l = new ArrayList<Double>(cValue.size());
			l.add((double)o);
			while (it.hasNext()) {
				l.add((double)it.next());
			}
			s.value.setVDoubles(l);
		} else if (o instanceof Double) {
			s.value.setVDoubles(new ArrayList<Double>((Collection<Double>)cValue));
		} else if (o instanceof Integer) {
			s.value.setVIntegers(new ArrayList<Integer>((Collection<Integer>)cValue));
		} else if (o instanceof Long) {
			s.value.setVLongs(new ArrayList<Long>((Collection<Long>)cValue));
		} else if (o instanceof Short) {
			s.value.setVShorts(new ArrayList<Short>((Collection<Short>)cValue));
		} else if (o instanceof String) {
			s.value.setVStrings(new ArrayList<String>((Collection<String>)cValue));
		} else if (o instanceof Boolean) {
			s.value.setVBooleans(new ArrayList<Boolean>((Collection<Boolean>)cValue));
		} else if (o != null) {
			throw new IllegalArgumentException(String.format("Unsupported element type '%s'", value.getClass().getName()));
		} else {
			throw new IllegalArgumentException("Null values inside collections are not allowed");
		}
	}

	/* uncomment when we can retrieve more interesting things through Hawk queries
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
	*/

}
