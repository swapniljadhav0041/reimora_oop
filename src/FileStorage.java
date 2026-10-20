import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Reads and writes the two readable UTF-8 CSV data files used by the program. */
public class FileStorage {
    private final Path dataDirectory;
    private final Path studentsFile;
    private final Path reimbursementsFile;

    public FileStorage(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.studentsFile = dataDirectory.resolve("students.csv");
        this.reimbursementsFile = dataDirectory.resolve("reimbursements.csv");
        createDataFiles();
    }

    public List<Student> loadStudents() {
        List<Student> students = new ArrayList<>();
        List<String> lines = readDataLines(studentsFile);
        for (int lineNumber = 1; lineNumber < lines.size(); lineNumber++) {
            try {
                List<String> values = parseCsvLine(lines.get(lineNumber));
                if (values.size() != 9) {
                    throw new IllegalArgumentException("expected 9 fields");
                }
                students.add(new Student(values.get(0), values.get(1), values.get(2), values.get(3), values.get(4),
                        values.get(5), values.get(6), values.get(7), values.get(8)));
            } catch (RuntimeException exception) {
                System.out.println("Warning: skipped invalid student record at line " + (lineNumber + 1) + ".");
            }
        }
        return students;
    }

    public void saveStudents(List<Student> students) {
        List<String> lines = new ArrayList<>();
        lines.add("studentId,name,prn,branch,yearDivision,accountHolderName,bankName,accountNumber,ifscCode");
        for (Student student : students) {
            lines.add(csv(student.getStudentId()) + "," + csv(student.getName()) + "," + csv(student.getPrn())
                    + "," + csv(student.getBranch()) + "," + csv(student.getYearDivision())
                    + "," + csv(student.getAccountHolderName()) + "," + csv(student.getBankName())
                    + "," + csv(student.getAccountNumber()) + "," + csv(student.getIfscCode()));
        }
        writeLines(studentsFile, lines);
    }

    public List<Reimbursement> loadReimbursements() {
        List<Reimbursement> reimbursements = new ArrayList<>();
        List<String> lines = readDataLines(reimbursementsFile);
        for (int lineNumber = 1; lineNumber < lines.size(); lineNumber++) {
            try {
                List<String> values = parseCsvLine(lines.get(lineNumber));
                if (values.size() != 13) {
                    throw new IllegalArgumentException("expected 13 fields");
                }
                LocalDate decisionDate = values.get(8).isBlank() ? null : LocalDate.parse(values.get(8));
                Payment payment = values.get(10).isBlank() ? null
                        : new Payment(new BigDecimal(values.get(9)), LocalDate.parse(values.get(10)), values.get(11));
                reimbursements.add(new Reimbursement(values.get(0), values.get(1), LocalDate.parse(values.get(2)),
                        values.get(3), values.get(4), values.get(5), new BigDecimal(values.get(6)),
                        Reimbursement.Status.valueOf(values.get(7)), decisionDate, values.get(12), payment));
            } catch (RuntimeException exception) {
                System.out.println("Warning: skipped invalid reimbursement record at line " + (lineNumber + 1) + ".");
            }
        }
        return reimbursements;
    }

    public void saveReimbursements(List<Reimbursement> reimbursements) {
        List<String> lines = new ArrayList<>();
        lines.add("reimbursementId,studentId,expenseDate,category,description,vendorName,requestedAmount,status,decisionDate,paymentAmount,paymentDate,utrNumber,rejectionReason");
        for (Reimbursement reimbursement : reimbursements) {
            Payment payment = reimbursement.getPayment();
            lines.add(csv(reimbursement.getReimbursementId()) + "," + csv(reimbursement.getStudentId())
                    + "," + csv(reimbursement.getExpenseDate().toString()) + "," + csv(reimbursement.getCategory())
                    + "," + csv(reimbursement.getDescription()) + "," + csv(reimbursement.getVendorName())
                    + "," + csv(reimbursement.getRequestedAmount().toPlainString())
                    + "," + csv(reimbursement.getStatus().name())
                    + "," + csv(reimbursement.getDecisionDate() == null ? "" : reimbursement.getDecisionDate().toString())
                    + "," + csv(payment == null ? "" : payment.getAmount().toPlainString())
                    + "," + csv(payment == null ? "" : payment.getPaymentDate().toString())
                    + "," + csv(payment == null ? "" : payment.getUtrNumber())
                    + "," + csv(reimbursement.getRejectionReason()));
        }
        writeLines(reimbursementsFile, lines);
    }

    private void createDataFiles() {
        try {
            Files.createDirectories(dataDirectory);
            if (!Files.exists(studentsFile)) {
                Files.writeString(studentsFile,
                        "studentId,name,prn,branch,yearDivision,accountHolderName,bankName,accountNumber,ifscCode\n",
                        StandardCharsets.UTF_8);
            }
            if (!Files.exists(reimbursementsFile)) {
                Files.writeString(reimbursementsFile,
                        "reimbursementId,studentId,expenseDate,category,description,vendorName,requestedAmount,status,decisionDate,paymentAmount,paymentDate,utrNumber,rejectionReason\n",
                        StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create the local data files: " + exception.getMessage());
        }
    }

    private List<String> readDataLines(Path file) {
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + file.getFileName() + ": " + exception.getMessage());
        }
    }

    private void writeLines(Path file, List<String> lines) {
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save " + file.getFileName() + ": " + exception.getMessage());
        }
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean insideQuotes = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '\"') {
                if (insideQuotes && index + 1 < line.length() && line.charAt(index + 1) == '\"') {
                    value.append('\"');
                    index++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (character == ',' && !insideQuotes) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(character);
            }
        }
        if (insideQuotes) {
            throw new IllegalArgumentException("unclosed quotes");
        }
        values.add(value.toString());
        return values;
    }
}
