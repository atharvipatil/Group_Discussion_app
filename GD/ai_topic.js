let timerInterval;

async function getTopic() {
  try {
    // Call Java backend
    const response = await fetch("http://localhost:9000/get-topic", { cache: "no-store" });
    const data = await response.json();

    // Display topic
    document.getElementById("gdTopic").textContent = " " + data.topic;

    // Start 5-minute timer
    startTimer(5 * 60);
  } catch (error) {
    console.error("Error fetching topic:", error);
    document.getElementById("gdTopic").textContent = " Server not running!";
  }
}

function startTimer(duration) {
  let timer = duration;
  const display = document.getElementById("timerDisplay");
  document.getElementById("timerContainer").style.display = "block";

  if (timerInterval) {
    clearInterval(timerInterval);
  }

  timerInterval = setInterval(() => {
    let minutes = Math.floor(timer / 60);
    let seconds = timer % 60;

    display.textContent = 
      (minutes < 10 ? "0" : "") + minutes + ":" +
      (seconds < 10 ? "0" : "") + seconds;

    if (--timer < 0) {
      clearInterval(timerInterval);
      display.textContent = " Time’s up!";
    }
  }, 1000);
}
