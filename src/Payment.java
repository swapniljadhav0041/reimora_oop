import java.math.BigDecimal;
import java.time.LocalDate;

/** Represents one manually recorded reimbursement payment. */
public class Payment {
    private final BigDecimal amount;
    private final LocalDate paymentDate;
    private final String utrNumber;

    public Payment(BigDecimal amount, LocalDate paymentDate, String utrNumber) {
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.utrNumber = utrNumber;
    }

    public BigDecimal getAmount() { return amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getUtrNumber() { return utrNumber; }
}
