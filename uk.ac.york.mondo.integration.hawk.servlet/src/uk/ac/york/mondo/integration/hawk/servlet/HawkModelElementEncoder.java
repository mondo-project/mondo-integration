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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hawk.graph.GraphWrapper;
import org.hawk.graph.ModelElementNode;

import uk.ac.york.mondo.integration.api.AttributeSlot;
import uk.ac.york.mondo.integration.api.ContainerSlot;
import uk.ac.york.mondo.integration.api.ModelElement;
import uk.ac.york.mondo.integration.api.ReferenceSlot;
import uk.ac.york.mondo.integration.api.Variant;

/**
 * Encodes a graph of Hawk {@link ModelElementNode}s into Thrift
 * {@link ModelElement}s. This is an accumulator: the user should specify the
 * encoding options, call {@link #encode(ModelElementNode)} repeatedly and then
 * finally call {@link #getRootElements()}.
 *
 * Depending on whether we intend to send the entire model or not, it might make
 * sense to call {@link #setElementNodeIDs(boolean)} accordingly before any
 * calls to {@link #encode(ModelElementNode)}.
 */
public class HawkModelElementEncoder {

	private final GraphWrapper graph;

	private final Map<String, ModelElement> nodeIdToElement = new HashMap<>();
	private final Set<ModelElement> rootElements = new IdentityLinkedHashSet<>();

	private String lastMetamodelURI, lastTypename;
	private boolean sendElementNodeIDs = false;

	public HawkModelElementEncoder(GraphWrapper gw) {
		this.graph = gw;
	}

	/**
	 * If <code>true</code>, the encoder will include node IDs in the model elements. Otherwise,
	 * it will not include them (the default).
		 * 
	 * Note: if we do not include node IDs, it will not be possible to resolve non-containment
	 * references to the encoded elements from elements that were not encoded. Therefore, setting
	 * this to <code>false</code> is only advisable when we're encoding an entire model. 
		 */
	public boolean isSendElementNodeIDs() {
		return sendElementNodeIDs;
	}

	/**
	 * Changes the value of {@link #isSendElementNodeIDs()}.
	 */
	public void setElementNodeIDs(boolean newValue) {
		this.sendElementNodeIDs = newValue;
	}

	/**
	 * Returns the list of the encoded {@link ModelElement}s that are not
	 * contained within any other encoded {@link ModelElement}s.
	 */
	public List<ModelElement> getRootElements() {
		final List<ModelElement> lRoots = new ArrayList<>(rootElements);

		final HashMap<String, Integer> id2pos = new HashMap<>();
		computePreorderPositionMap(lRoots, id2pos, 0);
		lastMetamodelURI = lastTypename = null;
		optimizeTree(lRoots, id2pos);

		return lRoots;
	}

	private int computePreorderPositionMap(Collection<ModelElement> elems, Map<String, Integer> id2pos, int i) {
		for (ModelElement elem : elems) {
			if (elem.isSetId()) {
				id2pos.put(elem.id, i);
			}
			i++;

			if (elem.isSetContainers()) {
				for (ContainerSlot s : elem.containers) {
					i = computePreorderPositionMap(s.elements, id2pos, i);
				}
			}
		}
		return i;
	}

	private void optimizeTree(Collection<ModelElement> elems, Map<String, Integer> id2pos) {
		for (ModelElement me : elems) {
			if (!isSendElementNodeIDs()) {
				me.unsetId();
			}

			// We replace id-based references with position-based references, when we can:
			// the referenced element may not have been encoded. In that case, we'll need
			// to preserve the actual node ID, so we may request that element later on.
			if (me.isSetReferences()) {
				for (ReferenceSlot r : me.getReferences()) {
					final List<String> oldIds = r.ids;
					
					final List<String> newIds = new ArrayList<>();
					final List<Integer> positions = new ArrayList<>();
					for (String id : oldIds) {
						final Integer pos = id2pos.get(id);
						if (pos != null) {
							positions.add(pos);
						} else {
							newIds.add(id);
						}
					}

					if (newIds.isEmpty()) {
						r.unsetIds();
					} else {
						r.setIds(newIds);
					}

					if (positions.isEmpty()) {
						r.unsetPositions();
					} else {
						r.setPositions(positions);
					}
				}
			}

			// we don't repeat typenames or metamodel URIs if they're the same as the previous element's
			final String currTypename = me.getTypeName();
			final String currMetamodelURI = me.getMetamodelUri();
			if (lastTypename != null && lastTypename.equals(currTypename)) {
				me.unsetTypeName();
			}
			if (lastMetamodelURI != null && lastMetamodelURI.equals(currMetamodelURI)) {
				me.unsetMetamodelUri();
			}
			lastTypename = currTypename;
			lastMetamodelURI = currMetamodelURI;

			if (me.isSetContainers()) {
				for (ContainerSlot s : me.getContainers()) {
					optimizeTree(s.elements, id2pos);
				}
			}
		}
	}

