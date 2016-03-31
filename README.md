mondo-integration
=================

Integrates the components developed within the [EU MONDO project](http://www.mondo-project.org/).

Overall structure and dependencies
----------------------------------

The main artefacts developed within this project are:

* `uk.ac.york.mondo.integration.server.product`, an Eclipse product for a [Jetty](http://www.eclipse.org/jetty/) application server that hosts the integrated MONDO components and provides [Apache Thrift](https://thrift.apache.org/)-based APIs to access them remotely. Clients targeting this API are included in the `uk.ac.york.mondo.integration.updatesite` Eclipse P2 update site (GUI-based clients and OSGi console extensions) and `uk.ac.york.mondo.integration.clients.cli.product` Eclipse product (command-line client).
* `uk.ac.york.mondo.integration.eclipse.product`, an Eclipse product with a custom Mars SR.1 distribution that includes all the Eclipse-based tools in the MONDO project.

The project has the following external dependencies:

* Most of the OSGi-ready dependencies come from the `uk.ac.york.mondo.integration.targetplatform` target platform definition. Due to conflicting licenses, developers are expected to build the [Hawk GPL](https://github.com/kb634/mondo-hawk) update site on their own and edit the target platform to refer to their locally built archive. Please refer to [mondo-hawk/README.md](https://github.com/kb634/mondo-hawk/blob/master/README.md) for instructions. The Hawk GPL update site must be served through a local web server on port 8000: in most UNIX environments, changing to its directory and running `python -m SimpleHTTPServer 8000` should be enough.
* The `atl-mr` Eclipse project of the [bluezio/integrate-hawk-emf](https://github.com/bluezio/ATL_MR) branch of the [ATL/MapReduce](https://github.com/atlanmod/atl_mr) project.
* Some of the plugins in the [org.eclipse.atl.atlMR](https://github.com/atlanmod/org.eclipse.atl.atlMR) project (see the CloudATL section for details).
* The [Apache Artemis](https://activemq.apache.org/artemis/) core client and server libraries. These are listed in `uk.ac.york.mondo.integration.artemis/ivy.xml`, so they are easy to download using [IvyDE](https://ant.apache.org/ivy/ivyde/).

Compiled versions of the remote client components are available as an Eclipse update site and as a Maven repository [here](http://mondo-project.github.io/mondo-updates/).

Getting started
---------------

We recommend using Eclipse Modeling (Luna or later).

* Go to the [MONDO updates](http://mondo-project.github.io/mondo-updates/) site and install the fix for SNI updates.
* Import all `uk.ac.york.mondo.integration.*` projects from this repository.

### Artemis dependencies

The dependencies for the project using Artemis are located in `uk.ac.york.mondo.integration.artemis`. There are two ways to fetch the dependencies:

1. Use the Apache IvyDE plug-in. Install everything from the [IvyDE update site](http://www.apache.org/dist/ant/ivyde/updatesite/). Right click the `fetch-deps.xml` file and choose **Run as** | **Ant Build**. If this does not work, go to option 2.
2. Use the command-line tool. Install the `ivy` Ubuntu package and issue the following command in the repository:

    ```
    ant -Dnative-package-type=jar -lib /usr/share/java/ivy.jar -f uk.ac.york.mondo.integration.artemis/fetch-deps.xml
    ```

You may have to run the Ant job _twice_ in order to succeed.

### Setting the target platform
Before attempting to resolve the target platform, don't forget to start the local webserver in the mondo-hawk directory:

    cd mondo-hawk/org.hawk.updateiste/hawk-gpl
    python -m SimpleHTTPServer 8000

Go to the `uk.ac.york.mondo.integration.targetplatform`, open the `uk.ac.york.mondo.thrift.osgi.example.targetplatform.target` Target Definition file and click **Set as Target Platform**. 
    
API
---

The Thrift APIs are defined in `uk.ac.york.mondo.integration.api`, particularly in the `src/api.emf` [Emfatic](https://www.eclipse.org/emfatic/) file. Emfatic produces an `.ecore` file from it, which we transform to the `api.thrift` Thrift IDL file using the [ecore2thrift](https://github.com/Taneb/ecore2thrift/) plugin. The Thrift compiler then uses `api.thrift` to generate Java and JavaScript client and server stubs (Client and Processor), as usual.

The same `.api` project also provides some utility methods and classes in its `api.utils` package, which is not manually generated. It is recommended to use `APIUtils` to connect to the Thrift APIs instead of using the Thrift classes directly.

Eclipse workbench
------

The MONDO Eclipse workbench product (`uk.ac.york.mondo.integration.eclipse.product`) runs on Mars SR.1, which has some graphics corruption issues in some GNU/Linux distributions. GNU/Linux users are recommended to use the provided `run-eclipse.sh` script, which sets up the environment variable appropriately to avoid these issues.

Server
------

The MONDO server product (`uk.ac.york.mondo.integration.server.product`) consists of several servlets that implement the Thrift APIs for the various MONDO components, plus two customizations for the standard OSGi HttpService: `uk.ac.york.mondo.integration.server.logback` binds [SLF4J](http://www.slf4j.org/) to the [Logback](http://logback.qos.ch/) library, and `uk.ac.york.mondo.integration.server.gzip` adds gzip compression to all HTTP responses coming from the server.

When deploying the server in a production environment, it is important to set up the secure store correctly, as it keeps the usernames and passwords of all the VCS that Hawk indexes. This has two steps (in Linux, they are automated when using the provided `run-server.sh` script):
* The secure store must be placed in a place no other program will try to access concurrently. This can be done by editing `mondo-server.ini` and adding `-eclipse.keyring <newline> /path/to/keyringfile`. That path should be only readable by the user running the server, for added security.
* An encryption password must be set. For Windows and Mac, the available OS integration should be enough.   For Linux environments, two lines have to be added at the beginning of the `mondo-server.ini` file, specifying the path to a password file:

    -eclipse.password
    /path/to/passwordfile

Creating a password file from 100 bytes of random data can be produced with these commands:

    head -c 100 /dev/random | base64 > /path/to/password
    chmod 400 /path/to/password

The server will test on startup that the secure store has been set properly: if you get a warning that encryption is not available, you will need to revise your setup.

Another important detail for production environments is turning on security. This is disabled by default to help with testing and initial evaluations, but it can be enabled by running the server once, shutting it down and then editing the `shiro.ini` file appropriately (relevant sections include comments on what to do) and switching `artemis.security.enabled` to `true` in the `mondo-server.ini` file. The MONDO server uses an embedded MapDB database, which is managed through the Users Thrift API. Once security is enabled, all Thrift APIs and all external (not in-VM) Artemis connections become password-protected.

If you enable security, you might want to ensure that -Dhawk.tcp.port is not present in the `mondo-server.ini` file, since the Hawk TCP port does not support security for the sake of raw performance.

If you are deploying this across a network, you will need to edit the `mondo-server.ini` file and customize the `hawk.artemis.host` line to the host that you want the Artemis server to listen to. This should be the IP address or hostname of the MONDO server in the network, normally. The Thrift API uses this hostname as well in its replies to the `watchModelChanges` operation in the Hawk API.

Additionally, if the server IP is dynamic but has a consistent DNS name (e.g. an Amazon VM + a dynamic DNS provider), we recommend setting `hawk.artemis.listenAll` to `true` (so the Artemis server will keep listening on all interfaces, even if the IP address changes) and using the DNS name for `hawk.artemis.host` instead of a literal IP address.

Finally, production environments should enable and enforce SSL as well, since plain HTTP is insecure. Some pointers on how to do this are provided [https://www.eclipse.org/forums/index.php/t/24782/](here).

Hawk integration
----------------

Hawk is integrated through its Thrift API, which is implemented in the `uk.ac.york.mondo.integration.hawk.servlet` plugin as standard OSGi HttpService servlets. The plugin takes care of saving and reloading the created Hawk instances and starting and stopping the embedded Apache Artemis messaging server.

On top of this, the integration project includes the following clients for the Hawk Thrift APIs:

* `uk.ac.york.mondo.integration.hawk.cli` maps the Thrift API to the OSGi console view (use `hawkhelp` to see the available commands).
* `uk.ac.york.mondo.integration.hawk.emf` provides a EMF Resource implementation that allows treating a Hawk index (or a part of it) as a remote read-only model.
* `uk.ac.york.mondo.integration.hawk.remote.thrift` extends Hawk's Eclipse UI with an additional `IModelIndexer` implementation, so it can manage and access remote Hawk indexes in the same way as local ones.

### Hawk EMF Resource

Access details for Hawk model indexes are saved as `.hawkmodel` files or provided through `hawk+http(s)://` URLs. Both can be created with the editor provided by `uk.ac.york.mondo.integration.hawk.emf.dt`. `.hawkmodel` files can be then opened as models with the sample Ecore reflective editor or by the [Epsilon Exeed](http://www.eclipse.org/epsilon/doc/exeed/) editor.

`.hawkmodel` files are Java [property files](http://docs.oracle.com/javase/7/docs/api/java/util/Properties.html), so their file format is quite simple. They define the following options:

* Server URL, instance name and Thrift protocol: these are required to contact the Thrift API. `hawk.servlet` provides servlets in `/thrift/hawk/json`, `/thrift/hawk/binary`, `/thrift/hawk/compact` and `/thrift/hawk/tuple`: they are all backed by the same storage and logic, but they use different Thrift [protocols](https://thrift.apache.org/docs/concepts) with varying degrees of language compatibility and efficiency. JSON works for all languages but is the least efficient, `binary` works for everything but JavaScript and is quite efficient, `compact` takes up less space at the expense of some extra processing (but only works for a reduced set of languages) and `tuple` is the most space-efficient but only works for Java.
* Pattern for the version control repositories whose contents we want to see (we only support "*" as a wildcard at the moment).
* Comma-separated filename patterns for the files whose contents we want to see (again, only "*" is supported at the moment).
* Loading mode: from most eager to least, `GREEDY` requests all model elements at once, `LAZY_ATTRIBUTES` requests all model elements without attributes and then fetches attributes on the fly, `LAZY_CHILDREN` requests only the roots and then fetches all reference fields of a node once a reference is accessed, and `LAZY_REFERENCES` requests only the roots and fetches single reference fields once they are accessed. It is also possible to combine `LAZY_ATTRIBUTES` with the other two lazy modes (`LAZY_ATTRIBUTES_CHILDREN` and `LAZY_ATTRIBUTES_REFERENCES`). For very large models (with millions of elements), `LAZY_CHILDREN` performs the best so far at browsing models (when combined with the Epsilon Exeed editor).
* By checking the "Subscribe" box, the resource will ask Hawk to feed an Artemis queue with events about changes in the indexed models. This queue will be used by the resource to update the local view of the model incrementally on the fly. A unique client ID must be provided, in order to support reconnections and durable queues. The durability of the queue can be `DEFAULT` (survive reconnections), `DURABLE` (survive reconnections and server restarts) or `TEMPORARY` (removed after disconnecting).

CloudATL integration
--------------------

CloudATL (also known as ATL/MapReduce) is integrated in a similar way to Hawk. `fr.inria.atlanmod.mondo.integration.cloudatl.servlet` implements the Thrift API, which is exposed by the `fr.inria.atlanmod.mondo.integration.cloudatl.cli` as a set of OSGi console commands.

### Setting up a trivial Hadoop cluster for testing

The CloudATL servlet works as a frontend node for a Hadoop cluster, which must have been set up in advance. The `conf` folder of the `.servlet` project provides an example of how the configuration would look like for a trivial one-node cluster. Using [Docker](https://www.docker.com/), it is quite simple to start a one-node pseudo-distributed Hadoop cluster. Install Docker, make sure you have over 10% of free disk space (required by Hadoop to start a node) and issue the following command.

    sudo docker run -it bluezio/hadoop-jh /etc/bootstrap.sh -bash

After this, the `conf/*.xml` files should be updated to reflect the IP address of the Docker instance. `hdfs-site.xml` should also be edited to provide valid local directories writable by the user running the MONDO server. When running the server, the `HADOOP_USER_NAME` environment variable should be set to `root` (the username running Hadoop in the Docker instance) and `HADOOP_CONF_DIR` should be set to the absolute path to the `conf` directory.

*NOTE*: pseudo-distributed Hadoop clusters are only meant for quick test runs. To obtain actual performance gains, users will need to set up more realistic Hadoop clusters on their own. More advanced Hadoop configurations are outside of the scope of this document.

### Running CloudATL jobs

Once set up, running a CloudATL job from the OSGi console can be done with two commands (the full list is available through `cloudatlhelp`):

    cloudatlconnect http://mondo_server_ip:port/thrift/cloudatl
    cloudatllaunch emftvm_url sourcemetamodel_url targetmetamodel_url sourcemodel_url targetmodel_url

The supported URL schemes are `hdfs://` (for files that have been previously uploaded to the Hadoop Distributed File System, using e.g. `hdfs dfs -put`) and `hawk+http://` (for models indexed by Hawk: these URLs are produced by the `.hawkmodel` editor in the `hawk.emf.dt` plugin).

### Building the `.jar` files for Hadoop

The CloudATL servlet needs to have up-to-date builds of ATL/MapReduce and the Hawk EMF driver in its `libs` folder, so it can send them to Hadoop for processing. Building the `.jar` requires these steps:

1. Download and install a recent version of [Eclipse](https://www.eclipse.org/downloads/) and a JDK for Java 7.
2. Go to "Window > Preferences > Java > Compiler" and change the default "Compiler compliance level" to 1.7. This is needed for the Docker image above, which comes with Java 7.
3. Go to "Help > Install New Software..." and install everything from IvyDE through its [update site](http://www.apache.org/dist/ant/ivyde/updatesite/). Let Eclipse restart.
4. Go to the Git perspective with "Window > Perspective > Open Perspective > Other... > Git".
5. Clone the `integrate-hawk-emf` `https://github.com/bluezio/ATL_MR` Git repository and import its projects. This can be done by copying the URL into the clipboard, right-clicking on the "Git Repositories" view and selecting "Paste Repository Path or URI". Make sure to check the "Import all existing Eclipse projects" box on the last step of the wizard.
6. Clone the `https://github.com/atlanmod/org.eclipse.atl.atlMR.git` repository, but do *not* import all projects. Instead, uncheck the box, let the clone finish and right click on the "plugins" folder within "Working Directory", selecting the "Import Projects..." menu entry.
7. Close all projects except for `atl-mr` and open `org.eclipse.m2m.atl.emftvm` and `org.eclipse.m2m.atl.emftvm.trace`, without letting it open any referenced projects.
8. Right-click on `atl-mr` in the "Package Explorer" view and select "Export... > Ant Buildfiles".
9. Right-click on the generated `build.xml` file in the "Package Explorer" view and select "Run As > Ant Build...". Select the `dist-emftvm` configuration, and make sure in the "JRE" tab that it runs in the same JRE as the workspace.
10. Refresh the `atl-mr` project by right clicking on it in the "Package Explorer" view and selecting "Refresh".
11. Again, right-click on the generated `build.xml` file, but this time select the `dist` configuration. It does not need to run in the same JRE as the workspace, but nevertheless check in the "JRE" tab that a valid JRE has been selected.
12. Refresh the project, and we're done: the binary distribution will be located in `atl-mr/dist`.
