#!/bin/bash

./to-maven.sh

DEST_P2="$HOME/Documents/mondo-updates/mondo-updates"
rm -rf "$DEST_P2"
mkdir "$DEST_P2"

for i in plugins features content.jar artifacts.jar category.xml; do
  cp -r "$i" "$DEST_P2"
done

cd "$DEST_P2"
git add --all
