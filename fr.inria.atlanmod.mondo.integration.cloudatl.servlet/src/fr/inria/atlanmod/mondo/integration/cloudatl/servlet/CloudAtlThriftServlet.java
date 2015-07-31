/*******************************************************************************
 * Copyright (c) 2015 Atlanmod.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Abel Gómez - initial API and implementation
 *******************************************************************************/
package fr.inria.atlanmod.mondo.integration.cloudatl.servlet;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.JobStatus;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.server.TServlet;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.osgi.framework.Bundle;

import uk.ac.york.mondo.integration.api.CloudATL;
import uk.ac.york.mondo.integration.api.InvalidModelSpec;
import uk.ac.york.mondo.integration.api.InvalidTransformation;
import uk.ac.york.mondo.integration.api.ModelSpec;
import uk.ac.york.mondo.integration.api.TransformationStatus;
import uk.ac.york.mondo.integration.api.TransformationTokenNotFound;
import fr.inria.atlanmod.atl_mr.ATLMRMapper;
import fr.inria.atlanmod.atl_mr.ATLMRMaster;
import fr.inria.atlanmod.atl_mr.ATLMRReducer;
import fr.inria.atlanmod.atl_mr.builder.RecordBuilder;
import fr.inria.atlanmod.atl_mr.builder.RecordBuilder.Builder;
import fr.inria.atlanmod.atl_mr.utils.ATLMRUtils;

/**
 * Entry point to the Cloud-based ATL engine. This servlet exposes a
 * Thrift-based API.
 *
 * @author agomez
 *
 */
public class CloudAtlThriftServlet extends TServlet {

	/**
	 * Dummy implementation of the class controlling the CloudATL cluster
	 * 
	 * @author agomez
	 *
	 */
	private static class DummyIface implements CloudATL.Iface {

		@SuppressWarnings("unused")
		private class TransformationInformation {
			private String transformation;
			private ModelSpec source;
			private ModelSpec target;
			private TransformationStatus status;
		}

		/**
		 * Transient {@link Map} that mimics the behavior of a Job Registry
		 */
		private Map<String, TransformationInformation> transformations = new ConcurrentHashMap<>();

		@Override
		public String launch(String transformation, ModelSpec source, ModelSpec target) throws InvalidTransformation, InvalidModelSpec, TException {
			String id = UUID.randomUUID().toString();

			TransformationInformation information = new TransformationInformation();

			information.transformation = transformation;
			information.source = source;
			information.target = target;

			information.status = new TransformationStatus();
			information.status.setElapsed(0);
			information.status.setFinished(false);
			information.status.setError("");

			transformations.put(id, information);

			return id;
		}

		@Override
		public List<String> getJobs() throws TException {
			List<String> ids = new ArrayList<String>();
			ids.addAll(transformations.keySet());
			return ids;
		}

		@Override
		public TransformationStatus getStatus(String token) throws TransformationTokenNotFound, TException {
			return getTransformationInformation(token).status;
		}

		@Override
		public void kill(String token) throws TransformationTokenNotFound, TException {
			getTransformationInformation(token).status.finished = true;
		}

		/**
		 * Returns the {@link TransformationInformation}
		 * 
		 * @param token
		 *            The transformation Id
		 * @return
		 * @throws TransformationTokenNotFound
		 *             Transformation id not found
		 */
		private TransformationInformation getTransformationInformation(String token) throws TransformationTokenNotFound {
			TransformationInformation information = transformations.get(token);
			if (information != null) {
				return information;
			}
			throw new TransformationTokenNotFound(token);
		}
	}

	/**
	 * {@link CloudATL.Iface} implementation for controlling a real CloudATL
	 * cluster running on top of Hadoop
	 * 
	 * @author agomez
	 *
	 */
	@SuppressWarnings("unused")
	private static class CloudATLIface implements CloudATL.Iface {

		private Cluster cluster;
		private Configuration configuration;
		
		public CloudATLIface() throws MalformedURLException, IOException {
			this.cluster = new Cluster(getConfiguration());
		}
		
