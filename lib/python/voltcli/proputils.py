import sys
global_debug_flag = False
global_stream_flag = False

def UTILPRINT(txt):
    global global_stream_flag
    if global_stream_flag: 
        print    # clear print line
        SET_STREAM(False)
    print (txt)

def STREAM(txt):
    SET_STREAM(True)
    sys.stdout.write(txt)
    sys.stdout.flush()
    
            
def INFO(txt):
    UTILPRINT( "INFO: " + txt)

def WARNING(txt):
    UTILPRINT(  "WARNING: " + txt)

def ERROR(txt):
    UTILPRINT(  "ERROR: " + txt)

def FATAL(txt):
    UTILPRINT(  "FATAL: " + txt)
    exit()

def DEBUG(txt):
    global global_debug_flag
    if global_debug_flag: UTILPRINT(  "DEBUG: " + txt)

def SET_DEBUG(flag):
    global global_debug_flag
    if flag:
        global_debug_flag = True
    else:
        global_debug_flag  = False
        
def SET_STREAM(flag):
    global global_stream_flag
    if flag:
        global_stream_flag = True
    else:
        global_stream_flag  = False
    

def CHECK_DEBUG():
    global global_debug_flag
    if global_debug_flag:
        UTILPRINT(  "Debug flag is true!")
    else:
        UTILPRINT(  "Debug flag is False!")
	