#!/usr/bin/lftp -v -f

open sftp://agd516@sftp.york.ac.uk
cd /shared/storage/cs/groupstore/es/mondo/mondo-cli
mirror -R -r uk.ac.york.mondo.integration.clients.cli.product/target/products/ . -I *.zip
cd ../mondo-server
mirror -R -r uk.ac.york.mondo.integration.server.product/target/products/ . -I *.zip -X *nogpl*.zip
cd ../mondo-eclipse
mirror -R -r uk.ac.york.mondo.integration.eclipse.product/target/products/ . -I *.zip -X *nogpl*.zip
cd ../../mondo_public/mondo-cli
mirror -R -r uk.ac.york.mondo.integration.clients.cli.product/target/products/ . -I *.zip
cd ../mondo-server
mirror -R -r uk.ac.york.mondo.integration.server.product/target/products/ . -I *nogpl*.zip
cd ../mondo-eclipse
mirror -R -r uk.ac.york.mondo.integration.eclipse.product/target/products/ . -I *nogpl*.zip
