// See LICENSE.Sifive for license details.
#include <platform.h>
#include "common.h"

    .equ  SCRATCH_PAGE_SIZE,  0x10000
    .equ  SCRATCH_TOP,        (PAYLOAD_DEST + SCRATCH_PAGE_SIZE)
    .section .text.init
    .option  norvc
    .globl   _prog_start
_prog_start:
    la      t0, _trap_table
    csrw    mtvec, t0
    csrw    mie, zero
    li      sp, SCRATCH_TOP - 16
    la      t0, _fbss
    la      t1, _ebss
1:  bgeu    t0, t1, 2f
    sw      zero, 0(t0)
    addi    t0, t0, 4
    j       1b
2:  li      t0, 1
    li      t1, (UART_CTRL_ADDR + UART_REG_TXCTRL)
    sw      t0, 0(t1)
    call    main
3:  wfi
    j       3b

    .align  8
_trap_table:
    .rept   32
    j       _trap_entry
    .endr

_trap_entry:
    addi    sp, sp, -64
    sw      ra, 0(sp)
    sw      t0, 4(sp)
    sw      t1, 8(sp)
    sw      t2, 12(sp)
    sw      a0, 16(sp)
    sw      a1, 20(sp)
    sw      a2, 24(sp)
    sw      a3, 28(sp)
    sw      a4, 32(sp)
    sw      a5, 36(sp)
    sw      a6, 40(sp)
    sw      a7, 44(sp)
    sw      t3, 48(sp)
    sw      t4, 52(sp)
    sw      t5, 56(sp)
    sw      t6, 60(sp)
    csrr    a0, mcause
    csrr    a1, mepc
    call    trap_handler
    lw      ra, 0(sp)
    lw      t0, 4(sp)
    lw      t1, 8(sp)
    lw      t2, 12(sp)
    lw      a0, 16(sp)
    lw      a1, 20(sp)
    lw      a2, 24(sp)
    lw      a3, 28(sp)
    lw      a4, 32(sp)
    lw      a5, 36(sp)
    lw      a6, 40(sp)
    lw      a7, 44(sp)
    lw      t3, 48(sp)
    lw      t4, 52(sp)
    lw      t5, 56(sp)
    lw      t6, 60(sp)
    addi    sp, sp, 64
    mret
