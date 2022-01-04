package net.octyl.stellatedroller;

import org.lwjgl.nanovg.NVGColor;

import static org.lwjgl.nanovg.NanoVG.nvgBeginPath;
import static org.lwjgl.nanovg.NanoVG.nvgFill;
import static org.lwjgl.nanovg.NanoVG.nvgFillColor;
import static org.lwjgl.nanovg.NanoVG.nvgStroke;
import static org.lwjgl.nanovg.NanoVG.nvgStrokeColor;

public class MicroVG {
    public static NVGColor ezColorOpaque(int rgb) {
        int red = (rgb >>> 16) & 0xFF;
        int green = (rgb >>> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return ezColor(red, green, blue, 0xFF);
    }

    public static NVGColor ezColor(int r, int g, int b, int a) {
        return NVGColor.create().r(r / 255f).g(g / 255f).b(b / 255f).a(a / 255f);
    }

    public static void beginPathAndFillWith(long vg, NVGColor color, Runnable path) {
        nvgFillColor(vg, color);
        nvgBeginPath(vg);
        path.run();
        nvgFill(vg);
    }

    public static void beginPathAndStrokeWith(long vg, NVGColor color, Runnable path) {
        nvgStrokeColor(vg, color);
        nvgBeginPath(vg);
        path.run();
        nvgStroke(vg);
    }
}
