import org.apache.commons.math3.linear.RealMatrix;

import java.io.Serializable;

public class SentTables implements Serializable {
    RealMatrix p,c,x,y;
    int start,end, currentI;

    public SentTables(RealMatrix p, RealMatrix c, RealMatrix x, RealMatrix y,int start,int end,int i) {
        this.p = p;
        this.c = c;
        this.x = x;
        this.y = y;
        this.start=start;
        this.end=end;
        this.currentI=i;
    }

    public SentTables(RealMatrix x, RealMatrix y,int start,int end) {
        this.x = x;
        this.y = y;
        this.start=start;
        this.end=end;
    }

    public RealMatrix getP() {
        return p;
    }

    public void setP(RealMatrix p) {
        this.p = p;
    }

    public RealMatrix getC() {
        return c;
    }

    public void setC(RealMatrix c) {
        this.c = c;
    }

    public RealMatrix getX() {
        return x;
    }

    public void setX(RealMatrix x) {
        this.x = x;
    }

    public RealMatrix getY() {
        return y;
    }

    public void setY(RealMatrix y) {
        this.y = y;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getCurrentI() {
        return currentI;
    }

    public void setCurrentI(int i) {
        this.currentI = i;
    }
}
