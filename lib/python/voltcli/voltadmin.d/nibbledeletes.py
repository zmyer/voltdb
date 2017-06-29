#!/usr/bin/env python
# This file is part of VoltDB.
# Copyright (C) 2008-2017 VoltDB Inc.
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
from voltcli import checkstats
from voltcli.clusterinfo import Cluster
from datetime import datetime
import sys
import time
import subprocess
import json
from voltcli.checkstats import StatisticsProcedureException

@VOLT.Command(
    bundles=VOLT.AdminBundle(),
    description="Show status of current cluster and remote cluster(s) it connects to",
    options=(
            VOLT.BooleanOption('-c', '--continuous', 'continuous', 'continuous listing', default=False),
            VOLT.BooleanOption('-j', '--json', 'json', 'print out JSON format instead of plain text', default=False),
            VOLT.BooleanOption(None, '--dr', 'dr', 'display DR/XDCR related status', default=False)
    ),
)

def nibbledeletes(runner):
    if runner.opts.continuous:
        try:
            while True:
                # clear screen first
                tmp = subprocess.call('clear', shell=True)
                doNibbledeletes(runner)
                time.sleep(2)  # used to be runner.opts.interval, default as 2 seconds
        except KeyboardInterrupt, e:
            pass  # don't care
    else:
        doNibbledeletes(runner)

def doNibbledeletes(runner):
    # the cluster which voltadmin is running on always comes first
    if runner.client.host != runner.opts.host:
        runner.voltdb_connect(runner.opts.host.host,
                              runner.opts.host.port,
                              runner.opts.username,
                              runner.opts.password,
                              runner.opts.ssl_config)

    statsInfo = getNibbleDeletesStats(runner)
    if runner.opts.json:
        printJSONSummary(statsInfo)
    else:
        printPlainSummary(statsInfo)

class NibbleDeletesRow(object):
    def __init__(self, hostid, hostname, query, pid, total, delete, percent):
        self.hostid = hostid
        self.hostname = hostname
        self.query = query
        self.pid = pid
        self.total = total
        self.delete = delete
        self.percent = percent

def getNibbleDeletesStats(runner):
    response = None
    try:
        response = checkstats.get_stats(runner, "NIBBLEDELETES")
    except StatisticsProcedureException as e:
        runner.info(e.message)
        sys.exit(e.exitCode)
    statsInfo = {}
    for row in response.table(0).tuples():
        hostid = row[1]
        hostname = row(row[2])
        query = row(row[3])
        pid = row[4]
        total = row[5]
        delete = row[6]
        percent = row[7]
        
        nibbledeletesrow = NibbleDeletesRow(hostid, hostname, query, pid, total, delete, percent)
        
        if statsInfo[query] == None:
             statsInfo[query] = [nibbledeletesrow]
        else:
            curList = statsInfo[query]
            curList.append(nibbledeletesrow)
        
    return statsInfo

def printPlainSummary(statsInfo):
    
    if len(statsInfo) == 0:
        sys.stdout.write('no running @NibbleDeletes query')
        return
    
    for query in statsInfo:
        
        
        
        
    
    sys.stdout.write(header1)
    sys.stdout.write('\n')
    

def printJSONSummary(cluster):

    remoteClusterInfos = []
    for clusterId, remoteCluster in cluster.remoteclusters_by_id.items():
        clusterInfo = {
            "clusterId": clusterId,
            "state": remoteCluster.status,
            "role": remoteCluster.role
        }
        remoteClusterInfos.append(clusterInfo)

    members = []
    for hostId, hostname in cluster.hosts_by_id.items():
        latencies = []
        for clusterId, remoteCluster in cluster.remoteclusters_by_id.items():
            latency = {
                "clusterId": clusterId,
                "delay": remoteCluster.producer_max_latency[hostname + str(clusterId)]
            }
            latencies.append(latency)
        hostInfo = {
            "hostId": hostId,
            "hostname": hostname,
            "replicationLatencies": latencies,
        }
        members.append(hostInfo)

    livehost = len(cluster.hosts_by_id)
    missing = cluster.hostcount - livehost
    body = {
        "clusterId": cluster.id,
        "version": cluster.version,
        "hostcount": cluster.hostcount,
        "kfactor": cluster.kfactor,
        "livehost": livehost,
        "missing": missing,
        "liveclient": cluster.liveclients,
        "uptime": cluster.uptime,
        "remoteClusters": remoteClusterInfos,
        "members": members
    }
    jsonStr = json.dumps(body)
    print jsonStr


