# Accounting Automator

A Kotlin command-line program that does the accounting cycle for you. You give it a list of
transactions, and it produces the general journal, general ledger, trial balance, income
statement and balance sheet.

I first wrote a version of this in high school to get through my accounting homework faster.
The original was lost with my school account, so this is a rebuild.

## What it does

1. Reads a chart of accounts and a list of transactions from CSV files.
2. Checks every transaction: the date, the amount, and that both accounts exist.
3. Posts each transaction to the ledger (debit one account, credit the other).
4. Prints five reports:
   - **General Journal** – every entry in date order
   - **General Ledger** – each account with a running balance
   - **Trial Balance** – proves total debits equal total credits
   - **Income Statement** – revenue minus expenses
   - **Balance Sheet** – assets = liabilities + owner's equity

Money is stored as whole cents, so totals never drift from rounding errors.

## How to run it

You need Java 8 or newer and the [Kotlin compiler](https://kotlinlang.org/docs/command-line.html).

```
kotlinc src/*.kt -include-runtime -d accounting.jar
java -jar accounting.jar
```

With no arguments it uses the sample files in `data/`. To use your own files and save the
report:

```
java -jar accounting.jar my-accounts.csv my-transactions.csv report.txt
```

## Input files

**Chart of accounts** (`data/accounts.csv`). The type is one of `asset`, `liability`,
`equity`, `drawings`, `revenue` or `expense`.

```
name,type
Cash,asset
Accounts Payable,liability
Service Revenue,revenue
```

**Transactions** (`data/transactions.csv`). Dates are `YYYY-MM-DD`. Put quotes around a
value that contains a comma.

```
date,description,debit,credit,amount
2024-03-01,Owner invested cash to start the business,Cash,"J. Rivera, Capital",15000.00
2024-03-02,Paid rent for March,Rent Expense,Cash,1200.00
```

If something is wrong, the program tells you the file and line number:

```
Problem with the input: data/transactions.csv line 7: account 'Wage Expense' is not in the chart of accounts
```

## How the code is organized

| File | What is in it |
| --- | --- |
| `src/Models.kt` | Account types, the `Account` and `Transaction` classes, money formatting |
| `src/Loader.kt` | Reading and validating the CSV files |
| `src/Ledger.kt` | Posting transactions and calculating balances |
| `src/Reports.kt` | Building the five reports |
| `src/Main.kt` | Ties it all together |

## Ideas for later

- Entries with more than one debit or credit (compound entries)
- Adjusting and closing entries
- Exporting the reports as CSV
