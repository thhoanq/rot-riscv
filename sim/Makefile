include ../common.mk

.DEFAULT_GOAL := sim

ifeq ($(HAS_IP),1)
CONFIG ?= rot_ip.MLDSASimConfig
else
CONFIG ?= rot.sim.IbexSimConfig
endif

BUILD := $(ROOT)/sim/build

ifdef PROG
BINARY ?= $(ROOT)/sw/$(PROG)/build-sim/$(PROG).elf
endif

VARS = SBT_PROJECT=chipyard_fpga GENERATOR_PACKAGE=chipyard \
	CONFIG_PACKAGE=$(call pkg,$(CONFIG)) CONFIG=$(call cls,$(CONFIG)) \
	gen_dir=$(BUILD) 'output_dir=$(BUILD)/output/$$(long_name)'

.PHONY: sim verilog run clean

sim: link jar
	$(MAKE) -C $(CY)/sims/verilator $(VARS)

verilog: link jar
	$(MAKE) -C $(CY)/sims/verilator $(VARS) verilog

run: link jar
	$(if $(BINARY),,$(error set PROG=<program in sw/> or BINARY=<elf>))
	$(if $(PROG),$(MAKE) -C $(ROOT)/sw/$(PROG) sim)
	$(MAKE) -C $(CY)/sims/verilator $(VARS) run-binary BINARY=$(abspath $(BINARY))

clean:
	rm -rf $(BUILD)
