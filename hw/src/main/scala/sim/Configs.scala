package rot.sim

import java.io.File

import scala.sys.process._

import chipyard.harness.{HarnessBinder, HarnessClockInstantiator, HarnessClockInstantiatorKey, HasHarnessInstantiators}
import chipyard.iobinders.SPIPort
import chisel3._
import freechips.rocketchip.devices.debug.DebugModuleKey
import freechips.rocketchip.devices.tilelink.BootROMLocated
import freechips.rocketchip.subsystem.{WithTimebase, WithoutTLMonitors}
import freechips.rocketchip.util.SystemFileName
import org.chipsalliance.cde.config.Config
import sifive.blocks.devices.gpio.{GPIOParams, PeripheryGPIOKey}
import sifive.blocks.devices.spi.{PeripherySPIKey, SPIParams}
import sifive.blocks.devices.uart.{PeripheryUARTKey, UARTParams}
import testchipip.boot.CustomBootPinKey

class SimAllClocksFromReferenceInstantiator extends HarnessClockInstantiator {
  def instantiateHarnessClocks(refClock: Clock, refClockFreqMHz: Double): Unit = {
    val freqs = clockMap.map(_._2._1)
    freqs.tail.foreach(t => require(t == freqs.head, s"Mismatching clocks $t != ${freqs.head}"))
    clockMap.values.foreach { case (_, clock) => clock := refClock }
  }
}

class WithSimAllClocksFromReference extends Config((site, here, up) => {
  case HarnessClockInstantiatorKey => () => new SimAllClocksFromReferenceInstantiator
})

class WithSimSPITiedOff extends HarnessBinder({
  case (th: HasHarnessInstantiators, port: SPIPort, chipId: Int) =>
    port.io.dq.foreach(_.i := false.B)
})

class WithSimPeripherals extends Config((site, here, up) => {
  case PeripheryUARTKey => List(UARTParams(address = BigInt(0x64000000L), initBaudRate = BigInt(1000000)))
  case PeripherySPIKey  => List(SPIParams(rAddress = BigInt(0x64001000L)))
  case PeripheryGPIOKey => List(GPIOParams(address = BigInt(0x64002000L), width = 24))
})

class WithSimSystemModifications extends Config((site, here, up) => {
  case BootROMLocated(x) => up(BootROMLocated(x)).map { p =>
    val dir = rot.Paths.resource("arty100t/simboot").getPath
    require(Seq("make", "-C", dir, "clean").! == 0, "Failed to clean simboot")
    require(Seq("make", "-C", dir).! == 0, "Failed to build simboot")
    p.copy(hang = 0x10000, contentFileName = SystemFileName(new File(dir, "build/simboot.img").getPath))
  }
  case DebugModuleKey => up(DebugModuleKey).map(_.copy(clockGate = false))
  case CustomBootPinKey => None
})

class WithSimTweaks extends Config(
  new WithSimAllClocksFromReference ++
  new chipyard.harness.WithHarnessBinderClockFreqMHz(50) ++
  new WithSimSPITiedOff ++
  new chipyard.iobinders.WithGPIOPunchthrough ++
  new chipyard.config.WithUniformBusFrequencies(50.0) ++
  new chipyard.clocking.WithPassthroughClockGenerator ++
  new WithSimPeripherals ++
  new WithSimSystemModifications ++
  new WithoutTLMonitors)

class IbexSimConfig extends Config(
  new WithTimebase(BigInt(50000000L)) ++
  new WithSimTweaks ++
  new rot.IbexBaseConfig)
