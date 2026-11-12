import java.io.File
import kotlin.system.exitProcess

/**
 * Accounting Automator
 *
 * Reads a chart of accounts and a list of transactions, then does the whole
 * accounting cycle: journal -> ledger -> trial balance -> income statement -> balance sheet.
 *
 * Usage:
 *   java -jar accounting.jar [accounts.csv] [transactions.csv] [output.txt]
 *
 * With no arguments it uses the sample files in the data folder.
 */
fun main(args: Array<String>) {
    val accountsPath = args.getOrElse(0) { "data/accounts.csv" }
    val transactionsPath = args.getOrElse(1) { "data/transactions.csv" }
    val outputPath = args.getOrNull(2)

    try {
        val accounts = loadAccounts(accountsPath)
        val transactions = loadTransactions(transactionsPath, accounts)

        val ledger = Ledger(accounts.values)
        for (t in transactions) ledger.post(t)

        val report = journalReport(transactions) +
            ledgerReport(ledger) +
            trialBalanceReport(ledger) +
            incomeStatementReport(ledger) +
            balanceSheetReport(ledger)

        println(report)
        if (outputPath != null) {
            File(outputPath).writeText(report)
            println("Saved a copy to $outputPath")
        }
    } catch (e: InputError) {
        System.err.println("Problem with the input: ${e.message}")
        exitProcess(1)
    }
}
