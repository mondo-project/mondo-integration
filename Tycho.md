## Tycho build

To create the local update site on <http://localhost:8080/site>, navigate to the `uk.ac.york.mondo.integration.p2` directory and issue:

```
mvn clean p2:site jetty:run
```

To build the project, simply run:

```
mvn clean install
```
