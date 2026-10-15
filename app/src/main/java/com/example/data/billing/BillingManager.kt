package com.example.data.billing

import com.example.data.db.ServerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

data class BillingFetchResult(
    val success: Boolean,
    val balance: Double? = null,
    val currency: String = "RUB",
    val hoursLeft: Int? = null,
    val daysLeft: Int? = null,
    val errorMessage: String? = null
)

object BillingManager {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    val dateFormat = SimpleDateFormat("d MMM", Locale("ru"))
    val fullDateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))

    fun getNextDueDate(server: ServerEntity): Calendar {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val bType = try {
            BillingType.valueOf(server.billingType)
        } catch (_: Exception) {
            BillingType.NONE
        }

        if (server.billingLastPaidDate > 0L) {
            if (server.billingLastPaidDate > today.timeInMillis) {
                return Calendar.getInstance().apply {
                    timeInMillis = server.billingLastPaidDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }

            val nextBilling = Calendar.getInstance().apply {
                timeInMillis = server.billingLastPaidDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            when (bType) {
                BillingType.MONTHLY_DAY -> {
                    val targetDay = server.billingDayOfMonth.coerceIn(1, 31)
                    nextBilling.set(Calendar.DAY_OF_MONTH, 1)
                    nextBilling.add(Calendar.MONTH, 1)
                    val maxDays = nextBilling.getActualMaximum(Calendar.DAY_OF_MONTH)
                    nextBilling.set(Calendar.DAY_OF_MONTH, minOf(targetDay, maxDays))
                    return nextBilling
                }

                BillingType.INTERVAL_DAYS -> {
                    val interval = if (server.billingIntervalDays > 0) server.billingIntervalDays else 30
                    nextBilling.add(Calendar.DAY_OF_YEAR, interval)
                    return nextBilling
                }

                else -> return nextBilling
            }
        }

        val nextBilling = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (bType) {
            BillingType.MONTHLY_DAY -> {
                val targetDay = server.billingDayOfMonth.coerceIn(1, 31)
                val currentYear = today.get(Calendar.YEAR)
                val currentMonth = today.get(Calendar.MONTH)
                val currentDay = today.get(Calendar.DAY_OF_MONTH)

                if (currentDay <= targetDay) {
                    val maxThisMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val dayToSet = minOf(targetDay, maxThisMonth)
                    nextBilling.set(currentYear, currentMonth, dayToSet)
                } else {
                    nextBilling.set(currentYear, currentMonth, 1)
                    nextBilling.add(Calendar.MONTH, 1)
                    val maxNextMonth = nextBilling.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val dayToSet = minOf(targetDay, maxNextMonth)
                    nextBilling.set(Calendar.DAY_OF_MONTH, dayToSet)
                }
                return nextBilling
            }

            BillingType.INTERVAL_DAYS -> {
                val interval = if (server.billingIntervalDays > 0) server.billingIntervalDays else 30
                val baseTime = if (server.createdAt > 0) server.createdAt else today.timeInMillis
                nextBilling.timeInMillis = baseTime + interval * 24L * 60 * 60 * 1000
                nextBilling.set(Calendar.HOUR_OF_DAY, 0)
                nextBilling.set(Calendar.MINUTE, 0)
                nextBilling.set(Calendar.SECOND, 0)
                nextBilling.set(Calendar.MILLISECOND, 0)
                return nextBilling
            }

            else -> return nextBilling
        }
    }

    fun addPeriod(server: ServerEntity, baseDateMs: Long? = null): Long {
        val bType = try {
            BillingType.valueOf(server.billingType)
        } catch (_: Exception) {
            BillingType.NONE
        }

        val baseCal = if (baseDateMs != null && baseDateMs > 0L) {
            Calendar.getInstance().apply {
                timeInMillis = baseDateMs
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } else {
            getNextDueDate(server)
        }

        when (bType) {
            BillingType.MONTHLY_DAY -> {
                val targetDay = server.billingDayOfMonth.coerceIn(1, 31)
                baseCal.set(Calendar.DAY_OF_MONTH, 1)
                baseCal.add(Calendar.MONTH, 1)
                val maxDays = baseCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                baseCal.set(Calendar.DAY_OF_MONTH, minOf(targetDay, maxDays))
                return baseCal.timeInMillis
            }

            BillingType.INTERVAL_DAYS -> {
                val interval = if (server.billingIntervalDays > 0) server.billingIntervalDays else 30
                baseCal.add(Calendar.DAY_OF_YEAR, interval)
                return baseCal.timeInMillis
            }

            else -> return baseCal.timeInMillis
        }
    }

    fun subtractPeriod(server: ServerEntity, baseDateMs: Long? = null): Long {
        val bType = try {
            BillingType.valueOf(server.billingType)
        } catch (_: Exception) {
            BillingType.NONE
        }

        val baseCal = if (baseDateMs != null && baseDateMs > 0L) {
            Calendar.getInstance().apply {
                timeInMillis = baseDateMs
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } else {
            getNextDueDate(server)
        }

        when (bType) {
            BillingType.MONTHLY_DAY -> {
                val targetDay = server.billingDayOfMonth.coerceIn(1, 31)
                baseCal.set(Calendar.DAY_OF_MONTH, 1)
                baseCal.add(Calendar.MONTH, -1)
                val maxDays = baseCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                baseCal.set(Calendar.DAY_OF_MONTH, minOf(targetDay, maxDays))
                return baseCal.timeInMillis
            }

            BillingType.INTERVAL_DAYS -> {
                val interval = if (server.billingIntervalDays > 0) server.billingIntervalDays else 30
                baseCal.add(Calendar.DAY_OF_YEAR, -interval)
                return baseCal.timeInMillis
            }

            else -> return baseCal.timeInMillis
        }
    }

    fun calculateStatus(server: ServerEntity): BillingStatus {
        val bType = try {
            BillingType.valueOf(server.billingType)
        } catch (_: Exception) {
            BillingType.NONE
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return when (bType) {
            BillingType.NONE -> {
                BillingStatus(
                    type = BillingType.NONE,
                    daysLeft = null,
                    badgeText = "",
                    detailsText = "",
                    isUrgent = false,
                    isExpired = false
                )
            }

            BillingType.MONTHLY_DAY -> {
                val nextBilling = getNextDueDate(server)
                val diffMs = nextBilling.timeInMillis - today.timeInMillis
                val daysLeft = (diffMs / (24L * 60 * 60 * 1000)).toInt()
                val dateStr = dateFormat.format(nextBilling.time)

                val badge = formatDaysBadge(daysLeft, dateStr)
                BillingStatus(
                    type = BillingType.MONTHLY_DAY,
                    daysLeft = daysLeft,
                    badgeText = badge,
                    detailsText = "Оплата $dateStr ($daysLeft дн.)",
                    isUrgent = daysLeft in 0..3,
                    isExpired = daysLeft < 0,
                    nextPaymentDateStr = dateStr
                )
            }

            BillingType.INTERVAL_DAYS -> {
                val nextBilling = getNextDueDate(server)
                val interval = if (server.billingIntervalDays > 0) server.billingIntervalDays else 30
                val diffMs = nextBilling.timeInMillis - today.timeInMillis
                val daysLeft = (diffMs / (24L * 60 * 60 * 1000)).toInt()
                val dateStr = dateFormat.format(nextBilling.time)

                val badge = formatDaysBadge(daysLeft, dateStr)
                BillingStatus(
                    type = BillingType.INTERVAL_DAYS,
                    daysLeft = daysLeft,
                    badgeText = badge,
                    detailsText = "Период $interval дн. ➔ $dateStr",
                    isUrgent = daysLeft in 0..3,
                    isExpired = daysLeft < 0,
                    nextPaymentDateStr = dateStr
                )
            }

            BillingType.TIMEWEB_CLOUD -> {
                val balance = server.cachedBalance
                val hours = server.cachedHoursLeft
                val days = server.cachedDaysLeft ?: (hours?.let { it / 24 })

                val badgeText = if (balance != null) {
                    val formattedBal = String.format(Locale.US, "%.2f", balance)
                    if (days != null) {
                        "$formattedBal ₽ ($days дн.)"
                    } else {
                        "$formattedBal ₽"
                    }
                } else {
                    if (server.billingApiKey.isNotBlank()) "Timeweb..." else "Timeweb (нет API)"
                }

                val isUrgent = days != null && days in 0..3
                val isExpired = (balance != null && balance <= 0.0) || (days != null && days < 0)

                BillingStatus(
                    type = BillingType.TIMEWEB_CLOUD,
                    daysLeft = days,
                    badgeText = badgeText,
                    detailsText = "Баланс Timeweb Cloud: ${server.cachedBalance ?: 0} ₽",
                    isUrgent = isUrgent,
                    isExpired = isExpired,
                    balance = balance,
                    currency = server.cachedCurrency
                )
            }

            BillingType.SELECTEL -> {
                val balance = server.cachedBalance
                val days = server.cachedDaysLeft

                val badgeText = if (balance != null) {
                    val formattedBal = String.format(Locale.US, "%.2f", balance)
                    if (days != null) {
                        "$formattedBal ₽ ($days дн.)"
                    } else {
                        "$formattedBal ₽"
                    }
                } else {
                    if (server.billingApiKey.isNotBlank()) "Selectel..." else "Selectel (нет API)"
                }

                val isUrgent = days != null && days in 0..3
                val isExpired = (balance != null && balance <= 0.0) || (days != null && days < 0)

                BillingStatus(
                    type = BillingType.SELECTEL,
                    daysLeft = days,
                    badgeText = badgeText,
                    detailsText = "Баланс Selectel: ${server.cachedBalance ?: 0} ₽",
                    isUrgent = isUrgent,
                    isExpired = isExpired,
                    balance = balance,
                    currency = server.cachedCurrency
                )
            }

            BillingType.BEGET -> {
                val balance = server.cachedBalance
                val days = server.cachedDaysLeft

                val badgeText = if (balance != null) {
                    val formattedBal = String.format(Locale.US, "%.2f", balance)
                    if (days != null) {
                        "$formattedBal ₽ ($days дн.)"
                    } else {
                        "$formattedBal ₽"
                    }
                } else {
                    if (server.billingApiKey.isNotBlank()) "Beget..." else "Beget (нет API)"
                }

                val isUrgent = days != null && days in 0..3
                val isExpired = (balance != null && balance <= 0.0) || (days != null && days < 0)

                BillingStatus(
                    type = BillingType.BEGET,
                    daysLeft = days,
                    badgeText = badgeText,
                    detailsText = "Баланс Beget: ${server.cachedBalance ?: 0} ₽",
                    isUrgent = isUrgent,
                    isExpired = isExpired,
                    balance = balance,
                    currency = server.cachedCurrency
                )
            }
        }
    }

    private fun formatDaysBadge(daysLeft: Int, dateStr: String): String {
        return when {
            daysLeft < 0 -> "Просрочено!"
            daysLeft == 0 -> "Оплата сегодня!"
            daysLeft == 1 -> "Оплата завтра"
            daysLeft in 2..4 -> "$dateStr ($daysLeft дня)"
            else -> "$dateStr ($daysLeft дн.)"
        }
    }

    suspend fun testBillingApi(
        bType: BillingType,
        apiKey: String,
        monthlyCost: Double = 0.0
    ): BillingFetchResult = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) {
            return@withContext BillingFetchResult(success = false, errorMessage = "API ключ не указан")
        }

        when (bType) {
            BillingType.TIMEWEB_CLOUD -> {
                try {
                    val request = Request.Builder()
                        .url("https://api.timeweb.cloud/api/v1/account/finances")
                        .addHeader("Authorization", "Bearer $cleanKey")
                        .get()
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            return@withContext BillingFetchResult(
                                success = false,
                                errorMessage = "HTTP ${response.code}: ${response.message}"
                            )
                        }
                        val body = response.body?.string() ?: ""
                        val root = JSONObject(body)
                        val finances = root.getJSONObject("finances")
                        val balance = finances.optDouble("balance", 0.0)
                        val currency = finances.optString("currency", "RUB")
                        val hoursLeft = if (finances.has("hours_left") && !finances.isNull("hours_left")) {
                            finances.getInt("hours_left")
                        } else null
                        val daysLeft = hoursLeft?.let { it / 24 }

                        BillingFetchResult(
                            success = true,
                            balance = balance,
                            currency = currency,
                            hoursLeft = hoursLeft,
                            daysLeft = daysLeft
                        )
                    }
                } catch (e: Exception) {
                    BillingFetchResult(success = false, errorMessage = e.message ?: "Ошибка сети Timeweb")
                }
            }

            BillingType.SELECTEL -> {
                try {
                    val request = Request.Builder()
                        .url("https://api.selectel.ru/v3/balances")
                        .addHeader("X-token", cleanKey)
                        .get()
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            return@withContext BillingFetchResult(
                                success = false,
                                errorMessage = "HTTP ${response.code}: ${response.message}"
                            )
                        }
                        val body = response.body?.string() ?: ""
                        val root = JSONObject(body)
                        val data = root.optJSONObject("data")
                        val billings = data?.optJSONArray("billings")

                        var finalSumKopecks = 0.0
                        if (billings != null && billings.length() > 0) {
                            for (i in 0 until billings.length()) {
                                val b = billings.getJSONObject(i)
                                finalSumKopecks += b.optDouble("final_sum", 0.0)
                            }
                        }

                        val balanceRub = finalSumKopecks / 100.0
                        val daysLeft = if (monthlyCost > 0.0) {
                            val dailyCost = monthlyCost / 30.0
                            if (dailyCost > 0.0) (balanceRub / dailyCost).toInt() else null
                        } else null

                        BillingFetchResult(
                            success = true,
                            balance = balanceRub,
                            currency = "RUB",
                            daysLeft = daysLeft
                        )
                    }
                } catch (e: Exception) {
                    BillingFetchResult(success = false, errorMessage = e.message ?: "Ошибка сети Selectel")
                }
            }

            BillingType.BEGET -> {
                try {
                    var balance: Double? = null
                    val isLoginPass = cleanKey.contains(":")

                    if (isLoginPass) {
                        val parts = cleanKey.split(":", limit = 2)
                        val login = parts[0].trim()
                        val pass = parts[1].trim()
                        val url = "https://api.beget.com/api/user/getAccountInfo?login=${login}&passwd=${pass}&output_format=json"
                        val req = Request.Builder().url(url).build()
                        httpClient.newCall(req).execute().use { resp ->
                            val body = resp.body?.string() ?: ""
                            val json = JSONObject(body)
                            if (json.optString("status") == "success") {
                                val answer = json.optJSONObject("answer")
                                val result = answer?.optJSONObject("result")
                                val balStr = result?.optString("user_balance", "0")
                                balance = balStr?.toDoubleOrNull()
                            } else {
                                val err = json.optString("error_text", "Ошибка авторизации Beget")
                                return@withContext BillingFetchResult(success = false, errorMessage = err)
                            }
                        }
                    } else {
                        // Cloud API v1 Bearer token or fallback
                        val url = "https://api.beget.com/v1/cloud/user/info"
                        val req = Request.Builder()
                            .url(url)
                            .addHeader("Authorization", "Bearer $cleanKey")
                            .build()
                        httpClient.newCall(req).execute().use { resp ->
                            val body = resp.body?.string() ?: ""
                            if (resp.isSuccessful && body.isNotBlank()) {
                                val json = JSONObject(body)
                                balance = if (json.has("balance")) json.optDouble("balance") else json.optDouble("user_balance", 0.0)
                            } else {
                                val url2 = "https://api.beget.com/api/user/getAccountInfo?login=${cleanKey}&output_format=json"
                                val req2 = Request.Builder().url(url2).build()
                                httpClient.newCall(req2).execute().use { resp2 ->
                                    val body2 = resp2.body?.string() ?: ""
                                    val json2 = JSONObject(body2)
                                    if (json2.optString("status") == "success") {
                                        val answer = json2.optJSONObject("answer")
                                        val result = answer?.optJSONObject("result")
                                        balance = result?.optString("user_balance", "0")?.toDoubleOrNull()
                                    } else {
                                        return@withContext BillingFetchResult(
                                            success = false,
                                            errorMessage = "Beget API: HTTP ${resp.code}. Укажите ключ в формате login:password"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val finalBal = balance
                    if (finalBal == null) {
                        return@withContext BillingFetchResult(success = false, errorMessage = "Не удалось узнать баланс Beget")
                    }

                    val daysLeft = if (monthlyCost > 0.0) {
                        val dailyCost = monthlyCost / 30.0
                        if (dailyCost > 0.0) (finalBal / dailyCost).toInt() else null
                    } else null

                    BillingFetchResult(
                        success = true,
                        balance = finalBal,
                        currency = "RUB",
                        daysLeft = daysLeft
                    )
                } catch (e: Exception) {
                    BillingFetchResult(success = false, errorMessage = e.message ?: "Ошибка сети Beget")
                }
            }

            else -> {
                BillingFetchResult(success = false, errorMessage = "API не поддерживается для данного типа")
            }
        }
    }

    suspend fun fetchBillingData(server: ServerEntity): BillingFetchResult {
        val bType = try {
            BillingType.valueOf(server.billingType)
        } catch (_: Exception) {
            BillingType.NONE
        }
        return testBillingApi(bType, server.billingApiKey, server.billingCostMonthly)
    }
}
