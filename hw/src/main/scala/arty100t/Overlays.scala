package rot.arty100t

import chisel3.Clock
import freechips.rocketchip.diplomacy.{InModuleBody, ValName}
import freechips.rocketchip.prci.ClockSourceNode
import sifive.fpgashells.shell._
import sifive.fpgashells.shell.xilinx._

class SysClockArtyPlacedOverlay(val shell: ArtyShell, name: String, val designInput: ClockInputDesignInput, val shellInput: ClockInputShellInput)
  extends SingleEndedClockInputXilinxPlacedOverlay(name, designInput, shellInput)
{
  val node = shell { ClockSourceNode(freqMHz = 100, jitterPS = 50) }

  shell { InModuleBody {
    val clk: Clock = io
    shell.xdc.addPackagePin(clk, "E3")
    shell.xdc.addIOStandard(clk, "LVCMOS33")
  } }
}

class SysClockArtyShellPlacer(val shell: ArtyShell, val shellInput: ClockInputShellInput)(implicit val valName: ValName)
  extends ClockInputShellPlacer[ArtyShell] {
  def place(designInput: ClockInputDesignInput) = new SysClockArtyPlacedOverlay(shell, valName.value, designInput, shellInput)
}

class UARTArtyPlacedOverlay(val shell: ArtyShell, name: String, val designInput: UARTDesignInput, val shellInput: UARTShellInput)
  extends UARTXilinxPlacedOverlay(name, designInput, shellInput, false)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(
      ("A9", IOPin(io.rxd)),
      ("D10", IOPin(io.txd)))

    packagePinsWithPackageIOs foreach { case (pin, io) =>
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
      shell.xdc.addIOB(io)
    }
  } }
}

class UARTArtyShellPlacer(val shell: ArtyShell, val shellInput: UARTShellInput)(implicit val valName: ValName)
  extends UARTShellPlacer[ArtyShell] {
  def place(designInput: UARTDesignInput) = new UARTArtyPlacedOverlay(shell, valName.value, designInput, shellInput)
}

class JTAGDebugArtyPlacedOverlay(val shell: ArtyShell, name: String, val designInput: JTAGDebugDesignInput, val shellInput: JTAGDebugShellInput)
  extends JTAGDebugXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    shell.sdc.addClock("JTCK", IOPin(io.jtag_TCK), 10)
    shell.sdc.addGroup(clocks = Seq("JTCK"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.jtag_TCK))
    val packagePinsWithPackageIOs = Seq(
      ("D4", IOPin(io.jtag_TDI)),
      ("D3", IOPin(io.jtag_TMS)),
      ("F4", IOPin(io.jtag_TCK)),
      ("F3", IOPin(io.jtag_TDO)),
      ("H2", IOPin(io.srst_n)))

    packagePinsWithPackageIOs foreach { case (pin, io) =>
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
      shell.xdc.addPullup(io)
    }
  } }
}

class JTAGDebugArtyShellPlacer(val shell: ArtyShell, val shellInput: JTAGDebugShellInput)(implicit val valName: ValName)
  extends JTAGDebugShellPlacer[ArtyShell] {
  def place(designInput: JTAGDebugDesignInput) = new JTAGDebugArtyPlacedOverlay(shell, valName.value, designInput, shellInput)
}

class SDIOArtyPlacedOverlay(val shell: ArtyShell, name: String, val designInput: SPIDesignInput, val shellInput: SPIShellInput)
  extends SDIOXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(
      ("D12", IOPin(io.spi_clk)),
      ("B11", IOPin(io.spi_cs)),
      ("A11", IOPin(io.spi_dat(0))),
      ("D13", IOPin(io.spi_dat(1))),
      ("B18", IOPin(io.spi_dat(2))),
      ("G13", IOPin(io.spi_dat(3))))

    packagePinsWithPackageIOs foreach { case (pin, io) =>
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
      shell.xdc.addIOB(io)
    }
    packagePinsWithPackageIOs drop 1 foreach { case (pin, io) =>
      shell.xdc.addPullup(io)
    }
  } }
}

class SDIOArtyShellPlacer(val shell: ArtyShell, val shellInput: SPIShellInput)(implicit val valName: ValName)
  extends SPIShellPlacer[ArtyShell] {
  def place(designInput: SPIDesignInput) = new SDIOArtyPlacedOverlay(shell, valName.value, designInput, shellInput)
}

class GPIOArtyPlacedOverlay(val shell: ArtyShell, name: String, val designInput: GPIODesignInput, val shellInput: GPIOShellInput)
  extends GPIOXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    val gpioLocations = List(
      "H5", "J5", "T9", "T10",
      "E1", "F6", "G6", "G4",
      "J4", "G3", "H4", "J2",
      "J3", "K2", "H6", "K1",
      "A8", "C11", "C10", "A10",
      "D9", "C9", "B9", "B8")
    val packagePinsWithPackageIOs = io.gpio.zip(gpioLocations).map { case (io, pin) => (pin, IOPin(io)) }

    packagePinsWithPackageIOs foreach { case (pin, io) =>
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS33")
      shell.xdc.addIOB(io)
    }
  } }
}

class GPIOArtyShellPlacer(val shell: ArtyShell, val shellInput: GPIOShellInput)(implicit val valName: ValName)
  extends GPIOShellPlacer[ArtyShell] {
  def place(designInput: GPIODesignInput) = new GPIOArtyPlacedOverlay(shell, valName.value, designInput, shellInput)
}
