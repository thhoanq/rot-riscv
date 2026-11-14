package rot.arty100t

import chipyard.harness._
import chisel3._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.prci.{ClockGroup, ClockSinkNode, ResetWrangler}
import freechips.rocketchip.subsystem.SystemBusKey
import org.chipsalliance.cde.config.Parameters
import sifive.blocks.devices.gpio.{GPIOPortIO, PeripheryGPIOKey}
import sifive.blocks.devices.spi.{PeripherySPIKey, SPIPortIO}
import sifive.blocks.devices.uart.{PeripheryUARTKey, UARTParams, UARTPortIO}
import sifive.fpgashells.clocks.PLLFactoryKey
import sifive.fpgashells.ip.xilinx.IBUF
import sifive.fpgashells.shell._

class Arty100THarness(override implicit val p: Parameters) extends ArtyShell {
  def dp = designParameters

  val sysClkNode = dp(ClockInputOverlayKey).head.place(ClockInputDesignInput()).overlayOutput.node
  val harnessSysPLL = dp(PLLFactoryKey)()
  harnessSysPLL := sysClkNode

  val dutFreqMHz = (dp(SystemBusKey).dtsFrequency.get / (1000 * 1000)).toInt
  val dutClock = ClockSinkNode(freqMHz = dutFreqMHz)
  val dutWrangler = LazyModule(new ResetWrangler())
  val dutGroup = ClockGroup()
  dutClock := dutWrangler.node := dutGroup := harnessSysPLL

  val jtagModule = dp(JTAGDebugOverlayKey).head.place(JTAGDebugDesignInput()).overlayOutput.jtag

  val io_uart_bb = BundleBridgeSource(() => new UARTPortIO(dp(PeripheryUARTKey).headOption.getOrElse(UARTParams(0))))
  val uartOverlay = dp(UARTOverlayKey).head.place(UARTDesignInput(io_uart_bb))

  val io_gpio_bb = dp(PeripheryGPIOKey).map(p => BundleBridgeSource(() => new GPIOPortIO(p)))
  (dp(GPIOOverlayKey) zip dp(PeripheryGPIOKey)).zipWithIndex.map { case ((placer, params), i) =>
    placer.place(GPIODesignInput(params, io_gpio_bb(i)))
  }

  val io_spi_bb = BundleBridgeSource(() => new SPIPortIO(dp(PeripherySPIKey).head))
  dp(SPIOverlayKey).head.place(SPIDesignInput(dp(PeripherySPIKey).head, io_spi_bb))

  override lazy val module = new Arty100THarnessImp(this)
}

class Arty100THarnessImp(_outer: Arty100THarness) extends LazyRawModuleImp(_outer) with HasHarnessInstantiators {
  override def provideImplicitClockToLazyChildren = true
  val harness = _outer

  val reset = IO(Input(Bool())).suggestName("reset")
  _outer.xdc.addBoardPin(reset, "reset")

  val reset_ibuf = Module(new IBUF)
  reset_ibuf.io.I := reset

  val sysclk: Clock = _outer.sysClkNode.out.head._1.clock

  _outer.pllReset := !reset_ibuf.io.O

  def referenceClockFreqMHz = _outer.dutFreqMHz
  def referenceClock = _outer.dutClock.in.head._1.clock
  def referenceReset = _outer.dutClock.in.head._1.reset
  def success = { require(false, "Unused"); false.B }

  childClock := referenceClock
  childReset := referenceReset

  instantiateChipTops()
}
