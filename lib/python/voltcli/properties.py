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

PROPDEFROOT = sys.path[0] + "/voltcli/properties.d/"

class property_def():
    def __init__(self,name,repeatable,uniquechild):
        self.name = name
        self.multi = repeatable
        self.uniquechild = uniquechild
class global_property_def():
    def __init__(self,name,deftype,savefile,settype,setdef,allowed):
        self.definition = name.strip()
        self.type = deftype.strip()
        self.savefile = savefile.strip()
        self.settype = settype.strip()
        self.setdef = setdef.strip()
        self.allowed = allowed.strip()
        
class property_instance():
    def __init__(self,definition,value,uniqueIDs):
        self.definition = definition
        self.value = value
        self.uniqueIDs = uniqueIDs
        self.setdef = None
        DEBUG("Defining property instance:\n" + self.definition \
        + "\n" + self.value)
        
        
    def validate(self, condition):
        global PROPERTY_DEFS
        
        # Look for a matching property
        property = None
        for p in PROPERTY_DEFS:
            #DEBUG("Matching " + self.definition + " to " + p.definition)
            if p.definition == self.definition:
                property = p
                break
                
        # If no property found, undefined
        if property is None:
            WARNING("Unrecognized property name: " + self.definition)
            return False
            
        # Store the set definition for later
        self.setdef = property.setdef
        
        # If condition is not allowed, false
        allowed = property.allowed
        #DEBUG   ("Checking [" + allowed + "] for condition " + str(condition) + " (" + allowed[condition-1:condition] + ")")
        if allowed[condition-1:condition] == "0":
            WARNING("The property " + self.definition + " cannot be set at this time.")
            return False
            
        return True
        
    def get_xmldef():
        global PROPERTY_DEFS
        
        
class tokenizer():
    def __init__(self,input):
        self.buffer = input
        
    def gettoken(self):
        # first trim the string
        self.buffer = self.buffer.strip()
        
        # return nothing if we are at the end of the buffer
        if len(self.buffer) == 0: return None
        
        #Check to see if this is quoted
        quoted = self.pullquote()
        if quoted: return quoted
        
        # Look for valid tokens
        token = ""
        p = [-1,-1,-1,-1,-1,-1]
        p[0] = self.buffer.find(".")
        p[1] = self.buffer.find("\"")
        p[2] = self.buffer.find("(")
        p[3] = self.buffer.find(")")
        p[4] = self.buffer.find(":")
        p[5] = self.buffer.find("*")
        nexttoken = -1
        for i in range(0,len(p)):
            if p[i] == 0:
                retval = self.buffer[0:1]
                self.buffer = self.buffer[1:]
                return retval
            if nexttoken < 0: nexttoken = p[i]
            if p[i] > 0 and p[i] < nexttoken: nexttoken = p[i]
        
        # if the next character is not a valid token, return the string
        # up to the next token
        if nexttoken > 0:
            retval = self.buffer[0:nexttoken].strip()
            self.buffer = self.buffer[nexttoken:]
        else:
            retval = self.buffer.strip()
            self.buffer = ""
        return retval
                
        
    def getremainder(self):
        return self.buffer
    def gettotoken(self,token):
        p = self.buffer.find(token)
        if p < 0: return None
        retval = self.buffer[:p]
        self.buffer = self.buffer[p+1:]
        return retval
        
    def pullquote(self):
    # See if the next token is quoted
        if self.buffer[0:1] is not "\"": return None
        buff = self.buffer
        retval = ""
        state = 0
        for i in range(0,len(buff)):
            if state == 0:
                if buff[i,i+1] == "\"":
                    # end of quote
                    self.buffer = buff[1:]
                    return retval
                if buff[i,i+1] == "\\":
                    # escape next character
                    retval = retval + buff[1:2]
                    buff = buff[2:]
            else:
                retval = retval + buff[0:1]
                buff = buff [1:]
        
        # If we get here, the quote is unfinished
        FATAL("Unmatched quotation marks.")
        return None
            
        
        
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
    DEPLOYMENT_XML_SET = load(PROPDEFROOT + "xml_property_set.conf")
    
    pass
    # This still needs to be defined.

def load_global_property_defs():
    # This function loads all property definitions,
    # including  properties and XML equivalents
    global PROPDEFROOT
    global PROPERTY_DEFS
    SET_DEBUG(True)

    filename = PROPDEFROOT + "properties-model.csv"
    try:
        f = open(os.path.abspath(os.path.expanduser(filename)),"r")
    except Exception as e:
        FATAL("Cannot read properties definition file " + filename + "\n" + str(e))
        
    property_file_lines = f.readlines()
    #DEBUG("Properties file length: " + str(len(property_file_lines)))

    PROPERTY_DEFS = []
    linecount = 0
    for line in property_file_lines:
        linecount = linecount + 1
        tokens = line.split(",")
        if len(tokens) == 1 and len(tokens[0].strip()) == 0: 
            #DEBUG("Blank line")
            continue
        if tokens[0].strip()[0:1] == "#": 
            #DEBUG("Comment")
            continue
        if len(tokens) < 6:  FATAL("Invalid property definition in properties model. only " \
            + str(len(tokens)) + " elements on line " + str(linecount) )
        PROPERTY_DEFS.append(global_property_def(tokens[0],tokens[1],tokens[2],tokens[3],tokens[4],tokens[5]))
        
    DEBUG(str(len(PROPERTY_DEFS)) + " properties defined.")
    
    
###############     EXTERNAL FUNCTIONS

def load_property_instance(text):
    definition = ""
    value = None
    uniqueID = []
    t = tokenizer(text)
    state = 0  # wait for token
    
    while state < 99:
        if state == 0:
            name = t.gettoken()
            #INFO("Token [" + name + "]")
            
            if name == "(":
                unique = t.gettotoken(")").strip()
                definition = definition + "(*)"
                if  uniqueID is None:
                    FATAL("Invalid property definition. Mismatched parentheses.")
                    return None
                else:
                    uniqueID.append(unique)
                continue
                
            if name == ":":
                #declaration is complete, now for value
                value = t.getremainder().strip()
                state = 99
                continue
                
            if name is None:
                WARNING("Incomplete property definition ignored: " + text)
                return None
            definition = definition + name.strip()
            
    # If no qualifiers, set it to null
    if len(uniqueID) == 0: uniqueID = None
    return property_instance(definition,value,uniqueID)

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
    for p in a: props[p] = a[p]
    for p in b:
        if p in props:
            if mustbeunique:
                WARNING("Multiple values for property " + p)
                validated = False
            else:
                props[p] = b[p]
        else:
            props[p] = b[p]
            
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
    
def return_intersect_set(props, propset):
    venn = {}
    for p in propset:
        if p in props: 
            venn[p] = props[p]
    return venn