// See LICENSE.Sifive for license details.

#ifndef _SIFIVE_UART_H
#define _SIFIVE_UART_H

#define UART_REG_TXFIFO         0x00
#define UART_REG_RXFIFO         0x04
#define UART_REG_TXCTRL         0x08
#define UART_REG_RXCTRL         0x0c
#define UART_REG_IE             0x10
#define UART_REG_IP             0x14
#define UART_REG_DIV            0x18

#define UART_TXEN               0x1
#define UART_TXNSTOP            0x2
#define UART_TXWM(x)            (((x) & 0xffff) << 16)

#define UART_RXEN               0x1
#define UART_RXWM(x)            (((x) & 0xffff) << 16)

#define UART_IP_TXWM            0x1
#define UART_IP_RXWM            0x2

#endif
