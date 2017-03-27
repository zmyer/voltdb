import json
import time
import sys
import tokenize
import os

from proputils import INFO, WARNING, ERROR, FATAL,DEBUG, SET_DEBUG, STREAM, UTILPRINT

CLI_START_SET = {}
PROPERTY_FILE_SET = {}
DEPLOYMENT_XML_SET = {}
PROPERTY_DEFS = {}
DEFAULT_SET = {}
INIT_FILE_SET = {}

PROPDEFROOT = "/Users/ajgent/demos/properties/definitions/"

class property_def():
    def __init__(self,name,repeatable,uniquechild):
        self.name = name
        self.multi = repeatable
        self.uniquechild = uniquechild

############# INTERNAL FUNCTIONS

def ttype(t):
    return tokenize.tok_name[t[0]]
 
def gettext(line,start,end):
    global property_file_lines
    if line-1 > len(property_file_lines): FATAL("Internal error. Requesting line past end of file. line number " + str(line))
    t = property_file_lines[line-1]
    return t[start-1:end]
    
def gettokenargument():
    global tokens, t
    kname = ""
    t = t + 1
    token = tokens[t]
    type = ttype(token)
    if type == "NAME" or type == "STRING":
        kname = token[1]
    elif type == "NUMBER":
        kname = str(token[1])
    elif type == "OP" and token[1] == "\"":
        kname = gettokenquote()
    else:
        TOKENERROR(token,"Unidentified token type starts argument: " + str(token))
      
    
    t = t+1
    token = tokens[t]
    type = ttype(token)
    if type == "OP" and token[1] == ")":
       #WARNING("Resulting Argument text [" + kname + "]")
       return kname
    else:
        INFO(str(token))
        TOKENFATAL(token,"Invalid syntax for argument")

def gettokenquote():
    global tokens, t
    # grab start of quote
    qstart = tokens[t][2][1]

    # find end of quote
    t = t + 1
    token = tokens[t]
    type = ttype(token)
    
    #INFO("QUOTE START TOKEN: " + str(token))
    while not (type == "OP" and token[1] == "\""):
        t = t + 1
        token = tokens[t]
        type = ttype(token)

    #INFO("QUOTE END TOKEN: " + str(token))

    # retrieve raw quote
    qend = tokens[t][2][1]
    quote = lines[tokens[t][2][0] - 1]
    quote = quote[qstart:qend-1]
    
    #unescape quote
    unescaped = ""
    p = quote.find("\\")
    while p >= 0:
        unescaped = unescaped + quote[:p] + quote[p+1:p+2]
        quote = quote[p+2:]
    #WARNING("Resulting quoted text [" + unescaped + "]")
    return unescaped
    
def tokenfilemessage(token,error):
    global property_file_lines
    linenum = token[2][0]
    charposition = token[2][1]
    t = error + "\n" + property_file_lines[linenum-1]
    t = t + (" " * (charposition-1))  + "^"
    return t
    
def TOKENERROR(token,error):  
    ERROR(tokenfilemessage(token,error))
      
def TOKENFATAL(token,error):      
    FATAL(tokenfilemessage(token,error))
 
def load_property_defs():
    global PROPDEFROOT
    global PROPERTY_DEFS
    
    global CLI_START_SET
    global INIT_FILE_SET
    global PROPERTY_FILE_SET
    global DEPLOYMENT_XML_SET
    global PROPERTY_DEFS
    global DEFAULT_SET
    CLI_START_SET = load(PROPDEFROOT + "cli_start_property_set.conf")
    INIT_FILE_SET = load(PROPDEFROOT + "init_file_property_set.conf")
    
    pass
    # This still needs to be defined.
    
###############     EXTERNAL FUNCTIONS


def load(filename):
    global property_file_lines, tokens, t
    try:
        f = open(os.path.abspath(os.path.expanduser(filename)),"r")
    except Exception as e:
        FATAL("Cannot read file " + filename + "\n" + str(e))
        
    property_file_lines = f.readlines()
    f.seek(0)    

    properties = []
    tokens = tokenize.generate_tokens(f.readline)
    tokens = list(tokens)

    t = 0
    state = 0
    stack = []
    props = {}
    tcount = 0
    pcount = 0
    pvalue = ""
    pname=""

    while t < len(tokens) and state > -1:
        token = tokens[t]
        type = ttype(token)
        #INFO("Type: " + type + "  (" + str(token[1]) + "]")
        if state == 0:
            if type == "NAME": 
                pname= token[1]
                #INFO("property name: " + token[1])
                #stack.append(token[1])
            elif type == "OP" and token[1] == ":": 
                stack.append([pname,pvalue])
                pname = ""
                pvalue = ""
                #INFO("property: " + str(stack))
                state = 1
            elif type == "OP" and token[1] == ".": 
                stack.append([pname,pvalue])
                pname = ""
                pvalue = ""
                #INFO("property: " + str(stack))
            elif type == "OP" and token[1] == "(": 
                pvalue = gettokenargument()
            else:
                # Allow blank lines
                if type == "COMMENT" or type == "NL" or type == "NEWLINE" or type == "ENDMARKER":
                    if len(stack) == 0 and pname == "" and pvalue == "":
                        pass
                    else:
                        TOKENFATAL(token,"Incomplete property definition")
                else:
                    TOKENFATAL(token,"Invalid property syntax")
        elif state == 1:
            if type == "NEWLINE" or type == "NL" or type == "COMMENT" or type == "ENDMARKER":
                #INFO("Property value: " + pvalue)
            
                # report completed declaration
                if len(pvalue) > 0:
                    o = "PROPERTY "
                    p = ""
                    for i in stack:
                        if len(p) == 0:
                            p = i[0]
                        else:
                            p = p + "." + i[0]
                        if len(i[1]) > 0: p = p + ":" + i[1]
                    #print o + p + " " + pvalue
                    properties.append([p,pvalue])
                    props[p] = pvalue
                else:
                    TOKENERROR(token, "No value specified in property declaration")
                    
                state = 0
                pcount = pcount + 1
                stack = []
                pvalue = ""
            else:
                pvalue = pvalue + token[1]
            
        if type == "ENDMARKER":
            #INFO("[eof]")
            state = -1
        t = t +1
    return props

def dump(props):
    print("==================\nList of properties:\n==================")
    for p in props:
        print p + " = " + props[p]
    return
    
def merge(a,b, mustbeunique):
    props = {}
    validated = True
    for p in a:
        if p in b:
            if mustbeunique:
                WARNING("Multiple values for property " + p)
                validated = False
            else:
                props[p] = b[p]
        else:
            props[p] = a[p]
            
    if validated:
        return props
    else:
        return None
    
def write_set(props, propset, filename):
    try:
        f = open(filename,"w")
    except Exception as e:
        FATAL("Cannot write file " + filename + "\n" + str(e))
    f.write ("# Saved properties. DO NOT EDIT."+ os.linesep)
    for p in propset:
        if p in props: f.write(p + ": " + props[p]+ os.linesep)
    f.close()
        
def write_cli_set(props, propset):
    fragment = ""
    for p in propset:
        if p in props: 
            if len(fragment) > 0: fragment = fragment + " "
            fragment = fragment + propset[p] + props[p]
    return fragment