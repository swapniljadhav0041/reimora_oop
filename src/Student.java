/**
 * Stores one student's academic and demonstration bank details.
 * Account numbers are masked whenever a student is displayed.
 */
public class Student {
    private final String studentId;
    private final String name;
    private final String prn;
    private final String branch;
    private final String yearDivision;
    private final String accountHolderName;
    private final String bankName;
    private final String accountNumber;
    private final String ifscCode;

    public Student(String studentId, String name, String prn, String branch, String yearDivision,
                   String accountHolderName, String bankName, String accountNumber, String ifscCode) {
        this.studentId = studentId;
        this.name = name;
        this.prn = prn;
        this.branch = branch;
        this.yearDivision = yearDivision;
        this.accountHolderName = accountHolderName;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.ifscCode = ifscCode;
    }

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getPrn() { return prn; }
    public String getBranch() { return branch; }
    public String getYearDivision() { return yearDivision; }
    public String getAccountHolderName() { return accountHolderName; }
    public String getBankName() { return bankName; }
    /** Used only by FileStorage while saving locally; the terminal displays the masked version. */
    public String getAccountNumber() { return accountNumber; }
    public String getIfscCode() { return ifscCode; }

    public String getMaskedAccountNumber() {
        if (accountNumber.length() <= 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    public String getFullDetails() {
        return "Student ID          : " + studentId + "\n"
                + "Name                : " + name + "\n"
                + "PRN / Roll Number   : " + prn + "\n"
                + "Branch / Department : " + branch + "\n"
                + "Year and Division   : " + yearDivision + "\n"
                + "Account Holder      : " + accountHolderName + "\n"
                + "Bank Name           : " + bankName + "\n"
                + "Account Number      : " + getMaskedAccountNumber() + "\n"
                + "IFSC Code           : " + ifscCode;
    }
}
