#!/usr/bin/env python3

import argparse

ENTRIES_NOGPL = (
    ('fr.inria.atlanmod.mondo.integration.cloudatl.cli.feature.feature.group', '[0.0.0,2.0.0)'),
    ('org.eclipse.ecf.core.feature.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.ecf.core.ssl.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.eclipse.ecf.filetransfer.feature.feature.group', '[3.10.0,4.0.0)'),
    ('org.eclipse.ecf.filetransfer.httpclient4.feature.feature.group', '[3.10.0,4.0.0)'),
    ('org.eclipse.ecf.filetransfer.httpclient4.ssl.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.eclipse.ecf.filetransfer.ssl.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.eclipse.epsilon.core.dependencies.feature.feature.group', '[1.3.0,2.0.0)'),
    ('org.eclipse.epsilon.core.dt.feature.feature.group', '[1.3.0,2.0.0)'),
    ('org.eclipse.epsilon.core.feature.feature.group', '[1.3.0,2.0.0)'),
    ('org.eclipse.epsilon.emf.dt.dependencies.feature.feature.group', '[1.3.0,2.0.0)'),
    ('org.eclipse.epsilon.emf.dt.feature.feature.group', '[1.3.0,2.0.0)'),
    ('org.eclipse.epsilon.emf.feature.feature.group', '[1.3.0,2.0.0)'),
    ('org.eclipse.incquery.databinding.runtime.feature.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.databinding.runtime.feature.source.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.querybasedfeatures.runtime.feature.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.querybasedfeatures.runtime.feature.source.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.querybasedfeatures.tooling.feature.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.querybasedfeatures.tooling.feature.source.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.runtime.evm.transactions.feature.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.runtime.evm.transactions.feature.source.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.runtime.feature.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.runtime.feature.source.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.runtime.generic.feature.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.runtime.generic.feature.source.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.sdk.feature.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.sdk.feature.source.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.validation.runtime.feature.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.validation.runtime.feature.source.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.validation.tooling.feature.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.validation.tooling.feature.source.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.viewers.runtime.feature.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.viewers.runtime.feature.source.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.viewers.tooling.ui.feature.feature.group', '[1.1.0,2.0.0)'),
    ('org.eclipse.incquery.viewers.tooling.ui.feature.source.feature.group', '[1.1.0,2.0.0)'),
    ('org.emf.splitter.feature.feature.group', '[0.1.0,2.0.0)'),
    ('org.hawk.bpmn.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.core.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.emf.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.emfresource.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.epsilon.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.git.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.localfolder.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.modelio.exml.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.orientdb.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.osgiserver.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.svn.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.ui.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.workspace.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.mondo.editor.feature.feature.group', '[0.3.8,2.0.0)'),
    ('uk.ac.york.mondo.integration.api.feature.feature.group', '[1.0.0,2.0.0)'),
    ('uk.ac.york.mondo.integration.clients.cli.feature.feature.group', '[1.0.0,2.0.0)'),
    ('uk.ac.york.mondo.integration.hawk.cli.feature.feature.group', '[1.0.0,2.0.0)'),
    ('uk.ac.york.mondo.integration.hawk.emf.dt.feature.feature.group', '[1.0.0,2.0.0)'),
    ('uk.ac.york.mondo.integration.hawk.emf.feature.feature.group', '[1.0.0,2.0.0)'),
    ('uk.ac.york.mondo.integration.hawk.emf.emfsplitter.feature.feature.group', '[1.0.0,2.0.0)'),
    ('uk.ac.york.mondo.integration.hawk.remote.thrift.feature.feature.group', '[1.0.0,2.0.0)'),
    ('uk.ac.york.mondo.integration.server.users.cli.feature.feature.group', '[1.0.0,2.0.0)'),

    ('ReactiveATLFeature.feature.group', '[1.0.0,2.0.0)'),
    ('org.eclipse.viatra.cep.feature.feature.group', '[0.8.0,2.0.0)'),
    ('org.eclipse.viatra.dse.feature.feature.group', '[0.8.0,2.0.0)'),
    ('org.eclipse.viatra.dse.merge.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.eclipse.viatra.emf.mwe2integration.feature.feature.group', '[0.8.0,2.0.0)'),
    ('org.eclipse.viatra.emf.runtime.feature.feature.group', '[0.8.0,2.0.0)'),
    ('org.eclipse.viatra.modelobfuscator.feature.feature.group', '[0.7.0,2.0.0)'),

    ('org.mondo.collaboration.security.macl.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.mondo.collaboration.security.mpbl.feature.feature.group', '[1.0.0,2.0.0)'),
    # ('org.mondo.wt.cstudy.editor.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.mondo.wt.cstudy.merge.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.mondo.wt.cstudy.model.feature.feature.group', '[1.0.0,2.0.0)'),
)

ENTRIES_GPL = (
    ('org.hawk.neo4jv2.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.ifc.feature.feature.group', '[1.0.0,2.0.0)'),
    ('uk.ac.york.mondo.integration.hawk.emf.ifcexport.feature.feature.group', '[1.0.0,2.0.0)'),
    ('org.hawk.modelio.feature.feature.group', '[1.0.0,2.0.0)'),
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
