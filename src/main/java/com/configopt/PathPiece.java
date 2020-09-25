package com.configopt;

public class PathPiece {
    String p;

    public PathPiece(String p) {
        this.p = p;
    }

    @Override
    public String toString() {
        return this.p;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PathPiece))
            return false;
        return o.toString().equals(this.toString()) || this.toString().startsWith("{")
                && this.toString().endsWith("}") || o.toString().startsWith("{") && o.toString().endsWith("}");
    }
}