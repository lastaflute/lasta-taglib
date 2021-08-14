/*
 * Copyright 2015-2021 the original author or authors.
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

import static org.junit.Assert.assertArrayEquals;

import java.time.LocalDateTime;

import org.dbflute.utflute.core.PlainTestCase;

/**
 * @author jflute
 */
public class TaglibEnhanceLogicTest extends PlainTestCase {

    public void test_htmlEscapeMessageArgs_basic() {
        // ## Arrange ##
        TaglibEnhanceLogic logic = new TaglibEnhanceLogic();
        LocalDateTime dateTime = LocalDateTime.now();
        Object[] args = new Object[] { "<script>sea</script>", 2, dateTime };

        // ## Act ##
        logic.htmlEscapeMessageArgs(args);

        // ## Assert ##
        log(args);
        assertArrayEquals(new Object[] { "&lt;script&gt;sea&lt;/script&gt;", "2", dateTime.toString() }, args);
    }
}
