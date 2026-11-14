SHELL := /bin/bash

ROOT := $(abspath $(dir $(lastword $(MAKEFILE_LIST))))
CY   := $(ROOT)/chipyard
IP   := $(ROOT)/rot-riscv-ip
JAR  := $(CY)/.classpath_cache/chipyard_fpga.jar

export ROT_ROOT := $(ROOT)

HAS_IP := $(if $(wildcard $(IP)/src/main/scala/*),1)

pkg = $(basename $(1))
cls = $(patsubst .%,%,$(suffix $(1)))

.PHONY: link jar

link:
	ln -sfn ../rot.sbt $(CY)/rot.sbt

jar:
	@if [ -f $(JAR) ] && [ -n "$$(find $(ROOT)/rot.sbt $(ROOT)/hw/src $(IP)/src -type f -not -path '*/build/*' -newer $(JAR) -print -quit 2>/dev/null)" ]; then \
		rm -f $(JAR); fi
