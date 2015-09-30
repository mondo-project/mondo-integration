#!/bin/bash

DEST="$HOME/Documents/mondo-downloads/mondo-m2"
CURDIR="$(dirname $(readlink -f "$0"))"

patch_pom() {
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

rm -rf "$DEST"
mvn eclipse:to-maven \
    "-DdeployTo=id::default::file://$DEST" \
    "-DeclipseDir=$CURDIR" \
    -DstripQualifier=true

for f in $DEST/org/eclipse/core/runtime/3.*/runtime-3.*.pom; do
    patch_pom "$f" "org.eclipse.equinox" "app" "[1.0.0,2.0.0)"
done

for f in $DEST/uk/ac/york/mondo/integration/hawk/emf/1.*/emf-1.*.pom; do
    patch_pom "$f" "org.eclipse.emf" "ecore" "[2.10.0,3.0.0)"
done

