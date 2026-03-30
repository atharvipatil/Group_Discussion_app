// ---------------- Video Call ----------------
let localStream;
let peerConnection;
let mediaRecorder;
let recordedChunks = [];
let remoteStream = new MediaStream();
const servers = { iceServers: [{ urls: "stun:stun.l.google.com:19302" }] };

async function startCall() {
  try {
    localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
    document.getElementById('localVideo').srcObject = localStream;

    peerConnection = new RTCPeerConnection(servers);
    localStream.getTracks().forEach(track => peerConnection.addTrack(track, localStream));

    peerConnection.ontrack = (event) => {
      remoteStream = event.streams[0];
      document.getElementById('remoteVideo').srcObject = remoteStream;
    };

    const offer = await peerConnection.createOffer();
    await peerConnection.setLocalDescription(offer);

    console.log("Offer Created (send to other peer):", offer);
  } catch (err) {
    console.error("Error starting call:", err);
    alert("Could not start the call. Check camera/microphone permissions.");
  }
}

function startRecording() {
  if (!localStream) {
    alert("Start the call first.");
    return;
  }
  recordedChunks = [];

  const mixedStream = new MediaStream();
  localStream.getTracks().forEach(track => mixedStream.addTrack(track));
  if (remoteStream) remoteStream.getTracks().forEach(track => mixedStream.addTrack(track));

  mediaRecorder = new MediaRecorder(mixedStream, { mimeType: 'video/webm; codecs=vp9' });
  mediaRecorder.ondataavailable = (event) => {
    if (event.data.size > 0) recordedChunks.push(event.data);
  };
  mediaRecorder.onstop = () => {
    const blob = new Blob(recordedChunks, { type: 'video/webm' });
    const url = URL.createObjectURL(blob);
    const downloadLink = document.getElementById('downloadLink');
    downloadLink.href = url;
    downloadLink.style.display = 'block';
  };
  mediaRecorder.start();
  alert("Recording started!");
}

function stopRecording() {
  if (mediaRecorder && mediaRecorder.state !== "inactive") {
    mediaRecorder.stop();
    alert("Recording stopped! Download link ready.");
  }
}

// ---------------- Chat ----------------
const chatMessages = document.getElementById("chatMessages");

function sendMessage() {
  const msg = document.getElementById("chatInput").value;
  if (!msg) return;
  addMessage("You", msg);
  document.getElementById("chatInput").value = "";
  // TODO: Send message via WebSocket to others
}

function addMessage(sender, message) {
  const p = document.createElement("p");
  p.innerHTML = `<strong>${sender}:</strong> ${message}`;
  chatMessages.appendChild(p);
  chatMessages.scrollTop = chatMessages.scrollHeight;
}

// ---------------- AI Co-pilot ----------------
function askAI() {
  const question = document.getElementById("aiQuestion").value.trim();
  if (!question) return;
  document.getElementById("aiResponse").innerText = "AI thinking...";

  fetch("http://localhost:7100/ask-ai", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ question: question })
  })
  .then(res => res.json())
  .then(data => {
    if (Array.isArray(data.answer)) {
      document.getElementById("aiResponse").innerText = "AI Points:\n" + data.answer.join("\n");
    } else {
      document.getElementById("aiResponse").innerText = "AI: " + data.answer;
    }
  })
  .catch(err => {
    console.error("AI error:", err);
    document.getElementById("aiResponse").innerText = "Error: Unable to contact AI.";
  });
}

// ---------------- Suggest GD Topic ----------------
function suggestTopic() {
  fetch("http://localhost:7100/get-topic")
    .then(res => res.json())
    .then(data => {
      const topicDisplay = document.getElementById("aiResponse");
      topicDisplay.innerText = `Suggested Topic: ${data.topic}\n\nPoints:\n${data.points.join("\n")}`;
    })
    .catch(err => {
      console.error("Error fetching topic:", err);
      document.getElementById("aiResponse").innerText = "Error fetching topic.";
    });
}
