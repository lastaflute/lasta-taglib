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

import java.util.function.Supplier;

import javax.servlet.jsp.JspException;

import org.lastaflute.taglib.base.BaseTouchableBodyTag;
import org.lastaflute.taglib.base.TaglibAttributeKey;

/**
 * @author modified by jflute (originated in Struts)
 */
public class HtmlMultiboxTag extends BaseTouchableBodyTag {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String name = TaglibAttributeKey.BEAN_KEY; // form, as default
    protected String property; // property of form, required
    protected String value;
    protected String constant;

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    public int doStartTag() throws JspException {
        constant = null;
        return EVAL_BODY_BUFFERED;
    }

    @Override
    protected String prepareName() throws JspException {
        return property;
    }

    // ===================================================================================
    //                                                                          After Body
    //                                                                          ==========
    @Override
    public int doAfterBody() throws JspException {
        if (bodyContent != null) {
            constant = bodyContent.getString().trim();
        }
        if ("".equals(constant)) {
            constant = null;
        }
        return SKIP_BODY;
    }

    // ===================================================================================
    //                                                                             End Tag
    //                                                                             =======
    @Override
    public int doEndTag() throws JspException {
        final StringBuilder sb = new StringBuilder("<input type=\"checkbox\"");
        prepareAttribute(sb, "name", prepareName());
        prepareAttribute(sb, "accesskey", getAccesskey());
        prepareAttribute(sb, "tabindex", getTabindex());
        String value = prepareValue(sb);
        prepareChecked(sb, value);
        sb.append(prepareEventHandlers());
        sb.append(prepareStyles());
        prepareOtherAttributes(sb);
        sb.append(">"); // HTML5 style
        write(sb.toString());
        return EVAL_PAGE;
    }

    protected String prepareValue(StringBuilder sb) throws JspException {
        String value = (this.value == null) ? this.constant : this.value;
        if (value == null) {
            throw new IllegalStateException("Not found the value for multibox: " + name);
        }
        prepareAttribute(sb, "value", getEnhanceLogic().filter(value));
        return value;
    }

    protected void prepareChecked(StringBuilder sb, String value) throws JspException {
        final Object bean = lookupBean(name);
        final String[] values = getEnhanceLogic().getPropertyAsStringArray(bean, property, new Supplier<Object>() {
            public Object get() {
                return "multiboxTag name=" + name;
            }
        });
        for (int i = 0; i < values.length; i++) {
            if (value.equals(values[i])) {
                sb.append(" checked=\"checked\"");
                break;
            }
        }
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
        value = null;
        constant = null;
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
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
