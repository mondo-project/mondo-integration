#!/usr/bin/env python3

import argparse

ENTRIES_NOGPL = (
    ('fr.inria.atlanmod.mondo.integration.cloudatl.servlet.feature.feature.group', '[0.0.0,2.0.0)'),
    ('org.eclipse.ecf.core.feature.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.ecf.core.ssl.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.eclipse.ecf.filetransfer.feature.feature.group', '[3.10.0,4.0.0)'),
    ('org.eclipse.ecf.filetransfer.httpclient4.feature.feature.group', '[3.10.0,4.0.0)'),
    ('org.eclipse.ecf.filetransfer.httpclient4.ssl.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.eclipse.ecf.filetransfer.ssl.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.eclipse.epsilon.core.dependencies.feature.feature.group', '[1.3.0,2.0.0)'),
    ('org.eclipse.epsilon.core.feature.feature.group', '[1.3.0,2.0.0)'),
    ('org.hawk.bpmn.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.core.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.emf.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.emfresource.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.epsilon.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.localfolder.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.git.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.http.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.orientdb.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.osgiserver.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.svn.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.ui.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.mondo.editor.feature.feature.group', '[0.3.8,2.0.0)'),
    ('org.mondo.collaboration.security.lens.offline.server.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.mondo.collaboration.security.online.server.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.mondo.wt.cstudy.online.feature.feature.group', '[1.0.0,2.0.0)'),
    ('uk.ac.york.mondo.integration.api.feature.feature.group', '[1.0.0,2.0.0)'),
    ('uk.ac.york.mondo.integration.hawk.servlet.feature.feature.group', '[1.0.0,2.0.0)'),
    ('uk.ac.york.mondo.integration.server.feature.feature.group', '[1.0.0,2.0.0)'),
    ('uk.ac.york.mondo.integration.hawk.emf.feature.feature.group', '[1.0.0,2.0.0)'),
)

ENTRIES_GPL = (
    ('org.hawk.neo4jv2.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.ifc.feature.feature.group', '[1.0.0,2.0.0)'),
)

parser = argparse.ArgumentParser(description='Generates the p2.inf file for the MONDO Eclipse products.')
parser.add_argument('--gpl', action='store_true', help='Include advice for GPL features')
args = parser.parse_args()

ENTRIES = ENTRIES_NOGPL + ENTRIES_GPL if args.gpl else ENTRIES_NOGPL

n_requires = 0
for entry in ENTRIES:
    print("""requires.{n}.namespace = org.eclipse.equinox.p2.iu
requires.{n}.name = {name}
requires.{n}.range = {range}
""".format(n=n_requires, name=entry[0], range=entry[1]))
    n_requires += 1
