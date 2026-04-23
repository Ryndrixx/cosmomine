package com.ryndrixx.cosmomine;

public enum ShapeMode {
    VEIN("Vein", "Flood-fills all connected blocks of the same type"),
    TUNNEL_1x2("Tunnel 1x2", "1 wide x 2 tall tunnel in your facing direction"),
    TUNNEL_3x3("Tunnel 3x3", "3x3 tunnel in your facing direction"),
    FLAT_3x3("Flat 3x3", "3x3 plane perpendicular to your facing direction"),
    STAIR_DOWN("Mine Staircase", "Descending staircase in your facing direction"),
    STAIR_UP("Escape Tunnel", "Ascending staircase in your facing direction");

    public final String displayName;
    public final String description;

    ShapeMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public ShapeMode next() {
        ShapeMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public ShapeMode previous() {
        ShapeMode[] values = values();
        return values[(ordinal() - 1 + values.length) % values.length];
    }
}
