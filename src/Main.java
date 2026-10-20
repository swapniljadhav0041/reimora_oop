import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/** Entry point and simple numbered terminal menu for the one-operator application. */
public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final StudentManager studentManager;
    private final ReimbursementManager reimbursementManager;

    public Main() {
        FileStorage storage = new FileStorage(Path.of("data"));
        studentManager = new StudentManager(storage);
        reimbursementManager = new ReimbursementManager(storage);
    }

    public static void main(String[] args) {
        try {
            new Main().run();
        } catch (RuntimeException exception) {
            System.out.println("The application could not start: " + exception.getMessage());
        }
    }

    public void run() {
        System.out.println("\n====================================================");
        System.out.println("  REIMORA - STUDENT REIMBURSEMENT SYSTEM");
        System.out.println("  Java OOP Laboratory terminal application");
        System.out.println("====================================================");
        System.out.println("One operator manages students, approvals, and payment records.");
        System.out.println("Use fictional demonstration bank details only. No bank transfer is made.\n");

        boolean running = true;
        while (running) {
            printMenu();
            try {
                switch (readMenuChoice()) {
                    case 0 -> running = false;
                    case 1 -> addStudent();
                    case 2 -> viewAllStudents();
                    case 3 -> searchStudent();
                    case 4 -> createReimbursementRequest();
                    case 5 -> viewAllReimbursements();
                    case 6 -> approveOrRejectReimbursement();
                    case 7 -> recordPayment();
                    case 8 -> checkStatus();
                    case 9 -> viewStudentHistory();
                    case 10 -> displayTextReceipt();
                    default -> System.out.println("Please enter a menu number from 0 to 10.");
                }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                System.out.println("Operation not completed: " + exception.getMessage());
            }
        }
        System.out.println("Goodbye. Records are saved in the data folder.");
    }

    private void printMenu() {
        System.out.println("\n---------------- MAIN MENU ----------------");
        System.out.println("1. Add New Student");
        System.out.println("2. View All Students");
        System.out.println("3. Search Student");
        System.out.println("4. Create Reimbursement Request");
        System.out.println("5. View All Reimbursement Requests");
        System.out.println("6. Approve or Reject Reimbursement");
        System.out.println("7. Record Payment");
        System.out.println("8. Check Reimbursement and Payment Status");
        System.out.println("9. View Student Reimbursement History");
        System.out.println("10. Generate or Display Text Receipt");
        System.out.println("0. Exit");
    }

    private void addStudent() {
        System.out.println("\n--- Add New Student ---");
        System.out.println("Enter fictional demonstration bank information only.");
        Student student = studentManager.addStudent(
                readRequired("Student name: "),
                readRequired("PRN / roll number: "),
                readRequired("Branch / department: "),
                readRequired("Year and division: "),
                readRequired("Bank account holder name: "),
                readRequired("Bank name: "),
                readRequired("Account number: "),
                readRequired("IFSC code: "));
        System.out.println("Student added successfully. Generated student ID: " + student.getStudentId());
    }

    private void viewAllStudents() {
        List<Student> students = studentManager.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students have been added yet.");
            return;
        }
        System.out.println("\n--- All Students ---");
        for (Student student : students) {
            System.out.println(student.getStudentId() + " | " + student.getName() + " | PRN: "
                    + student.getPrn() + " | " + student.getBranch() + " | " + student.getMaskedAccountNumber());
        }
    }

    private void searchStudent() {
        String value = readRequired("Enter Student ID or PRN: ");
        Student student = studentManager.findById(value);
        if (student == null) {
            student = studentManager.findByPrn(value);
        }
        if (student == null) {
            System.out.println("Student was not found.");
            return;
        }
        System.out.println("\n--- Student Details ---");
        System.out.println(student.getFullDetails());
    }

    private void createReimbursementRequest() {
        if (studentManager.getAllStudents().isEmpty()) {
            System.out.println("Add a student before creating a reimbursement request.");
            return;
        }
        System.out.println("\n--- Create Reimbursement Request ---");
        viewAllStudents();
        String studentId = readRequired("Student ID: ");
        Student student = studentManager.findById(studentId);
        if (student == null) {
            System.out.println("Student ID was not found.");
            return;
        }
        Reimbursement reimbursement = reimbursementManager.createRequest(
                student.getStudentId(),
                readDate("Expense date (YYYY-MM-DD): ", false),
                readRequired("Expense category: "),
                readRequired("Expense description / purpose: "),
                readRequired("Vendor or shop name: "),
                readAmount("Reimbursement amount (INR): "));
        System.out.println("Request " + reimbursement.getReimbursementId()
                + " created successfully with status PENDING_APPROVAL.");
    }

    private void viewAllReimbursements() {
        List<Reimbursement> reimbursements = reimbursementManager.getAllReimbursements();
        if (reimbursements.isEmpty()) {
            System.out.println("No reimbursement requests have been created yet.");
            return;
        }
        System.out.println("\n--- All Reimbursement Requests ---");
        for (Reimbursement reimbursement : reimbursements) {
            Student student = studentManager.findById(reimbursement.getStudentId());
            System.out.println(formatReimbursementLine(reimbursement, student));
        }
    }

    private void approveOrRejectReimbursement() {
        List<Reimbursement> pending = reimbursementManager.getPendingReimbursements();
        if (pending.isEmpty()) {
            System.out.println("There are no reimbursement requests pending approval.");
            return;
        }
        System.out.println("\n--- Pending Reimbursement Requests ---");
        for (Reimbursement reimbursement : pending) {
            System.out.println(formatReimbursementLine(reimbursement, studentManager.findById(reimbursement.getStudentId())));
        }
        String reimbursementId = readRequired("Reimbursement ID: ");
        Reimbursement reimbursement = reimbursementManager.findById(reimbursementId);
        if (reimbursement == null) {
            System.out.println("Reimbursement ID was not found.");
            return;
        }
        if (reimbursement.getStatus() != Reimbursement.Status.PENDING_APPROVAL) {
            System.out.println("Only a PENDING_APPROVAL request can be decided.");
            return;
        }
        System.out.println("1. Approve reimbursement");
        System.out.println("2. Reject reimbursement");
        System.out.println("0. Cancel");
        int action = readMenuChoice();
        if (action == 0) {
            System.out.println("No decision was made.");
        } else if (action == 1) {
            reimbursementManager.approve(reimbursementId, readDate("Decision date (blank = today): ", true));
            System.out.println("Reimbursement approved. Payment must be recorded separately.");
        } else if (action == 2) {
            String reason = readRequired("Short rejection reason: ");
            reimbursementManager.reject(reimbursementId, readDate("Decision date (blank = today): ", true), reason);
            System.out.println("Reimbursement rejected. It cannot be paid.");
        } else {
            System.out.println("Please select 1, 2, or 0.");
        }
    }

    private void recordPayment() {
        System.out.println("\n--- Record Payment ---");
        String reimbursementId = readRequired("Reimbursement ID: ");
        Reimbursement reimbursement = reimbursementManager.findById(reimbursementId);
        if (reimbursement == null) {
            System.out.println("Reimbursement ID was not found.");
            return;
        }
        if (reimbursement.getStatus() != Reimbursement.Status.APPROVED) {
            System.out.println("Payment can be recorded only for an APPROVED reimbursement.");
            return;
        }
        System.out.println("Approved amount: " + formatMoney(reimbursement.getRequestedAmount()));
        BigDecimal amount = readAmount("Payment amount (must match approved amount): ");
        LocalDate paymentDate = readDate("Payment date (blank = today): ", true);
        String utrNumber = readRequired("UTR / transaction reference number: ");
        reimbursementManager.recordPayment(reimbursementId, amount, paymentDate, utrNumber);
        System.out.println("Payment saved successfully. Status is now PAID.");
        System.out.println("This is a manual record only; Reimora does not transfer or verify money with a bank.");
    }

    private void checkStatus() {
        String reimbursementId = readRequired("Reimbursement ID: ");
        Reimbursement reimbursement = reimbursementManager.findById(reimbursementId);
        if (reimbursement == null) {
            System.out.println("Reimbursement ID was not found.");
            return;
        }
        Student student = studentManager.findById(reimbursement.getStudentId());
        System.out.println("\n--- Reimbursement and Payment Status ---");
        printReimbursementDetails(reimbursement, student);
    }

    private void viewStudentHistory() {
        String studentId = readRequired("Student ID: ");
        Student student = studentManager.findById(studentId);
        if (student == null) {
            System.out.println("Student ID was not found.");
            return;
        }
        List<Reimbursement> history = reimbursementManager.getHistoryForStudent(student.getStudentId());
        System.out.println("\n--- Reimbursement History: " + student.getName() + " ---");
        if (history.isEmpty()) {
            System.out.println("This student has no reimbursement records.");
            return;
        }
        for (Reimbursement reimbursement : history) {
            System.out.println(formatReimbursementLine(reimbursement, student));
            if (reimbursement.getPayment() != null) {
                System.out.println("  Payment: " + formatMoney(reimbursement.getPayment().getAmount())
                        + " | UTR: " + reimbursement.getPayment().getUtrNumber());
            }
        }
    }

    private void displayTextReceipt() {
        String reimbursementId = readRequired("Reimbursement ID: ");
        Reimbursement reimbursement = reimbursementManager.findById(reimbursementId);
        if (reimbursement == null) {
            System.out.println("Reimbursement ID was not found.");
            return;
        }
        Student student = studentManager.findById(reimbursement.getStudentId());
        System.out.println(buildTextReceipt(reimbursement, student));
    }

    private void printReimbursementDetails(Reimbursement reimbursement, Student student) {
        System.out.println("Reimbursement ID   : " + reimbursement.getReimbursementId());
        System.out.println("Student ID         : " + reimbursement.getStudentId());
        System.out.println("Student Name       : " + (student == null ? "Unknown student" : student.getName()));
        System.out.println("Expense Date       : " + reimbursement.getExpenseDate());
        System.out.println("Expense Category   : " + reimbursement.getCategory());
        System.out.println("Vendor / Shop      : " + reimbursement.getVendorName());
        System.out.println("Description        : " + reimbursement.getDescription());
        System.out.println("Requested Amount   : " + formatMoney(reimbursement.getRequestedAmount()));
        System.out.println("Approval Status    : " + reimbursement.getApprovalStatus());
        System.out.println("Payment Status     : " + reimbursement.getPaymentStatus());
        System.out.println("Decision Date      : " + (reimbursement.getDecisionDate() == null ? "Not decided" : reimbursement.getDecisionDate()));
        if (reimbursement.getStatus() == Reimbursement.Status.REJECTED) {
            System.out.println("Rejection Reason   : " + reimbursement.getRejectionReason());
        }
        if (reimbursement.getPayment() != null) {
            System.out.println("Payment Amount     : " + formatMoney(reimbursement.getPayment().getAmount()));
            System.out.println("Payment Date       : " + reimbursement.getPayment().getPaymentDate());
            System.out.println("UTR Number         : " + reimbursement.getPayment().getUtrNumber());
        }
    }

    private String buildTextReceipt(Reimbursement reimbursement, Student student) {
        String receiptId = "REC" + reimbursement.getReimbursementId().substring(3);
        StringBuilder receipt = new StringBuilder();
        receipt.append("\n========================================\n");
        receipt.append("       REIMORA REIMBURSEMENT RECEIPT\n");
        receipt.append("========================================\n\n");
        receipt.append("Receipt ID       : ").append(receiptId).append("\n");
        receipt.append("Reimbursement ID : ").append(reimbursement.getReimbursementId()).append("\n");
        receipt.append("Student ID       : ").append(reimbursement.getStudentId()).append("\n");
        receipt.append("Student Name     : ").append(student == null ? "Unknown student" : student.getName()).append("\n");
        receipt.append("Expense Date     : ").append(reimbursement.getExpenseDate()).append("\n");
        receipt.append("Expense Category : ").append(reimbursement.getCategory()).append("\n");
        receipt.append("Vendor / Shop    : ").append(reimbursement.getVendorName()).append("\n");
        receipt.append("Description      : ").append(reimbursement.getDescription()).append("\n");
        receipt.append("Expense Amount   : ").append(formatMoney(reimbursement.getRequestedAmount())).append("\n");
        receipt.append("Approval Status  : ").append(reimbursement.getApprovalStatus()).append("\n");
        receipt.append("Payment Status   : ").append(reimbursement.getPaymentStatus()).append("\n");
        receipt.append("UTR Number       : ").append(reimbursement.getPayment() == null ? "Not recorded" : reimbursement.getPayment().getUtrNumber()).append("\n");
        receipt.append("========================================\n");
        receipt.append("Payment details are a manual record only.\n");
        return receipt.toString();
    }

    private String formatReimbursementLine(Reimbursement reimbursement, Student student) {
        String studentName = student == null ? "Unknown student" : student.getName();
        return reimbursement.getReimbursementId() + " | " + studentName + " (" + reimbursement.getStudentId() + ")"
                + " | " + reimbursement.getCategory() + " | " + formatMoney(reimbursement.getRequestedAmount())
                + " | " + reimbursement.getStatus() + " | Payment: " + reimbursement.getPaymentStatus();
    }

    private int readMenuChoice() {
        String input = readLine("Choose an option: ");
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private String readRequired(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isBlank()) {
                return value;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    private LocalDate readDate(String prompt, boolean allowTodayDefault) {
        while (true) {
            String input = readLine(prompt);
            if (allowTodayDefault && input.isBlank()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException exception) {
                System.out.println("Enter a valid date in YYYY-MM-DD format.");
            }
        }
    }

    private BigDecimal readAmount(String prompt) {
        while (true) {
            String input = readLine(prompt);
            try {
                BigDecimal amount = new BigDecimal(input);
                if (amount.signum() <= 0 || amount.scale() > 2) {
                    throw new NumberFormatException();
                }
                return amount.setScale(2, RoundingMode.UNNECESSARY);
            } catch (NumberFormatException | ArithmeticException exception) {
                System.out.println("Enter a positive INR amount with at most two decimal places.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println("\nInput closed. Goodbye.");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    private String formatMoney(BigDecimal amount) {
        return "INR " + amount.setScale(2).toPlainString();
    }
}
