#include <platform.h>
#include <smp.h>
#include "common.h"

  .section .text.init
  .option norvc
  .globl _prog_start
_prog_start:
  j _real_start
  .org 0x80
  j _real_start
_real_start:
  smp_pause(s1, s2)
  li s1, UART_CTRL_ADDR
  li s2, UART_TXEN
  sw s2, UART_REG_TXCTRL(s1)
1:
  wfi
  csrr s2, mip
  andi s2, s2, 0x8
  beqz s2, 1b
  fence.i
  smp_resume(s1, s2)
  csrr a0, mhartid
  la a1, dtb
  li s1, PAYLOAD_DEST
  jr s1

  .section .dtb
  .align 3
dtb:
