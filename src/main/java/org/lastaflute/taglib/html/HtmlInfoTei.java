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
package org.lastaflute.taglib.html;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * @author modified by jflute (originated in Struts)
 */
public class HtmlInfoTei extends TagExtraInfo {

    protected static final String TYPE = "java.lang.String";

    @Override
    public VariableInfo[] getVariableInfo(TagData data) {
        return new VariableInfo[] { createVariableInfo(data) };
    }

    protected VariableInfo createVariableInfo(TagData data) {
        return new VariableInfo(data.getAttributeString("id"), TYPE, true, VariableInfo.NESTED);
    }
}
