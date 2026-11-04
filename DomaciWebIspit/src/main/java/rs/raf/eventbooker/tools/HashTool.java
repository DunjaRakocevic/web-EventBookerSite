package rs.raf.eventbooker.tools;
import org.mindrot.jbcrypt.BCrypt;
public class HashTool {
    public static void main(String[] args) {
        String pw = "admin123";
        System.out.println(BCrypt.hashpw(pw, BCrypt.gensalt(10)));
    }
}
