#include <stdint.h>
#include "kprintf.h"
#include "test.h"

#ifdef SIM
volatile uint64_t tohost __attribute__((section(".tohost"), aligned(64)));
volatile uint64_t fromhost __attribute__((section(".tohost"), aligned(64)));
#endif

static int failures;

void check(const char *name, int ok, unsigned long detail)
{
  kprintf("  [%s] %s: %l\r\n", ok ? "PASS" : "FAIL", name, detail);
  if (!ok)
    failures++;
}

int same64(const uint64_t *a, const uint64_t *b, uint32_t n)
{
  for (uint32_t i = 0; i < n; i++)
    if (a[i] != b[i])
      return 0;
  return 1;
}

static void puthex(uint32_t v, int digits)
{
  for (int i = (digits - 1) * 4; i >= 0; i -= 4) {
    uint32_t d = (v >> i) & 0xF;
    kputc(d < 10 ? '0' + d : 'a' + d - 10);
  }
}

void diff64(const uint64_t *got, const uint64_t *exp, uint32_t n)
{
  for (uint32_t i = 0; i < n; i++)
    if (got[i] != exp[i]) {
      kprintf("    first mismatch at word %l: got ", (unsigned long)i);
      puthex((uint32_t)(got[i] >> 32), 8);
      puthex((uint32_t)got[i], 8);
      kprintf(" exp ");
      puthex((uint32_t)(exp[i] >> 32), 8);
      puthex((uint32_t)exp[i], 8);
      kprintf("\r\n");
      return;
    }
}

static void uart_drain(void)
{
  REG32(uart, UART_REG_TXCTRL) = UART_TXEN | UART_TXWM(1);
  while (!(REG32(uart, UART_REG_IP) & UART_IP_TXWM))
    ;
  for (volatile int i = 0; i < 2000; i++)
    ;
}

int finish(const char *prog)
{
  if (failures)
    kprintf("%s: %l FAILED\r\n", prog, (unsigned long)failures);
  else
    kprintf("%s: ALL PASS\r\n", prog);
  uart_drain();
#ifdef SIM
  tohost = (failures << 1) | 1;
#endif
  return failures;
}

void trap_handler(unsigned long mcause, unsigned long mepc)
{
  kprintf("trap: mcause %x mepc %x\r\n", (uint32_t)mcause, (uint32_t)mepc);
  failures++;
  finish("trap");
  for (;;)
    ;
}
