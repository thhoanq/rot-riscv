package rot.arty100t

import chipyard.CanHaveMasterTLMemPort
import chipyard.iobinders._
import chisel3._
import chisel3.reflect.DataMirror
import freechips.rocketchip.resources.{Resource, ResourceAddress, ResourceBinding}
import freechips.rocketchip.subsystem.{HasTileLinkLocations, PBUS}
import freechips.rocketchip.tilelink.TLBundle
import org.chipsalliance.diplomacy.lazymodule.InModuleBody
import org.chipsalliance.diplomacy.nodes.HeterogeneousBag
import sifive.blocks.devices.gpio.HasPeripheryGPIO
import sifive.blocks.devices.spi.{HasPeripherySPI, MMCDevice}
import sifive.blocks.devices.uart.HasPeripheryUART

class WithUARTIOPassthrough extends OverrideIOBinder({
  (system: HasPeripheryUART) => {
    val bus = system.asInstanceOf[HasTileLinkLocations].locateTLBusWrapper(PBUS)
    val freqMHz = (bus.dtsFrequency.get / 1000000).toInt
    val ports = system.uart.zipWithIndex.map { case (sysio, i) =>
      val io = IO(sysio.cloneType).suggestName(s"uart_$i")
      io <> sysio
      UARTPort(() => io, i, freqMHz)
    }
    (ports, Nil)
  }
})

class WithSPIIOPassthrough extends OverrideLazyIOBinder({
  (system: HasPeripherySPI) => {
    ResourceBinding {
      Resource(new MMCDevice(system.tlSpiNodes.head.device, 1), "reg").bind(ResourceAddress(0))
    }
    InModuleBody {
      val ports = system.spi.zipWithIndex.map { case (sysio, i) =>
        val io = IO(sysio.cloneType).suggestName(s"spi_$i")
        io <> sysio
        SPIPort(() => io)
      }
      (ports, Nil)
    }
  }
})

class WithTLIOPassthrough extends OverrideIOBinder({
  (system: CanHaveMasterTLMemPort) => {
    val io = IO(DataMirror.internal.chiselTypeClone[HeterogeneousBag[TLBundle]](system.mem_tl)).suggestName("tl_slave")
    io <> system.mem_tl
    (Seq(TLMemPort(() => io)), Nil)
  }
})

class WithGPIOIOPassthrough extends OverrideIOBinder({
  (system: HasPeripheryGPIO) => {
    val ports = system.gpio.zipWithIndex.map { case (sysio, i) =>
      val io = IO(sysio.cloneType).suggestName(s"gpio_$i")
      io <> sysio
      GPIOPinsPort(() => io, i)
    }
    (ports, Nil)
  }
})
