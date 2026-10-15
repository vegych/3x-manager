package com.example.data.billing

enum class BillingType(val title: String, val description: String) {
    NONE("Отключено", "Без отслеживания оплаты"),
    MONTHLY_DAY("Число месяца", "Оплата в конкретное число каждого месяца (напр. 4-го числа)"),
    INTERVAL_DAYS("Интервал (дней)", "Оплата каждые N дней (напр. каждые 30 или 31 день)"),
    TIMEWEB_CLOUD("Timeweb Cloud API", "Авто-получение баланса и времени работы по токену Timeweb"),
    SELECTEL("Selectel API", "Авто-получение баланса по API-ключу Selectel"),
    BEGET("Beget API", "Авто-получение баланса по API-ключу или логину/паролю Beget")
}

data class BillingStatus(
    val type: BillingType,
    val daysLeft: Int?,
    val badgeText: String,
    val detailsText: String,
    val isUrgent: Boolean,     // <= 3 дней
    val isExpired: Boolean,    // < 0 дней
    val balance: Double? = null,
    val currency: String = "RUB",
    val nextPaymentDateStr: String? = null
)
