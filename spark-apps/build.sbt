name := "dynatron-etl-spark-apps"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.15"

javacOptions ++= Seq("-source", "12", "-target", "12")


libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "3.3.0" ,
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % "2.20.1",
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.20.1" % "provided",
  "org.scala-lang" % "scala-reflect"  % "provided",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.12.381" ,
  "org.apache.hadoop" % "hadoop-aws" % "3.0.0",
  "org.apache.commons" % "commons-compress" % "1.24.0"
)





