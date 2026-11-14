// See LICENSE.Sifive for license details.
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
  li sp, (PAYLOAD_DEST + MEM_SIZE - 16)
  call main
  smp_resume(s1, s2)
  csrr a0, mhartid
  la a1, dtb
  li s1, PAYLOAD_DEST
  jr s1

  .section .dtb
  .align 3
dtb:
