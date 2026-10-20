# Reimora — Student Reimbursement System

Reimora is a simple Java terminal application for recording student reimbursement requests. It is designed for an Object-Oriented Programming Laboratory mini-project and is operated by one administrator/operator from the terminal.

The operator can add students, store fictional bank details, create one-expense reimbursement requests, approve or reject requests, record a full payment with a UTR number, inspect histories, and display a text receipt.

## Important scope

- This is an educational local demonstration application.
- Use fictional bank details and fictional UTR values only.
- A recorded payment is a manual record. The application does not transfer money, contact a bank, or independently verify a UTR.
- Receipt images, PDFs, screenshots, email, AI/OCR, login, roles, cloud services, and payment gateways are intentionally not included.

## Requirements

- Windows
- JDK 17 or newer, with `javac` available on the PATH
- No internet connection, API key, Maven, Gradle, or external library is needed

## Run the application

1. Open the `Reimora-Terminal` folder.
2. Double-click `run.bat`, or open Command Prompt in the folder and run:

   ```bat
   run.bat
   ```

3. The program compiles the Java files into a generated `build` folder and opens the numbered menu.
4. Select `0` when you want to exit. Saved records remain available the next time the program is started.

## Main menu workflow

1. Select **Add New Student**. Enter a name, PRN, branch, year/division, and fictional bank details. The program generates an ID such as `STU001`.
2. Select **Create Reimbursement Request**. Choose an existing student and enter one expense. The program generates an ID such as `REM001` and sets its status to `PENDING_APPROVAL`.
3. Select **Approve or Reject Reimbursement**. Approved requests move to `APPROVED`. Rejected requests move to `REJECTED` and cannot be paid.
4. Select **Record Payment** only after approval. Enter the exact approved amount, payment date, and a non-empty UTR. The request then becomes `PAID`.
5. Use **Check Status**, **View Student Reimbursement History**, and **Generate or Display Text Receipt** to demonstrate the saved data.

## Data files

When the program first starts, it creates these UTF-8 CSV files in the `data` folder:

- `students.csv` — student and fictional bank information
- `reimbursements.csv` — reimbursement, decision, payment, and UTR information

The CSV writer wraps fields in quotes and doubles embedded quotation marks, so commas entered in normal fields do not break the file structure. Invalid saved rows are skipped with a warning instead of crashing the whole program.

Do not use this project to store real financial information. The account number is masked whenever it is shown in the terminal, but it is stored locally in the CSV file so the demonstration record can be retained.

## Manual test checklist

- [ ] Start the program: no login is requested.
- [ ] Add a fictional student and confirm that an automatic `STU` ID appears.
- [ ] List and search the student by ID and PRN.
- [ ] Create a request and confirm `PENDING_APPROVAL`.
- [ ] Approve a request and confirm `APPROVED`.
- [ ] Try to pay with a blank UTR: the payment must not be saved.
- [ ] Record a full payment with a valid UTR and confirm `PAID`.
- [ ] Display status, history, and the text receipt.
- [ ] Restart the application and confirm the saved records remain.
- [ ] Reject another request and confirm it cannot be paid.

Read [VIVA_GUIDE.md](VIVA_GUIDE.md) for the code explanation, [AUDIT_SUMMARY.md](AUDIT_SUMMARY.md) for the refactoring audit, and [TEST_RESULTS.md](TEST_RESULTS.md) for executed verification.
