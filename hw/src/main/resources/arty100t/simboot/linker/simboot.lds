OUTPUT_ARCH("riscv")
ENTRY(_prog_start)

MEMORY
{
  bootrom_mem (rx) : ORIGIN = 0x10000, LENGTH = 0x2000
}

SECTIONS
{
  .text : {
    *(.text.init)
    *(.text .text.*)
  } >bootrom_mem

  .rodata : {
    *(.rodata .rodata.*)
    . = ALIGN(8);
    *(.dtb)
  } >bootrom_mem

  /DISCARD/ : { *(.data .data.* .sdata .sdata.* .bss .bss.* .sbss .sbss.* .riscv.attributes .comment) }
}
