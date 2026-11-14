### how to use


#### Install

```
git clone <rot-riscv> && cd rot-riscv
git submodule update --init chipyard
cd chipyard && ./build-setup.sh riscv-tools --use-lean-conda --skip-ctags --skip-firesim --skip-marshal
```


### ML-DSA firmware flow

Luồng của `mldsa_run()` trong [sw/mldsa-test/mldsa.c](sw/mldsa-test/mldsa.c), điều khiển core ML-DSA qua MMIO tại `0x10007000` (wrapper: [MLDSA.scala](rot-riscv-ip/src/main/scala/cipher/MLDSA.scala)).

```mermaid
flowchart TD
    A(["mldsa_run(cmd, mlen, rnd, in, n_in, out, n_out)"]) --> B{"rnd != NULL?<br/>(chỉ Sign)"}
    B -- có --> C["Ghi 4 word rnd<br/>RND_LO, RND_HI → rndQ"]
    B -- không --> D
    C --> D["CMD = cmd<br/>0 KeyGen · 1 Verify · 2 Sign"]
    D --> E["MSGLEN = mlen"]
    E --> F["CTRL = START<br/>(core chỉ nhận nếu !busy)"]
    F --> G{"i < n_in hoặc<br/>o < n_out?"}
    G -- có --> H["st = STATUS"]
    H --> I{"i < n_in và<br/>IN_READY?"}
    I -- có --> J["Ghi IN_LO, IN_HI → inQ<br/>i++, spin = 0"]
    J --> G
    I -- không --> K{"o < n_out và<br/>OUT_VALID?"}
    K -- có --> L["Đọc OUT_LO, OUT_HI → pop outQ<br/>out[o++], spin = 0"]
    L --> G
    K -- không --> M{"++spin > TIMEOUT?"}
    M -- không --> G
    M -- có --> T(["return -1 (TIMEOUT)"])
    G -- không --> N["st = STATUS"]
    N --> O{"DONE?"}
    O -- không --> P{"++spin > TIMEOUT?"}
    P -- không --> N
    P -- có --> T
    O -- có --> Q{"OVERFLOW?"}
    Q -- có --> R(["return -2 (OVERFLOW)"])
    Q -- không --> S(["return 0<br/>Verify: đọc VERIFY_OK"])
```

- `n_in` / `n_out` (số word 64-bit) do caller truyền vào: KeyGen 4 / 480, Sign `320 + m` / 303, Verify `467 + m` / 0.
- rnd được nạp vào `rndQ` (depth 4) **trước** START, nên 4 word vừa đủ.
- `START` chỉ có tác dụng khi core không busy (`i_start := start && !o_busy`); firmware không kiểm tra BUSY trước khi start.
- Input và output được xử lý xen kẽ trong cùng một vòng poll, vì `inQ`/`outQ` chỉ sâu 4 word.
- `VERIFY_OK` và `CYCLES` đọc sau khi hàm trả về; `CTRL = RESET` (soft reset) xoá trạng thái.
