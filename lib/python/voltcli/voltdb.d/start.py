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

import os
from voltcli import properties 
#from voltcli import xmlproperties 

voltdbroot_help = ('Specifies the root directory for the database. The default '
                   'is voltdbroot under the current working directory.')
server_list_help = ('{hostname-or-ip[,...]}, '
             'Specifies the leader(s) for coordinating cluster startup. ')
config_help = ('{property-file[,...]}, '
             'Specifies 1 or more property files for changing server settings')

@VOLT.Command(
    bundles = VOLT.ServerBundle('probe',
                                needs_catalog=False,
                                supports_live=False,
                                default_host=False,
                                safemode_available=True,
                                supports_daemon=True,
                                supports_multiple_daemons=True,
                                check_environment_config=True,
                                force_voltdb_create=False,
                                supports_paused=True,
                                is_legacy_verb=False),
    options = (
        VOLT.StringListOption('-H', '--host', 'server_list', server_list_help, default = ''),
        VOLT.IntegerOption('-c', '--count', 'hostcount', 'number of hosts in the cluster'),
        VOLT.StringOption('-D', '--dir', 'directory_spec', voltdbroot_help, default = None),
        VOLT.BooleanOption('-r', '--replica', 'replica', 'start replica cluster (deprecated, please use role="replica" in the deployment file)', default = False),
        VOLT.BooleanOption('-A', '--add', 'enableadd', 'allows the server to elastically expand the cluster if the cluster is already complete', default = False),
        VOLT.IntegerOption('-s', '--sitesperhost', 'sitesperhost', None),
        VOLT.IntegerOption('-m', '--missing', 'missing', 'specifying how many nodes are missing at K-safe cluster startup'),
        VOLT.StringOption('-C', '--config', 'config', config_help, default = None),
    ),
    description = 'Starts a database, which has been initialized.'
)
def start(runner):
    if runner.opts.directory_spec:
        upath = os.path.expanduser(runner.opts.directory_spec)
        runner.args.extend(['voltdbroot', upath])
        
    #print "DEBUG opts: " + str(runner.opts)
    preconfig(runner)
    #print "DEBUG opts: " + str(runner.opts)
    if not runner.opts.server_list:
        runner.abort_with_help('You must specify the --host option.')
    runner.args.extend(['mesh', ','.join(runner.opts.server_list)])
    if runner.opts.hostcount:
        runner.args.extend(['hostcount', runner.opts.hostcount])
    if runner.opts.sitesperhost:
        runner.args.extend(['sitesperhost', runner.opts.sitesperhost])
    if runner.opts.missing:
        runner.args.extend(['missing', runner.opts.missing])
    if runner.opts.enableadd:
        runner.args.extend(['enableadd'])

    #postconfig(runner)
    #print "DEBUG args: " + str(runner.args)
    runner.go()

def preconfig(runner):
    global voltdb_properties
    parent = runner.opts.directory_spec
    config = runner.opts.config
    root = parent
    props = []
    files = []
    voltdbproperties = {}
    
    # Find the root directory
    if (not root): root = "."
    root = os.path.abspath(os.path.expanduser(root))
    #print "DEBUG: dbroot is " + root + "/voltdbroot"
    
    #  Determine if a properties file was saved on init
    pfile = root + "/voltdbroot/config/properties.conf"
    if os.path.exists(pfile): files.append(pfile)
    
    # Check for properties file(s) on the command line
    if (config):
        cfiles = config.split(",")
        for i in range(0,len(cfiles)): 
            cfile = os.path.abspath(os.path.expanduser(cfiles[i].strip()))
            parts = cfile.split(".")
            extension = parts[len(parts)-1].lower()
            if extension == "xml":
                print "FATAL: You cannot change the XML deployment file on the start command.\n" + \
                        "Only properties files are allowed in the --config option." 
                exit()
            files.append(cfile)
            
    # Parse and merge the properties files
    for f in files: 
        #print "DEBUG: property file " + f
        try:
            props.append(properties.load(f))
        except Exception as e:
            print "FATAL: Cannot load properties file " + f + "\n" + str(e)
            exit()
        voltdb_properties = props[0]
        
    # No files, nothing to do
    if len(files) == 0: return 
       
    #load defs then parse
    try:
        properties.load_property_defs()
    except Exception as e:
        WARNING("Cannot load property definitions.\n" + str(e))

    for i in range(1,len(props)): 
        voltdb_properties = properties.merge(props[0],props[i],False)
            
    # Pick out properties that need to be applied to the command line
    cliprops = properties.return_intersect_set(voltdb_properties,properties.CLI_START_SET)
    for p in cliprops:
        a = properties.CLI_START_SET[p].split(",")
        answer = None
        if len(a) >1: answer = format_cli_arg(cliprops[p],a[1])
        if not hasattr(runner.opts,a[0]):
            if len(a) > 1:
                if answer is not None: 
                    runner.opts.extend([a[0],answer])
                    #print "DEBUG: set property for " + a[0] + " to " + str(answer)
                else:
                    print "WARNING: internal error. Unrecognized CLI property option " + a[1]
            else:
                runner.opts.extend([a[0]])
                #print "DEBUG: set property " + a[0] + " with no value."
                answer = True
        else:
            override = False
            current_value = getattr(runner.opts,a[0])
            if current_value == None: 
                override = True
            else:
                if type(current_value) is list:
                    if len(current_value) == 0: override = True
                    if len(current_value) == 1: 
                        if len(current_value[0]) == 0: override = True
                    
            if override: 
                # Need special handling for server list, since it is an opt not an arg
                if a[0] == "server_list":
                    setattr(runner.opts,a[0],answer)   
                else:         
                    if len(a) > 0: 
                        if answer is not None:
                            setattr(runner.opts,a[0],answer)
                            #print "DEBUG: set empty property for " + a[0] + " to " + str(answer)
                        else:
                            print "WARNING: internal error. Unrecognized CLI property option " + a[1]
                    else:
                        setattr(runner.opts,a[0],None)
                        #print "DEBUG: set property for " + a[0] + " to nothing"           
            else:
                pass
                #print "DEBUG: property " + a[0] + " overridden by command line option." 
    #properties.dump(cliprops)
           
#def postconfig(runner):
#    global voltdb_properties
#    
#    # first see if there are any properties to set
#    if len(voltdb_properties) == 0: return
#    xmlprops = properties.return_intersect_set(voltdb_properties,properties.DEPLOYMENT_XML_SET)
#    if len(xmlprops) == 0: return
#     
#    print ("DEBUG: settable properties.")
#    # Next, see if they are settable. not implemented yet.
#     
#    #Finally, load the deployment file, set the properties, and write the deployment file
#    deploymentfile = runner.opts.directory_spec + "/voltdbroot/config/deployment.xml"
#    xml = xmlproperties.loadxmlfile(deploymentfile)
#    xmlproperties.walktree(xml,"")
#    for p in xmlprops:
#        print "DEBUG: set " + p
#        xmlproperties.setxml(xml,properties.DEPLOYMENT_XML_SET[p],xmlprops[p])

     
def format_cli_arg(value,format):
    answer = None
    if format == "*": answer = value
    if format == "n": answer = int(value)
    if format == "[*]": answer = value.split(",")
    return answer
    