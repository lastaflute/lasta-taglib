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
package org.lastaflute.taglib.html;

import javax.servlet.jsp.JspException;

import org.lastaflute.taglib.base.BaseTouchableBodyTag;

/**
 * @author modified by jflute (originated in Struts)
 */
public class HtmlSubmitTag extends BaseTouchableBodyTag {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String property;
    protected String value;
    protected String text; // body content

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    public int doStartTag() throws JspException {
        this.text = null;
        return EVAL_BODY_BUFFERED;
    }

    // ===================================================================================
    //                                                                          After Body
    //                                                                          ==========
    @Override
    public int doAfterBody() throws JspException {
        if (bodyContent != null) {
            String value = bodyContent.getString().trim();
            if (value.length() > 0) {
                text = value;
            }
        }
        return SKIP_BODY;
    }

    // ===================================================================================
    //                                                                             End Tag
    //                                                                             =======
    @Override
    public int doEndTag() throws JspException {
        final StringBuilder sb = new StringBuilder();
        sb.append(getElementOpen());
        prepareAttribute(sb, "name", prepareName());
        prepareButtonAttributes(sb);
        sb.append(prepareEventHandlers());
        sb.append(prepareStyles());
        prepareOtherAttributes(sb);
        sb.append(">"); // HTML5 style
        write(sb.toString());
        return EVAL_PAGE;
    }

    protected String getElementOpen() {
        return "<input type=\"" + getInputTypeName() + "\"";
    }

    protected String getInputTypeName() {
        return "submit";
    }

    protected String prepareName() throws JspException {
        if (property == null) {
            return null;
        }
        if (indexed) {
            StringBuilder sb = new StringBuilder();
            sb.append(property);
            prepareIndex(sb, null);
            return sb.toString();
        }
        return property;
    }

    protected void prepareButtonAttributes(StringBuilder results) throws JspException {
        prepareAttribute(results, "accesskey", getAccesskey());
        prepareAttribute(results, "tabindex", getTabindex());
        prepareValue(results);
    }

    protected void prepareValue(StringBuilder results) {
        String label = resolveSubmitValueResource(value);
        if (label == null && text != null) {
            label = text;
        }
        if (label == null || label.length() < 1) {
            label = getDefaultValue();
        }
        prepareAttribute(results, "value", label);
    }

    protected String resolveSubmitValueResource(String submitValue) {
        return resolveLabelResource(submitValue);
    }

    protected String getDefaultValue() {
        return "Submit";
    }

    // ===================================================================================
    //                                                                    Various Override
    //                                                                    ================
    @Override
    protected String buildErrorIdentity() {
        return "property=" + property + " value=" + value + " text=" + text + " tag=" + getClass().getName();
    }

    // ===================================================================================
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        property = null;
        value = null;
        text = null;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getProperty() {
        return this.property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
