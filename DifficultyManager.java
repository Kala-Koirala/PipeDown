public class DifficultyManager {
    private final int baseVelocityX = -4;
    private final int baseOpeningSpace = 160;
    private final int baseSpawnInterval = 1500;

    public DifficultyManager() {
    }

    public int getVelocityX(double score) {
        int speedStep = (int) (score / 5);
        int additionalSpeed = speedStep; // 1 unit additional speed per step
        int currentVelocity = baseVelocityX - additionalSpeed;
        return Math.max(currentVelocity, -8);
    }


    public int getOpeningSpace(double score) {
        int gapStep = (int) (score / 3);
        int reduction = gapStep * 5;
        int currentGap = baseOpeningSpace - reduction;
        return Math.max(currentGap, 110);
    }

    public int getSpawnInterval(double score) {
        int intervalStep = (int) (score / 5);
        int reduction = intervalStep * 100;
        int currentInterval = baseSpawnInterval - reduction;
        return Math.max(currentInterval, 1000);
    }
}
