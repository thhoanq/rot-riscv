package rot.arty100t

import chipyard.harness._
import chipyard.iobinders._
import chisel3._

class WithArty100TUARTHarnessBinder extends HarnessBinder({
  case (th: Arty100THarnessImp, port: UARTPort, chipId: Int) =>
    th.harness.io_uart_bb.bundle <> port.io
})

class WithArty100TSPISDCardHarnessBinder extends HarnessBinder({
  case (th: Arty100THarnessImp, port: SPIPort, chipId: Int) =>
    th.harness.io_spi_bb.bundle <> port.io
})

class WithArty100TJTAGHarnessBinder extends HarnessBinder({
  case (th: Arty100THarnessImp, port: JTAGPort, chipId: Int) =>
    val jtagModule = th.harness.jtagModule.getWrappedValue
    jtagModule.TDO.data := port.io.TDO
    jtagModule.TDO.driven := true.B
    port.io.TCK := jtagModule.TCK
    port.io.TMS := jtagModule.TMS
    port.io.TDI := jtagModule.TDI
    port.io.reset.foreach(_ := th.referenceReset)
})

class WithArty100TGPIOHarnessBinder extends HarnessBinder({
  case (th: Arty100THarnessImp, port: GPIOPinsPort, chipId: Int) =>
    th.harness.io_gpio_bb(port.gpioId).bundle <> port.io
})