		@Override
		public String launch(String transformation, ModelSpec source, ModelSpec target) throws InvalidTransformation, InvalidModelSpec, TException {

			try {
				
				Job job = Job.getInstance(getConfiguration(), ATLMRMaster.DEFAULT_JOB_NAME);
				
				Configuration conf = job.getConfiguration();
				conf.set("mapreduce.app-submission.cross-platform", "true");

				// Configure classes
				job.setJarByClass(ATLMRMaster.class);
				job.setMapperClass(ATLMRMapper.class);
				job.setReducerClass(ATLMRReducer.class);
				job.setInputFormatClass(NLineInputFormat.class);
				job.setOutputFormatClass(SequenceFileOutputFormat.class);
				job.setMapOutputKeyClass(Text.class);
				job.setMapOutputValueClass(BytesWritable.class);
				job.setNumReduceTasks(1);
				
				// Configure MapReduce input/outputs
				ResourceSet resourceSet = new ResourceSetImpl();
				ATLMRUtils.configureRegistry(conf);
				
				Builder builder = new RecordBuilder.Builder(
						URI.createURI(source.getUri()),
						Arrays.asList(new URI[]{ URI.createURI(source.getMetamodelUris().get(0)) } ));
				
				Path recordsPath = new Path("/tmp/" + UUID.randomUUID().toString() + ".rec");
				FileSystem recordsFileSystem = FileSystem.get(recordsPath.toUri(), conf);
				
				builder.save(recordsFileSystem.create(recordsPath));
				
				FileInputFormat.setInputPaths(job, recordsPath);
				String timestamp = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
				String outDirName = "atlmr-out-" + timestamp + "-" + UUID.randomUUID();
				FileOutputFormat.setOutputPath(job, new Path(job.getWorkingDirectory().suffix(Path.SEPARATOR + outDirName).toUri()));

				// Configure records per map
				InputStream inputStream = recordsFileSystem.open(recordsPath);
				long linesPerMap = (long) Math.ceil((double) ATLMRMaster.countLines(inputStream) / 1);
				job.getConfiguration().setLong(NLineInputFormat.LINES_PER_MAP, linesPerMap);

				recordsFileSystem.close();
				
				// Configure ATL related inputs/outputs
				job.getConfiguration().set(ATLMRMaster.TRANSFORMATION, transformation);
				job.getConfiguration().set(ATLMRMaster.SOURCE_METAMODEL, source.getMetamodelUris().get(0));
				job.getConfiguration().set(ATLMRMaster.TARGET_METAMODEL, target.getMetamodelUris().get(0));
				job.getConfiguration().set(ATLMRMaster.INPUT_MODEL, source.getUri());
				job.getConfiguration().set(ATLMRMaster.OUTPUT_MODEL, target.getUri());
				
				Bundle bundle = Platform.getBundle(CloudAtlServletPlugin.PLUGIN_ID);
				IPath path = new org.eclipse.core.runtime.Path("libs");
				URL fileURL = FileLocator.find(bundle, path, null);
				
				String localJarsDir = new File(FileLocator.resolve(fileURL).toURI()).getAbsolutePath();
				String hdfsJarsDir = "/temp/hadoop/atlrm/libs";
				
				// TODO: This JobHelper needs to be updated to the new API
				JobHelper.copyLocalJarsToHdfs(localJarsDir, hdfsJarsDir, conf);
				JobHelper.addHdfsJarsToDistributedCache(hdfsJarsDir, configuration);
				
				Logger.getGlobal().log(Level.INFO, "Sending Job");
				job.submit();
				Logger.getGlobal().log(Level.INFO, "Job sent");
				
				return job.getJobID().toString();

			} catch (IOException | InterruptedException | ClassNotFoundException | URISyntaxException e) {
				throw new TException(e);
			}
		}

		@Override
		public List<String> getJobs() throws TException {
			List<String> jobs = new ArrayList<>();
			try {
				for (JobStatus status : cluster.getAllJobStatuses()) {
					jobs.add(status.getJobID().toString());
				}
			} catch (IOException | InterruptedException e) {
				throw new TException(e);
			}
			return jobs;
		}

		@Override
		public TransformationStatus getStatus(String token) throws TransformationTokenNotFound, TException {
			TransformationStatus transformationStatus = new TransformationStatus();
			try {
				Job job = cluster.getJob(JobID.forName(token));
				if (job.getStatus().isJobComplete()) {
					transformationStatus.setElapsed(job.getFinishTime() - job.getStartTime());
				} else {
					transformationStatus.setElapsed(System.currentTimeMillis() - job.getStartTime());
				}
				transformationStatus.setError(job.getStatus().getFailureInfo());
				transformationStatus.setFinished(job.getStatus().isJobComplete());
			} catch (IOException | InterruptedException e) {
				throw new TException(e);
			}
			return transformationStatus;

		}

		@Override
		public void kill(String token) throws TransformationTokenNotFound, TException {
			try {
				Job job = cluster.getJob(JobID.forName(token));
				job.killJob();
			} catch (IOException | InterruptedException e) {
				throw new TException(e);
			}
		}

		
		private synchronized Configuration getConfiguration() throws MalformedURLException {
			if (configuration == null) {
				Configuration conf = new Configuration();
				String confDirPath = System.getProperty("org.hadoop.conf.dir", System.getenv("HADOOP_CONF_DIR"));
				
				if (confDirPath != null) {
					File folder = new File(confDirPath);
					if (!folder.exists() || !folder.isDirectory()) {
						Logger.getGlobal().log(Level.WARNING, MessageFormat.format("Configuration folder ''{0}'' is invalid", confDirPath));
					}
					File[] configFiles = folder.listFiles(new FileFilter() {
						@Override
						public boolean accept(File pathname) {
							return pathname.getName().endsWith("xml");
						}
					});
					
					for (File file : configFiles) {
						conf.addResource(file.toURI().toURL());
					}
					
					conf.reloadConfiguration();
					configuration = conf;
				}
			}
			
			return configuration;
		}
	}

	private static final long serialVersionUID = 1L;

	public CloudAtlThriftServlet() throws Exception {
		super(new CloudATL.Processor<CloudATL.Iface>(new CloudATLIface()), new TTupleProtocol.Factory());
	}
}
