const sendOtpBtn = document.getElementById("sendOtpBtn");
const verifyOtpBtn = document.getElementById("verifyOtpBtn");
const statusElement = document.getElementById("status");

// Send OTP
sendOtpBtn.addEventListener("click", () => {
  const email = document.getElementById("email").value;
  if (!email) {
    alert("Please enter an email.");
    return;
  }
  fetch("http://localhost:9090/send-otp", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email: email })
  })
  .then(res => res.json())
  .then(data => {
    statusElement.innerText = "OTP sent to " + email;
  })
  .catch(err => {
    console.error(err);
    statusElement.innerText = "Error sending OTP.";
  });
});

// Verify OTP & Register
verifyOtpBtn.addEventListener("click", () => {
  const email = document.getElementById("email").value;
  const otp = document.getElementById("otp").value;
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;
  const degree = document.getElementById("degree").value;

  if (!otp || !username || !password || !degree) {
    alert("Please fill all fields.");
    return;
  }

  fetch("http://localhost:9090/verify-otp", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      email: email,
      otp: otp,
      username: username,
      password: password,
      degree: degree
    })
  })
  .then(res => res.json())
  .then(data => {
    statusElement.innerText = data.status;
  })
  .catch(err => {
    console.error(err);
    statusElement.innerText = "Error verifying OTP.";
  });
});
