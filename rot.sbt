LocalProject("chipyard_fpga") / Compile / unmanagedSourceDirectories ++= {
  val root = (LocalRootProject / baseDirectory).value.getParentFile
  Seq(root / "hw" / "src" / "main" / "scala", root / "rot-riscv-ip" / "src" / "main" / "scala").filter(_.isDirectory)
}

LocalProject("chipyard_fpga") / Compile / unmanagedResourceDirectories ++= {
  val root = (LocalRootProject / baseDirectory).value.getParentFile
  Seq(root / "hw" / "src" / "main" / "resources", root / "rot-riscv-ip" / "src" / "main" / "resources").filter(_.isDirectory)
}

LocalProject("chipyard_fpga") / libraryDependencies ++=
  (LocalProject("chipyard") / libraryDependencies).value.filter(_.configurations.exists(_.startsWith("plugin")))
