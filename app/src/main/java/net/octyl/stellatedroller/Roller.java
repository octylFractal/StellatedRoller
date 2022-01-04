/*
 * Copyright (c) Gradle, Inc.
 * Copyright (c) Octavia Togami <https://octyl.net>
 *
 * All Rights Reserved.
 */

package net.octyl.stellatedroller;

import org.lwjgl.nanovg.NVGColor;
import org.spongepowered.math.vector.Vector2f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static net.octyl.stellatedroller.MicroVG.beginPathAndFillWith;
import static net.octyl.stellatedroller.MicroVG.beginPathAndStrokeWith;
import static net.octyl.stellatedroller.MicroVG.ezColorOpaque;
import static net.octyl.stellatedroller.MiddleSchoolMath.createFlippedDirectionRad;
import static net.octyl.stellatedroller.StellatedRoller.JB_MONO;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_MIDDLE;
import static org.lwjgl.nanovg.NanoVG.NVG_CW;
import static org.lwjgl.nanovg.NanoVG.nvgArc;
import static org.lwjgl.nanovg.NanoVG.nvgBeginPath;
import static org.lwjgl.nanovg.NanoVG.nvgClosePath;
import static org.lwjgl.nanovg.NanoVG.nvgFill;
import static org.lwjgl.nanovg.NanoVG.nvgFillColor;
import static org.lwjgl.nanovg.NanoVG.nvgFontFace;
import static org.lwjgl.nanovg.NanoVG.nvgFontSize;
import static org.lwjgl.nanovg.NanoVG.nvgLineTo;
import static org.lwjgl.nanovg.NanoVG.nvgMoveTo;
import static org.lwjgl.nanovg.NanoVG.nvgRestore;
import static org.lwjgl.nanovg.NanoVG.nvgSave;
import static org.lwjgl.nanovg.NanoVG.nvgScale;
import static org.lwjgl.nanovg.NanoVG.nvgStroke;
import static org.lwjgl.nanovg.NanoVG.nvgStrokeColor;
import static org.lwjgl.nanovg.NanoVG.nvgStrokeWidth;
import static org.lwjgl.nanovg.NanoVG.nvgText;
import static org.lwjgl.nanovg.NanoVG.nvgTextAlign;
import static org.lwjgl.nanovg.NanoVG.nvgTranslate;

public class Roller {
    public enum Feature {
        FULL_SCREEN,
        OUTER_CIRCLE,
        INNER_CIRCLES,
        STAR,
        INTRA_POLY,
        INTER_POLY,
        POINTS,
    }

    // How many "tips" on the "star"?
    private final int vertices;
    // How many points does each circle get?
    private final int pointsPerCircle;
    // How far out is each point placed?
    private final double pointDistancePercentage;

    private final double outerRadius = 1;
    private final double innerRadius;
    private final double innerVelocity;
    private final int repeats;

    private final EnumSet<Feature> features = EnumSet.allOf(Feature.class);
    {
        features.remove(Feature.FULL_SCREEN);
    }
    private double progress;

    public Roller(int vertices, int pointsPerCircle, double pointDistancePercentage) {
        this.vertices = vertices;
        this.pointsPerCircle = pointsPerCircle;
        this.pointDistancePercentage = pointDistancePercentage;

        this.innerRadius = pointsPerCircle / (double) vertices;
        this.innerVelocity = 1.0 / innerRadius - 1;
        this.repeats = this.vertices - this.pointsPerCircle;
    }

    public void toggleFeature(Feature feature) {
        if (features.contains(feature)) {
            features.remove(feature);
        } else {
            features.add(feature);
        }
    }

    public boolean isFeatureEnabled(Feature feature) {
        return features.contains(feature);
    }

    public void tick() {
        progress = (progress + 0.01) % pointsPerCircle;
    }

    private final NVGColor errorColor = ezColorOpaque(0xFF0000);

    public void draw(long vg, int width, int height) {
        if (repeats < 0) {
            nvgFontSize(vg, 16.0f);
            nvgFontFace(vg, JB_MONO);
            nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_MIDDLE);
            beginPathAndFillWith(vg, errorColor, () ->
                nvgText(
                    vg,
                    width / 2f,
                    height / 2f,
                    "Invalid configuration, try using more vertices or fewer points per circle."
                )
            );
            return;
        }
        nvgSave(vg);
        // Scale so this logic is independent of the screen size

        int min = Math.min(width, height);
        int max = Math.max(width, height);

        // Offset as needed
        if (min == width) {
            nvgTranslate(vg, 0, (max - min) / 2f);
        } else {
            nvgTranslate(vg, (max - min) / 2f, 0);
        }
        // Full screen feature scales the outer circle to (nearly) the whole screen, and moves it to the full center.
        boolean fullScreen = isFeatureEnabled(Feature.FULL_SCREEN);
        float scaleTarget = fullScreen ? (1f + (16f / min)) : 1.5f;
        float scale = min / (scaleTarget * 2);
        nvgScale(vg, scale, scale);
        nvgTranslate(vg, scaleTarget, scaleTarget + (fullScreen ? 0 : 0.25f));
        nvgStrokeWidth(vg, 2 / scale);

        if (isFeatureEnabled(Feature.OUTER_CIRCLE)) {
            drawOuterCircle(vg);
        }
        if (isFeatureEnabled(Feature.STAR)) {
            drawStar(vg);
        }

