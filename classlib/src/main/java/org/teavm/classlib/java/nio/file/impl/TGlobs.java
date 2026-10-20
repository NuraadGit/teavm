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
package org.teavm.classlib.java.nio.file.impl;

import java.util.regex.PatternSyntaxException;

final class TGlobs {
    private static final String REGEX_META = ".^$+{[]|()";
    private static final String GLOB_META = "\\*?[{";

    private TGlobs() {
    }

    private static boolean isRegexMeta(char c) {
        return REGEX_META.indexOf(c) != -1;
    }

    private static boolean isGlobMeta(char c) {
        return GLOB_META.indexOf(c) != -1;
    }

    static String toRegex(String glob, char separator) {
        boolean inGroup = false;
        StringBuilder regex = new StringBuilder("^");
        String sep = separator == '\\' ? "\\\\" : String.valueOf(separator);
        int i = 0;
        int len = glob.length();
        while (i < len) {
            char c = glob.charAt(i++);
            switch (c) {
                case '\\': {
                    if (i == len) {
                        throw new PatternSyntaxException("No character to escape", glob, i - 1);
                    }
                    char next = glob.charAt(i++);
                    if (isGlobMeta(next) || isRegexMeta(next)) {
                        regex.append('\\');
                    }
                    regex.append(next);
                    break;
                }
                case '/':
                    regex.append(sep);
                    break;
                case '[': {
                    regex.append("[[^").append(sep).append("]&&[");
                    if (i < len && glob.charAt(i) == '^') {
                        regex.append("\\^");
                        i++;
                    } else {
                        if (i < len && glob.charAt(i) == '!') {
                            regex.append('^');
                            i++;
                        }
                        if (i < len && glob.charAt(i) == '-') {
                            regex.append('-');
                            i++;
                        }
                    }
                    boolean hasRangeStart = false;
                    char last = 0;
                    while (i < len) {
                        c = glob.charAt(i++);
                        if (c == ']') {
                            break;
                        }
                        if (c == separator) {
                            throw new PatternSyntaxException("Explicit 'name separator' in class", glob, i - 1);
                        }
                        if (c == '\\' || c == '[' || (c == '&' && i < len && glob.charAt(i) == '&')) {
                            regex.append('\\');
                        }
                        regex.append(c);
                        if (c == '-') {
                            if (!hasRangeStart) {
                                throw new PatternSyntaxException("Invalid range", glob, i - 1);
                            }
                            if (i == len) {
                                break;
                            }
                            c = glob.charAt(i++);
                            if (c < last) {
                                throw new PatternSyntaxException("Invalid range", glob, i - 3);
                            }
                            regex.append(c);
                            hasRangeStart = false;
                        } else {
                            hasRangeStart = true;
                            last = c;
                        }
                    }
                    if (c != ']') {
                        throw new PatternSyntaxException("Missing ']", glob, i - 1);
                    }
                    regex.append("]]");
                    break;
                }
                case '{':
                    if (inGroup) {
                        throw new PatternSyntaxException("Cannot nest groups", glob, i - 1);
                    }
                    regex.append("(?:(?:");
                    inGroup = true;
                    break;
                case '}':
                    if (inGroup) {
                        regex.append("))");
                        inGroup = false;
                    } else {
                        regex.append('}');
                    }
                    break;
                case ',':
                    if (inGroup) {
                        regex.append(")|(?:");
                    } else {
                        regex.append(',');
                    }
                    break;
                case '*':
                    if (i < len && glob.charAt(i) == '*') {
                        regex.append(".*");
                        i++;
                    } else {
                        regex.append("[^").append(sep).append("]*");
                    }
                    break;
                case '?':
                    regex.append("[^").append(sep).append("]");
                    break;
                default:
                    if (isRegexMeta(c)) {
                        regex.append('\\');
                    }
                    regex.append(c);
                    break;
            }
        }
        if (inGroup) {
            throw new PatternSyntaxException("Missing '}", glob, i - 1);
        }
        return regex.append('$').toString();
    }
}
