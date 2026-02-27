import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AdminHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "Admin@123"; // <-- put your admin password here
        System.out.println(encoder.encode(rawPassword));
    }
}