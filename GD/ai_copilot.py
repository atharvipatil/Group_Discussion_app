from http.server import BaseHTTPRequestHandler, HTTPServer
import json

# Pre-defined GD topics with points
GD_TOPICS = {
    "Climate Change": [
        "Global warming due to greenhouse gases is increasing rapidly.",
        "Melting glaciers and rising sea levels are major threats.",
        "Countries must collaborate on renewable energy and carbon reduction."
    ],
    "AI in Jobs": [
        "AI automation can reduce repetitive human tasks.",
        "Upskilling is crucial to avoid job losses.",
        "AI can create new job opportunities in tech sectors."
    ],
    "Electric Vehicles": [
        "EVs reduce pollution and dependency on fossil fuels.",
        "Battery technology is improving, increasing driving range.",
        "Government subsidies are encouraging EV adoption."
    ]
}

class AICopilotHandler(BaseHTTPRequestHandler):
    def _set_headers(self):
        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()

    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.end_headers()

    def do_POST(self):
        if self.path == "/ask-ai":
            content_length = int(self.headers["Content-Length"])
            post_data = self.rfile.read(content_length)
            data = json.loads(post_data)
            question = data.get("question", "").strip()

            # Fetch points for topic
            points = GD_TOPICS.get(question, [
                "No exact data found for this topic.",
                "Try asking about: Climate Change, AI in Jobs, or Electric Vehicles."
            ])

            self._set_headers()
            self.wfile.write(json.dumps({"answer": points}).encode())

def run(server_class=HTTPServer, handler_class=AICopilotHandler, port=7100):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    print(f"AI Co-pilot server running on port {port}...")
    httpd.serve_forever()

if __name__ == "__main__":
    run()
