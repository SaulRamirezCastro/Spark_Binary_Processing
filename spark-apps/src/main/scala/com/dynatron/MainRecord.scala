package com.dynatron

case class MainRecord(
  von: String,
  client: String,
  packageObject: String,
  ros: Ros
)

case class Ros(
  key: String = "",
  value: String = ""
)
