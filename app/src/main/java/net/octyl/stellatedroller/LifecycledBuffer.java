/*
 * Copyright (c) Gradle, Inc.
 * Copyright (c) Octavia Togami <https://octyl.net>
 *
 * All Rights Reserved.
 */

package net.octyl.stellatedroller;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memCopy;
import static org.lwjgl.system.MemoryUtil.memFree;

public sealed interface LifecycledBuffer<B extends Buffer> extends AutoCloseable {

    private static boolean isMinimized(Buffer buffer) {
        return buffer.position() == 0 && buffer.remaining() == buffer.capacity();
    }

    record Byte(ByteBuffer buffer) implements LifecycledBuffer<ByteBuffer> {
        @Override
        public Byte minimize() {
            if (isMinimized(buffer)) {
                return this;
            }
            var minimized = memAlloc(buffer.remaining());
            memCopy(buffer, minimized);
            return new Byte(minimized);
        }
    }

    B buffer();

    /**
     * Minimize the memory usage of this buffer.
     *
     * @return a buffer that has a capacity equal to the {@linkplain Buffer#remaining() remaining count}
     */
    LifecycledBuffer<B> minimize();

    @Override
    default void close() {
        memFree(buffer());
    }
}
