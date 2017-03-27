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
from voltcli import properties 

# Main Java Class
VoltDB = 'org.voltdb.VoltDB'

@VOLT.Command(
    options = (
        VOLT.StringOption('-C', '--config', 'configfile',
                         'specify the location of the deployment file',
                          default = None),
        VOLT.StringOption('-D', '--dir', 'directory_spec',
                          'Specifies the root directory for the database. The default is voltdbroot under the current working directory.',
                          default = None),
        VOLT.BooleanOption('-f', '--force', 'force',
                           'Start a new, empty database even if the VoltDB managed directories contain files from a previous session that may be overwritten.')
    ),
    description = 'Initializes a new, empty database.'
)
def init(runner):
    global voltdb_properties
    voltdb_properties = {}
    runner.args.extend(['initialize'])
    if runner.opts.configfile:
        runner.opts.configfile = preconfig(runner.opts.configfile)
        if runner.opts.configfile: 
            runner.args.extend(['deployment', runner.opts.configfile])
    if runner.opts.directory_spec:
        runner.args.extend(['voltdbroot', runner.opts.directory_spec])
    if runner.opts.force:
        runner.args.extend(['force'])
    args = runner.args
    runner.java_execute(VoltDB, None, *args)
    if len(voltdb_properties) > 0:
        print "Saving properties file... " + runner.opts.directory_spec + "/voltdbroot/config/properties.conf"
        properties.write_set(voltdb_properties,properties.INIT_FILE_SET,runner.opts.directory_spec + "/voltdbroot/config/properties.conf")

def preconfig(f):
    global voltdb_property_files, voltdb_properties
    loaded = False
    deploymentfile = None
    voltdb_property_files = [] 
    props = []
    files = f.split(",")
    for i in range(0,len(files)):
        files[i] = files[i].strip()   # remove excess white space
        parts = files[i].split(".")
        extension = parts[len(parts)-1].lower()
        if extension == "xml":
            if not deploymentfile:
                deploymentfile = files[i]
            else:
                print "FATAL: too many deployment files. Only one XML deployment file is allowed."
                exit()
        else:
            voltdb_property_files.append(files[i])
    if deploymentfile: print "DEBUG: Deployment file is " + deploymentfile
    if len(voltdb_property_files) > 0: 
        for i in voltdb_property_files: 
            if not loaded:
                try:
                    properties.load_property_defs()
                except Exception as e:
                    WARNING("Cannot load property definitions.\n" + str(e))
            print "Parsing configuration file " + i
            props.append(properties.load(i))
        voltdb_properties = props[0]    
        for i in range(1,len(props)): 
            voltdb_properties = properties.merge(props[0],props[i],False)
    return deploymentfile