# This file is part of VoltDB.
# Copyright (C) 2008-2016 VoltDB Inc.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.

from voltcli.hostinfo import Host
from voltcli.hostinfo import Hosts

@VOLT.Command(
    bundles = VOLT.AdminBundle(),
    options = (
        VOLT.IntegerOption('-s', '--hsid', 'site_id', 'host site id in the cluster'),
        VOLT.StringOption('-t', '--target', 'target_host', 'target host in the cluster'),
    ),
    description = 'Replicate a partition on the target host.',
    hideverb = True
)

def replicate(runner):

    if not runner.opts.site_id and runner.opts.site_id <>0:
        runner.abort('The site id is missing.')

    if not runner.opts.target_host:
        runner.abort('The target host is missing.')

    # find out about the cluster.
    response = runner.call_proc('@SystemInformation',[VOLT.FastSerializer.VOLTTYPE_STRING],['OVERVIEW'])
    if response.status() <> 1:
        runner.abort("Unexpected response from @SystemInformation %s" % response)

    hosts = Hosts(runner.abort)
    for tuple in response.table(0).tuples():
        hosts.update(tuple[0], tuple[1], tuple[2])

    # Connect to the target host
    chost = hosts.get_host(runner.opts.target_host)
    if chost is None:
        runner.abort('Could not find the host in the cluster: %s' % runner.opts.target_host)

    # validate if the host already has the partition
    validate(runner, chost.id)

    user_info = ''
    if runner.opts.username:
        user_info = ', user: %s' % runner.opts.username

    runner.info('Connecting to %s:%d%s (%s) to issue "replicate" command' %
                (chost.get_admininterface(), chost.adminport, user_info, chost.hostname))

    #set up connections
    runner.voltdb_connect(chost.get_admininterface(), chost.adminport,runner.opts.username, runner.opts.password)

    if not runner.opts.dryrun:
        response = runner.call_proc('@Replicate', [VOLT.FastSerializer.VOLTTYPE_INTEGER, VOLT.FastSerializer.VOLTTYPE_INTEGER], [runner.opts.site_id, chost.id])
        print response

def validate(runner, target_host_id):

    response = runner.call_proc('@Statistics',[VOLT.FastSerializer.VOLTTYPE_STRING, VOLT.FastSerializer.VOLTTYPE_INTEGER],['TOPO', 0])
    if response.status() <> 1:
        runner.abort("Unexpected response from @Statistics %s" % response)

    sitefound = False
    for tuple in response.table(0).tuples():
        if runner.opts.site_id == tuple[0]:
            sitefound = True
            partition_sites = tuple[1].split(',')
            print partition_sites
            for site in partition_sites:
                pair = site.split(':')
                if int(pair[0]) == target_host_id:
                    runner.abort('Site %s is already on host %s in cluster.' % (runner.opts.site_id, target_host_id))
    if sitefound == False:
          runner.abort('The site %s is not found in the cluster.' % runner.opts.site_id)
