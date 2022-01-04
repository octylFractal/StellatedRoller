/*
 * Copyright (c) Gradle, Inc.
 * Copyright (c) Octavia Togami <https://octyl.net>
 *
 * All Rights Reserved.
 */

package net.octyl.stellatedroller;

import java.time.Duration;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class GameFrameAdapter {
    private final Runnable tick;
    private final double gameFramesPerSeconds;
    private final double gameFramesPerTick;
    private final TreeSet<Double> renderFrameTimes = new TreeSet<>();
    private double rollingGameFrames;

    public GameFrameAdapter(Runnable tick, double gameFramesPerSeconds, double ticksPerGameFrames) {
        this.tick = tick;
        this.gameFramesPerSeconds = gameFramesPerSeconds;
        this.gameFramesPerTick = ticksPerGameFrames;
    }

    public double getRenderFramesPerSecond() {
        double nanoseconds = renderFrameTimes.isEmpty() ? 1 : (renderFrameTimes.last() - renderFrameTimes.first());
        return (renderFrameTimes.size() / nanoseconds) * TimeUnit.SECONDS.toNanos(1);
    }

    public void onRenderFrame(Duration delta) {
        updateRenderFramesPerSecond(delta);
        runGameFrames(delta);
    }

    private void runGameFrames(Duration delta) {
        rollingGameFrames += (delta.toNanos() * gameFramesPerSeconds) / TimeUnit.SECONDS.toNanos(1);
        while (rollingGameFrames >= gameFramesPerTick) {
            rollingGameFrames -= gameFramesPerTick;
            tick.run();
        }
    }

    private void updateRenderFramesPerSecond(Duration delta) {
        var newTotalTime = (renderFrameTimes.isEmpty() ? 0 : renderFrameTimes.last()) + delta.toNanos();
        renderFrameTimes.add(newTotalTime);
        // Remove everything less then a second ago
        renderFrameTimes.headSet(newTotalTime - TimeUnit.SECONDS.toNanos(1)).clear();
    }
}
