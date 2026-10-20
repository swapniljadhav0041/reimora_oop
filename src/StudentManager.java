import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Adds, validates, finds, and persists student records. */
public class StudentManager {
    private final FileStorage storage;
    private final List<Student> students;

    public StudentManager(FileStorage storage) {
        this.storage = storage;
        this.students = new ArrayList<>(storage.loadStudents());
    }

    public Student addStudent(String name, String prn, String branch, String yearDivision,
                              String accountHolderName, String bankName, String accountNumber, String ifscCode) {
        requireText(name, "Student name");
        requireText(prn, "PRN / roll number");
        requireText(branch, "Branch / department");
        requireText(yearDivision, "Year and division");
        requireText(accountHolderName, "Account holder name");
        requireText(bankName, "Bank name");
        requireText(accountNumber, "Account number");
        requireText(ifscCode, "IFSC code");

        if (findByPrn(prn) != null) {
            throw new IllegalArgumentException("A student with this PRN / roll number already exists.");
        }

        Student student = new Student(nextStudentId(), name.trim(), prn.trim(), branch.trim(), yearDivision.trim(),
                accountHolderName.trim(), bankName.trim(), accountNumber.trim(), ifscCode.trim());
        students.add(student);
        save();
        return student;
    }

    public List<Student> getAllStudents() {
        return new ArrayList<>(students);
    }

    public Student findById(String studentId) {
        for (Student student : students) {
            if (student.getStudentId().equalsIgnoreCase(studentId.trim())) {
                return student;
            }
        }
        return null;
    }

    public Student findByPrn(String prn) {
        for (Student student : students) {
            if (student.getPrn().equalsIgnoreCase(prn.trim())) {
                return student;
            }
        }
        return null;
    }

    public List<Student> search(String searchText) {
        String query = searchText.trim().toLowerCase(Locale.ROOT);
        List<Student> matches = new ArrayList<>();
        for (Student student : students) {
            if (student.getStudentId().toLowerCase(Locale.ROOT).contains(query)
                    || student.getPrn().toLowerCase(Locale.ROOT).contains(query)
                    || student.getName().toLowerCase(Locale.ROOT).contains(query)) {
                matches.add(student);
            }
        }
        return matches;
    }

    private String nextStudentId() {
        int highestNumber = 0;
        for (Student student : students) {
            try {
                highestNumber = Math.max(highestNumber, Integer.parseInt(student.getStudentId().substring(3)));
            } catch (RuntimeException ignored) {
                // A malformed old ID is ignored; new IDs remain unique among valid generated IDs.
            }
        }
        return String.format("STU%03d", highestNumber + 1);
    }

    private void save() {
        storage.saveStudents(students);
    }

    private void requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
    }
}
