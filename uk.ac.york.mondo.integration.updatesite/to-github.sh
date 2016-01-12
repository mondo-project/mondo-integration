#!/bin/bash

DEST_P2="$HOME/Documents/mondo-integration-ghpages/client-updates"
rm -rf "$DEST_P2"
mkdir "$DEST_P2"

pushd target/repository
for i in plugins features content.jar artifacts.jar ../../category.xml; do
  cp -r "$i" "$DEST_P2"
done
popd

cd "$DEST_P2"
git add --all
