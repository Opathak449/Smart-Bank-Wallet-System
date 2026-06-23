import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("Admin Hash: " + encoder.encode("Admin@123"));
        System.out.println("User Hash: " + encoder.encode("User@123"));
    }
}
