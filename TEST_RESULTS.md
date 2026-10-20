# Test Results

Test date: 21 October 2026

## Compilation

Executed successfully with JDK 26 using the Java 17 language target:

```text
javac --release 17 -encoding UTF-8 -d <temporary-test-build> src/*.java
```

All seven source files compiled without errors or external dependencies.

## Scripted terminal workflow tests

The following real terminal runs were performed against a separate temporary test workspace. The final project folder was not used for test data.

| Check | Result |
|---|---|
| Program starts directly at menu with no login | Passed |
| Add a fictional student and create `STU001` | Passed |
| List all students and search by student ID | Passed |
| Create `REM001` with status `PENDING_APPROVAL` | Passed |
| Approve `REM001`, then record its full INR 650.00 payment | Passed |
| Blank UTR is rejected before payment can be saved | Passed |
| Valid UTR changes payment status to `PAID` | Passed |
| Status displays approval as `APPROVED` and payment as `PAID` separately | Passed |
| Text receipt displays live student, reimbursement, amount, status, and UTR data | Passed |
| Student reimbursement history displays the completed payment | Passed |
| Restart application and read previously saved `STU001` | Passed |
| Invalid menu value is handled without crashing | Passed |
| Duplicate PRN is rejected | Passed |
| Invalid expense date and negative amount are rejected and re-prompted | Passed |
| Rejected `REM002` cannot be paid | Passed |
| Nonexistent reimbursement ID is handled with a clear message | Passed |

## Limits of the test

No real bank accounts, money transfers, emails, receipts, screenshots, AI/OCR services, or external payment verification were used. The payment process was tested only as a local manual record with fictional UTR values.
