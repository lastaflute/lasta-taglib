/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.lasta.taglib.function;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dbflute.helper.HandyDate;
import org.dbflute.lasta.di.helper.beans.BeanDesc;
import org.dbflute.lasta.di.helper.beans.factory.BeanDescFactory;
import org.dbflute.lasta.di.util.LdiStringUtil;
import org.dbflute.lastaflute.web.util.LaRequestUtil;
import org.dbflute.lastaflute.web.util.LaResponseUtil;

/**
 * @author modified by jflute (originated in Seasar)
 */
public class LaFunctions {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final int HIGHEST_SPECIAL = '>';
    private static String BR = "<br />";
    private static String NBSP = "&nbsp;";
    private static char[][] specialCharactersRepresentation = new char[HIGHEST_SPECIAL + 1][];
    static {
        specialCharactersRepresentation['&'] = "&amp;".toCharArray();
        specialCharactersRepresentation['<'] = "&lt;".toCharArray();
        specialCharactersRepresentation['>'] = "&gt;".toCharArray();
        specialCharactersRepresentation['"'] = "&#034;".toCharArray();
        specialCharactersRepresentation['\''] = "&#039;".toCharArray();
    }

    // ===================================================================================
    //                                                                         HTML Escape
    //                                                                         ===========
    /**
     * Escapes characters that could be interpreted as HTML.
     * @param input The object to be escaped. (NullAllowed: if null, returns empty string)
     * @return The escaped string. (NotNull, EmptyAllowed)
     */
    public static String h(Object input) {
        if (input == null) {
            return "";
        }
        String str = "";
        if (input.getClass().isArray()) {
            Class<?> clazz = input.getClass().getComponentType();
            if (clazz == String.class) {
                str = Arrays.toString((Object[]) input);
            } else if (clazz == boolean.class) {
                str = Arrays.toString((boolean[]) input);
            } else if (clazz == int.class) {
                str = Arrays.toString((int[]) input);
            } else if (clazz == long.class) {
                str = Arrays.toString((long[]) input);
            } else if (clazz == byte.class) {
                str = Arrays.toString((byte[]) input);
            } else if (clazz == short.class) {
                str = Arrays.toString((short[]) input);
            } else if (clazz == float.class) {
                str = Arrays.toString((float[]) input);
            } else if (clazz == double.class) {
                str = Arrays.toString((double[]) input);
            } else if (clazz == char.class) {
                str = Arrays.toString((char[]) input);
            } else {
                str = Arrays.toString((Object[]) input);
            }
        } else {
            str = input.toString();
        }
        return escape(str);
    }

    protected static String escape(String buffer) {
        int start = 0;
        int length = buffer.length();
        char[] arrayBuffer = buffer.toCharArray();
        StringBuilder escapedBuffer = null;

        for (int i = 0; i < length; i++) {
            char c = arrayBuffer[i];
            if (c <= HIGHEST_SPECIAL) {
                char[] escaped = specialCharactersRepresentation[c];
                if (escaped != null) {
                    if (start == 0) {
                        escapedBuffer = new StringBuilder(length + 5);
                    }
                    if (start < i) {
                        escapedBuffer.append(arrayBuffer, start, i - start);
                    }
                    start = i + 1;
                    escapedBuffer.append(escaped);
                }
            }
        }
        if (start == 0) {
            return buffer;
        }
        if (start < length) {
            escapedBuffer.append(arrayBuffer, start, length - start);
        }
        return escapedBuffer.toString();
    }

    // ===================================================================================
    //                                                                          Escape URL
    //                                                                          ==========
    public static String u(String input) {
        return encode(input);
    }

    protected static String encode(String input) {
        final String encoding = LaRequestUtil.getRequest().getCharacterEncoding();
        try {
            return URLEncoder.encode(input, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unknown encoding: " + encoding, e);
        }
    }

    // ===================================================================================
    //                                                                         Resolve URL
    //                                                                         ===========
    public static String url(String input) {
        if (input == null) {
            String msg = "The argument 'input' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!input.startsWith("/")) {
            String msg = "The argument 'input' should start with slash '/': " + input;
            throw new IllegalArgumentException(msg);
        }
        final String contextPath = LaRequestUtil.getRequest().getContextPath();
        final StringBuilder sb = new StringBuilder();
        if (contextPath.length() > 1) {
            sb.append(contextPath);
        }
        sb.append(input);
        return LaResponseUtil.getResponse().encodeURL(sb.toString());
    }

    // ===================================================================================
    //                                                                          Formatting
    //                                                                          ==========
    public static String formatDate(LocalDate date, String pattern) {
        return new HandyDate(date).toDisp(pattern);
    }

    public static String formatDateTime(LocalDateTime datetime, String pattern) {
        return new HandyDate(datetime).toDisp(pattern);
    }

    // ===================================================================================
    //                                                                      Line and Space
    //                                                                      ==============
    public static String br(String input) {
        if (LdiStringUtil.isEmpty(input)) {
            return "";
        }
        return input.replaceAll("\r\n", BR).replaceAll("\r", BR).replaceAll("\n", BR);
    }

    public static String nbsp(String input) {
        if (LdiStringUtil.isEmpty(input)) {
            return "";
        }
        return input.replaceAll(" ", NBSP);
    }

    // ===================================================================================
    //                                                                               Label
    //                                                                               =====
    @SuppressWarnings("unchecked")
    public static String label(Object value, List<?> beanList, String valueName, String labelName) {
        if (valueName == null) {
            throw new IllegalArgumentException("valueName");
        }
        if (labelName == null) {
            throw new IllegalArgumentException("labelName");
        }
        if (beanList == null) {
            throw new IllegalArgumentException("dataList");
        }
        for (Object bean : beanList) {
            if (bean instanceof Map) {
                final Map<String, Object> map = (Map<String, Object>) bean;
                final Object mappedValue = map.get(valueName);
                if (equals(value, mappedValue)) {
                    return (String) map.get(labelName);
                }
            } else {
                final BeanDesc beanDesc = BeanDescFactory.getBeanDesc(bean.getClass());
                final Object propertyValue = beanDesc.getPropertyDesc(valueName).getValue(bean);
                if (equals(value, propertyValue)) {
                    return (String) beanDesc.getPropertyDesc(labelName).getValue(bean);
                }
            }
        }
        return "";
    }

    // ===================================================================================
    //                                                                            equals()
    //                                                                            ========
    private static boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == null) {
            if (o2 instanceof String && LdiStringUtil.isEmpty((String) o2)) {
                return true;
            }
            return false;
        }
        if (o2 == null) {
            if (o1 instanceof String && LdiStringUtil.isEmpty((String) o1)) {
                return true;
            }
            return false;
        }
        if (o1.getClass() == o2.getClass()) {
            return o1.equals(o2);
        }
        return o1.toString().equals(o2.toString());
    }
}