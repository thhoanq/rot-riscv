#include <stdint.h>
#include "kprintf.h"
#include "test.h"
#include "mldsa.h"
#include "vectors.h"

#define N(a) (sizeof(a) / sizeof((a)[0]))

static const uint64_t rnd_zero[MLDSA_RND_W];
static uint64_t out[MLDSA_KG_OUT];
static uint64_t vf[N(kat_vf_in)];

static void check_out(const char *name, int rc, const uint64_t *exp, uint32_t n)
{
  int ok = rc == 0 && same64(out, exp, n);

  if (rc != 0)
    kprintf("    rc = -%l\r\n", (unsigned long)-rc);
  else if (!ok)
    diff64(out, exp, n);
  check(name, ok, mldsa_cycles());
}

static void show_verify(int rc)
{
  if (rc != 0)
    kprintf("    rc = -%l\r\n", (unsigned long)-rc);
  kprintf("  out: verify_ok = %l\r\n", (unsigned long)mldsa_verify_ok());
}

int main(void)
{
  int rc;

  kputs("mldsa-test");

  kprintf("KeyGen (mlen %l)\r\n", (unsigned long)KAT_KG_MLEN);
  rc = mldsa_run(MLDSA_CMD_KEYGEN, KAT_KG_MLEN, 0, kat_kg_in, N(kat_kg_in), out, N(kat_kg_out));
  check_out("KeyGen, KAT", rc, kat_kg_out, N(kat_kg_out));

  kprintf("Sign (mlen %l)\r\n", (unsigned long)KAT_SG_MLEN);
  rc = mldsa_run(MLDSA_CMD_SIGN, KAT_SG_MLEN, rnd_zero, kat_sg_in, N(kat_sg_in), out, N(kat_sg_out));
  check_out("Sign rnd = 0, KAT", rc, kat_sg_out, N(kat_sg_out));

  kprintf("Verify, good signature (mlen %l)\r\n", (unsigned long)KAT_VF_MLEN);
  rc = mldsa_run(MLDSA_CMD_VERIFY, KAT_VF_MLEN, 0, kat_vf_in, N(kat_vf_in), 0, 0);
  show_verify(rc);
  check("Verify, good signature", rc == 0 && mldsa_verify_ok(), mldsa_cycles());

  for (uint32_t i = 0; i < N(vf); i++)
    vf[i] = kat_vf_in[i];
  vf[200] ^= 1;
  kprintf("Verify, tampered signature (word 200 bit 0 flipped)\r\n");
  rc = mldsa_run(MLDSA_CMD_VERIFY, KAT_VF_MLEN, 0, vf, N(vf), 0, 0);
  show_verify(rc);
  check("Verify, tampered signature", rc == 0 && !mldsa_verify_ok(), mldsa_cycles());

  mldsa_reset();
  check("soft reset clears the status", (mldsa_status() & ~MLDSA_ST_IN_READY & ~MLDSA_ST_RND_READY) == 0,
        mldsa_status());

  return finish("mldsa-test");
}
