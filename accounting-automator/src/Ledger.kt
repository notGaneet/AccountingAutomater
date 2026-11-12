/**
 * The general ledger: every account gets its own list of postings.
 * Posting a transaction puts the amount on the debit side of one account
 * and the credit side of another, which is what you do by hand with T-accounts.
 */
class Ledger(accounts: Collection<Account>) {

    private val pages = linkedMapOf<Account, MutableList<Posting>>()

    init {
        for (account in accounts) pages[account] = mutableListOf()
    }

    fun post(t: Transaction) {
        pages.getValue(t.debit).add(Posting(t.date, t.description, t.cents, 0))
        pages.getValue(t.credit).add(Posting(t.date, t.description, 0, t.cents))
    }

    fun accounts(): List<Account> = pages.keys.toList()

    fun postings(account: Account): List<Posting> = pages.getValue(account)

    /**
     * Balance on the account's normal side.
     * Example: Cash (an asset) with 500 of debits and 200 of credits has a balance of 300.
     * A negative number means the account is on its "wrong" side.
     */
    fun balance(account: Account): Long {
        val debits = postings(account).sumOf { it.debitCents }
        val credits = postings(account).sumOf { it.creditCents }
        return if (account.type.normalSideIsDebit) debits - credits else credits - debits
    }

    /** Adds up the balances of every account of one type. */
    fun total(type: AccountType): Long =
        accounts().filter { it.type == type }.sumOf { balance(it) }

    fun netIncome(): Long = total(AccountType.REVENUE) - total(AccountType.EXPENSE)

    /** Owner's equity at the end of the period: capital + net income - drawings. */
    fun endingEquity(): Long =
        total(AccountType.EQUITY) + netIncome() - total(AccountType.DRAWINGS)
}
