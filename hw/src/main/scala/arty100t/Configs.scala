package rot.arty100t

import java.io.File

import scala.sys.process._

import freechips.rocketchip.devices.debug.DebugModuleKey
import freechips.rocketchip.devices.tilelink.BootROMLocated
import freechips.rocketchip.resources.DTSTimebase
import freechips.rocketchip.subsystem._
import freechips.rocketchip.util.SystemFileName
import org.chipsalliance.cde.config._
import org.chipsalliance.diplomacy.lazymodule._
import sifive.blocks.devices.gpio.{GPIOParams, PeripheryGPIOKey}
import sifive.blocks.devices.spi.{PeripherySPIKey, SPIParams}
import sifive.blocks.devices.uart.{PeripheryUARTKey, UARTParams}
import sifive.fpgashells.shell.DesignKey
import testchipip.boot.CustomBootPinKey
import testchipip.serdes.SerialTLKey
import testchipip.soc.BankedScratchpadKey

class WithSystemModifications extends Config((site, here, up) => {
  case DTSTimebase => BigInt(1000000L)
  case BootROMLocated(x) => up(BootROMLocated(x), site).map { p =>
    val memSize = site(BankedScratchpadKey).find(_.base == BigInt(0x80000000L)).map(_.size).getOrElse(BigInt(16 << 10))
    val freqMHz = (site(SystemBusKey).dtsFrequency.get / (1000 * 1000)).toLong
    val dir = rot.Paths.resource("arty100t/sdboot").getPath
    require(Seq("make", "-C", dir, "clean").! == 0, "Failed to clean sdboot")
    require(Seq("make", "-C", dir, s"XLEN=${site(MaxXLen)}", s"PBUS_CLK=$freqMHz", s"MEM_SIZE=$memSize").! == 0, "Failed to build sdboot")
    p.copy(hang = 0x10000, contentFileName = SystemFileName(new File(dir, "build/sdboot.bin").getPath))
  }
  case DesignKey => (p: Parameters) => new SimpleLazyRawModule()(p)
  case DebugModuleKey => up(DebugModuleKey).map(_.copy(clockGate = false))
  case CustomBootPinKey => None
  case SerialTLKey => Nil
})

class WithDefaultPeripherals extends Config((site, here, up) => {
  case PeripheryUARTKey => List(UARTParams(address = BigInt(0x64000000L)))
  case PeripherySPIKey  => List(SPIParams(rAddress = BigInt(0x64001000L)))
  case PeripheryGPIOKey => List(GPIOParams(address = BigInt(0x64002000L), width = 24))
})

class WithArty100TTweaks extends Config(
  new chipyard.harness.WithHarnessBinderClockFreqMHz(50) ++
  new chipyard.config.WithUniformBusFrequencies(50.0) ++
  new chipyard.harness.WithAllClocksFromHarnessClockInstantiator ++
  new chipyard.clocking.WithPassthroughClockGenerator ++
  new WithArty100TUARTHarnessBinder ++
  new WithArty100TSPISDCardHarnessBinder ++
  new WithArty100TJTAGHarnessBinder ++
  new WithArty100TGPIOHarnessBinder ++
  new WithGPIOIOPassthrough ++
  new WithUARTIOPassthrough ++
  new WithSPIIOPassthrough ++
  new WithTLIOPassthrough ++
  new WithDefaultPeripherals ++
  new WithSystemModifications ++
  new WithoutTLMonitors)

class IbexArty100TConfig extends Config(
  new WithTimebase(BigInt(50000000L)) ++
  new WithArty100TTweaks ++
  new rot.IbexBaseConfig)
