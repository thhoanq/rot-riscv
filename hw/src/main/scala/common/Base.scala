package rot

import java.io.File

import freechips.rocketchip.subsystem._
import org.chipsalliance.cde.config.Config

object Paths {
  val root: File = new File(sys.env.getOrElse("ROT_ROOT", "..")).getCanonicalFile
  def resource(path: String): File = new File(root, s"hw/src/main/resources/$path")
}

class WithNonSystemBusWidth(bitWidth: Int) extends Config((site, here, up) => {
  case MemoryBusKey    => up(MemoryBusKey, site).copy(beatBytes = bitWidth / 8)
  case FrontBusKey     => up(FrontBusKey, site).copy(beatBytes = bitWidth / 8)
  case PeripheryBusKey => up(PeripheryBusKey, site).copy(beatBytes = bitWidth / 8)
  case ControlBusKey   => up(ControlBusKey, site).copy(beatBytes = bitWidth / 8)
})

class WithIbexDebugAddrs extends Config((site, here, up) => {
  case TilesLocated(InSubsystem) => up(TilesLocated(InSubsystem), site).map {
    case tp: ibex.IbexTileAttachParams => tp.copy(tileParams = tp.tileParams.copy(
      core = tp.tileParams.core.copy(dmHaltAddr = 0x800, dmExceptionAddr = 0x808)))
    case other => other
  }
})

class IbexBaseConfig extends Config(
  new WithIbexDebugAddrs ++
  new testchipip.soc.WithSbusScratchpad(base = 0x80000000L, size = 64 << 10) ++
  new testchipip.soc.WithNoScratchpads ++
  new WithIncoherentBusTopology ++
  new WithNoMemPort ++
  new WithNonSystemBusWidth(32) ++
  new ibex.WithNIbexCores(1) ++
  new chipyard.config.AbstractConfig)
