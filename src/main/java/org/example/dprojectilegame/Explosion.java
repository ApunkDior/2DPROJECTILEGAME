package org.example.dprojectilegame;

public final class Explosion {
    //I wanted the explosion to be 1.5 seconds which I divided in 3 phases
    private static final double PHASE1_TOTAL_SEC = 0.865;
    private static final double PHASE2_TOTAL_SEC = 0.465;
    private static final double PHASE3_TOTAL_SEC = 0.17;
    //I used 13 picture in a files which created with chatGPT and editied with Photoshop
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
    //Stores the explosion's X Position
    private final double anchorX;
    //Stores the explosion's Y Position
    private final double anchorY;
    //Stores how large the explosion should be displayed
    private final double displayScaleRadiusPx;
    //Track frame the animation is currently on
    private int frameIndex;
    //Stores how much time has passed inside the current frames
    private double timer;

    //Contructor
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

    public static int getFrameCount() {
        return FRAME_COUNT;
    }
}
