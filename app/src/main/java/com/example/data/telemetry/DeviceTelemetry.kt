package com.example.data.telemetry

data class DeviceTelemetry(
  val batteryLevel: Int = 100,
  val isCharging: Boolean = false,
  val chargeSource: String = "BATTERY",
  val batteryTempCelsius: Float = 28.0f,
  val batteryVoltageVolts: Float = 4.1f,
  val batteryHealth: String = "GOOD",
  val totalRamMb: Long = 8192,
  val availRamMb: Long = 4096,
  val usedRamPercent: Int = 50,
  val isLowMemory: Boolean = false,
  val networkType: String = "WIFI",
  val isConnected: Boolean = true,
  val deviceModel: String = "MARK IV",
  val androidVersion: String = "Android 14",
  val uptimeFormatted: String = "00:00:00",
  val storageFreeGb: Double = 32.0,
  val storageTotalGb: Double = 128.0,
  val cpuCoreCount: Int = 8
)
