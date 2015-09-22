package fr.inria.atlanmod.mondo.integration.cloudatl.servlet;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;

public class JobHelper {
    private static final Logger logger = Logger.getLogger(JobHelper.class.getCanonicalName());

    public static void copyLocalJarsToHdfs(String localJarsDir, String hdfsJarsDir, Configuration configuration) throws IOException {
        checkRequiredArgument(localJarsDir, "Local JARs dir is null");
        checkRequiredArgument(hdfsJarsDir, "HDFS JARs dir is null");
        checkRequiredArgument(configuration, "Configuration is null");

        Set<File> jarFiles = collectJarFilesFromLocalDir(localJarsDir);

        if (jarFiles.isEmpty()) {
            logger.info(MessageFormat.format("No JAR files found for copying to HDFS under local dir: {0}", localJarsDir));
        } else {
            logger.info(MessageFormat.format("Copying {0} JAR files from local dir ({1}) to HDFS dir ({2} at {3})", jarFiles.size(), localJarsDir, hdfsJarsDir, resolveHdfsAddress(configuration)));
            FileSystem hdfsFileSystem = FileSystem.get(configuration);

            for (File jarFile : jarFiles) {
                Path localJarPath = new Path(jarFile.toURI());
                Path hdfsJarPath = new Path(hdfsJarsDir, jarFile.getName());
                hdfsFileSystem.copyFromLocalFile(false, true, localJarPath, hdfsJarPath);
            }
        }
    }

    private static void checkRequiredArgument(Object argument, String errorMessage) {
        if (argument == null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static Set<File> collectJarFilesFromLocalDir(String localJarsDirPath) {
        File directoryFile = new File(localJarsDirPath);
        if (!directoryFile.exists()) {
            throw new IllegalArgumentException("No directory found at local path: " + localJarsDirPath);
        }
        if (!directoryFile.isDirectory()) {
            throw new IllegalArgumentException("Path points to file, not directory: " + localJarsDirPath);
        }

        Set<File> jarFiles = new HashSet<File>();
        for (File libFile : directoryFile.listFiles()) {
            if (libFile.exists() && !libFile.isDirectory() && libFile.getName().endsWith(".jar")) {
                jarFiles.add(libFile);
            }
        }
        return jarFiles;
    }

    public static void addHdfsJarsToDistributedCache(String hdfsJarsDir, Job job) throws IOException {
        checkRequiredArgument(hdfsJarsDir, "HDFS JARs dir is null");
        checkRequiredArgument(job, "Job is null");

        Set<Path> jarPaths = collectJarPathsOnHdfs(hdfsJarsDir, job.getConfiguration());
        if (!jarPaths.isEmpty()) {
            logger.info(MessageFormat.format("Adding following JARs to distributed cache: {0}", jarPaths));
            System.setProperty("path.separator", ":"); // due to https://issues.apache.org/jira/browse/HADOOP-9123

            for (Path jarPath : jarPaths) {
                job.addFileToClassPath(jarPath);
            }
        }
    }

    private static Set<Path> collectJarPathsOnHdfs(String hdfsJarsDir, Configuration configuration) throws IOException {
        Set<Path> jarPaths = new HashSet<Path>();
        FileSystem fileSystem = FileSystem.get(configuration);
        Path jarsDirPath = new Path(hdfsJarsDir);
        if (!fileSystem.exists(jarsDirPath)) {
            throw new IllegalArgumentException("Directory '" + hdfsJarsDir + "' doesn't exist on HDFS (" + resolveHdfsAddress(configuration) + ")");
        }
        if (fileSystem.isFile(jarsDirPath)) {
            throw new IllegalArgumentException("Path '" + hdfsJarsDir + "' on HDFS (" + resolveHdfsAddress(configuration) + ") is file, not directory");
        }

        FileStatus[] fileStatuses = fileSystem.listStatus(jarsDirPath);
        for (FileStatus fileStatus : fileStatuses) {
            if (!fileStatus.isDirectory()) {
                jarPaths.add(fileStatus.getPath());
            }
        }
        return jarPaths;
    }

    private static String resolveHdfsAddress(Configuration configuration) {
        return configuration.get("fs.default.name");
    }
}