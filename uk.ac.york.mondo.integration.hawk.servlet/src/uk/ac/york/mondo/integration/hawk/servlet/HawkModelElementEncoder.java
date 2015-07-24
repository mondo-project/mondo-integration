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
import uk.ac.york.mondo.integration.api.ScalarList;

/**
 * Collection of methods for converting Hawk {@link ModelElementNode}s into Thrift {@link ModelElement}s.
 */
public class HawkModelElementEncoder {

	private HawkModelElementEncoder() {}

	public static ModelElement encodeModelElement(ModelElementNode meNode) throws Exception {
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

	public static ReferenceSlot encodeReferenceSlot(Entry<String, Object> slotEntry) {
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

	public static AttributeSlot encodeAttributeSlot(Entry<String, Object> slotEntry) {
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
	private static void encodeNonEmptyListAttributeSlot(AttributeSlot s,
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
