#!/bin/bash

DEST="$HOME/Documents/mondo-updates/mondo-m2"
CURDIR="$(dirname $(readlink -f "$0"))"
LATEST_API_VERSION="$(basename "$(ls -d "$DEST"/uk/ac/york/mondo/integration/api/1.* | sort | head -1)")"
LATEST_ARTEMIS_VERSION="$(basename "$(ls -d "$DEST"/uk/ac/york/mondo/integration/artemis/1.* | sort | head -1)")"
LATEST_HAWK_VERSION="$(basename "$(ls -d "$DEST"/org/hawk/1.* | sort | head -1)")"

pom_replace_version() {
    FILE="$1"
    GROUP="$2"
    ARTIFACT="$3"
    NEWVALUE="$4"

    # Update the POM file in place
    xmlstarlet ed -S -L \
	       -N "m=http://maven.apache.org/POM/4.0.0" \
	       -u "//m:dependency[m:groupId='$GROUP' and m:artifactId='$ARTIFACT']/m:version" \
	       -v "$NEWVALUE" "$FILE"

    # Recompute the .md5 and .sha1 files
    md5sum "$FILE" | awk '{printf "%s",$1}' > "$FILE.md5"
    sha1sum "$FILE" | awk '{printf "%s",$1}' > "$FILE.sha1"
}

pom_add_dependency() {
    FILE="$1"
    GROUP="$2"
    ARTIFACT="$3"
    VERSION="$4"

    xmlstarlet ed -S -L \
	       -N "m=http://maven.apache.org/POM/4.0.0" \
	       -s "//m:dependencies" -t elem -n 'dependencyTMP' \
	       -s "//dependencyTMP" -t elem -n groupId -v "$GROUP" \
	       -s "//dependencyTMP" -t elem -n artifactId -v "$ARTIFACT" \
	       -s "//dependencyTMP" -t elem -n version -v "$VERSION" \
	       -r "//dependencyTMP" -v "dependency" \
	       "$f"
}

rm -rf "$DEST"
mvn eclipse:to-maven \
    "-DdeployTo=id::default::file://$DEST" \
    "-DeclipseDir=$CURDIR"

for f in $DEST/org/eclipse/core/runtime/3.*/runtime-3.*.pom; do
    pom_replace_version "$f" "org.eclipse.equinox" "app" "[1.0.0,2.0.0)"
done

for f in $(find $DEST/uk/ac/york/mondo/integration -name "*.pom") $(find $DEST/org/hawk -name "*.pom"); do
    pom_replace_version "$f" "org.slf4j" "api" "[1.7.0,2.0.0)"
    pom_replace_version "$f" "org.eclipse.equinox" "ds" "[1.4,2.0)"
    pom_replace_version "$f" "org.eclipse.emf" "ecore" "[2.10.0,3.0.0)"

    pom_replace_version "$f" "uk.ac.york.mondo.integration" "api" "$LATEST_API_VERSION"
    pom_replace_version "$f" "uk.ac.york.mondo.integration" "artemis" "$LATEST_ARTEMIS_VERSION"

    pom_replace_version "$f" "org.hawk" "core" "$LATEST_HAWK_VERSION"
    pom_replace_version "$f" "org.hawk.core" "dependencies" "$LATEST_HAWK_VERSION"
done

# nested jars are not handled by to-eclipse: need to add those transitive deps to the pom.xml
for f in $DEST/uk/ac/york/mondo/integration/artemis/1.*/artemis-1.*.pom; do
    pom_add_dependency "$f" "org.apache.activemq" "artemis-server" "1.0.0"
    pom_add_dependency "$f" "org.apache.activemq" "artemis-stomp-protocol" "1.0.0"
    pom_add_dependency "$f" "org.javassist" "javassist" "3.20.0-GA"
done
for f in $DEST/uk/ac/york/mondo/integration/hawk/emf/1.*/emf-1.*.pom; do
    pom_add_dependency "$f" "cglib" "cglib-nodep" "3.1"
done
