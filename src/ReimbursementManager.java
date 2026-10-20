import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Creates and updates reimbursement requests, then saves them using FileStorage. */
public class ReimbursementManager {
    private final FileStorage storage;
    private final List<Reimbursement> reimbursements;

    public ReimbursementManager(FileStorage storage) {
        this.storage = storage;
        this.reimbursements = new ArrayList<>(storage.loadReimbursements());
    }

    public Reimbursement createRequest(String studentId, LocalDate expenseDate, String category,
                                       String description, String vendorName, BigDecimal amount) {
        requireText(category, "Expense category");
        requireText(description, "Expense description");
        requireText(vendorName, "Vendor / shop name");
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Reimbursement amount must be greater than zero.");
        }

        Reimbursement reimbursement = new Reimbursement(nextReimbursementId(), studentId, expenseDate,
                category.trim(), description.trim(), vendorName.trim(), amount);
        reimbursements.add(reimbursement);
        save();
        return reimbursement;
    }

    public List<Reimbursement> getAllReimbursements() {
        return new ArrayList<>(reimbursements);
    }

    public List<Reimbursement> getPendingReimbursements() {
        List<Reimbursement> pending = new ArrayList<>();
        for (Reimbursement reimbursement : reimbursements) {
            if (reimbursement.getStatus() == Reimbursement.Status.PENDING_APPROVAL) {
                pending.add(reimbursement);
            }
        }
        return pending;
    }

    public Reimbursement findById(String reimbursementId) {
        for (Reimbursement reimbursement : reimbursements) {
            if (reimbursement.getReimbursementId().equalsIgnoreCase(reimbursementId.trim())) {
                return reimbursement;
            }
        }
        return null;
    }

    public List<Reimbursement> getHistoryForStudent(String studentId) {
        List<Reimbursement> history = new ArrayList<>();
        for (Reimbursement reimbursement : reimbursements) {
            if (reimbursement.getStudentId().equalsIgnoreCase(studentId.trim())) {
                history.add(reimbursement);
            }
        }
        return history;
    }

    public void approve(String reimbursementId, LocalDate decisionDate) {
        Reimbursement reimbursement = requireReimbursement(reimbursementId);
        reimbursement.approve(decisionDate);
        save();
    }

    public void reject(String reimbursementId, LocalDate decisionDate, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("A short rejection reason is required.");
        }
        Reimbursement reimbursement = requireReimbursement(reimbursementId);
        reimbursement.reject(decisionDate, reason.trim());
        save();
    }

    public void recordPayment(String reimbursementId, BigDecimal amount, LocalDate paymentDate, String utrNumber) {
        if (utrNumber == null || utrNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("UTR number is required to record payment.");
        }
        Reimbursement reimbursement = requireReimbursement(reimbursementId);
        reimbursement.recordPayment(new Payment(amount, paymentDate, utrNumber.trim()));
        save();
    }

    private Reimbursement requireReimbursement(String reimbursementId) {
        Reimbursement reimbursement = findById(reimbursementId);
        if (reimbursement == null) {
            throw new IllegalArgumentException("Reimbursement ID was not found.");
        }
        return reimbursement;
    }

    private String nextReimbursementId() {
        int highestNumber = 0;
        for (Reimbursement reimbursement : reimbursements) {
            try {
                highestNumber = Math.max(highestNumber,
                        Integer.parseInt(reimbursement.getReimbursementId().substring(3)));
            } catch (RuntimeException ignored) {
                // A malformed old ID is ignored; new IDs remain unique among valid generated IDs.
            }
        }
        return String.format("REM%03d", highestNumber + 1);
    }

    private void save() {
        storage.saveReimbursements(reimbursements);
    }

    private void requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
    }
}
