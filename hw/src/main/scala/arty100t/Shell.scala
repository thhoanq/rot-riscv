package rot.arty100t

import chisel3._
import freechips.rocketchip.diplomacy.InModuleBody
import org.chipsalliance.cde.config.Parameters
import sifive.fpgashells.shell._
import sifive.fpgashells.shell.xilinx.Series7Shell

abstract class ArtyShell()(implicit p: Parameters) extends Series7Shell {
  val pllReset  = InModuleBody { Wire(Bool()) }
  val sys_clock = Overlay(ClockInputOverlayKey, new SysClockArtyShellPlacer(this, ClockInputShellInput()))
  val jtag      = Overlay(JTAGDebugOverlayKey, new JTAGDebugArtyShellPlacer(this, JTAGDebugShellInput()))
  val uart      = Overlay(UARTOverlayKey, new UARTArtyShellPlacer(this, UARTShellInput()))
  val sdio      = Overlay(SPIOverlayKey, new SDIOArtyShellPlacer(this, SPIShellInput()))
  val gpio      = Seq.tabulate(24)(i => { Overlay(GPIOOverlayKey, new GPIOArtyShellPlacer(this, GPIOShellInput())) })
}
