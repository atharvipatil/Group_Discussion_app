import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import jakarta.mail.*;
import jakarta.mail.internet.*;



import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import org.json.*;

public class register_server {

    private static final String USERS_FILE = "registered_users.json";
    private static final Map<String, String> otpStore = new HashMap<>();

    private static final String SENDER_EMAIL = "atharvipatil3@gmail.com";
    private static final String APP_PASSWORD = "rgiqprqnoyokiwgt"; // Gmail App Password

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);

        server.createContext("/send-otp", new SendOtpHandler());
        server.createContext("/verify-otp", new VerifyOtpHandler());
        server.createContext("/users", new UsersHandler());
        server.createContext("/", new DefaultHandler());

        server.setExecutor(null);
        System.out.println("Java OTP Registration server running on port 9090...");
        server.start();
    }

    // -------------------- Handlers --------------------

    static class SendOtpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCORS(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"status\":\"Method Not Allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject data = new JSONObject(body);

            String email = data.getString("email");
            String otp = generateOtp();
            otpStore.put(email, otp);

            boolean sent = sendOtpEmail(email, otp);
            if (sent) {
                sendResponse(exchange, 200, "{\"status\":\"OTP sent\"}");
            } else {
                sendResponse(exchange, 500, "{\"status\":\"Failed to send OTP\"}");
            }
        }
    }

    static class VerifyOtpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCORS(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"status\":\"Method Not Allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject data = new JSONObject(body);

            String email = data.getString("email");
            String otp = data.getString("otp");

            if (otpStore.containsKey(email) && otpStore.get(email).equals(otp)) {
                JSONObject userInfo = new JSONObject();
                userInfo.put("email", email);
                userInfo.put("username", data.getString("username"));
                userInfo.put("password", hashPassword(data.getString("password")));
                userInfo.put("degree", data.getString("degree"));

                saveUser(userInfo);

                otpStore.remove(email);
                sendResponse(exchange, 200, "{\"status\":\"Registration successful\"}");
            } else {
                sendResponse(exchange, 400, "{\"status\":\"Invalid OTP\"}");
            }
        }
    }

    static class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JSONArray users = loadUsers();
            sendResponse(exchange, 200, users.toString());
        }
    }

    static class DefaultHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendResponse(exchange, 200, "{\"status\":\"Server running\"}");
        }
    }

    // -------------------- Helper Functions --------------------

    private static void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private static void sendCORS(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(200, -1);
    }

    private static String generateOtp() {
        Random rand = new Random();
        return String.valueOf(100000 + rand.nextInt(900000));
    }

    private static boolean sendOtpEmail(String toEmail, String otp) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your OTP Code");
            message.setText("Your OTP is: " + otp);

            Transport.send(message);
            System.out.println("OTP " + otp + " sent to " + toEmail);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static JSONArray loadUsers() {
        try {
            String content = Files.readString(Paths.get(USERS_FILE), StandardCharsets.UTF_8);
            return new JSONArray(content);
        } catch (IOException e) {
            return new JSONArray();
        }
    }

    private static void saveUser(JSONObject user) {
        JSONArray users = loadUsers();
        users.put(user);
        try (FileWriter file = new FileWriter(USERS_FILE)) {
            file.write(users.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // fallback
        }
    }
}
