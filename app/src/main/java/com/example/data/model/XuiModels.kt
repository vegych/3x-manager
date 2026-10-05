package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "msg") val msg: String = ""
)

@JsonClass(generateAdapter = true)
data class ServerStatusResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "msg") val msg: String = "",
    @Json(name = "obj") val obj: ServerStatusObj? = null
)

@JsonClass(generateAdapter = true)
data class ServerStatusObj(
    @Json(name = "cpu") val cpu: Double = 0.0,
    @Json(name = "mem") val mem: StorageStat = StorageStat(),
    @Json(name = "swap") val swap: StorageStat? = null,
    @Json(name = "disk") val disk: StorageStat? = null,
    @Json(name = "xray") val xray: XrayStatusObj = XrayStatusObj(),
    @Json(name = "uptime") val uptime: Long = 0L,
    @Json(name = "loads") val loads: List<Double>? = null,
    @Json(name = "tcpCount") val tcpCount: Int = 0,
    @Json(name = "udpCount") val udpCount: Int = 0,
    @Json(name = "netTraffic") val netTraffic: NetTraffic? = null
)

@JsonClass(generateAdapter = true)
data class StorageStat(
    @Json(name = "current") val current: Long = 0L,
    @Json(name = "total") val total: Long = 1L
) {
    val percent: Int
        get() = if (total > 0) ((current.toDouble() / total.toDouble()) * 100).toInt() else 0
}

@JsonClass(generateAdapter = true)
data class XrayStatusObj(
    @Json(name = "state") val state: String = "unknown",
    @Json(name = "errorMsg") val errorMsg: String = "",
    @Json(name = "version") val version: String = ""
)

@JsonClass(generateAdapter = true)
data class NetTraffic(
    @Json(name = "sent") val sent: Long = 0L,
    @Json(name = "recv") val recv: Long = 0L
)

@JsonClass(generateAdapter = true)
data class InboundsListResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "msg") val msg: String = "",
    @Json(name = "obj") val obj: List<InboundItem>? = null
)

@JsonClass(generateAdapter = true)
data class InboundItem(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "up") val up: Long = 0L,
    @Json(name = "down") val down: Long = 0L,
    @Json(name = "total") val total: Long = 0L,
    @Json(name = "remark") val remark: String = "",
    @Json(name = "enable") val enable: Boolean = true,
    @Json(name = "expiryTime") val expiryTime: Long = 0L,
    @Json(name = "listen") val listen: String? = null,
    @Json(name = "port") val port: Int = 0,
    @Json(name = "protocol") val protocol: String = "vless",
    @Json(name = "settings") val settings: String? = null,
    @Json(name = "streamSettings") val streamSettings: String? = null,
    @Json(name = "sniffing") val sniffing: String? = null,
    @Json(name = "clientStats") val clientStats: List<ClientStatItem>? = null
)

@JsonClass(generateAdapter = true)
data class ClientStatItem(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "inboundId") val inboundId: Int = 0,
    @Json(name = "enable") val enable: Boolean = true,
    @Json(name = "email") val email: String = "",
    @Json(name = "up") val up: Long = 0L,
    @Json(name = "down") val down: Long = 0L,
    @Json(name = "expiryTime") val expiryTime: Long = 0L,
    @Json(name = "total") val total: Long = 0L
)

// Helper model for parsed inbound client details
data class ParsedClient(
    val id: String = "", // UUID or password
    val email: String = "",
    val flow: String = "",
    val enable: Boolean = true,
    val upBytes: Long = 0L,
    val downBytes: Long = 0L,
    val totalLimitBytes: Long = 0L,
    val expiryTime: Long = 0L,
    val subId: String = ""
)
