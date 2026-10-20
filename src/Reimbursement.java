import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A single expense reimbursement request. A request moves from pending approval
 * to approved and then paid, or it can be rejected.
 */
public class Reimbursement {
    public enum Status {
        PENDING_APPROVAL, APPROVED, REJECTED, PAID
    }

    private final String reimbursementId;
    private final String studentId;
    private final LocalDate expenseDate;
    private final String category;
    private final String description;
    private final String vendorName;
    private final BigDecimal requestedAmount;
    private Status status;
    private LocalDate decisionDate;
    private String rejectionReason;
    private Payment payment;

    public Reimbursement(String reimbursementId, String studentId, LocalDate expenseDate, String category,
                         String description, String vendorName, BigDecimal requestedAmount) {
        this(reimbursementId, studentId, expenseDate, category, description, vendorName, requestedAmount,
                Status.PENDING_APPROVAL, null, "", null);
    }

    public Reimbursement(String reimbursementId, String studentId, LocalDate expenseDate, String category,
                         String description, String vendorName, BigDecimal requestedAmount, Status status,
                         LocalDate decisionDate, String rejectionReason, Payment payment) {
        this.reimbursementId = reimbursementId;
        this.studentId = studentId;
        this.expenseDate = expenseDate;
        this.category = category;
        this.description = description;
        this.vendorName = vendorName;
        this.requestedAmount = requestedAmount;
        this.status = status;
        this.decisionDate = decisionDate;
        this.rejectionReason = rejectionReason == null ? "" : rejectionReason;
        this.payment = payment;
    }

    public String getReimbursementId() { return reimbursementId; }
    public String getStudentId() { return studentId; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getVendorName() { return vendorName; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public Status getStatus() { return status; }
    public LocalDate getDecisionDate() { return decisionDate; }
    public String getRejectionReason() { return rejectionReason; }
    public Payment getPayment() { return payment; }

    public void approve(LocalDate date) {
        if (status != Status.PENDING_APPROVAL) {
            throw new IllegalStateException("Only pending requests can be approved.");
        }
        status = Status.APPROVED;
        decisionDate = date;
    }

    public void reject(LocalDate date, String reason) {
        if (status != Status.PENDING_APPROVAL) {
            throw new IllegalStateException("Only pending requests can be rejected.");
        }
        status = Status.REJECTED;
        decisionDate = date;
        rejectionReason = reason;
    }

    public void recordPayment(Payment newPayment) {
        if (status != Status.APPROVED) {
            throw new IllegalStateException("Payment can be recorded only for an approved reimbursement.");
        }
        if (newPayment.getAmount().compareTo(requestedAmount) != 0) {
            throw new IllegalArgumentException("Payment amount must exactly match the approved reimbursement amount.");
        }
        if (newPayment.getUtrNumber().isBlank()) {
            throw new IllegalArgumentException("UTR number is required to record payment.");
        }
        payment = newPayment;
        status = Status.PAID;
    }

    public String getPaymentStatus() {
        return payment == null ? "NOT_PAID" : "PAID";
    }

    /** Approval remains APPROVED after payment; payment has its own separate status. */
    public String getApprovalStatus() {
        if (status == Status.PAID) {
            return Status.APPROVED.name();
        }
        return status.name();
    }
}
