/*******************************************************************************
 * Copyright (c) 2016 University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Antonio Garcia-Dominguez - initial API and implementation
 *******************************************************************************/
package uk.ac.york.mondo.integration.hawk.emf.emfsplitter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.hawk.core.IMetaModelResourceFactory;
import org.hawk.core.IStateListener.HawkState;
import org.hawk.core.IVcsManager;
import org.hawk.core.query.IQueryEngine;
import org.hawk.core.runtime.LocalHawkFactory;
import org.hawk.emf.EMFPackage;
import org.hawk.emf.metamodel.EMFMetaModelResource;
import org.hawk.emf.metamodel.EMFMetaModelResourceFactory;
import org.hawk.emfresource.impl.LocalHawkResourceImpl;
import org.hawk.epsilon.emc.EOLQueryEngine;
import org.hawk.orientdb.OrientDatabase;
import org.hawk.osgiserver.HModel;
import org.hawk.ui2.util.HUIManager;
import org.hawk.workspace.Workspace;
import org.mondo.modular.references.ext.IEditorCrossReferences;

/**
 * Integrates Hawk into the cross reference selector dialog for EMF-Splitter. At
 * the moment, it indexes the whole workspace into a local Hawk instance, which
 * is directly accessed through the underlying graph.
 */
public class HawkCrossReferences implements IEditorCrossReferences {

	private static final String HAWK_INSTANCE = "emfsplitter";

	@Override
	public boolean init(List<String> metamodelURIs, String modularNature) {
		try {
			final HModel hm = getHawkInstance();

			if (!hm.getRegisteredMetamodels().containsAll(metamodelURIs)) {
				final List<File> dumped = new ArrayList<>();

				try {
					final IMetaModelResourceFactory emfFactory = hm.getIndexer()
							.getMetaModelParser(EMFMetaModelResourceFactory.class.getName());

					for (String metamodelURI : metamodelURIs) {
						if (hm.getRegisteredMetamodels().contains(metamodelURI)) {
							continue;
						}

						final EPackage epkg = EPackage.Registry.INSTANCE.getEPackage(metamodelURI);
						if (epkg == null) {
							throw new NoSuchElementException(
									String.format("No metamodel with URL '%s' is available in the global EMF registry.",
											metamodelURI));
						}

						final String pkgEcore = emfFactory.dumpPackageToString(
								new EMFPackage(epkg, new EMFMetaModelResource(epkg.eResource(), emfFactory)));
						final File tmpEcore = File.createTempFile("tmp", ".ecore");
						try (final FileOutputStream fOS = new FileOutputStream(tmpEcore)) {
							fOS.write(pkgEcore.getBytes());
						}
						dumped.add(tmpEcore);
					}

					hm.getIndexer().registerMetamodels(dumped.toArray(new File[dumped.size()]));
				} finally {
					for (File tmpEcore : dumped) {
						if (tmpEcore.exists()) {
							tmpEcore.delete();
						}
					}
				}

				hm.sync();
			}
		} catch (Exception e) {
			HawkCrossReferencesPlugin.getDefault().logError(e);
			return false;
		}
		return true;
	}

	@Override
	public boolean finish(String modularNature) {
		// Nothing to do - Hawk shuts down by itself using a workbench listener
		return true;
	}

	@Override
	public boolean isGlobal() {
		// The Hawk Workspace VCS doesn't allow filtering projects yet
		return true;
	}

	@Override
	public boolean isNature(String modularNature) {
		// The Hawk Workspace VCS doesn't allow filtering projects yet
		return true;
	}

