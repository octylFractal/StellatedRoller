/*
 * Copyright (c) Gradle, Inc.
 * Copyright (c) Octavia Togami <https://octyl.net>
 *
 * All Rights Reserved.
 */

package net.octyl.stellatedroller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memRealloc;

public class Res {
    public static LifecycledBuffer.Byte loadIntoAllocBuffer(String name) throws IOException {
        ByteBuffer buffer = memAlloc(4096);
        try (var stream = get(name)) {
            var channel = Channels.newChannel(new BufferedInputStream(stream));
            while (true) {
                // Read pos -> limit
                var read = channel.read(buffer);
                // After, pos has been moved towards limit, no adjustments needed
                if (read == -1) {
                    // If we're at the end, flip the buffer to finish it out
                    buffer.flip();
                    break;
                }
                if (buffer.remaining() == 0) {
                    // Realloc handles keeping the pos and moving the limit
                    buffer = memRealloc(buffer, (3 * buffer.capacity()) / 2);
                }
            }
        }
        return new LifecycledBuffer.Byte(buffer);
    }

    public static InputStream get(String name) {
        return Res.class.getResourceAsStream("/" + name);
    }

    private Res() {
    }
}
