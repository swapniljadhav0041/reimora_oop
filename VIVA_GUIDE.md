# Reimora Viva Guide

## 1. What the project does

Reimora records student reimbursement requests in a terminal. One operator performs every action. The operator adds a student, creates a request for one expense, approves or rejects it, records a manual payment with a UTR number, and checks its history or text receipt.

The problem it solves is keeping a small, clear local record of reimbursement requests and their payment status. A request is not treated as paid when it is approved: payment is a separate step.

## 2. Folder structure

```text
Reimora-Terminal/
├── src/
│   ├── Main.java
│   ├── Student.java
│   ├── StudentManager.java
│   ├── Reimbursement.java
│   ├── ReimbursementManager.java
│   ├── Payment.java
│   └── FileStorage.java
├── data/                 (created or filled at runtime)
│   ├── students.csv
│   └── reimbursements.csv
├── run.bat
├── README.md
├── VIVA_GUIDE.md
└── AUDIT_SUMMARY.md
```

`build/` is generated when `run.bat` compiles the program. It is not part of the source project.

## 3. Java files and their responsibilities

| File / class | Important fields and methods | Simple explanation |
|---|---|---|
| `Main.java` / `Main` | `StudentManager`, `ReimbursementManager`, `run`, `addStudent`, `createReimbursementRequest`, `recordPayment`, `buildTextReceipt` | Starts the program, shows the numbered menu, accepts `Scanner` input, and coordinates the other classes. |
| `Student.java` / `Student` | private ID, name, PRN, bank fields; constructor; getters; `getMaskedAccountNumber` | Represents one student. Private fields demonstrate encapsulation. The account number is masked in terminal output. |
| `StudentManager.java` / `StudentManager` | `ArrayList<Student>`; `addStudent`, `findById`, `findByPrn`, `search` | Manages students, checks duplicate PRNs, generates IDs such as `STU001`, and saves changes. |
| `Reimbursement.java` / `Reimbursement` | private request fields; `Status`; `approve`, `reject`, `recordPayment` | Represents one reimbursement for one expense. It owns an optional `Payment` object. |
| `ReimbursementManager.java` / `ReimbursementManager` | `ArrayList<Reimbursement>`; `createRequest`, `approve`, `reject`, `recordPayment`, `getHistoryForStudent` | Applies the reimbursement workflow, creates IDs such as `REM001`, and saves changes. |
| `Payment.java` / `Payment` | amount, date, UTR; constructor; getters | Stores the manual payment amount, date, and UTR number. |
| `FileStorage.java` / `FileStorage` | `loadStudents`, `saveStudents`, `loadReimbursements`, `saveReimbursements` | Reads and writes the two local UTF-8 CSV files using Java file APIs. |

## 4. Execution flow

```text
Main → StudentManager → Student → FileStorage → students.csv

Main → ReimbursementManager → Reimbursement → Payment → FileStorage → reimbursements.csv
```

1. `Main` reads a menu choice using `Scanner`.
2. For a student action, `Main` calls `StudentManager`. The manager creates a `Student` object and asks `FileStorage` to save the complete list.
3. For a reimbursement action, `Main` checks the student, then calls `ReimbursementManager`. The manager creates or updates a `Reimbursement` and saves the complete list.
4. `Reimbursement` creates a `Payment` object only when the approved amount and non-empty UTR are valid.

## 5. Reimbursement lifecycle

```text
PENDING_APPROVAL ──approve──> APPROVED ──record full payment with UTR──> PAID
        │
        └──reject──> REJECTED
```

Only a pending request can be approved or rejected. Only an approved request can receive payment. A rejected request cannot be paid. Payment amount must exactly equal the requested/approved amount.

## 6. How records are saved

At startup, `FileStorage` creates `data/students.csv` and `data/reimbursements.csv` when they do not exist. Both files are readable UTF-8 CSV files. A save happens immediately after adding a student, creating a request, deciding a request, or recording a payment. This is why data is still available after restarting.

CSV values are quoted. If a user enters a comma in a description, it stays in that one field instead of shifting the columns. If a saved row cannot be read, the application prints a warning and skips only that row.

## 7. Text receipt and UTR

`Main.buildTextReceipt` reads the latest `Reimbursement`, its `Student`, and its optional `Payment`. It displays an ID such as `REC001`, the expense details, approval status, payment status, and UTR. If no payment exists, it correctly displays `NOT_PAID` and `Not recorded` for the UTR.

The UTR is manually entered and stored in the payment record. It is evidence typed by the operator for demonstration; the program does not connect to any bank or verify a transaction.

## 8. OOP concepts demonstrated

- **Classes and objects:** `Student`, `Reimbursement`, and `Payment` are real domain objects.
- **Constructors:** each object receives its required data when it is created.
- **Encapsulation:** data fields are `private`; callers use getters and business methods such as `approve` and `recordPayment`.
- **Composition:** a `Reimbursement` contains a `Payment` only after successful payment recording.
- **Collections:** the two manager classes use `ArrayList` to keep records in memory.
- **Methods:** managers contain actions related to their responsibility, while models contain their own state-changing business operations.
- **Exception handling:** invalid input and file errors are caught or reported as clear terminal messages.
- **File handling:** `FileStorage` uses `Files.readAllLines` and `Files.write` with UTF-8.

## 9. Short viva answers

**Why is `Payment` a separate class?** It groups payment amount, date, and UTR together, and shows composition because one reimbursement can contain one payment.

**Why use `BigDecimal` instead of `double`?** Money should not use floating-point rounding. `BigDecimal` compares the payment amount exactly with the reimbursement amount.

**Why are IDs generated?** The managers create unique, easy-to-read IDs such as `STU001` and `REM001`, so the operator can select records reliably.

**Why use CSV?** It is easy to read, inspect, and explain in a viva. It also avoids databases and external dependencies for a small local demonstration.

**What is the limitation?** Payment entry is manual. The project does not send money, verify UTRs with a bank, upload receipts, or perform AI image processing.
