package org.example.dprojectilegame;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Loads fire sprites, spawns {@link Explosion} instances at impact, updates frame timers, renders above world.
 */
public final class ExplosionManager {

    private static final String GRAPHICS_DIR = "/Users/shakinatoussaint/Downloads/Graphics/";

    private final List<Explosion> explosions = new ArrayList<>();
    private final Image[] fireFrames = new Image[Explosion.getFrameCount()];

    public ExplosionManager() {
        loadFireFrames();
    }

    private void loadFireFrames() {
        for (int i = 0; i < fireFrames.length; i++) {
            String path = GRAPHICS_DIR + "Fire" + (i + 1) + ".png";
            try {
                File f = new File(path);
                if (f.exists()) {
                    fireFrames[i] = new Image(f.toURI().toString());
                }
            } catch (Exception e) {
                System.err.println("Explosion frame load failed: " + path + " — " + e.getMessage());
            }
        }
    }

    public void spawn(double anchorX, double anchorY, double radiusPx) {
        explosions.add(new Explosion(anchorX, anchorY, radiusPx));
    }

    public void clear() {
        explosions.clear();
    }

    public void update(double deltaTime) {
        Iterator<Explosion> it = explosions.iterator();
        while (it.hasNext()) {
            Explosion e = it.next();
            e.update(deltaTime);
            if (e.isFinished()) {
                it.remove();
            }
        }
    }

    public void render(GraphicsContext ctx) {
        for (Explosion e : explosions) {
            if (e.isFinished()) {
                continue;
            }
            int fi = e.getFrameIndex();
            Image img = fireFrames[fi];
            double ax = e.getAnchorX();
            double ay = e.getAnchorY();
            double r = Math.max(24.0, e.getDisplayScaleRadiusPx());

            if (img != null && !img.isError()) {
                double iw = img.getWidth();
                double ih = img.getHeight();
                if (iw > 0 && ih > 0) {
                    double maxDim = Math.max(iw, ih);
                    double scale = (2.0 * r) / maxDim;
                    double dw = iw * scale;
                    double dh = ih * scale;
                    double dx = ax - dw / 2.0;
                    double dy = ay - dh;
                    ctx.drawImage(img, dx, dy, dw, dh);
                    continue;
                }
            }
            ctx.save();
            ctx.setGlobalAlpha(0.65);
            ctx.setFill(Color.ORANGE);
            ctx.fillOval(ax - r, ay - 2 * r, 2 * r, 2 * r);
            ctx.restore();
        }
    }
}
