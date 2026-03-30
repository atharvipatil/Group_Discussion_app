from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import random

# Sample GD topics and AI points
GD_TOPICS = {
    "Climate Change": [
        "Rising global temperatures due to greenhouse gases.",
        "Melting polar ice caps causing sea level rise.",
        "Extreme weather events affecting agriculture and biodiversity."
    ],
    "AI in Jobs": [
        "AI automation can replace repetitive tasks, increasing efficiency.",
        "Certain job roles might become obsolete due to AI advancements.",
        "Upskilling and reskilling are crucial for the workforce to adapt."
    ],
    "Electric Vehicles": [
        "EVs help reduce carbon emissions and air pollution.",
        "Battery technology is improving, reducing range anxiety.",
        "Government policies are encouraging EV adoption."
    ]
}

def get_random_topic():
    topic = random.choice(list(GD_TOPICS.keys()))
    return topic, GD_TOPICS[topic]

class VideoServerHandler(BaseHTTPRequestHandler):
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

    def do_GET(self):
        if self.path == "/get-topic":
            topic, points = get_random_topic()
            self._set_headers()
            self.wfile.write(json.dumps({"topic": topic, "points": points}).encode())
        else:
            self._set_headers()
            self.wfile.write(json.dumps({"status": "Server running"}).encode())

    def do_POST(self):
        if self.path == "/ask-ai":
            content_length = int(self.headers["Content-Length"])
            post_data = self.rfile.read(content_length)
            data = json.loads(post_data)
            question = data.get("question", "")

            # Find related points or return generic response
            response_points = GD_TOPICS.get(question, [
                "No data available for this topic.",
                "Try asking about: Climate Change, AI in Jobs, or Electric Vehicles."
            ])

            self._set_headers()
            self.wfile.write(json.dumps({"answer": response_points}).encode())

def run(server_class=HTTPServer, handler_class=VideoServerHandler, port=7000):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    print(f"Video + AI Co-pilot server running on port {port}...")
    httpd.serve_forever()

if __name__ == "__main__":
    run()
