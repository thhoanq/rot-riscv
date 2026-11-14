if {$argc < 1} {
    puts "ERROR: No bitstream path provided."
    exit 1
}
set bit_file [lindex $argv 0]

if {![file exists $bit_file]} {
    puts "ERROR: Bitstream file '$bit_file' does not exist."
    exit 1
}
puts "INFO: Bitstream = '$bit_file'."

open_hw_manager
connect_hw_server -allow_non_jtag

set targets [get_hw_targets]
if {[llength $targets] == 0} {
    puts "ERROR: No hardware targets found."
    close_hw_manager
    exit 1
}
current_hw_target [lindex $targets 0];
open_hw_target
puts "INFO: Hardware target opened [current_hw_target]."

set ARTY100T "xc7a100*"
set fpga ""
foreach dev [get_hw_devices] {
    if {[string match -nocase $ARTY100T $dev]} {
        set fpga $dev
        break
    }
}
if {$fpga eq "" } {
    puts "ERROR: No $ARTY100T device found."
    exit 1
}
current_hw_device [get_hw_devices $fpga]
refresh_hw_device -update_hw_probe false [current_hw_device]
puts "INFO: FPGA target = $fpga."

set_property PROGRAM.FILE $bit_file [current_hw_device]
puts "INFO: Bitstream is programming..."
program_hw_devices [current_hw_device]
refresh_hw_device [current_hw_device]
puts "INFO: Bitstream programmed successfully."

close_hw_target
disconnect_hw_server
close_hw_manager
exit 0
