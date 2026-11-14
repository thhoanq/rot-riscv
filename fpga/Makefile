include ../common.mk

.DEFAULT_GOAL := verilog

ifeq ($(HAS_IP),1)
CONFIG ?= rot_ip.MLDSAArty100TConfig
else
CONFIG ?= rot.arty100t.IbexArty100TConfig
endif

BUILD := $(ROOT)/fpga/build
BIT   := $(BUILD)/rot.arty100t.Arty100THarness.$(call cls,$(CONFIG))/obj/Arty100THarness.bit

VARS = SUB_PROJECT=arty100t SBT_PROJECT=chipyard_fpga \
	MODEL_PACKAGE=rot.arty100t \
	CONFIG_PACKAGE=$(call pkg,$(CONFIG)) CONFIG=$(call cls,$(CONFIG)) \
	gen_dir=$(BUILD)

.PHONY: verilog bitstream debug-bitstream program clean

verilog bitstream debug-bitstream: link jar
	$(MAKE) -C $(CY)/fpga $(VARS) $@

program:
	vivado -mode batch -nolog -nojournal -notrace -source $(ROOT)/fpga/scripts/program.tcl -tclargs $(BIT)

clean:
	rm -rf $(BUILD)
