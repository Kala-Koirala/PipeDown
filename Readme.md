# 🐤 PipeDown — A Flappy Bird Clone in Java (Swing)

A fun, lightweight recreation of the classic **Flappy Bird** — built from scratch in Java using Swing, with progressive difficulty, persistent high scores, and live-synthesized MIDI sound effects (no audio files needed!).
<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-Swing-orange?logo=openjdk&logoColor=white">
  <img alt="Platform" src="https://img.shields.io/badge/Platform-Desktop-blue">
  <img alt="Status" src="https://img.shields.io/badge/Status-In%20Progress-yellow">
  <a href="https://github.com/Kala-Koirala/PipeDown/stargazers"><img alt="Stars" src="https://img.shields.io/github/stars/Kala-Koirala/PipeDown?style=flat"></a>
  <a href="https://github.com/Kala-Koirala/PipeDown/commits/main"><img alt="Last Commit" src="https://img.shields.io/github/last-commit/Kala-Koirala/PipeDown"></a>
</p>


---

## ✨ Features

- 🎯 **Classic Gameplay** — Flap through gaps, avoid the pipes, chase a high score
- 👤 **Player Profiles** — Enter your name at launch; scores are tracked per player
- 🏆 **Persistent High Scores** — Personal bests and the all-time leaderboard are saved to disk and reloaded next time you play
- 📈 **Progressive Difficulty** — Pipe speed increases, gaps narrow, and spawn rate quickens the further you get, so the game keeps getting harder
- 🔊 **Live Synthesized Sound** — Jump, score, level-up, high-score, and game-over sounds are generated in real time through the Java MIDI synthesizer — zero external audio files
- ⚡ **Quick Restart** — Jump straight back in after a Game Over
- ⌨️ **One-Button Controls** — Just the space bar to play, jump, and restart

---

## 🎮 Controls

| Key | Action |
|---|---|
| `SPACE` | Start the game / Flap / Restart after Game Over |
| `M` | Menu |
| `Q` | Quit |
| `P` | Pause / Resume |


---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| GUI | Java Swing (`JPanel`, `JFrame`, `Timer`) |
| Sound | `javax.sound.midi` (synthesized in real time — no `.wav`/`.mp3` files) |
| Data persistence | Plain text files (`highscore.txt`, `player_name.txt`) |

---

## 📂 Project Structure

```
PipeDown/
├── Main.java                       # Entry point — creates the window and shows the name prompt
├── FlappyBird.java                 # Core game panel — game loop, drawing, input handling
├── Bird.java                       # Bird sprite/position data
├── Pipe.java                       # Base pipe class
├── TopPipe.java / BottomPipe.java  # Pipe variants
├── DifficultyManager.java          # Calculates speed, gap size, and spawn rate from score
├── HighScoreManager.java           # Loads/saves player names and high scores
├── ThemeManager.java               # Stores the different themes taht can be toggled
├── SoundManager.java               # Generates all sound effects via MIDI
├── UsernameDialog.java              # Popup for entering the player's name
└── assets/                          # Sprites and backgrounds

```

## 🐦 Project Preview


<table>
  <tr>
    <td align="center">
      <img width="455" height="803" alt="image" src="https://github.com/user-attachments/assets/93e99ac0-3ca8-4ef2-98b7-c385b09bc1ee" />
<br />
      <sub>Figure 1: Welcome Screen</sub>
    </td>
    <td align="center">
      <img width="445" height="801" alt="image" src="https://github.com/user-attachments/assets/61cadcab-e7fb-415b-a912-c3d4d1effb4f" />
<br />
      <sub>Figure 2: Game Over Screen</sub>
    </td>
    <td align="center">
      <img width="451" height="803" alt="image" src="https://github.com/user-attachments/assets/ed0f70a1-aab7-4346-82e8-189f542503f9" />
<br />
      <sub>Figure 3: Paused Screen</sub>
    </td>
    <td align="center">
      <img width="450" height="801" alt="image" src="https://github.com/user-attachments/assets/84cda54a-aa1a-4487-ae66-fe3341ad1f88" />
          <sub>Figure 4: Username Enter Screen</sub>
    </td>
  </tr>
</table>


---


## 📼 The FSM Diagram


```mermaid
stateDiagram
  accTitle: Game state flow
  accDescr: A state diagram showing the flow from starting the game through gameplay, pausing, game over, scoring, the high-score menu, and quitting.
  direction LR
  classDef Aqua stroke-width:1px,stroke-dasharray:none,stroke:#46EDC8,fill:#DEFFF8,color:#378E7A;
  Start --> Playing:Space
  Playing --> Game_Over:Collide
  Game_Over --> Score:auto
  Score --> Menu/Highscore:M
  Menu/Highscore --> Quit:Q
  Playing --> Pause:ESC
  Pause --> Playing:ESC
  Score --> Start:R
  Menu/Highscore --> Start:M
  class Start Aqua
```

---

## 🚀 Getting Started

**Requirements:** a Java Development Kit (JDK) installed, so both `javac` and `java` are available on your PATH.

```bash
# 1. Navigate into the project folder
cd PipeDown
 
# 2. Compile and run in one line
javac Main.java && java Main
```
On first launch, you'll be asked to enter a player name — your scores are saved and tracked against that name for future sessions.

---

## 🙌 Contributors
 
Thanks to everyone who's worked on this project:
 
- **Purnima Koirala** - [@Kala-Koirala](https://github.com/Kala-Koirala)
- **Shahisha Adhikari** - [@Shahisha1](https://github.com/Shahisha1)
- **Shreya Dhakal** [@Shreyaoff](https://github.com/shreyaoff)
- **Kabir Lama** - [@kabirlama28-web](https://github.com/kabirlama28-web)

Want to contribute too? Fork the [repo](https://github.com/Kala-Koirala/PipeDown), make your changes, and open a pull request against `main`.

