/*
 * Copyright 2015-2024 the original author or authors.
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

import org.lastaflute.taglib.base.BaseTouchableInputTag;

import jakarta.servlet.jsp.JspException;

/**
 * @author modified by jflute (originated in Struts)
 */
public class HtmlTextareaTag extends BaseTouchableInputTag {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String cols;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HtmlTextareaTag() {
        super();
        doReadonly = true;
    }

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    protected String getTagName() throws JspException {
        return "textarea";
    }

    @Override
    protected void prepareBasicInputAttribute(StringBuilder results) throws JspException {
        prepareAttribute(results, "name", prepareName());
        prepareAttribute(results, "accesskey", getAccesskey());
        prepareAttribute(results, "tabindex", getTabindex());
        prepareAttribute(results, "cols", getCols());
        prepareAttribute(results, "rows", getRows());
    }

    @Override
    protected void prepareClosingInputAttribute(StringBuilder results) throws JspException {
        results.append(">");
        results.append(renderData());
        results.append("</textarea>");
    }

    protected String renderData() throws JspException {
        final String data = value != null ? value : (String) lookupProperty(name, property);
        return data != null ? getEnhanceLogic().filter(data) : "";
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
        cols = null;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getCols() {
        return cols;
    }

    public void setCols(String cols) {
        this.cols = cols;
    }
}
