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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

/**
 * Loads and saves {@link EffectiveMetamodelStore} instances.
 */
public class EffectiveMetamodelStoreSerializer {

	private static final char SLOT_SEPARATOR = ',';
	private static final char PART_SEPARATOR = '.';
	private final String propertyPrefix;

	public EffectiveMetamodelStoreSerializer(String propertyPrefix) {
		this.propertyPrefix = propertyPrefix;
	}

	public EffectiveMetamodelStore load(Properties props) {
		/*
		 * Do not assume any ordering in the properties while reading them
		 * (they're a Hashtable internally anyway). The tree-based data
		 * structures will restore the original order.
		 */
		final Map<Integer, String> metamodels = new TreeMap<>();
		final Table<Integer, Integer, String> typeTable = TreeBasedTable.create();
		final Table<Integer, Integer, Set<String>> slotTable = TreeBasedTable.create();

		for (String propName : props.stringPropertyNames()) {
			if (propName.startsWith(propertyPrefix)) {
				final String unprefixed = propName.substring(propertyPrefix.length());
				final String[] parts = unprefixed.split("[" + PART_SEPARATOR + "]");

				final String propValue = props.getProperty(propName).trim();
				int iMetamodel, iType;
				switch (parts.length) {
				case 1: // prefix0 -> URI of the first metamodel
					iMetamodel = Integer.valueOf(parts[0]);
					String mmURI = propValue;
					metamodels.put(iMetamodel, mmURI);
					break;
				case 2: // prefix0.0 -> name of the first type of the first metamodel
					iMetamodel = Integer.valueOf(parts[0]);
					iType = Integer.valueOf(parts[1]);
					String type = propValue;
					typeTable.put(iMetamodel, iType, type);
					break;
				case 3: // prefix0.0.slots -> comma-separated slots for the first type of first metamodel (if not all)
					iMetamodel = Integer.valueOf(parts[0]);
					iType = Integer.valueOf(parts[1]);
					Set<String> slots;
					if (propValue.length() > 0) {
						slots = ImmutableSet.copyOf(propValue.split("[" + SLOT_SEPARATOR + "]"));
					} else {
						slots = ImmutableSet.of();
					}
					slotTable.put(iMetamodel, iType, slots);
					break;
				default:
					throw new IllegalArgumentException(String
							.format("Property %s should only have 1-3 parts, but has %d",
									propName, parts.length));
				}
			}
		}

		/* Now that we have the tables, populate the metamodel */
		final EffectiveMetamodelStore store = new EffectiveMetamodelStore();
		for (final Entry<Integer, String> mmEntry : metamodels.entrySet()) {
			final String mmURI = mmEntry.getValue();
			final Map<Integer, String> types = typeTable.row(mmEntry.getKey());

			for (Entry<Integer, String> typeEntry : types.entrySet()) {
				final String type = typeEntry.getValue();
				final Set<String> slots = slotTable.get(mmEntry.getKey(), typeEntry.getKey());
				if (slots == null) {
					store.addType(mmURI, type);
				} else {
					store.addType(mmURI, type, ImmutableSet.copyOf(slots));
				}
			}
		}

		return store;
	}

	public void save(EffectiveMetamodelStore store, Properties props) {
		if (store.isEverythingIncluded()) {
			// by default, everything is included (see #load and EffectiveMetamodelStore javadocs) 
			return;
		}

		int iMetamodel = 0;
		for (Entry<String, EffectiveMetamodel> mmEntry : store.getIncludedMetamodels().entrySet()) {
			final String mmURI = mmEntry.getKey();
			props.put(propertyPrefix + iMetamodel, mmURI);

			int iType = 0;
			for (Entry<String, Set<String>> typeEntry : mmEntry.getValue().getIncludedTypes().entrySet()) {
				final String type = typeEntry.getKey();
				props.put(propertyPrefix + iMetamodel + PART_SEPARATOR + iType, type);

				final Set<String> slots = typeEntry.getValue();
				final StringBuffer sbuf = new StringBuffer();
				boolean first = true;
				for (String slot : slots) {
					if (first) {
						first = false;
					} else {
						sbuf.append(SLOT_SEPARATOR);
					}
					sbuf.append(slot);
				}
				props.put(propertyPrefix + iMetamodel + PART_SEPARATOR + iType + PART_SEPARATOR + "slots", sbuf.toString());
				iType++;
			}

			iMetamodel++;
		}
	}
}
