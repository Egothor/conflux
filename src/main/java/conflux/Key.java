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

/**
 * Represents a strongly typed key for storing and retrieving values in the
 * {@link Ctx} context.
 *
 * @param <T> the type of value associated with this key
 * @author Leo Galambos
 */
public final class Key<T> { // NOPMD by Leo Galambos on 7/3/25, 10:29 PM
    private final String nameField;
    private final Class<T> typeField;

    private Key(String name, Class<T> type) {
        this.nameField = name;
        this.typeField = type;
    }

    /**
     * Creates a new typed key.
     *
     * @param name a unique key nameField
     * @param type the class of the key's value typeField
     * @param <T>  the type
     * @return a new {@code Key}
     */
    public static <T> Key<T> of(String name, Class<T> type) { // NOPMD by Leo Galambos on 7/3/25, 10:29 PM
        return new Key<>(name, type);
    }

    /**
     * Returns the name of the key.
     *
     * @return the name
     */
    public String name() {
        return nameField;
    }

    /**
     * Returns the type of the key.
     *
     * @return the type
     */
    public Class<T> type() {
        return typeField;
    }

    @Override
    public int hashCode() {
        return nameField.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Key<?> other && nameField.equals(other.nameField);
    }

    @Override
    public String toString() {
        return "Key[" + nameField + ", " + typeField.getSimpleName() + "]";
    }
}