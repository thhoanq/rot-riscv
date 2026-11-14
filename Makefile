include common.mk

.DEFAULT_GOAL := link

.PHONY: setup

setup:
	git -C $(ROOT) submodule update --init chipyard
	cd $(CY) && for v in $$(env | grep -oE '^CONDA[A-Z_]*'); do unset $$v; done && \
		source $$(conda info --base)/etc/profile.d/conda.sh && conda activate base && \
		./build-setup.sh riscv-tools --use-lean-conda --skip-ctags --skip-firesim --skip-marshal
	$(MAKE) link
