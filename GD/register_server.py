from http.server import BaseHTTPRequestHandler, HTTPServer
import json, random, smtplib, os

otp_store = {}
USERS_FILE = "registered_users.json"

def generate_otp():
    return str(random.randint(100000, 999999))

def send_otp_email(to_email, otp):
    try:
        sender_email = "atharvipatil3@gmail.com"
        app_password = "rgiqprqnoyokiwgt"  # Correct Gmail App Password (no spaces)

        with smtplib.SMTP_SSL("smtp.gmail.com", 465) as server:
            server.login(sender_email, app_password)
            message = f"Subject: Your OTP Code\n\nYour OTP is: {otp}"
            server.sendmail(sender_email, to_email, message)

        print(f"OTP {otp} sent to {to_email}")
        return True
    except Exception as e:
        print("Error sending email:", e)
        return False

def load_users():
    if os.path.exists(USERS_FILE):
        with open(USERS_FILE, "r") as f:
            return json.load(f)
    return []

def save_user(user_data):
    users = load_users()
    users.append(user_data)
    with open(USERS_FILE, "w") as f:
        json.dump(users, f, indent=4)

class RegisterHandler(BaseHTTPRequestHandler):
    def _set_headers(self, status=200):
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()

    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.end_headers()

    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        data = self.rfile.read(content_length)
        user_data = json.loads(data.decode())

        if self.path == "/send-otp":
            email = user_data.get("email")
            otp = generate_otp()
            otp_store[email] = otp

            if send_otp_email(email, otp):
                self._set_headers()
                self.wfile.write(json.dumps({"status": "OTP sent"}).encode())
            else:
                self._set_headers(500)
                self.wfile.write(json.dumps({"status": "Failed to send OTP"}).encode())

        elif self.path == "/verify-otp":
            email = user_data.get("email")
            otp = user_data.get("otp")
            if otp_store.get(email) == otp:
                user_info = {
                    "email": email,
                    "username": user_data.get("username"),
                    "password": user_data.get("password"),
                    "degree": user_data.get("degree")
                }
                save_user(user_info)
                self._set_headers()
                self.wfile.write(json.dumps({"status": "Registration successful"}).encode())
                del otp_store[email]
            else:
                self._set_headers(400)
                self.wfile.write(json.dumps({"status": "Invalid OTP"}).encode())

    def do_GET(self):
        if self.path == "/users":
            self._set_headers()
            users = load_users()
            self.wfile.write(json.dumps(users).encode())
        else:
            self._set_headers()
            self.wfile.write(json.dumps({"status": "Server running"}).encode())

def run(server_class=HTTPServer, handler_class=RegisterHandler):
    server_address = ('', 8000)
    httpd = server_class(server_address, handler_class)
    print("Server running on port 8000...")
    httpd.serve_forever()

if __name__ == "__main__":
    run()