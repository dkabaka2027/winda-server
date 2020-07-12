package co.winda.android

import co.winda.models.Subscription

case class Payment(description: String, amount: BigDecimal, currency: String, reference: String, firstName: String,
                   lastName: String, email: String, telephone: String, subscription: Subscription)
