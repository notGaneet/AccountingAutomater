import java.time.LocalDate

/** The kind of account decides which side (debit or credit) makes it go up. */
enum class AccountType(val normalSideIsDebit: Boolean) {
    ASSET(true),
    LIABILITY(false),
    EQUITY(false),
    DRAWINGS(true),
    REVENUE(false),
    EXPENSE(true)
}

data class Account(val name: String, val type: AccountType)

/**
 * One journal entry: debit one account, credit another, same amount.
 * Money is stored in cents (Long) so there are never rounding errors.
 */
data class Transaction(
    val date: LocalDate,
    val description: String,
    val debit: Account,
    val credit: Account,
    val cents: Long
)

/** One line inside an account's ledger page. */
data class Posting(
    val date: LocalDate,
    val description: String,
    val debitCents: Long,
    val creditCents: Long
)

/** Turns "1,250.50" or "$1250.5" into 125050 cents. Returns null if it is not a valid amount. */
fun parseCents(text: String): Long? {
    val cleaned = text.trim().replace("$", "").replace(",", "")
    if (!Regex("""\d+(\.\d{1,2})?""").matches(cleaned)) return null
    val parts = cleaned.split(".")
    val dollars = parts[0].toLong()
    val cents = if (parts.size == 2) parts[1].padEnd(2, '0').toLong() else 0L
    return dollars * 100 + cents
}

/** Turns 125050 cents into "1,250.50". Negative amounts are shown in brackets, like accountants do. */
fun money(cents: Long): String {
    val abs = kotlin.math.abs(cents)
    val text = "%,d.%02d".format(abs / 100, abs % 100)
    return if (cents < 0) "($text)" else text
}
