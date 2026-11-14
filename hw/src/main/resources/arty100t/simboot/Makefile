ROOT_DIR   := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
BUILD_DIR  := $(ROOT_DIR)build
SDBOOT_DIR := $(abspath $(ROOT_DIR)/../sdboot)

CC      = $(RISCV)/bin/riscv64-unknown-elf-gcc
OBJCOPY = $(RISCV)/bin/riscv64-unknown-elf-objcopy
OBJDUMP = $(RISCV)/bin/riscv64-unknown-elf-objdump

CFLAGS  = -march=rv32im_zicsr_zifencei -mabi=ilp32 -mcmodel=medany -nostartfiles -nostdlib
CFLAGS += -DNONSMP_HART=0 -I$(SDBOOT_DIR)/include -I$(SDBOOT_DIR)
LFLAGS  = -static -T $(ROOT_DIR)linker/simboot.lds

ELF  := $(BUILD_DIR)/simboot.elf
IMG  := $(BUILD_DIR)/simboot.img
DUMP := $(BUILD_DIR)/simboot.dump

.PHONY: default clean
default: $(IMG) $(DUMP)

$(ELF): $(ROOT_DIR)head.S $(ROOT_DIR)linker/simboot.lds $(ROOT_DIR)Makefile
	mkdir -p $(BUILD_DIR)
	$(CC) $(CFLAGS) $(LFLAGS) -o $@ $(ROOT_DIR)head.S

$(IMG): $(ELF)
	$(OBJCOPY) -O binary $< $@

$(DUMP): $(ELF)
	$(OBJDUMP) -D $< > $@

clean:
	rm -rf $(BUILD_DIR)
