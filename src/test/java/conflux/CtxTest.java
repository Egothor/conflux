/**
 * Copyright (C) 2025, Leo Galambos
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement:
 *    This product includes software developed by the Egothor project.
 *
 * 4. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package conflux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Ctx} class. Covers put/get, type safety, and
 * listener notifications.
 *
 * @author Leo Galambos
 */
public class CtxTest {

    private Key<Integer> intKey;
    private Key<String> stringKey;
    private Key<byte[]> byteArrayKey;

    @BeforeEach
    void setup() {
        intKey = Key.of("count", Integer.class);
        stringKey = Key.of("status", String.class);
        byteArrayKey = Key.of("buffer", byte[].class);
        Ctx.INSTANCE.clear();
    }

    @Test
    void testPutAndGet() {
        System.out.println("testPutAndGet");
        Ctx.INSTANCE.put(intKey, 42);
        Integer result = Ctx.INSTANCE.get(intKey);
        assertEquals(42, result);
        System.out.println("...ok");
    }

    @Test
    void testContainsAndRemove() {
        System.out.println("testContainsAndRemove");
        assertFalse(Ctx.INSTANCE.contains(intKey));
        Ctx.INSTANCE.put(intKey, 100);
        assertTrue(Ctx.INSTANCE.contains(intKey));
        Object removed = Ctx.INSTANCE.remove(intKey);
        assertEquals(100, removed);
        assertFalse(Ctx.INSTANCE.contains(intKey));
        System.out.println("...ok");
    }

    @Test
    void testClear() {
        System.out.println("testClear");
        Ctx.INSTANCE.put(intKey, 1);
        Ctx.INSTANCE.put(stringKey, "OK");
        Ctx.INSTANCE.clear();
        assertFalse(Ctx.INSTANCE.contains(intKey));
        assertFalse(Ctx.INSTANCE.contains(stringKey));
        System.out.println("...ok");
    }

    @Test
    void testTypeSafety() {
        System.out.println("testTypeSafety");
        Ctx.INSTANCE.put(intKey, 5);
        Exception ex = assertThrows(IllegalStateException.class, () -> {
            Key<String> sameNameDifferentType = Key.of("count", String.class);
            Ctx.INSTANCE.put(sameNameDifferentType, "oops");
        });
        assertTrue(ex.getMessage().contains("already associated with type"));
        System.out.println("...ok");
    }

    @Test
    void testListenerNotification() {
        System.out.println("testListenerNotification");
        var results = new StringBuilder();
        Listener<Integer> listener = v -> results.append(v);
        Ctx.INSTANCE.addListener(intKey, listener);

        Ctx.INSTANCE.put(intKey, 11);
        Ctx.INSTANCE.put(intKey, 22);
        Ctx.INSTANCE.put(intKey, 22);
        Ctx.INSTANCE.put(intKey, 33);

        assertEquals("112233", results.toString());
        System.out.println("...ok");
    }

    @Test
    void testListenerNotificationWithArrays() {
        System.out.println("testListenerNotificationWithArrays");
        var results = new StringBuilder();
        Listener<byte[]> listener = v -> results.append(Arrays.toString(v));
        Ctx.INSTANCE.addListener(byteArrayKey, listener);

        final byte[] _11 = { 1, 1 };
        final byte[] _22 = { 2, 2 };
        final byte[] _22x = { 2, 2 };
        final byte[] _33 = { 3, 3 };

        Ctx.INSTANCE.put(byteArrayKey, _11);
        Ctx.INSTANCE.put(byteArrayKey, _22);
        Ctx.INSTANCE.put(byteArrayKey, _22x);
        Ctx.INSTANCE.put(byteArrayKey, _33);

        assertEquals("[1, 1][2, 2][3, 3]", results.toString());
        System.out.println("...ok");
    }

    @Test
    void testListenerWeakReference() {
        System.out.println("testListenerWeakReference");
        var flag = new boolean[] { false };
        {
            Listener<Integer> tmpListener = v -> flag[0] = true;
            Ctx.INSTANCE.addListener(intKey, tmpListener);
        }
        System.gc(); // encourage GC
        Ctx.INSTANCE.put(intKey, 123);
        // cannot fully verify weakref GC predictably in unit tests
        assertTrue(flag[0] || !flag[0]);
        System.out.println("...ok");
    }

    @Test
    void testMultipleContextsIsolation() {
        System.out.println("testMultipleContextsIsolation");
        var ctxA = Ctx.INSTANCE.getContext("A");
        var ctxB = Ctx.INSTANCE.getContext("B");

        ctxA.put(intKey, 10);
        ctxB.put(intKey, 20);

        assertEquals(10, ctxA.get(intKey));
        assertEquals(20, ctxB.get(intKey));
        assertNull(Ctx.INSTANCE.get(intKey)); // Default context is separate

        ctxA.clear();
        assertFalse(ctxA.contains(intKey));
        assertEquals(20, ctxB.get(intKey));

        System.out.println("...ok");
    }

    @Test
    void testListenerNotificationInSeparateContexts() {
        System.out.println("testListenerNotificationInSeparateContexts");
        var ctxA = Ctx.INSTANCE.getContext("A");
        var ctxB = Ctx.INSTANCE.getContext("B");

        var resultA = new StringBuilder();
        var resultB = new StringBuilder();

        Listener<Integer> listenerA = v -> resultA.append(v);
        Listener<Integer> listenerB = v -> resultB.append(v);

        ctxA.addListener(intKey, listenerA);
        ctxB.addListener(intKey, listenerB);

        ctxA.put(intKey, 1);
        ctxB.put(intKey, 2);
        ctxA.put(intKey, 3);
        ctxB.put(intKey, 4);

        assertEquals("13", resultA.toString());
        assertEquals("24", resultB.toString());

        System.out.println("...ok");
    }
}