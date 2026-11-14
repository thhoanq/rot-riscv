#ifndef _MLDSA_H
#define _MLDSA_H

#include <stdint.h>

#define MLDSA_BASE 0x10007000UL

#define MLDSA_CTRL    0x00
#define MLDSA_CMD     0x04
#define MLDSA_MSGLEN  0x08
#define MLDSA_STATUS  0x0C
#define MLDSA_IN_LO   0x10
#define MLDSA_IN_HI   0x14
#define MLDSA_OUT_LO  0x18
#define MLDSA_OUT_HI  0x1C
#define MLDSA_RND_LO  0x20
#define MLDSA_RND_HI  0x24
#define MLDSA_CYCLES  0x28

#define MLDSA_CTRL_START 0x1u
#define MLDSA_CTRL_RESET 0x2u

#define MLDSA_ST_BUSY      0x01u
#define MLDSA_ST_DONE      0x02u
#define MLDSA_ST_VERIFY_OK 0x04u
#define MLDSA_ST_IN_READY  0x08u
#define MLDSA_ST_OUT_VALID 0x10u
#define MLDSA_ST_OUT_LAST  0x20u
#define MLDSA_ST_OVERFLOW  0x40u
#define MLDSA_ST_RND_READY 0x80u

#define MLDSA_CMD_KEYGEN 0
#define MLDSA_CMD_VERIFY 1
#define MLDSA_CMD_SIGN   2

#define MLDSA_KG_IN    4
#define MLDSA_KG_OUT   480
#define MLDSA_SG_IN(m) (320 + (m))
#define MLDSA_SG_OUT   303
#define MLDSA_VF_IN(m) (467 + (m))
#define MLDSA_RND_W    4

#define MLDSA_ERR_TIMEOUT  -1
#define MLDSA_ERR_OVERFLOW -2

void mldsa_reset(void);
int mldsa_run(uint32_t cmd, uint32_t mlen, const uint64_t *rnd,
              const uint64_t *in, uint32_t n_in, uint64_t *out, uint32_t n_out);
int mldsa_verify_ok(void);
uint32_t mldsa_status(void);
uint32_t mldsa_cycles(void);

#endif
