name := "messenger"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.3",
  "org.reactivemongo" %% "reactivemongo" % "0.10.0"
)     

play.Project.playScalaSettings
