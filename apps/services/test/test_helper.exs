Application.stop(:services)
Application.stop(:ra)
{_, 0} = System.cmd("epmd", ["-daemon"])
:ok = LocalCluster.start()
ExUnit.start()
