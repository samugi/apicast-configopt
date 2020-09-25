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

    public boolean equalsExceptParameter(PathPiece pp) {
        if (pp.isParameter())
            return false;
        if (this.isParameter())
            return true;
        return this.toString().equals(pp.toString());
    }

    public boolean isParameter() {
        return this.p.startsWith("{") && this.p.endsWith("}");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PathPiece))
            return false;
        return o.toString().equals(this.toString()) || this.isParameter() || ((PathPiece) o).isParameter();
    }
}