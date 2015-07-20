package uk.ac.york.mondo.integration.hawk.emf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import uk.ac.york.mondo.integration.api.ScalarList._Fields;

public class HawkResourceImpl extends ResourceImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(HawkResourceImpl.class);

	private String hawkURL;
	private String hawkInstance;
	private String hawkRepository;
	private String[] hawkFilePatterns;

	private Map<String, EObject> nodeIdToEObjectMap;

	public HawkResourceImpl() {}

	public HawkResourceImpl(URI uri) {
		super(uri);
	}

	public String getHawkURL() {
		return hawkURL;
	}

	public void setHawkURL(String hawkURL) {
		this.hawkURL = hawkURL;
	}

	public String getHawkInstance() {
		return hawkInstance;
	}

	public void setHawkInstance(String hawkInstance) {
		this.hawkInstance = hawkInstance;
	}

	public String getHawkRepository() {
		return hawkRepository;
	}

	public void setHawkRepository(String hawkRepository) {
		this.hawkRepository = hawkRepository;
	}

	public String[] getHawkFilePatterns() {
		return hawkFilePatterns;
	}

	public void setHawkFilePatterns(String[] hawkFilePatterns) {
		this.hawkFilePatterns = hawkFilePatterns;
	}

	@Override
	protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
		final Properties props = new Properties();
		props.load(inputStream);

		this.hawkURL = requiredProperty(props, "hawk.url");
		this.hawkInstance = requiredProperty(props, "hawk.instance");
		this.hawkRepository = optionalProperty(props, "hawk.repository", "*");
		this.hawkFilePatterns = optionalProperty(props, "hawk.files", "*").split(",");
		try {
			final Hawk.Client client = new Hawk.Client(new TCompactProtocol(new THttpClient(hawkURL)));
			final List<ModelElement> elems = client.getModel(
				hawkInstance, hawkRepository, Arrays.asList(hawkFilePatterns)
			);

			// Do a first pass, creating all the objects with their attributes and saving their graph IDs
			// into the One Map to bind them.
			final Registry packageRegistry = getResourceSet().getPackageRegistry();
			this.nodeIdToEObjectMap = new HashMap<>();
			for (ModelElement me : elems) {
				final EPackage pkg = packageRegistry.getEPackage(me.metamodelUri);
				final EClassifier eClassifier = pkg.getEClassifier(me.typeName);
				if (!(eClassifier instanceof EClass)) {
					throw new IOException(String.format("Received an element of type '%s', which is not an EClass", eClassifier));
				}
				final EClass eClass = (EClass)eClassifier;

				final EFactory factory = packageRegistry.getEFactory(me.metamodelUri);
				final EObject obj = factory.create(eClass);
				nodeIdToEObjectMap.put(me.id, obj);

				for (AttributeSlot s : me.attributes) {
					if (!s.isSet) continue;
					setStructuralFeatureFromSlot(eClass, obj, s);
				}
			}

			// TODO On the second pass, fill in the references
		} catch (TException e) {
			throw new IOException(e);
		}
	}

	private EStructuralFeature setStructuralFeatureFromSlot(
		final EClass eClass, final EObject eObject, AttributeSlot slot
	) throws IOException {
		assert slot.isSet : "This method should only be called for slots with set values";

		final EStructuralFeature feature = eClass.getEStructuralFeature(slot.name);
		final EClassifier eType = feature.getEType();

		if (feature.isMany()) {
			switch (eType.getClassifierID()) {
			case EcorePackage.EBYTE:
				if (!slot.values.isSetVBytes()) {
					throw new IOException(String.format(
						"Expected to receive bytes for feature '%s' in type '%s', but did not",
						feature.getName(), eClass.getName()));
				} else {
					final EList<Byte> bytes = new BasicEList<Byte>(slot.values.getVBytes().length);
					for (byte b : slot.values.getVBytes()) {
						bytes.add(b);
					}
					eObject.eSet(feature, bytes);
				}
				break;

			case EcorePackage.EFLOAT:
				if (!slot.values.isSetVDoubles()) {
					throw new IOException(String.format(
							"Expected to receive doubles for feature '%s' in type '%s', but did not",
							feature.getName(), eClass.getName()));
					
				} else {
					final EList<Float> floats = new BasicEList<Float>(slot.values.getVDoubles().size());
					for (double d : slot.values.getVDoubles()) {
						floats.add((float)d);
					}
					eObject.eSet(feature, floats);
				}
				break;

			case EcorePackage.EDOUBLE:
				setManyStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, _Fields.V_DOUBLES); break;
			case EcorePackage.EINT:
				setManyStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, _Fields.V_INTEGERS); break;
			case EcorePackage.ELONG:
				setManyStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, _Fields.V_LONGS); break;
			case EcorePackage.ESHORT:
				setManyStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, _Fields.V_SHORTS); break;
			case EcorePackage.ESTRING:
				setManyStructuralFeatureWithExpectedType(eClass, eObject, slot, feature, _Fields.V_STRINGS); break;
			default:
				throw new IOException(String.format("Unknown data type %s with isMany = true", eType.getName()));
			}
		} else {
			switch (eType.getClassifierID()) {
			case EcorePackage.EBYTE_ARRAY:
				// TODO same as isMany + BYTE?
				break;
			// TODO rest of the types
			default:
				throw new IOException(String.format("Unknown data type %s with isMany = false", eType.getName()));
			}
		}
		return feature;
	}

	private void setManyStructuralFeatureWithExpectedType(final EClass eClass,
			final EObject eObject, AttributeSlot slot,
			final EStructuralFeature feature, final _Fields expectedType)
			throws IOException {
		if (!slot.values.isSet(expectedType)) {
			throw new IOException(String.format(
					"Expected to receive '%s' for feature '%s' in type '%s', but did not",
					expectedType, feature.getName(), eClass.getName()));
		} else {
			eObject.eSet(feature, ECollections.toEList((Iterable<?>) slot.values.getFieldValue(expectedType)));
		}
	}

	private String requiredProperty(Properties props, String name) throws IOException {
		final String value = (String) props.get(name);
		if (value == null) {
			throw new IOException(name + " has not been set");
		}
		return value;
	}

	private String optionalProperty(Properties props, String name, String defaultValue) throws IOException {
		final String value = (String) props.get(name);
		if (value == null) {
			LOGGER.info("{} has not been set, using {} as default", name, defaultValue);
			return defaultValue;
		}
		return value;
	}
}
