package org.example.dprojectilegame;


public class Player {
    private String name;
    private Tank tank;
    private int consecutiveHits;
    private static final int HITS_FOR_NUKE = 5;
    
    public Player(String name, Tank tank) {
        this.name = name;
        this.tank = tank;
        this.consecutiveHits = 0;
    }
    
    public Tank getTank() {
        return tank;
    }
    
    public String getName() {
        return name;
    }
    

    public boolean recordHit() {
        consecutiveHits++;
        if (consecutiveHits >= HITS_FOR_NUKE) {
            consecutiveHits = 0; // Reset after nuke
            return true; // Nuke available
        }
        return false;
    }
    public void resetConsecutiveHits() {
        consecutiveHits = 0;
    }
    public int getConsecutiveHits() {
        return consecutiveHits;
    }
    public boolean hasNukeAvailable() {
        return consecutiveHits >= HITS_FOR_NUKE;
    }

}
