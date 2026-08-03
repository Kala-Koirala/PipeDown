import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HighScoreManager {
    private String currentPlayerName = "Player";
    private String highScorePlayer = "None";
    private int highScore = 0;
    private final Map<String, Integer> playerScores = new HashMap<>(); // key is lowercase
    private final Map<String, String> playerOriginalNames = new HashMap<>(); // lowercase -> original casing

    public HighScoreManager() {
        loadCurrentPlayerName();
        loadHighScore();
    }

    public void loadCurrentPlayerName() {
        try (BufferedReader reader = new BufferedReader(new FileReader("player_name.txt"))) {
            String name = reader.readLine();
            if (name != null && !name.trim().isEmpty()) {
                currentPlayerName = name.trim();
                playerOriginalNames.put(currentPlayerName.toLowerCase(), currentPlayerName);
            }
        } catch (IOException e) {
            currentPlayerName = "Player";
            playerOriginalNames.put("player", "Player");
        }
    }

    public void saveCurrentPlayerName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            currentPlayerName = name.trim();
            playerOriginalNames.put(currentPlayerName.toLowerCase(), currentPlayerName);
            try (PrintWriter writer = new PrintWriter(new FileWriter("player_name.txt"))) {
                writer.println(currentPlayerName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //  Load High Scores
    public void loadHighScore() {
        playerScores.clear();
        playerOriginalNames.clear();
        highScore = 0;
        highScorePlayer = "None";

        playerOriginalNames.put(currentPlayerName.toLowerCase(), currentPlayerName);

        File file = new File("highscore.txt");
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String player = parts[0].trim();
                    String key = player.toLowerCase();
                    try {
                        int score = Integer.parseInt(parts[1].trim());
                        playerScores.put(key, score);
                        playerOriginalNames.put(key, player);

                        if (score > highScore) {
                            highScore = score;
                            highScorePlayer = player;
                        }
                    } catch (NumberFormatException e) {
                        
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkAndUpdateHighScore(int score) {
        String key = currentPlayerName.toLowerCase();
        int personalBest = getPersonalBest(currentPlayerName);
        if (score > personalBest) {
            playerScores.put(key, score);
            playerOriginalNames.put(key, currentPlayerName);

            if (score > highScore) {
                highScore = score;
                highScorePlayer = currentPlayerName;
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter("highscore.txt"))) {
                for (Map.Entry<String, Integer> entry : playerScores.entrySet()) {
                    String origName = playerOriginalNames.getOrDefault(entry.getKey(), entry.getKey());
                    writer.println(origName + ":" + entry.getValue());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public int getPersonalBest(String playerName) {
        if (playerName == null)
            return 0;
        return playerScores.getOrDefault(playerName.toLowerCase(), 0);
    }

    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    public String getHighScorePlayer() {
        return highScorePlayer;
    }

    public int getHighScore() {
        return highScore;
    }

    
    public static void clearSavedData() {
        File nameFile = new File("player_name.txt");
        if (nameFile.exists()) {
            nameFile.delete();
        }
        File scoreFile = new File("highscore.txt");
        if (scoreFile.exists()) {
            scoreFile.delete();
        }
    }
}