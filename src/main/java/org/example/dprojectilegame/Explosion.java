package org.example.dprojectilegame;

public final class Explosion {

    public static final double TOTAL_DURATION_SEC = 1.5;
    private static final double PHASE1_TOTAL_SEC = 0.865;
    private static final double PHASE2_TOTAL_SEC = 0.465;
    private static final double PHASE3_TOTAL_SEC = 0.17;

    private static final int FRAME_COUNT = 13;

    private static final double[] FRAME_DURATIONS_SEC = buildFrameDurations();

    private static double[] buildFrameDurations() {
        double[] d = new double[FRAME_COUNT];
        double p1 = PHASE1_TOTAL_SEC / 5.0;
        double p2 = PHASE2_TOTAL_SEC / 5.0;
        double p3 = PHASE3_TOTAL_SEC / 3.0;
        for (int i = 0; i < 5; i++) {
            d[i] = p1;
        }
        for (int i = 5; i < 10; i++) {
            d[i] = p2;
        }
        for (int i = 10; i < 13; i++) {
            d[i] = p3;
        }
        return d;
    }
    private final double anchorX;
    private final double anchorY;
    private final double displayScaleRadiusPx;

    private int frameIndex;
    private double timer;

    public Explosion(double anchorX, double anchorY, double displayScaleRadiusPx) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.displayScaleRadiusPx = displayScaleRadiusPx;
        this.frameIndex = 0;
        this.timer = 0.0;
    }

    public void update(double deltaTime) {
        if (frameIndex >= FRAME_COUNT) {
            return;
        }
        timer += deltaTime;
        // Subtract per-frame duration so large dt advances multiple frames correctly (equivalent to reset-to-0 when dt is small).
        while (frameIndex < FRAME_COUNT && timer >= FRAME_DURATIONS_SEC[frameIndex]) {
            timer -= FRAME_DURATIONS_SEC[frameIndex];
            frameIndex++;
        }
    }

    public int getFrameIndex() {
        return Math.min(frameIndex, FRAME_COUNT - 1);
    }

    public boolean isFinished() {
        return frameIndex >= FRAME_COUNT;
    }

    public double getAnchorX() {
        return anchorX;
    }

    public double getAnchorY() {
        return anchorY;
    }

    public double getDisplayScaleRadiusPx() {
        return displayScaleRadiusPx;
    }

    public double getCurrentFrameDurationSec() {
        if (frameIndex >= FRAME_COUNT) {
            return 0;
        }
        return FRAME_DURATIONS_SEC[frameIndex];
    }

    public double getTimer() {
        return timer;
    }

    public static int getFrameCount() {
        return FRAME_COUNT;
    }
}
