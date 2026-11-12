private const val WIDTH = 78

private fun title(text: String): String {
    val line = "=".repeat(WIDTH)
    return "\n$line\n${text.uppercase()}\n$line\n"
}

private fun rule() = "-".repeat(WIDTH) + "\n"

/** The general journal: every transaction in date order, debit line first, credit line indented. */
fun journalReport(transactions: List<Transaction>): String {
    val out = StringBuilder(title("General Journal"))
    out.append("%-12s%-38s%14s%14s\n".format("Date", "Account / Explanation", "Debit", "Credit"))
    out.append(rule())
    for (t in transactions) {
        out.append("%-12s%-38s%14s\n".format(t.date, t.debit.name, money(t.cents)))
        out.append("%-12s%-38s%14s%14s\n".format("", "    " + t.credit.name, "", money(t.cents)))
        out.append("%-12s%s\n\n".format("", "  (" + t.description + ")"))
    }
    return out.toString()
}

/** The general ledger: one section per account, with a running balance after every posting. */
fun ledgerReport(ledger: Ledger): String {
    val out = StringBuilder(title("General Ledger"))
    for (account in ledger.accounts()) {
        val postings = ledger.postings(account)
        if (postings.isEmpty()) continue
        out.append("\n${account.name}  [${account.type.name.lowercase()}]\n")
        out.append("%-12s%-26s%13s%13s%14s\n".format("Date", "Explanation", "Debit", "Credit", "Balance"))
        out.append(rule())
        var running = 0L
        for (p in postings) {
            val change = p.debitCents - p.creditCents
            running += if (account.type.normalSideIsDebit) change else -change
            out.append(
                "%-12s%-26s%13s%13s%14s\n".format(
                    p.date,
                    p.description.take(25),
                    if (p.debitCents > 0) money(p.debitCents) else "",
                    if (p.creditCents > 0) money(p.creditCents) else "",
                    money(running)
                )
            )
        }
    }
    return out.toString()
}

/**
 * The trial balance: every account's balance in the debit or credit column.
 * If the two columns do not add up to the same total, something was posted wrong.
 */
fun trialBalanceReport(ledger: Ledger): String {
    val out = StringBuilder(title("Trial Balance"))
    out.append("%-50s%14s%14s\n".format("Account", "Debit", "Credit"))
    out.append(rule())
    var debitTotal = 0L
    var creditTotal = 0L
    for (account in ledger.accounts()) {
        val balance = ledger.balance(account)
        if (balance == 0L) continue
        // A negative balance sits on the opposite side from normal.
        val onDebitSide = account.type.normalSideIsDebit == (balance > 0)
        val amount = kotlin.math.abs(balance)
        if (onDebitSide) {
            debitTotal += amount
            out.append("%-50s%14s\n".format(account.name, money(amount)))
        } else {
            creditTotal += amount
            out.append("%-50s%14s%14s\n".format(account.name, "", money(amount)))
        }
    }
    out.append(rule())
    out.append("%-50s%14s%14s\n".format("Totals", money(debitTotal), money(creditTotal)))
    out.append(if (debitTotal == creditTotal) "\nThe books balance.\n" else "\nWARNING: the books do NOT balance.\n")
    return out.toString()
}

/** The income statement: revenue minus expenses for the period. */
fun incomeStatementReport(ledger: Ledger): String {
    val out = StringBuilder(title("Income Statement"))
    out.append("Revenue\n")
    for (a in ledger.accounts().filter { it.type == AccountType.REVENUE }) {
        out.append("    %-46s%14s\n".format(a.name, money(ledger.balance(a))))
    }
    out.append("    %-46s%14s%14s\n".format("Total revenue", "", money(ledger.total(AccountType.REVENUE))))
    out.append("\nExpenses\n")
    for (a in ledger.accounts().filter { it.type == AccountType.EXPENSE }) {
        if (ledger.balance(a) == 0L) continue
        out.append("    %-46s%14s\n".format(a.name, money(ledger.balance(a))))
    }
    out.append("    %-46s%14s%14s\n".format("Total expenses", "", money(ledger.total(AccountType.EXPENSE))))
    out.append(rule())
    val net = ledger.netIncome()
    out.append("%-50s%14s%14s\n".format(if (net >= 0) "Net income" else "Net loss", "", money(net)))
    return out.toString()
}

/** The balance sheet: assets on one side, liabilities plus owner's equity on the other. */
fun balanceSheetReport(ledger: Ledger): String {
    val out = StringBuilder(title("Balance Sheet"))
    out.append("Assets\n")
    for (a in ledger.accounts().filter { it.type == AccountType.ASSET }) {
        out.append("    %-46s%14s\n".format(a.name, money(ledger.balance(a))))
    }
    val assets = ledger.total(AccountType.ASSET)
    out.append("    %-46s%14s%14s\n".format("Total assets", "", money(assets)))

    out.append("\nLiabilities\n")
    for (a in ledger.accounts().filter { it.type == AccountType.LIABILITY }) {
        out.append("    %-46s%14s\n".format(a.name, money(ledger.balance(a))))
    }
    val liabilities = ledger.total(AccountType.LIABILITY)
    out.append("    %-46s%14s%14s\n".format("Total liabilities", "", money(liabilities)))

    out.append("\nOwner's Equity\n")
    out.append("    %-46s%14s\n".format("Capital, beginning", money(ledger.total(AccountType.EQUITY))))
    out.append("    %-46s%14s\n".format("Add: net income", money(ledger.netIncome())))
    out.append("    %-46s%14s\n".format("Less: drawings", money(ledger.total(AccountType.DRAWINGS))))
    val equity = ledger.endingEquity()
    out.append("    %-46s%14s%14s\n".format("Capital, ending", "", money(equity)))
    out.append(rule())
    out.append("%-50s%14s%14s\n".format("Total liabilities and owner's equity", "", money(liabilities + equity)))
    out.append(
        if (assets == liabilities + equity) "\nAssets = Liabilities + Owner's Equity. It balances.\n"
        else "\nWARNING: the balance sheet does NOT balance.\n"
    )
    return out.toString()
}
