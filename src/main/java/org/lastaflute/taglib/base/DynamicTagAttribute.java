/*
 * Copyright 2015-2018 the original author or authors.
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
package org.lastaflute.taglib.base;

/**
 * @author jflute
 */
public class DynamicTagAttribute {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String key;
    protected final Object value;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DynamicTagAttribute(String key, String value) {
        this.key = key;
        this.value = value;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    // required to avoid duplicate attribute
    // (release() is unavailable for clearance)
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DynamicTagAttribute)) {
            return false;
        }
        final String yourKey = ((DynamicTagAttribute) obj).key;
        if (key == null && yourKey == null) {
            return true;
        } else {
            return key != null && yourKey != null && key.equals(yourKey);
        }
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "dynamicTag:{" + key + ", " + value + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