	public void encode(String id) throws Exception {
		final ModelElementNode me = graph.getModelElementNodeById(id);
		encodeInternal(me);
	}

	public void encode(ModelElementNode meNode) throws Exception {
		assert meNode.getNode().getGraph() == this.graph.getGraph()
			: "The node should belong to the same graph as this encoder";
		encodeInternal(meNode);
	}

	private ModelElement encodeInternal(ModelElementNode meNode) throws Exception {
		final ModelElement existing = nodeIdToElement.get(meNode.getId());
		if (existing != null) {
			return existing;
		}

		final ModelElement me = new ModelElement();
		me.setTypeName(meNode.getTypeNode().getTypeName());
		me.setMetamodelUri(meNode.getTypeNode().getMetamodelName());

		// we won't set the ID until someone refers to it, but we
		// need to keep track of the element for later
		nodeIdToElement.put(meNode.getId(), me);

		// initially, the model element is not contained in any other
		rootElements.add(me);

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

			if (meNode.isContainment(ref.getKey())) {
				final ContainerSlot slot = encodeContainerSlot(ref);
				if (slot.elements.isEmpty()) continue;
				me.addToContainers(slot);
			} else {
				final ReferenceSlot slot = encodeReferenceSlot(ref);
				if (slot.ids.isEmpty()) continue;
				me.addToReferences(slot);
			}
		}
		return me;
	}

	private ContainerSlot encodeContainerSlot(Entry<String, Object> slotEntry) throws Exception {
		assert slotEntry.getValue() != null;

		ContainerSlot s = new ContainerSlot();
		s.name = slotEntry.getKey();

		final Object value = slotEntry.getValue();
		if (value instanceof Collection) {
			for (Object o : (Collection<?>)value) {
				final ModelElementNode meNode = graph.getModelElementNodeById((long)o);
				final ModelElement me = encodeInternal(meNode);
				s.addToElements(me);
				rootElements.remove(me);
			}
		} else {
			final ModelElementNode meNode = graph.getModelElementNodeById((long)value);
			final ModelElement me = encodeInternal(meNode);
			s.addToElements(me);
			rootElements.remove(me);
		}

		return s;
	}

	private ReferenceSlot encodeReferenceSlot(Entry<String, Object> slotEntry) throws Exception {
		assert slotEntry.getValue() != null;

		ReferenceSlot s = new ReferenceSlot();
		s.name = slotEntry.getKey();

		final Object value = slotEntry.getValue();
		s.ids = new ArrayList<>();
		if (value instanceof Collection) {
			for (Object o : (Collection<?>)value) {
				addToIds(o, s);
			}
		} else {
			addToIds(value, s);
		}

		return s;
	}

	private void addToIds(Object o, ReferenceSlot s) throws Exception {
		final String referencedId = o.toString();
		final ModelElementNode meNode = graph.getModelElementNodeById(referencedId);
		final ModelElement me = encodeInternal(meNode);
		me.setId(meNode.getId());
		s.addToIds(meNode.getId());
	}

	private AttributeSlot encodeAttributeSlot(Entry<String, Object> slotEntry) {
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

	private void encodeSingleValueAttributeSlot(AttributeSlot s, final Object value) {
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
	private void encodeNonEmptyListAttributeSlot(AttributeSlot s, final Object value, final Collection<?> cValue) {
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

}
