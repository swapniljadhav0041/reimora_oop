# Refactoring Audit: Original Reimora to Reimora-Terminal

## Audit approach

The original Java project was inspected before creating this separate clean copy. It contained 23 production Java files under `src/reimora`, a test file, generated binary state, notification folders, compiled output, sample imports, Git data, and documentation for an advanced multi-user workflow.

The final application keeps the useful rules for IDs, money validation, local persistence, a clear status lifecycle, student details, payment references, terminal input validation, and printable summaries. It removes features that only existed for the earlier multi-user reimbursement platform.

The original folder was not modified. This project is a new `Reimora-Terminal` folder.

## Original class audit and final decision

| Original file / class | Important methods, purpose, and original caller | Final decision |
|---|---|---|
| `Main.java` | `main` built `FileRepository`, `Security`, documents, reports, worker, then called `ConsoleApp.run`. | Rewritten as a direct `Scanner` menu in the new `Main.java`. |
| `ConsoleApp.java` | `run`, `signedIn`, `pendingMenu`, `manageClaim`, and `approve` drove login menus, role menus, claims, advances, evidence, and exports. It called `ReimoraService`, `Reports`, `DocumentStorage`, and `NotificationWorker`. | Removed. One operator menu replaces all role-specific screens. |
| `Models.java` | Nested `User`, `AccessRequest`, `ExpenseItem`, `Claim`, `Advance`, and `Notification` classes held data. `Claim` calculated totals, settlement, payments, and revisions. These were called throughout `ReimoraService`, `ConsoleApp`, and `Reports`. | Split and simplified into `Student`, `Reimbursement`, and `Payment`. Multiple expenses, advances, revisions, notifications, and role data removed. |
| `Types.java` | Defined `Role`, `Branch`, request, claim, category, payment, and mail enums used by `Models`, `ConsoleApp`, `ReimoraService`, and `Reports`. | Removed. Only `Reimbursement.Status` is needed for the simple lifecycle. Branch is a direct student field. |
| `ReimoraService.java` | `setupAdmin`, `login`, `register`, `decideAccess`, `saveBeneficiary`, `createClaim`, `addExpense`, `submit`, `approve`, `issueAdvance`, `recordPayment`, and notification methods enforced the advanced workflow. `ConsoleApp` and `Reports` called it. | Replaced by focused `StudentManager` and `ReimbursementManager`. Simple validation and status rules are retained. |
| `Repository.java` | Declared `snapshot` and `update` operations for mutable `State`; implemented by `FileRepository` and used by `ReimoraService` and `NotificationWorker`. | Removed. The small project uses direct file storage without a repository abstraction. |
| `FileRepository.java` | Encoded/decoded serialised `State`, copied state, wrote backup files, used file locks, and implemented repository updates. Called by `Main` and through `ReimoraService`. | Removed. Replaced by readable UTF-8 CSV read/write in `FileStorage`. |
| `State.java` | Held maps for users, claims, advances, notifications, and global ID counters. It was the serialised payload read and written by `FileRepository`. | Removed. Data is stored in `students.csv` and `reimbursements.csv`; no `state.bin` is used. |
| `Security.java` | `hashPassword`, `matches`, `encrypt`, and `sha256` supported login, encrypted beneficiary accounts, and uploaded-file hashes. Called by `ReimoraService` and `DocumentStorage`. | Removed because there is no login or authenticated multi-user system. Account numbers are masked in terminal output and only fictional data is documented. |
| `Terminal.java` | `line`, `password`, `choice`, `pick`, `date`, and `path` converted console input for `ConsoleApp`. | Simplified into direct `Scanner` helpers inside `Main` because passwords and deep menus are removed. |
| `DocumentStorage.java` | `importFile`, `path`, and `discardNewCopy` copied and validated PDF/image/text attachments. Called by `ConsoleApp`. | Removed; the new project has no uploads or evidence files. |
| `ExpenseEvidence.java` | Abstract base exposed IDs, file metadata, `kind`, and `warnings`; stored in each original `ExpenseItem`. | Removed; no attachment hierarchy is needed. |
| `ReceiptEvidence.java` | Implemented receipt type and invoice-reference warning; constructed by `DocumentStorage`. | Removed with attachments. |
| `UpiScreenshotEvidence.java` | Implemented screenshot type and UPI success/reference warnings; constructed by `DocumentStorage`. | Removed with attachments. |
| `ReceiptParser.java` | Declared `extract(Path)` for receipt import. `TextReceiptParser` implemented it; `ConsoleApp` used it while importing expenses. | Removed; manual fields are simpler and required. |
| `TextReceiptParser.java` | `extract` read key-value text receipts and `suggest` guessed a category. Called only from `ConsoleApp`. | Removed; there is no receipt import or AI/OCR. |
| `NotificationDelivery.java` | Declared `deliver(Notification, User)`. `LocalMailboxDelivery` implemented it and `NotificationWorker` called it. | Removed; no email or notifications. |
| `LocalMailboxDelivery.java` | `deliver` wrote local mailbox text files. Called by `NotificationWorker`, never by an external email service. | Removed; no local email simulation. |
| `NotificationWorker.java` | `start`, `run`, and `close` scheduled notification retries. Built in `Main`, shown and manually triggered in `ConsoleApp`. | Removed; no background tasks or concurrency are required. |
| `Reports.java` | `claimText`, `exportClaim`, `exportClaims`, and `dashboard` formatted authorised claims and exports. Called by `ConsoleApp`. | Simplified into `Main` detail, history, and text receipt display methods. |
| `ReportExporter.java` | Declared the CSV export method implemented by `Reports`. | Removed; no reporting abstraction is needed. |
| `Money.java` | `parse`, `positive`, and `format` validated and displayed INR amounts. Called by `ReimoraService`, `ConsoleApp`, and `Reports`. | Its useful rule is retained as simple `BigDecimal` validation and formatting methods in `Main` and the reimbursement model. |
| `DomainException.java` | Custom runtime exception used by all service, storage, parser, and terminal validation paths. | Removed. Standard `IllegalArgumentException` and `IllegalStateException` are enough for this small application. |

## Preserved useful behaviour

- `BigDecimal` with positive values and at most two decimal places for money.
- Automatically generated identifiers, now `STU001` and `REM001`.
- Student records and payment reference data remain after restart.
- Clear business validation: payment needs an approved request, exact amount, and a non-empty UTR.
- A text summary based on stored records, now presented as a simple terminal receipt.
- Friendly terminal messages for invalid menu choices, missing records, duplicate PRNs, invalid amounts, dates, and saved rows.

## Deliberately excluded from the final package

- `.git`, compiled `.class` files, generated build output, old `data/state.bin`, backups, locks, mailbox files, stored uploads, original sample receipts, original source classes, and old advanced-feature documentation.
- Existing hard-coded demo accounts, login credentials, passwords, and any generated user data.

The final source folder contains only the seven classes required to demonstrate the simplified one-operator workflow.
