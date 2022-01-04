/*
 * Copyright (c) Gradle, Inc.
 * Copyright (c) Octavia Togami <https://octyl.net>
 *
 * All Rights Reserved.
 */

package net.octyl.stellatedroller;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.math.vector.Vector2i;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.octyl.stellatedroller.MicroVG.beginPathAndFillWith;
import static net.octyl.stellatedroller.MicroVG.ezColorOpaque;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_HIDDEN;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_DONT_CARE;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_I;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SCALE_TO_MONITOR;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowContentScale;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetTime;
import static org.lwjgl.glfw.GLFW.glfwSetWindowContentScaleCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowMonitor;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_MIDDLE;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_TOP;
import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgCreateFontMem;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.nanovg.NanoVG.nvgFontFace;
import static org.lwjgl.nanovg.NanoVG.nvgFontSize;
import static org.lwjgl.nanovg.NanoVG.nvgText;
import static org.lwjgl.nanovg.NanoVG.nvgTextAlign;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_ANTIALIAS;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_DEBUG;
import static org.lwjgl.nanovg.NanoVGGL3.nvgCreate;
import static org.lwjgl.nanovg.NanoVGGL3.nvgDelete;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class StellatedRoller implements AutoCloseable {

    public static final String JB_MONO = "jb-mono";
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

    public static void main(String[] args) throws IOException {
        Configuration.DEBUG.set(true);

        System.err.println("Starting StellatedRoller");
        System.err.println("LWJGL Version: " + Version.getVersion());
        try (var roller = new StellatedRoller()) {
            roller.run();
        }
    }

    private final double gameFramesPerSeconds = 120;
    private final double gameFramesPerTick = 8;
    private final Roller roller = new Roller(11, 5, 0.45);
    private final GameFrameAdapter ticker = new GameFrameAdapter(roller::tick, gameFramesPerSeconds, gameFramesPerTick);

    private final long window;
    private final long vg;
    private LifecycledBuffer<ByteBuffer> fontData;
    private int framebufferWidth;
    private int framebufferHeight;

    private float contentScaleX;
    private float contentScaleY;

    public StellatedRoller() throws IOException {
        GLFWErrorCallback.createThrow().set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);

        window = glfwCreateWindow(DEFAULT_WIDTH, DEFAULT_HEIGHT, "StellatedRoller", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (mods != 0) {
                return;
            }
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
                return;
            }
            if (action == GLFW_RELEASE) {
                handleKeyRelease(key);
            }
        });

        var center = getWindowPosForCenter();
        glfwSetWindowPos(window, center.x(), center.y());

        glfwSetFramebufferSizeCallback(window, (handle, w, h) -> {
            framebufferWidth = w;
            framebufferHeight = h;
        });
        glfwSetWindowContentScaleCallback(window, (handle, xscale, yscale) -> {
            contentScaleX = xscale;
            contentScaleY = yscale;
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer fw = stack.mallocInt(1);
            IntBuffer fh = stack.mallocInt(1);
            FloatBuffer sx = stack.mallocFloat(1);
            FloatBuffer sy = stack.mallocFloat(1);

            glfwGetFramebufferSize(window, fw, fh);
            framebufferWidth = fw.get(0);
            framebufferHeight = fh.get(0);

            glfwGetWindowContentScale(window, sx, sy);
            contentScaleX = sx.get(0);
            contentScaleY = sy.get(0);
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glClearColor(0, 0, 0, 1);

        vg = nvgCreate(NVG_ANTIALIAS | NVG_DEBUG);
        if (vg == NULL) {
            throw new RuntimeException("Failed to create NanoVG");
        }

        initNanoVG();

        glfwSwapInterval(1);
        glfwShowWindow(window);
    }

    private Vector2i getWindowPosForCenter() {
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        if (videoMode == null) {
            throw new RuntimeException("Failed to get primary monitor video mode");
        }

        return new Vector2i(
            (videoMode.width() - DEFAULT_WIDTH) / 2,
            (videoMode.height() - DEFAULT_HEIGHT) / 2
        );
    }

    private void handleKeyRelease(int key) {
        switch (key) {
            case GLFW_KEY_O -> roller.toggleFeature(Roller.Feature.OUTER_CIRCLE);
            case GLFW_KEY_I -> roller.toggleFeature(Roller.Feature.INNER_CIRCLES);
            case GLFW_KEY_S -> roller.toggleFeature(Roller.Feature.STAR);
            case GLFW_KEY_A -> roller.toggleFeature(Roller.Feature.INTRA_POLY);
            case GLFW_KEY_E -> roller.toggleFeature(Roller.Feature.INTER_POLY);
            case GLFW_KEY_P -> roller.toggleFeature(Roller.Feature.POINTS);
            case GLFW_KEY_F -> roller.toggleFeature(Roller.Feature.FULL_SCREEN);
        }
    }

    private void initNanoVG() throws IOException {
        fontData = Res.loadIntoAllocBuffer("fonts/JetBrainsMono-Regular.ttf");
        int handle = nvgCreateFontMem(vg, JB_MONO, fontData.buffer(), 0);
        if (handle == -1) {
            throw new RuntimeException("Failed to load font");
        }
    }

    public void run() {
        glfwSetTime(0);
        double lastTime = glfwGetTime();
        boolean wasFullScreen = roller.isFeatureEnabled(Roller.Feature.FULL_SCREEN);
        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            Duration deltaTime = Duration.ofNanos(
                (long) (TimeUnit.SECONDS.toNanos(1) * (currentTime - lastTime))
            );
            lastTime = currentTime;

            ticker.onRenderFrame(deltaTime);

            glViewport(0, 0, framebufferWidth, framebufferHeight);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

            // Effective dimensions on hi-dpi devices.
            int width = (int) (framebufferWidth / contentScaleX);
            int height = (int) (framebufferHeight / contentScaleY);
            nvgBeginFrame(vg, width, height, Math.max(contentScaleX, contentScaleY));

            if (!wasFullScreen) {
                draw(width);
            }
            roller.draw(vg, width, height);

            nvgEndFrame(vg);

            glfwSwapBuffers(window);
            glfwPollEvents();

            if (!wasFullScreen && roller.isFeatureEnabled(Roller.Feature.FULL_SCREEN)) {
                var monitor = glfwGetPrimaryMonitor();
                if (monitor == NULL) {
                    throw new RuntimeException("Failed to get primary monitor");
                }
                var videoMode = glfwGetVideoMode(monitor);
                if (videoMode == null) {
                    throw new RuntimeException("Failed to get video mode");
                }
                glfwSetWindowMonitor(
                    window, monitor,
                    0, 0,
                    videoMode.width(), videoMode.height(),
                    videoMode.refreshRate()
                );
                // Hide the cursor when in full screen.
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
            } else if (wasFullScreen && !roller.isFeatureEnabled(Roller.Feature.FULL_SCREEN)) {
                var center = getWindowPosForCenter();
                glfwSetWindowMonitor(
                    window, NULL,
                    center.x(), center.y(),
                    DEFAULT_WIDTH, DEFAULT_HEIGHT,
                    GLFW_DONT_CARE
                );
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            }
            wasFullScreen = roller.isFeatureEnabled(Roller.Feature.FULL_SCREEN);
        }
    }

    private final NVGColor titleColor = ezColorOpaque(0x33_66_CC);

    private record HelpText(String key, String english, Roller.Feature feature) {
        public String toString(Roller roller) {
            var onOff = roller.isFeatureEnabled(feature) ? "ON" : "OFF";
            return "Press %s to toggle %s [%s]".formatted(key, english, onOff);
        }
    }

    private void draw(int width) {
        try (var ignored = stackPush()) {
            nvgFontSize(vg, 18.0f);
            nvgFontFace(vg, JB_MONO);
            nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_TOP);
            beginPathAndFillWith(vg, titleColor, () ->
                nvgText(
                    vg,
                    width / 2f,
                    8,
                    "Stellated Roller (FPS: %.02f)".formatted(ticker.getRenderFramesPerSecond())
                )
            );
            nvgFontSize(vg, 14.0f);
            nvgFontFace(vg, JB_MONO);
            nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_MIDDLE);
            var linesLeft = List.of(
                new HelpText("O", "[O]uter Circle", Roller.Feature.OUTER_CIRCLE),
                new HelpText("I", "[I]nner Circles", Roller.Feature.INNER_CIRCLES),
                new HelpText("S", "[S]tar", Roller.Feature.STAR)
            );
            renderLines(width / 4f, linesLeft);
            var linesRight = List.of(
                new HelpText("A", "Intr[a]-polygons", Roller.Feature.INTRA_POLY),
                new HelpText("E", "Int[e]r-polygons", Roller.Feature.INTER_POLY),
                new HelpText("P", "[P]oints", Roller.Feature.POINTS)
            );
            renderLines(3 * width / 4f, linesRight);
        }
    }

    private void renderLines(float x, List<HelpText> lines) {
        for (int line = 0; line < lines.size(); line++) {
            int finalLine = line;
            var text = lines.get(line);
            beginPathAndFillWith(vg, titleColor, () ->
                nvgText(
                    vg,
                    x,
                    40 + (finalLine * 20),
                    text.toString(roller)
                )
            );
        }
    }

    @Override
    public void close() {
        if (fontData != null) {
            fontData.close();
        }
        if (vg != NULL) {
            nvgDelete(vg);
        }
        GL.setCapabilities(null);
        if (window != NULL) {
            glfwFreeCallbacks(window);
        }
        glfwTerminate();
    }
}
