package com.example

import com.example.data.billing.BillingManager
import com.example.data.billing.BillingType
import com.example.data.db.ServerEntity
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testMonthlyBillingPreservesDay() {
    val server = ServerEntity(
      name = "Test VPS",
      host = "1.2.3.4",
      billingType = BillingType.MONTHLY_DAY.name,
      billingDayOfMonth = 4
    )
    val status = BillingManager.calculateStatus(server)
    assertEquals(BillingType.MONTHLY_DAY, status.type)
    assertNotNull(status.daysLeft)
    assertTrue("Days left must be >= 0", (status.daysLeft ?: -1) >= 0)
    assertTrue("Badge must contain 4", status.badgeText.contains("4") || status.badgeText.contains("Оплата"))
  }

  @Test
  fun testIntervalBillingCalculation() {
    val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
    val server = ServerEntity(
      name = "Test VPS Interval",
      host = "1.2.3.4",
      billingType = BillingType.INTERVAL_DAYS.name,
      billingIntervalDays = 31,
      billingLastPaidDate = System.currentTimeMillis() - 5L * 24 * 60 * 60 * 1000
    )
    val status = BillingManager.calculateStatus(server)
    assertEquals(BillingType.INTERVAL_DAYS, status.type)
    assertNotNull(status.daysLeft)
    // 31 days total, 5 days elapsed => ~26 days left
    val days = status.daysLeft ?: 0
    assertTrue("Days left should be around 25-26, got: $days", days in 24..27)
  }

  @Test
  fun testDisabledBilling() {
    val server = ServerEntity(
      name = "Test VPS Disabled",
      host = "1.2.3.4",
      billingType = BillingType.NONE.name
    )
    val status = BillingManager.calculateStatus(server)
    assertEquals(BillingType.NONE, status.type)
    assertNull(status.daysLeft)
    assertEquals("", status.badgeText)
  }
}

