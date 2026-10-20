/*
 *  Copyright 2026 N_uraad.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.classlib.java.nio.file.attribute;

import java.util.EnumSet;
import java.util.Set;

public final class TPosixFilePermissions {
    private TPosixFilePermissions() {
    }

    private static void writeBits(StringBuilder sb, boolean r, boolean w, boolean x) {
        sb.append(r ? 'r' : '-').append(w ? 'w' : '-').append(x ? 'x' : '-');
    }

    public static String toString(Set<TPosixFilePermission> perms) {
        StringBuilder sb = new StringBuilder(9);
        writeBits(sb, perms.contains(TPosixFilePermission.OWNER_READ), perms.contains(TPosixFilePermission.OWNER_WRITE),
                perms.contains(TPosixFilePermission.OWNER_EXECUTE));
        writeBits(sb, perms.contains(TPosixFilePermission.GROUP_READ), perms.contains(TPosixFilePermission.GROUP_WRITE),
                perms.contains(TPosixFilePermission.GROUP_EXECUTE));
        writeBits(sb, perms.contains(TPosixFilePermission.OTHERS_READ),
                perms.contains(TPosixFilePermission.OTHERS_WRITE), perms.contains(TPosixFilePermission.OTHERS_EXECUTE));
        return sb.toString();
    }

    private static boolean isSet(char c, char setValue) {
        if (c == setValue) {
            return true;
        }
        if (c == '-') {
            return false;
        }
        throw new IllegalArgumentException("Invalid mode");
    }

    private static boolean isR(char c) {
        return isSet(c, 'r');
    }

    private static boolean isW(char c) {
        return isSet(c, 'w');
    }

    private static boolean isX(char c) {
        return isSet(c, 'x');
    }

    public static Set<TPosixFilePermission> fromString(String perms) {
        if (perms.length() != 9) {
            throw new IllegalArgumentException("Invalid mode");
        }
        Set<TPosixFilePermission> result = EnumSet.noneOf(TPosixFilePermission.class);
        if (isR(perms.charAt(0))) {
            result.add(TPosixFilePermission.OWNER_READ);
        }
        if (isW(perms.charAt(1))) {
            result.add(TPosixFilePermission.OWNER_WRITE);
        }
        if (isX(perms.charAt(2))) {
            result.add(TPosixFilePermission.OWNER_EXECUTE);
        }
        if (isR(perms.charAt(3))) {
            result.add(TPosixFilePermission.GROUP_READ);
        }
        if (isW(perms.charAt(4))) {
            result.add(TPosixFilePermission.GROUP_WRITE);
        }
        if (isX(perms.charAt(5))) {
            result.add(TPosixFilePermission.GROUP_EXECUTE);
        }
        if (isR(perms.charAt(6))) {
            result.add(TPosixFilePermission.OTHERS_READ);
        }
        if (isW(perms.charAt(7))) {
            result.add(TPosixFilePermission.OTHERS_WRITE);
        }
        if (isX(perms.charAt(8))) {
            result.add(TPosixFilePermission.OTHERS_EXECUTE);
        }
        return result;
    }

    public static TFileAttribute<Set<TPosixFilePermission>> asFileAttribute(Set<TPosixFilePermission> perms) {
        Set<TPosixFilePermission> value = Set.copyOf(perms);
        return new TFileAttribute<Set<TPosixFilePermission>>() {
            @Override
            public String name() {
                return "posix:permissions";
            }

            @Override
            public Set<TPosixFilePermission> value() {
                return value;
            }
        };
    }
}
