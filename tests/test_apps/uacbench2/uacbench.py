# This file is part of VoltDB.
# Copyright (C) 2008-2017 VoltDB Inc.
#
# Permission is hereby granted, free of charge, to any person obtaining
# a copy of this software and associated documentation files (the
# "Software"), to deal in the Software without restriction, including
# without limitation the rights to use, copy, modify, merge, publish,
# distribute, sublicense, and/or sell copies of the Software, and to
# permit persons to whom the Software is furnished to do so, subject to
# the following conditions:
#
# The above copyright notice and this permission notice shall be
# included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
# IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
# OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
# ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
# OTHER DEALINGS IN THE SOFTWARE.

import os
import sys
import string
import random
import shutil
import subprocess
import shlex
from string import Template
from optparse import OptionParser

DEFAULT_CLASSES_DIR = "forjar/uac"
PROCEDURE_CLASSES_DIR = "procedures/uac"
SUPPORTING1_CLASSES_DIR = "supporting1/uac"
SUPPORTING2_CLASSES_DIR = "supporting2/uac"
DEFAULT_JARS_DIR = "jars"
JARS_CMD_PRFIX = "./run.sh uacjars "

PAT_CREATE_PROC_STMT = "CREATE PROCEDURE FROM CLASS uac.Proc{0};"
PAT_DROP_PROC_STMT = "DROP PROCEDURE uac.Proc{0};"
PAT_CLS_DEL_STMT = "uac.Proc{0}"

def generate_ddl(tables, procedurecount, ddlname):
    tb = ('create table t{0} (a bigint not null, b varchar(10), c integer,'
            'd smallint, e integer, primary key(a, b));'
          'create index t{1}_idx1 on t{2}(a,c);'
          'create index t{3}_idx2 on t{4}(a,d);'
          'partition table t{0} on column a;'
    )

    #ddl = "file -inlinebatch END_OF_BATCH\n";
    ddl = "";

    # all the tables
    for i in range(tables):
        ddl += tb.format(i, i, i, i, i, i, i) + "\n"

    # all the procedures
    for i in range(0, procedurecount):
        ddl += "CREATE PROCEDURE PARTITION ON TABLE T0 COLUMN a FROM CLASS uac.Proc%d;" % (i) + "\n"

    #ddl += "END_OF_BATCH\n"

    with open(ddlname, "w") as java_file:
        java_file.write(ddl)
    return

def procedure_stmts(pattern, pfrom, pto, filename):
    stmts = ""
    for i in range(pfrom, pto):
        stmts += pattern.format(i) + "\n"

    if len(stmts.strip(' \t\n\r')) == 0:
        sys.exit("stmts not generated from " + str(pfrom) + " to " + str(pto) + " for file: " + filename)

    with open(filename, "w") as stmts_file:
        stmts_file.write(stmts)
    return

def generateDataStmts():
    data_template = Template('    public$viz String data$n = "$str";\n')
    some_data = ''
    for j in range(0, 30):
        the_str = ''
        for k in range(0, 100):
            the_str += str(random.choice(string.letters))
        isStatic = "";
        if random.choice([True, False]):
            isStatic = " static"
        some_data += data_template.substitute(viz=isStatic, n=j, str=the_str)
    return some_data

def generateOpStmts():
    op_template = Template('        retval += data$num.$op("$findchar");\n')
    some_ops = ''
    for j in range(0, 20):
        the_str = ''
        which_data = random.randint(0, 29)
        which_char = random.choice(string.letters)
        which_op = "indexOf";
        if random.choice([True, False]):
            which_op = " lastIndexOf"
        some_ops += op_template.substitute(num=which_data, op=which_op, findchar=which_char)
    return some_ops

def generate_supporting_class(ithClass, filename1, filename2):
    with open('template_supportingclass.txt', 'r') as template_file:
        template = Template(template_file.read())

    java_str = template.substitute(num=ithClass,
                                   static_data=generateDataStmts(),
                                   random_churn1=generateOpStmts(),
                                   random_churn2=generateOpStmts(),
                                   value=str(random.randint(-9223372036854775807, 9223372036854775807))+"L")
    with open(filename1, "w") as java_file:
        java_file.write(java_str)

    java_str = template.substitute(num=ithClass,
                                   static_data=generateDataStmts(),
                                   random_churn1=generateOpStmts(),
                                   random_churn2=generateOpStmts(),
                                   value=str(random.randint(-9223372036854775807, 9223372036854775807))+"L")
    with open(filename2, "w") as java_file:
        java_file.write(java_str)

    return

