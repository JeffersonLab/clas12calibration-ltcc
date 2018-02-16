#########################################
# CLAS12 daq/trigger default config file #
##########################################

##############################
# TI settings (TI master only)
##############################

###################################
# FADC settings (detector-related)
###################################

# Common settings

FADC250_CRATE all
FADC250_SLOT	   all
FADC250_MODE         1
FADC250_NSB         12
FADC250_NSA         60
FADC250_NPEAK        1
FADC250_DAC       3300
FADC250_TET        400
FADC250_W_OFFSET  7650
FADC250_W_WIDTH	   400
FADC250_CRATE end


TDC1190_CRATE all
TDC1190_SLOT all
TDC1190_W_WIDTH   400
TDC1190_W_OFFSET -1500
TDC1190_CRATE end




###############################################
# These are the includes from ec and ltcc_calib
###############################################

# ECAL settings
include trigger/EC/ecal_newgain_prod.cnf

# TS, TDC, FADC various:
# Various included Trigger supervisor setting,
# But it was overwriting some values below.
# Removing it from now
# include trigger/LTCC/ltcc_calib_various.cnf

# masks :
include trigger/LTCC/ltcc_calib_masks.cnf

# threshold
include trigger/LTCC/ltcc_calib_thresholds.cnf


############################
# TS settings (trig1 crate)
############################

TS_CRATE trig1

#lock-roc mode
#TS_BLOCK_LEVEL   1
#TS_BUFFER_LEVEL  1

# production: 5 5 15 10
TS_BLOCK_LEVEL   20
TS_BUFFER_LEVEL   8
TS_HOLDOFF   1  5 1
TS_HOLDOFF   2  5 1
TS_HOLDOFF   3 15 1
TS_HOLDOFF   4 10 1


#
# TS GTP trigger mask - disabled
#
TS_GTP_INPUT_MASK 0x00000000


#
# TS FP trigger mask
#
# 0x40 - LTCC
# 0x100 - SVT
# 0x200 - CTOF
# 0x400 - CND
# 0x800 - MVT
# 0x1000 - helicity
#
TS_FP_INPUT_MASK 0x00000040


# First arg:      0-disable, 
#                 1-enable; 
#                 |   Prescale (15-7Hz, 7-3.5kHz, 5-15kHz, 4-30kHz, 3-60kHz)
#                 |   |
TS_RANDOM_TRIGGER 1   5

TS_CRATE end
