/*
 * Copyright 2015-2016 the original author or authors.
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

import javax.servlet.jsp.JspException;

import org.lastaflute.taglib.base.BaseTouchableFieldTag;

/**
 * @author modified by jflute (originated in Struts)
 */
public class HtmlHiddenTag extends BaseTouchableFieldTag {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean write;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HtmlHiddenTag() {
        super();
        type = "hidden";
    }

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    public int doStartTag() throws JspException {
        super.doStartTag();
        if (!write) {
            return EVAL_BODY_BUFFERED;
        }
        final String results;
        if (value != null) {
            results = getEnhanceLogic().filter(value);
        } else {
            final Object value = lookupProperty(name, property);
            results = value != null ? getEnhanceLogic().filter(value.toString()) : "";
        }
        write(results);
        return EVAL_BODY_BUFFERED;
    }

    // ===================================================================================
    //                                                                    Various Override
    //                                                                    ================
    @Override
    protected String buildErrorIdentity() {
        return "property=" + property + " tag=" + getClass().getName();
    }

    // ===================================================================================
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        write = false;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean getWrite() {
        return this.write;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }
}
