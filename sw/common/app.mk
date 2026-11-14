APP_DIR := $(dir $(abspath $(firstword $(MAKEFILE_LIST))))
COMMON  := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))

ifndef RISCV
$(error RISCV is not set: source env.sh first)
endif

CROSS   ?= $(RISCV)/bin/riscv64-unknown-elf-
CC      := $(CROSS)gcc
OBJCOPY := $(CROSS)objcopy
OBJDUMP := $(CROSS)objdump

CFLAGS  = -march=rv32imc_zicsr_zifencei -mabi=ilp32 -mcmodel=medany -O2 -std=gnu11 -Wall -nostartfiles
CFLAGS += -fno-common -fno-tree-loop-distribute-patterns -g
CFLAGS += -I$(COMMON) -I$(COMMON)include -I$(APP_DIR)
LFLAGS  = -static -nostdlib -L$(COMMON)linker -T app.lds

ALL_SRCS := $(COMMON)crt0.S $(COMMON)kprintf.c $(COMMON)test.c $(addprefix $(APP_DIR),$(SRCS))
DEPS     := $(ALL_SRCS) $(wildcard $(COMMON)*.h $(APP_DIR)*.h)

PAYLOAD_MAX := 32768

BUILD_DIR := $(APP_DIR)build
SIM_DIR   := $(APP_DIR)build-sim

.PHONY: default sim clean

default: $(BUILD_DIR)/$(PROG).bin $(BUILD_DIR)/$(PROG).dump

sim: $(SIM_DIR)/$(PROG).elf

$(BUILD_DIR)/$(PROG).elf: $(DEPS)
	mkdir -p $(BUILD_DIR)
	$(CC) $(CFLAGS) $(LFLAGS) -o $@ $(ALL_SRCS)

$(SIM_DIR)/$(PROG).elf: $(DEPS)
	mkdir -p $(SIM_DIR)
	$(CC) $(CFLAGS) -DSIM $(LFLAGS) -o $@ $(ALL_SRCS)

$(BUILD_DIR)/$(PROG).bin: $(BUILD_DIR)/$(PROG).elf
	$(OBJCOPY) -O binary $< $@
	@test $$(stat -c %s $@) -le $(PAYLOAD_MAX) || { echo "$@ does not fit the SD payload"; exit 1; }

$(BUILD_DIR)/$(PROG).dump: $(BUILD_DIR)/$(PROG).elf
	$(OBJDUMP) -D -S $< > $@

clean:
	rm -rf $(BUILD_DIR) $(SIM_DIR)
