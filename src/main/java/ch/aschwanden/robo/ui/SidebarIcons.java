package ch.aschwanden.robo.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/** Canvas-based sidebar icons, drawn at call-time with the requested color. */
final class SidebarIcons {

    static final Color ICON_DEFAULT = Color.web("#888888");
    static final Color ICON_ACTIVE  = Color.web("#e05555");
    private static final Color MOUSE_BODY = Color.web("#aaaaaa");
    private static final Color MOUSE_FILL = Color.web("#3a3a3a");

    private SidebarIcons() {}

    /**
     * Front-view mouse icon.
     * active=true  → left button red (active section)
     * active=false → both buttons grey (inactive)
     */
    static Canvas mouseIcon(double sz, boolean active) {
        Canvas c = new Canvas(sz, sz);
        GraphicsContext gc = c.getGraphicsContext2D();

        double bodyW  = sz * 0.60;
        double bodyH  = sz * 0.78;
        double bx     = (sz - bodyW) / 2;
        double by     = (sz - bodyH) / 2;
        double arcR   = bodyW * 0.48;
        double splitY = by + bodyH * 0.40;
        double midX   = bx + bodyW / 2;

        gc.setFill(MOUSE_FILL);
        gc.fillRoundRect(bx, by, bodyW, bodyH, arcR, arcR);

        // Left button area
        gc.setFill(active ? ICON_ACTIVE : Color.web("#555555"));
        gc.save();
        gc.beginPath();
        gc.moveTo(midX, by);
        gc.lineTo(bx + arcR / 2, by);
        gc.quadraticCurveTo(bx, by, bx, by + arcR / 2);
        gc.lineTo(bx, splitY);
        gc.lineTo(midX, splitY);
        gc.closePath();
        gc.fill();
        gc.restore();

        // Right button area
        gc.setFill(Color.web("#4a4a4a"));
        gc.save();
        gc.beginPath();
        gc.moveTo(midX, by);
        gc.lineTo(bx + bodyW - arcR / 2, by);
        gc.quadraticCurveTo(bx + bodyW, by, bx + bodyW, by + arcR / 2);
        gc.lineTo(bx + bodyW, splitY);
        gc.lineTo(midX, splitY);
        gc.closePath();
        gc.fill();
        gc.restore();

        // Outline
        Color strokeColor = active ? ICON_ACTIVE : MOUSE_BODY;
        gc.setStroke(strokeColor);
        gc.setLineWidth(1.4);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.strokeRoundRect(bx, by, bodyW, bodyH, arcR, arcR);
        gc.strokeLine(midX, by, midX, splitY);
        gc.strokeLine(bx, splitY, bx + bodyW, splitY);

        // Scroll wheel
        double wR  = bodyW * 0.11;
        double wCy = by + (splitY - by) / 2;
        gc.setFill(strokeColor);
        gc.fillOval(midX - wR, wCy - wR, wR * 2, wR * 2);

        return c;
    }

    /** Gear icon (8 teeth). */
    static Canvas gearIcon(double sz, Color color) {
        Canvas c = new Canvas(sz, sz);
        GraphicsContext gc = c.getGraphicsContext2D();
        gc.setFill(color);

        double cx     = sz / 2;
        double cy     = sz / 2;
        double outerR = sz * 0.40;
        double innerR = sz * 0.24;
        double holeR  = sz * 0.13;
        int    teeth  = 8;
        double toothW = Math.PI / teeth * 0.55;

        double[] xs = new double[teeth * 4];
        double[] ys = new double[teeth * 4];
        for (int i = 0; i < teeth; i++) {
            double base = 2 * Math.PI * i / teeth;
            double a0 = base - toothW, a1 = base, a2 = base + toothW, a3 = base + 2 * Math.PI / teeth - toothW;
            xs[i*4]   = cx + innerR * Math.cos(a0); ys[i*4]   = cy + innerR * Math.sin(a0);
            xs[i*4+1] = cx + outerR * Math.cos(a1); ys[i*4+1] = cy + outerR * Math.sin(a1);
            xs[i*4+2] = cx + outerR * Math.cos(a2); ys[i*4+2] = cy + outerR * Math.sin(a2);
            xs[i*4+3] = cx + innerR * Math.cos(a3); ys[i*4+3] = cy + innerR * Math.sin(a3);
        }
        gc.fillPolygon(xs, ys, teeth * 4);
        gc.setFill(Color.web("#2a2a2a"));
        gc.fillOval(cx - holeR, cy - holeR, holeR * 2, holeR * 2);
        return c;
    }

    /** Browser window icon (title bar + two tabs + address bar). */
    static Canvas browserIcon(double sz, Color color) {
        Canvas c = new Canvas(sz, sz);
        GraphicsContext gc = c.getGraphicsContext2D();
        gc.setStroke(color);
        gc.setLineWidth(1.5);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        double pad  = sz * 0.10;
        double w    = sz - 2 * pad;
        double h    = sz - 2 * pad;
        double barH = h * 0.28;

        gc.strokeRoundRect(pad, pad, w, h, 3.0, 3.0);
        gc.strokeLine(pad, pad + barH, pad + w, pad + barH);

        double tabW = w * 0.28;
        double tabH = barH * 0.60;
        double tabY = pad + barH - tabH;
        gc.setFill(color);
        gc.fillRoundRect(pad + w * 0.06, tabY, tabW, tabH, 2, 2);
        gc.strokeRoundRect(pad + w * 0.38, tabY, tabW, tabH, 2, 2);

        double addrY = pad + barH + h * 0.12;
        gc.strokeRoundRect(pad + w * 0.08, addrY, w * 0.84, h * 0.15, 2, 2);
        return c;
    }
}
