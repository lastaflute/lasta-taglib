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

import javax.servlet.jsp.JspException;

import org.lastaflute.taglib.base.BaseTouchableBodyTag;
import org.lastaflute.taglib.base.TaglibAttributeKey;

/**
 * @author modified by jflute (originated in Struts)
 */
public class HtmlCheckboxTag extends BaseTouchableBodyTag {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String name = TaglibAttributeKey.BEAN_KEY; // form, as default
    protected String property; // property of form, required
    protected String text;
    protected String value;

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    public int doStartTag() throws JspException {
        final StringBuilder sb = new StringBuilder("<input type=\"checkbox\"");
        prepareAttribute(sb, "name", prepareName());
        prepareAttribute(sb, "accesskey", getAccesskey());
        prepareAttribute(sb, "tabindex", getTabindex());
        prepareAttribute(sb, "value", getValue());
        if (isChecked()) {
            sb.append(" checked=\"checked\"");
        }
        sb.append(prepareEventHandlers());
        sb.append(prepareStyles());
        prepareOtherAttributes(sb);
        sb.append(">"); // HTML5 style
        write(sb.toString());
        this.text = null;
        return EVAL_BODY_BUFFERED;
    }

    @Override
    protected String prepareName() throws JspException {
        if (property == null) {
            return null;
        }
        if (indexed) {
            final StringBuilder sb = new StringBuilder();
            prepareIndex(sb, name);
            sb.append(property);
            return sb.toString();
        }
        return property;
    }

    protected boolean isChecked() throws JspException {
        final Object found = lookupProperty(name, property);
        if (found == null) {
            return false;
        }
        final String checked = found.toString();
        return checked.equalsIgnoreCase(value) //
                || checked.equalsIgnoreCase("true") //
                || checked.equalsIgnoreCase("yes") //
                || checked.equalsIgnoreCase("y") //
                || checked.equalsIgnoreCase("1") //
                || checked.equalsIgnoreCase("on"); //
    }

    // ===================================================================================
    //                                                                          After Body
    //                                                                          ==========
    @Override
    public int doAfterBody() throws JspException {
        if (bodyContent != null) {
            final String value = bodyContent.getString().trim();
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
        if (text != null) {
            write(text);
        }
        return EVAL_PAGE;
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
        name = TaglibAttributeKey.BEAN_KEY;
        property = null;
        text = null;
        value = null;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return value == null ? "on" : value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