        var circles = generateCircles();
        if (isFeatureEnabled(Feature.INNER_CIRCLES)) {
            drawInnerCircles(vg, circles);
        }
        nvgStrokeWidth(vg, 4 / scale);
        if (isFeatureEnabled(Feature.INTRA_POLY)) {
            drawIntraPolygons(vg, circles);
        }
        if (isFeatureEnabled(Feature.INTER_POLY)) {
            drawInterPolygons(vg, circles);
        }
        if (isFeatureEnabled(Feature.POINTS)) {
            drawPoints(vg, circles);
        }

        nvgRestore(vg);
    }

    private final NVGColor outerCircleColor = ezColorOpaque(0xFF0000);

    private void drawOuterCircle(long vg) {
        beginPathAndStrokeWith(vg, outerCircleColor, () ->
            nvgArc(vg, 0, 0, (float) outerRadius, 0, (float) (2 * Math.PI), NVG_CW)
        );
    }

    private final NVGColor starColor = ezColorOpaque(0x00FF00);

    private void drawStar(long vg) {
        nvgStrokeColor(vg, starColor);
        for (int i = 0; i < vertices; i++) {
            double startAngle = (2 * Math.PI * i) / vertices;
            nvgBeginPath(vg);
            var start = createFlippedDirectionRad(startAngle)
                .mul(outerRadius - (1 - pointDistancePercentage) * innerRadius)
                .toFloat();
            nvgMoveTo(vg, start.x(), start.y());
            int steps = 1_000;
            for (int j = 0; j < steps; j++) {
                double angleAdjust = 2 * Math.PI * innerRadius * (j / (double) steps);
                var angle = startAngle + angleAdjust;
                var innerAngle = startAngle - angleAdjust * innerVelocity;
                double dRadius = outerRadius - innerRadius;
                double pRadius = pointDistancePercentage * innerRadius;
                var nextPoint = createFlippedDirectionRad(angle)
                    .mul(dRadius)
                    .add(createFlippedDirectionRad(innerAngle).mul(pRadius))
                    .toFloat();
                nvgLineTo(vg, nextPoint.x(), nextPoint.y());
            }
            nvgStroke(vg);
        }
    }

    private record Circle(
        Vector2f center,
        List<Vector2f> points
    ) {
    }

    private List<Circle> generateCircles() {
        var list = new ArrayList<Circle>(repeats);
        for (int i = 0; i < repeats; i++) {
            var angle = 2 * Math.PI * (progress + i / (double) repeats);
            var center = createFlippedDirectionRad(angle)
                .mul(outerRadius - innerRadius);

            var points = new ArrayList<Vector2f>(pointsPerCircle);
            var innerAngle = 2 * Math.PI * -progress * innerVelocity;
            for (int j = 0; j < pointsPerCircle; j++) {
                points.add(
                    center.add(createFlippedDirectionRad(innerAngle + 2 * Math.PI * j / (double) pointsPerCircle)
                            .mul(pointDistancePercentage * innerRadius))
                        .toFloat()
                );
            }
            list.add(new Circle(center.toFloat(), points));
        }
        return list;
    }

    private final NVGColor innerCircleColor = ezColorOpaque(0x12E51A);

    private void drawInnerCircles(long vg, List<Circle> circles) {
        nvgStrokeColor(vg, innerCircleColor);
        for (var circle : circles) {
            nvgBeginPath(vg);
            nvgArc(vg, circle.center.x(), circle.center.y(), (float) innerRadius, 0, (float) (2 * Math.PI), NVG_CW);
            nvgStroke(vg);
        }
    }

    private final NVGColor intraPolyColor = ezColorOpaque(0x112299);

    private void drawIntraPolygons(long vg, List<Circle> circles) {
        nvgStrokeColor(vg, intraPolyColor);
        for (var circle : circles) {
            nvgBeginPath(vg);
            var firstPoint = circle.points.get(0);
            nvgMoveTo(vg, firstPoint.x(), firstPoint.y());
            for (int i = 1; i < circle.points.size(); i++) {
                var nextPoint = circle.points.get(i);
                nvgLineTo(vg, nextPoint.x(), nextPoint.y());
            }
            nvgClosePath(vg);
            nvgStroke(vg);
        }
    }

    private final NVGColor interPolyColor = ezColorOpaque(0xFB008A);

    private void drawInterPolygons(long vg, List<Circle> circles) {
        nvgStrokeColor(vg, interPolyColor);
        for (int i = 0; i < pointsPerCircle; i++) {
            nvgBeginPath(vg);
            var firstPoint = circles.get(0).points.get(i);
            nvgMoveTo(vg, firstPoint.x(), firstPoint.y());
            for (int j = 1; j < circles.size(); j++) {
                var nextPoint = circles.get(j).points.get(i);
                nvgLineTo(vg, nextPoint.x(), nextPoint.y());
            }
            nvgClosePath(vg);
            nvgStroke(vg);
        }
    }

    private final NVGColor pointColor = ezColorOpaque(0xFFFFFF);

    private void drawPoints(long vg, List<Circle> circles) {
        nvgFillColor(vg, pointColor);
        for (var circle : circles) {
            for (var point : circle.points) {
                nvgBeginPath(vg);
                nvgArc(vg, point.x(), point.y(), (float) 0.025, 0, (float) (2 * Math.PI), NVG_CW);
                nvgFill(vg);
            }
        }
    }
}
