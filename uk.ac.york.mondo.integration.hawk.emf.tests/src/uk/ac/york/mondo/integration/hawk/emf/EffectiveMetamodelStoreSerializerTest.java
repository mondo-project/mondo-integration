package uk.ac.york.mondo.integration.hawk.emf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class EffectiveMetamodelStoreSerializerTest {

	private static final String PROPERTY_EMM_PREFIX = "hawk.effectiveMetamodel";
	private EffectiveMetamodelStoreSerializer serializer;
	private Properties props;

	@Before
	public void setup() {
		this.serializer = new EffectiveMetamodelStoreSerializer(PROPERTY_EMM_PREFIX);
		this.props = new Properties();
	}

	@Test
	public void saveLoadEmpty() {
		final EffectiveMetamodelStore saved = new EffectiveMetamodelStore();
		serializer.save(saved, props);
		final EffectiveMetamodelStore loaded = serializer.load(props);
		assertEquals(saved, loaded);
		assertTrue(loaded.isEverythingIncluded());
	}

	@Test
	public void saveLoadEmptyUnrelated() {
		final EffectiveMetamodelStore saved = new EffectiveMetamodelStore();
		serializer.save(saved, props);
		props.put("ignore", "me");
		final EffectiveMetamodelStore loaded = serializer.load(props);
		assertEquals(saved, loaded);
		assertTrue(loaded.isEverythingIncluded());
	}

	@Test(expected=IllegalArgumentException.class)
	public void saveLoadEmptyTooManyParts() {
		final EffectiveMetamodelStore saved = new EffectiveMetamodelStore();
		serializer.save(saved, props);
		props.put(PROPERTY_EMM_PREFIX + "0.0.0.0", "imbad");
		serializer.load(props);
	}

	@Test
	public void saveLoadMetamodelWithNoTypes() {
		props.put(PROPERTY_EMM_PREFIX + "0", "http://foo/bar");
		final EffectiveMetamodelStore loaded = serializer.load(props);
		assertEquals(new EffectiveMetamodelStore(), loaded);
		assertTrue(loaded.isEverythingIncluded());
	}

	@Test
	public void saveLoadOneTypeAllSlotsExplicit() {
		final EffectiveMetamodelStore saved = new EffectiveMetamodelStore();
		saved.addType("x", "y");
		serializer.save(saved, props);
		final EffectiveMetamodelStore loaded = serializer.load(props);
		assertEquals(saved, loaded);
		assertFalse(loaded.isEverythingIncluded());
		assertTrue(loaded.isTypeIncluded("x", "y"));
		assertFalse(loaded.isTypeIncluded("x", "z"));
		assertFalse(loaded.isTypeIncluded("y", "x"));
		assertTrue(loaded.isSlotIncluded("x", "y", "z"));
	}

	@Test
	public void saveLoadOneTypeAllSlotsNoSlotsProperty() {
		props.put(PROPERTY_EMM_PREFIX + "0", "x");
		props.put(PROPERTY_EMM_PREFIX + "0.0", "y");
		final EffectiveMetamodelStore loaded = serializer.load(props);
		assertFalse(loaded.isEverythingIncluded());
		assertTrue(loaded.isTypeIncluded("x", "y"));
		assertTrue(loaded.isSlotIncluded("x", "y", "z"));
	}
	
	@Test
	public void saveLoadOneTypeSomeSlots() {
		final ImmutableSet<String> empty = ImmutableSet.of();
		final EffectiveMetamodelStore saved = new EffectiveMetamodelStore();
		saved.addType("x", "y", empty);
		saved.addType("x", "z", ImmutableSet.of("a", "b"));
		saved.addType("u", "w", ImmutableSet.of("f"));
		serializer.save(saved, props);

		final EffectiveMetamodelStore loaded = serializer.load(props);
		assertEquals(loaded, saved);
		assertFalse(loaded.isEverythingIncluded());
		assertTrue(loaded.isTypeIncluded("x", "y"));
		assertTrue(loaded.isTypeIncluded("x", "z"));
		assertTrue(loaded.isTypeIncluded("u", "w"));
		assertFalse(loaded.isTypeIncluded("u", "z"));

		assertFalse(loaded.isSlotIncluded("x", "y", "z"));
		assertTrue(loaded.isSlotIncluded("x", "z", "a"));
		assertTrue(loaded.isSlotIncluded("x", "z", "b"));
		assertTrue(loaded.isSlotIncluded("u", "w", "f"));
		assertFalse(loaded.isSlotIncluded("u", "w", "g"));
	}

}
