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
    description="Show in-flight @NibbleDeletes progress information",
    options=(
            VOLT.BooleanOption('-c', '--continuous', 'continuous', 'continuous listing', default=False),
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
        hostid = int(row[1])
        hostname = str(row[2])
        query = str(row[3])
        pid = int(row[4])
        total = long(row[5])
        delete = long(row[6])
        percent = int(row[7])

        nibbledeletesrow = NibbleDeletesRow(hostid, hostname, query, pid, total, delete, percent)

        if query in statsInfo:
            curList = statsInfo[query]
            curList.append(nibbledeletesrow)
        else:
            statsInfo[query] = [nibbledeletesrow]

    return statsInfo

def printPlainSummary(statsInfo):

    if len(statsInfo) == 0:
        sys.stdout.write('no running @NibbleDeletes query\n')
        return

    maxLen = 60.0
    for query in statsInfo:
        curList = statsInfo[query]
        curList.sort(key=lambda row: row.pid)
        maxRow = max(curList, key=lambda row: row.total)

        maxTotal = maxRow.total

        sys.stdout.write('Hostname: %s, HostId: %d \n' % (maxRow.hostname, maxRow.hostid))
        sys.stdout.write('Query:' + query + '\n')
        for row in curList:
            barLen = int(row.total * maxLen / float(maxTotal));
            status = '%d / %d' % (row.delete, row.total)
            progress(row.pid, barLen, row.delete, row.total, status)
            sys.stdout.write('\n')
            pass

        sys.stdout.write('\n')
    pass

def progress(pid, barLen, count, total, status=''):
    filledLen = int(round(barLen * count / float(total)))

    percent = round(100.0 * count / float(total), 1)
    bar = '=' * filledLen + '-' * (barLen - filledLen)

    sys.stdout.write('Partition %d [%s] %s%s ...%s\r' % (pid, bar, percent, '%', status))

