import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Thrown when an input file has a problem, with the file name and line number in the message. */
class InputError(message: String) : Exception(message)

/** Splits one CSV line on commas, but keeps commas that are inside "quotes". */
fun splitCsv(line: String): List<String> {
    val cells = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    for (ch in line) {
        when {
            ch == '"' -> inQuotes = !inQuotes
            ch == ',' && !inQuotes -> {
                cells.add(current.toString().trim())
                current.clear()
            }
            else -> current.append(ch)
        }
    }
    cells.add(current.toString().trim())
    return cells
}

/** Reads a CSV file and returns (line number, cells) for every row except the header and blank lines. */
private fun readRows(path: String): List<Pair<Int, List<String>>> {
    val file = File(path)
    if (!file.exists()) throw InputError("Cannot find file: $path")
    return file.readLines()
        .mapIndexed { index, line -> (index + 1) to line }
        .drop(1)
        .filter { it.second.isNotBlank() }
        .map { it.first to splitCsv(it.second) }
}

/** Loads the chart of accounts. Keys are lowercase so "cash" and "Cash" match. */
fun loadAccounts(path: String): Map<String, Account> {
    val accounts = linkedMapOf<String, Account>()
    for ((lineNo, cells) in readRows(path)) {
        if (cells.size != 2) throw InputError("$path line $lineNo: expected 2 columns (name,type)")
        val type = AccountType.entries.find { it.name.equals(cells[1], ignoreCase = true) }
            ?: throw InputError("$path line $lineNo: unknown account type '${cells[1]}'")
        val key = cells[0].lowercase()
        if (key in accounts) throw InputError("$path line $lineNo: account '${cells[0]}' is listed twice")
        accounts[key] = Account(cells[0], type)
    }
    if (accounts.isEmpty()) throw InputError("$path has no accounts in it")
    return accounts
}

/** Loads the transactions and checks every one against the chart of accounts. */
fun loadTransactions(path: String, accounts: Map<String, Account>): List<Transaction> {
    val transactions = mutableListOf<Transaction>()
    for ((lineNo, cells) in readRows(path)) {
        val where = "$path line $lineNo"
        if (cells.size != 5) {
            throw InputError("$where: expected 5 columns (date,description,debit,credit,amount)")
        }
        val date = try {
            LocalDate.parse(cells[0])
        } catch (e: DateTimeParseException) {
            throw InputError("$where: date '${cells[0]}' should look like 2024-03-15")
        }
        val debit = accounts[cells[2].lowercase()]
            ?: throw InputError("$where: account '${cells[2]}' is not in the chart of accounts")
        val credit = accounts[cells[3].lowercase()]
            ?: throw InputError("$where: account '${cells[3]}' is not in the chart of accounts")
        if (debit == credit) throw InputError("$where: debit and credit cannot be the same account")
        val cents = parseCents(cells[4])
            ?: throw InputError("$where: '${cells[4]}' is not a valid amount")
        if (cents == 0L) throw InputError("$where: amount cannot be zero")
        transactions.add(Transaction(date, cells[1], debit, credit, cents))
    }
    return transactions.sortedBy { it.date }
}
