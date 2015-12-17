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
package uk.ac.york.mondo.integration.hawk.emf.ifcexport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.bimserver.emf.IdEObject;
import org.bimserver.emf.IfcModelInterfaceException;
import org.bimserver.emf.MetaDataManager;
import org.bimserver.emf.PackageMetaData;
import org.bimserver.ifc.BasicIfcModel;
import org.bimserver.ifc.step.serializer.Ifc2x3tc1StepSerializer;
import org.bimserver.ifc.step.serializer.Ifc4StepSerializer;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.models.ifc4.Ifc4Package;
import org.bimserver.plugins.Plugin;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginDescriptor;
import org.bimserver.plugins.PluginImplementation;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.PluginSourceType;
import org.bimserver.plugins.serializers.ProgressReporter;
import org.bimserver.plugins.serializers.ProjectInfo;
import org.bimserver.plugins.serializers.Serializer;
import org.bimserver.plugins.serializers.SerializerException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.hawk.ifc.IFCModelResource;
import org.osgi.framework.FrameworkUtil;

import uk.ac.york.mondo.integration.hawk.emf.HawkModelDescriptor;
import uk.ac.york.mondo.integration.hawk.emf.impl.HawkResourceImpl;

public class IFCExportCommand extends AbstractHandler {

	private final class IFCExportJobFunction implements IJobFunction {
		private final File dest;
		private final IFile hawkModel;

		private IFCExportJobFunction(File dest, IFile hawkModel) {
			this.dest = dest;
			this.hawkModel = hawkModel;
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			final String pluginId = FrameworkUtil.getBundle(IFCExportCommand.class).getSymbolicName();
			try {
				exportToSTEP(hawkModel, dest, monitor);
				return new Status(IStatus.OK, pluginId, "Completed export");
			} catch (Exception e) {
				e.printStackTrace();
				return new Status(IStatus.ERROR, pluginId, e.getMessage());
			} finally {
				monitor.done();
			}
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ISelection selection0 = HandlerUtil.getCurrentSelection(event);
		if (selection0 instanceof IStructuredSelection) {
			final IStructuredSelection selection = (IStructuredSelection) selection0;
			final IFile hawkModel = (IFile) selection.getFirstElement();
			final File dest = hawkModel.getLocation().removeFileExtension().addFileExtension("ifc").toFile();

			final String jobName = "Export " + hawkModel.getName() + " to " + dest;
			final Job job = Job.create(jobName, new IFCExportJobFunction(dest, hawkModel));
			job.schedule();
		}

		return null;
	}

	protected void exportToSTEP(final IFile hawkModel, final File dest, IProgressMonitor monitor) throws IOException, FileNotFoundException,
			Exception, IfcModelInterfaceException, SerializerException, CoreException {
		monitor.beginTask("Exporting " + hawkModel.getName() + " to " + dest.getName(), 4);

		monitor.subTask("Loading descriptor");
		final HawkModelDescriptor desc = new HawkModelDescriptor();
		desc.load(new FileReader(hawkModel.getLocation().toFile()));
		desc.setSplit(false);
		monitor.worked(1);

		monitor.subTask("Loading remote Hawk resource");
		final URI emfURI = URI.createURI(hawkModel.getLocationURI().toString());
		final ResourceSet rs = new ResourceSetImpl();
		final HawkResourceImpl resource = new HawkResourceImpl(emfURI, desc);
		rs.getResources().add(resource);
		resource.load(null);
		monitor.worked(1);

		monitor.subTask("Populating IFC serializer");
		Serializer serializer = null;
		long oid = 0;
		for (TreeIterator<EObject> it = resource.getAllContents(); it.hasNext(); ) {
			EObject eo = it.next();

			if (eo.eClass().getEAnnotation("wrapped") != null) {
				// this is a wrapped object: no need to add it explicitly
			}
			else if (eo instanceof IdEObject) {
				if (serializer == null) {
					serializer = createSerializer(eo.eClass().getEPackage().getNsURI());
				}

				final IdEObject idEObj = (IdEObject)eo;
				serializer.getModel().add(oid, idEObj);
				oid++;
			}
		}
		serializer.getModel().generateMinimalExpressIds();
		monitor.worked(1);

		monitor.subTask("Writing STEP file");
		serializer.writeToFile(dest, new ProgressReporter() {
			@Override
			public void update(long progress, long max) {
				System.out.println(String.format("Exporting (%d/%d)", progress, max));
			}
		});
		monitor.worked(1);

		hawkModel.getParent().refreshLocal(IResource.DEPTH_ONE, null);
	}

	private Serializer createSerializer(final String nsURI) throws Exception {
		Serializer ser;
		String packageLowerCaseName;

		switch (nsURI) {
		case Ifc4Package.eNS_URI:
			ser = new Ifc4StepSerializer(new PluginConfiguration());
			packageLowerCaseName = Ifc4Package.eINSTANCE.getName().toLowerCase();
			break;
		default:
			ser = new Ifc2x3tc1StepSerializer(new PluginConfiguration());
			packageLowerCaseName = Ifc2x3tc1Package.eINSTANCE.getName().toLowerCase();
			break;
		}

		PluginManager bimPluginManager = createPluginManager();
		MetaDataManager bimMetaDataManager = new MetaDataManager(bimPluginManager);
		bimMetaDataManager.init();
		final PackageMetaData packageMetaData = bimMetaDataManager.getPackageMetaData(packageLowerCaseName);
		final BasicIfcModel ifcModel = new BasicIfcModel(packageMetaData, null);
		ser.init(ifcModel, new ProjectInfo(), bimPluginManager, null, packageMetaData, false);

		return ser;
	}

	@SuppressWarnings("unchecked")
	private PluginManager createPluginManager() throws Exception {
		final PluginManager bimPluginManager = new PluginManager();
		final InputStream isBIMPluginXML = IFCModelResource.class.getResourceAsStream("/plugin/plugin.xml");
		final PluginDescriptor desc = readPluginDescriptor(isBIMPluginXML);
		for (PluginImplementation impl : desc.getImplementations()) {
			final Class<? extends Plugin> interfaceClass = (Class<? extends Plugin>) Class.forName(impl.getInterfaceClass());
			final Class<?> implClass = Class.forName(impl.getImplementationClass());
			final Plugin plugin = (Plugin) implClass.newInstance();
			bimPluginManager.loadPlugin(interfaceClass, "", "", plugin,
					this.getClass().getClassLoader(),
					PluginSourceType.INTERNAL, impl);
		}
		return bimPluginManager;
	}

	private PluginDescriptor readPluginDescriptor(InputStream is) throws JAXBException {
		JAXBContext ctx = JAXBContext.newInstance(PluginDescriptor.class);
		Unmarshaller unm = ctx.createUnmarshaller();
		return (PluginDescriptor) unm.unmarshal(is);
	}

}