	@Override
	public EList<?> getChoicesOfValues(final String modularNature, final Resource res, final boolean searchAll, final EClass anEClass, final String eolFilter) {
		LocalHawkResourceImpl hawkResource = null;
		try {
			final HModel hawkInstance = getHawkInstance();

			// Look for the Hawk resource in the resource set first
			for (Resource r : res.getResourceSet().getResources()) {
				if (r instanceof LocalHawkResourceImpl) {
					hawkResource = (LocalHawkResourceImpl) r;
					if (hawkResource.getIndexer().getName().equals(HAWK_INSTANCE)) {
						// This is the EMF-Splitter local Hawk resource
						break;
					}
				}
			}

			// If it's not in the resource set, load it and add it to the resource set
			if (hawkResource == null) {
				hawkResource = new LocalHawkResourceImpl(URI.createURI("hawk://"), hawkInstance.getIndexer(), true, Arrays.asList("*"), Arrays.asList("*"));
				res.getResourceSet().getResources().add(hawkResource);
				hawkResource.load(null);
			}

			// Construct the select condition
			String selectCondition = eolFilter;
			final Map<String, Object> queryArguments = new HashMap<>();
			if (!searchAll) {
				if (selectCondition != null) {
					selectCondition += " and ";
				} else {
					selectCondition = "";
				}

				final String repoURL = new Workspace().getLocation();
				String filePath = res.getURI().toString();
				if (filePath.startsWith(repoURL)) {
					filePath = filePath.substring(repoURL.length());
				}
				queryArguments.put("repoURL", repoURL);
				queryArguments.put("filePath", filePath);

				selectCondition += "self.isContainedWithin(repoURL, filePath)";
			}

			// Construct the full EOL query
			String query = String.format("return `%s`::`%s`.all", anEClass.getEPackage().getNsURI(), anEClass.getName());
			if (selectCondition != null) {
				query += String.format(".select(self|%s)", selectCondition);
			}
			query += ";";

			// Run the query
			final Map<String, Object> context = new HashMap<>();
			context.put(IQueryEngine.PROPERTY_ARGUMENTS, queryArguments);
			context.put(IQueryEngine.PROPERTY_FILECONTEXT, "*");
			context.put(IQueryEngine.PROPERTY_REPOSITORYCONTEXT, "*");
			EList<EObject> instances = hawkResource.fetchByQuery(EOLQueryEngine.TYPE, query, context);

			// Optionally, filter by project nature
			if (modularNature != null) {
				final List<String> acceptedPrefixes = new ArrayList<>();
				for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
					if (project.isOpen() && project.getNature(modularNature) != null) {
						String prefix = URI.createPlatformResourceURI(project.getFullPath().toString(), false).path() + "/";
						acceptedPrefixes.add(prefix);
					}
				}

				filterByNature:
				for (Iterator<EObject> itInstance = instances.iterator(); itInstance.hasNext(); ) {
					final EObject eob = itInstance.next();
					for (String prefix : acceptedPrefixes) {
						final String path = eob.eResource().getURI().path();
						if (path.startsWith(prefix)) {
							continue filterByNature;
						}
					}
					itInstance.remove();
				}
			}

			return instances;
		} catch (Exception e) {
			HawkCrossReferencesPlugin.getDefault().logError(e);
			return new BasicEList<Object>();
		}
	}

	/**
	 * Returns the Hawk instance that indexes the whole workspace, ensuring that
	 * it exists and that it is in the {@link HawkState#RUNNING} state.
	 */
	protected HModel getHawkInstance() throws Exception {
		final HUIManager hawkManager = HUIManager.getInstance();
		synchronized (hawkManager) {
			HModel hawkInstance = hawkManager.getHawkByName(HAWK_INSTANCE);
			if (hawkInstance == null) {
				// TODO: use a path within the workspace directory?
				final File storageFolder = new File("hawk");

				// TODO: limit plugins to EMF, use Neo4j if available
				hawkInstance = HModel.create(new LocalHawkFactory(), HAWK_INSTANCE, storageFolder,
							storageFolder.toURI().toASCIIString(), OrientDatabase.class.getName(), null, hawkManager,
							hawkManager.getCredentialsStore(), 0, 0);

				final IVcsManager repo = new Workspace();
				hawkInstance.addVCS(repo.getLocation(), repo.getClass().getName(), "", "", false);
			}

			if (!hawkInstance.isRunning()) {
				hawkInstance.start(hawkManager);
				hawkInstance.getIndexer().waitFor(HawkState.UPDATING, 3000);
				hawkInstance.getIndexer().waitFor(HawkState.RUNNING);
			}
			return hawkInstance;
		}
	}

}