def generate_procedure_class(ithProc, classcount, filename):
    with open('template_procedure.txt', 'r') as template_file:
        template = Template(template_file.read())

    stmt_template = Template('    static final SQLStmt stmt$stmtnum = new SQLStmt("SELECT * FROM T$t0, T$t1, T$t2 order by T$t0.a, T$t0.b;");\n')
    call_template = Template('        value += new Extra$num().getValue();\n')

    some_static_data = generateDataStmts()
    some_stmts = ''
    for j in range(0, 2):
        some_stmts += stmt_template.substitute(
            stmtnum=j,
            t0=random.randint(0, 9),
            t1=random.randint(10, 19),
            t2=random.randint(20, 29))

    some_calls = ''
    for j in range(0, classcount):
        some_calls += call_template.substitute(num=j)

    java_str = template.substitute(procnum=ithProc, stmts=some_stmts, static_data=some_static_data, call_extras=some_calls)
    with open(filename, "w") as java_file:
        java_file.write(java_str)

    return

def create_jars(procedurecount, classcount, steps):
    # make some procedures
    print "Creating procedures"
    for i in range(0, procedurecount):
        filename = "%s/Proc%d.java" % (PROCEDURE_CLASSES_DIR, i)
        generate_procedure_class(i, classcount, filename)

    # make some supporting classes
    print "Creating supporting classes"
    for i in range(0, classcount):
        filename1 = "%s/Extra%d.java" % (SUPPORTING1_CLASSES_DIR, i)
        filename2 = "%s/Extra%d.java" % (SUPPORTING2_CLASSES_DIR, i)
        generate_supporting_class(i, filename1, filename2)

    # generate a random walk of classes in a giant matrix
    stepPlans = []
    mapForStep = []
    for i in range(0, classcount):
        mapForStep.append(True)
    for i in range(0, steps):
        # permute one value
        index = random.randint(0, classcount)
        mapForStep[index] = not mapForStep[index]
        # append a shallow copy
        stepPlans.append(mapForStep[:])

    # make some jarfiles that walk combos in a loop
    print "Creating jars for steps"
    for i in range(0, steps):
        # empty the source for jars
        clean_dirpath(DEFAULT_CLASSES_DIR)

        for j in range(0, procedurecount):
            cmd = "cp %s/Proc%d.java %s" % (PROCEDURE_CLASSES_DIR, j, DEFAULT_CLASSES_DIR)
            subprocess.call(shlex.split(cmd))

        # copy a complete set of classcount extra classes into folder
        mapForStep = stepPlans[i]
        for j in range(0, len(mapForStep)):
            step = mapForStep[j]
            cmd = ''
            if step == True:
                cmd = "cp %s/Extra%d.java %s" % (SUPPORTING1_CLASSES_DIR, j, DEFAULT_CLASSES_DIR)
            else:
                cmd = "cp %s/Extra%d.java %s" % (SUPPORTING2_CLASSES_DIR, j, DEFAULT_CLASSES_DIR)
            subprocess.call(shlex.split(cmd))

        # invoke shell script to compile jars
        cmd = "%s uac_%s.jar" % (JARS_CMD_PRFIX, i)
        subprocess.call(shlex.split(cmd))

# cleaning the input dir
def clean_dirpath(dirpath):
    if os.path.exists(dirpath):
        shutil.rmtree(dirpath)
    os.makedirs(dirpath, 0755)
    return

def clean():
    clean_dirpath(DEFAULT_JARS_DIR)
    clean_dirpath(DEFAULT_CLASSES_DIR)
    clean_dirpath(PROCEDURE_CLASSES_DIR)
    clean_dirpath(SUPPORTING1_CLASSES_DIR)
    clean_dirpath(SUPPORTING2_CLASSES_DIR)
    return

if __name__ == "__main__":
    parser = OptionParser()
    parser.add_option("-t", "--tablecount", type="int", default=500,
                      help="number of tables")
    parser.add_option("-p", "--procedurecount", type="int", default=2,
                      help="number of procedures")
    parser.add_option("-c", "--classcount", type="int", default=1000,
                      help="number of extra supporting classes")
    parser.add_option("-s", "--steps", type="int", default=10,
                      help="number of class jars to cycle through")

    (options, args) = parser.parse_args()

    # generate base jars: 500 tables with 1000 procedures
    generate_ddl(options.tablecount, options.procedurecount, "ddlbase.sql")

    # clean everything
    clean()

    # create all the classes and jars
    create_jars(options.procedurecount, options.classcount, options.steps)

