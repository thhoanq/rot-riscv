#include "mldsa.h"

#define TIMEOUT 2000000u

static inline void wr(uint32_t off, uint32_t v)
{
  *(volatile uint32_t *)(MLDSA_BASE + off) = v;
}

static inline uint32_t rd(uint32_t off)
{
  return *(volatile uint32_t *)(MLDSA_BASE + off);
}

void mldsa_reset(void)
{
  wr(MLDSA_CTRL, MLDSA_CTRL_RESET);
}

uint32_t mldsa_status(void)
{
  return rd(MLDSA_STATUS);
}

uint32_t mldsa_cycles(void)
{
  return rd(MLDSA_CYCLES);
}

int mldsa_verify_ok(void)
{
  return (rd(MLDSA_STATUS) & MLDSA_ST_VERIFY_OK) != 0;
}

int mldsa_run(uint32_t cmd, uint32_t mlen, const uint64_t *rnd,
              const uint64_t *in, uint32_t n_in, uint64_t *out, uint32_t n_out)
{
  uint32_t i = 0, o = 0, spin = 0, st;

  if (rnd)
    for (uint32_t k = 0; k < MLDSA_RND_W; k++) {
      wr(MLDSA_RND_LO, (uint32_t)rnd[k]);
      wr(MLDSA_RND_HI, (uint32_t)(rnd[k] >> 32));
    }
  wr(MLDSA_CMD, cmd);
  wr(MLDSA_MSGLEN, mlen);
  wr(MLDSA_CTRL, MLDSA_CTRL_START);

  while (i < n_in || o < n_out) {
    st = rd(MLDSA_STATUS);
    if (i < n_in && (st & MLDSA_ST_IN_READY)) {
      wr(MLDSA_IN_LO, (uint32_t)in[i]);
      wr(MLDSA_IN_HI, (uint32_t)(in[i] >> 32));
      i++;
      spin = 0;
    } else if (o < n_out && (st & MLDSA_ST_OUT_VALID)) {
      uint32_t lo = rd(MLDSA_OUT_LO);
      uint32_t hi = rd(MLDSA_OUT_HI);
      out[o++] = ((uint64_t)hi << 32) | lo;
      spin = 0;
    } else if (++spin > TIMEOUT) {
      return MLDSA_ERR_TIMEOUT;
    }
  }

  while (!((st = rd(MLDSA_STATUS)) & MLDSA_ST_DONE))
    if (++spin > TIMEOUT)
      return MLDSA_ERR_TIMEOUT;
  if (st & MLDSA_ST_OVERFLOW)
    return MLDSA_ERR_OVERFLOW;
  return 0;
}
