// See LICENSE.Sifive for license details.
#ifndef SIFIVE_SMP
#define SIFIVE_SMP
#include "platform.h"

#ifndef MAX_HARTS
#define MAX_HARTS 32
#endif
#define CLINT_END_HART_IPI CLINT_CTRL_ADDR + (MAX_HARTS*4)
#define CLINT1_END_HART_IPI CLINT1_CTRL_ADDR + (MAX_HARTS*4)

#ifndef NONSMP_HART
#define NONSMP_HART 0
#endif

#define smp_disable(reg1, reg2)			 \
  csrr reg1, mhartid				;\
  li   reg2, NONSMP_HART			;\
  beq  reg1, reg2, hart0_entry			;\
42:						;\
  wfi    					;\
  j 42b						;\
hart0_entry:

#define smp_pause(reg1, reg2)	 \
  li reg2, 0x8			;\
  csrw mie, reg2		;\
  li   reg1, NONSMP_HART	;\
  csrr reg2, mhartid		;\
  bne  reg1, reg2, 42f

#ifdef CLINT1_CTRL_ADDR

#define smp_resume(reg1, reg2)	 \
    \
  li reg1, CLINT_CTRL_ADDR	;\
41:				;\
  li reg2, 1			;\
  sw reg2, 0(reg1)		;\
  addi reg1, reg1, 4		;\
  li reg2, CLINT_END_HART_IPI	;\
  blt reg1, reg2, 41b		;\
    \
  li reg1, CLINT1_CTRL_ADDR	;\
41:				;\
  li reg2, 1			;\
  sw reg2, 0(reg1)		;\
  addi reg1, reg1, 4		;\
  li reg2, CLINT1_END_HART_IPI	;\
  blt reg1, reg2, 41b		;\
    \
42:				;\
  wfi    			;\
  csrr reg2, mip		;\
  andi reg2, reg2, 0x8		;\
  beqz reg2, 42b		;\
    \
  csrr reg2, mhartid		;\
  bnez reg2, 41f; \
    \
  li reg1, CLINT_CTRL_ADDR	;\
  slli reg2, reg2, 2		;\
  add reg2, reg2, reg1		;\
  sw zero, 0(reg2)		;\
  j 42f; \
41: \
    \
  li reg1, CLINT1_CTRL_ADDR	;\
  addi reg2, reg2, -2; \
  slli reg2, reg2, 2		;\
  add reg2, reg2, reg1		;\
  sw zero, 0(reg2)		; \
42: \
    \
  li reg1, CLINT_CTRL_ADDR	;\
41:				;\
  lw reg2, 0(reg1)		;\
  bnez reg2, 41b		;\
  addi reg1, reg1, 4		;\
  li reg2, CLINT_END_HART_IPI	;\
  blt reg1, reg2, 41b; \
    \
  li reg1, CLINT1_CTRL_ADDR	;\
41:				;\
  lw reg2, 0(reg1)		;\
  bnez reg2, 41b		;\
  addi reg1, reg1, 4		;\
  li reg2, CLINT1_END_HART_IPI	;\
  blt reg1, reg2, 41b; \

#else

#define smp_resume(reg1, reg2)	 \
  li reg1, CLINT_CTRL_ADDR	;\
41:				;\
  li reg2, 1			;\
  sw reg2, 0(reg1)		;\
  addi reg1, reg1, 4		;\
  li reg2, CLINT_END_HART_IPI	;\
  blt reg1, reg2, 41b		;\
42:				;\
  wfi    			;\
  csrr reg2, mip		;\
  andi reg2, reg2, 0x8		;\
  beqz reg2, 42b		;\
  li reg1, CLINT_CTRL_ADDR	;\
  csrr reg2, mhartid		;\
  slli reg2, reg2, 2		;\
  add reg2, reg2, reg1		;\
  sw zero, 0(reg2)		;\
41:				;\
  lw reg2, 0(reg1)		;\
  bnez reg2, 41b		;\
  addi reg1, reg1, 4		;\
  li reg2, CLINT_END_HART_IPI	;\
  blt reg1, reg2, 41b

#endif

#endif
