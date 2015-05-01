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
package org.dbflute.lasta.taglib.html;

import java.util.function.Supplier;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.dbflute.lasta.taglib.base.BaseTouchableBodyTag;
import org.dbflute.lasta.taglib.base.TaglibAttributeKey;

/**
 * @author modified by jflute (originated in Struts)
 */
public class HtmlSelectTag extends BaseTouchableBodyTag {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;
    public static final String SELECT_KEY = TaglibAttributeKey.Package + ".SELECT";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                           Native Item
    //                                           -----------
    protected String multiple; // true or false
    protected String name = TaglibAttributeKey.BEAN_KEY; // e.g. form
    protected String property; // property name of bean for the select tag, required
    protected String size; // display row count, 1: pull down, 2 or more: list style 
    protected String value; // selected value

    // -----------------------------------------------------
    //                                     Internal Handling
    //                                     -----------------
    protected String savedBody;
    protected String[] matchedValues;

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    public int doStartTag() throws JspException {
        write(renderSelectStartElement());
        pageContext.setAttribute(SELECT_KEY, this);
        calculateMatchValues();
        return EVAL_BODY_BUFFERED;
    }

    protected String renderSelectStartElement() throws JspException {
        final StringBuilder sb = new StringBuilder("<select");
        prepareAttribute(sb, "name", prepareName());
        prepareAttribute(sb, "accesskey", getAccesskey());
        if (multiple != null) {
            sb.append(" multiple=\"multiple\"");
        }
        prepareAttribute(sb, "size", getSize());
        prepareAttribute(sb, "tabindex", getTabindex());
        sb.append(prepareEventHandlers());
        sb.append(prepareStyles());
        prepareOtherAttributes(sb);
        sb.append(">");
        return sb.toString();
    }

    protected void calculateMatchValues() throws JspException {
        if (value != null) {
            matchedValues = new String[1];
            matchedValues[0] = value;
        } else {
            matchedValues = getEnhanceLogic().getPropertyAsStringArray(lookupBean(name), property, new Supplier<Object>() {
                @Override
                public Object get() {
                    return "selectTag name=" + name;
                }
            }); // not lambda for Jetty6
            if (matchedValues == null) {
                matchedValues = EMPTY_STRING_ARRAY;
            }
        }
    }

    // ===================================================================================
    //                                                                          After Body
    //                                                                          ==========
    @Override
    public int doAfterBody() throws JspException {
        if (bodyContent != null) {
            final String value = bodyContent.getString();
            this.savedBody = value != null ? value.trim() : "";
        }
        return SKIP_BODY;
    }

    // ===================================================================================
    //                                                                             End Tag
    //                                                                             =======
    @Override
    public int doEndTag() throws JspException {
        pageContext.removeAttribute(SELECT_KEY);
        final StringBuilder sb = new StringBuilder();
        if (savedBody != null) {
            sb.append(savedBody);
            savedBody = null;
        }
        sb.append("</select>");
        write(sb.toString());
        return EVAL_PAGE;
    }

    // ===================================================================================
    //                                                                    Various Override
    //                                                                    ================
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

    @Override
    protected String buildErrorIdentity() {
        return "property=" + property + " tag=" + getClass().getName();
    }

    // ===================================================================================
    //                                                                      OptionTag Call
    //                                                                      ==============
    public static HtmlSelectTag selectTag(PageContext pageContext, Supplier<String> notFoundCall) throws JspException {
        final HtmlSelectTag selectTag = (HtmlSelectTag) pageContext.getAttribute(SELECT_KEY);
        if (selectTag == null) {
            throw new JspException("Not found the select tag in option tag: " + notFoundCall.get());
        }
        return selectTag;
    }

    public boolean isMatched(String value) { // called by option tag
        if (matchedValues == null || value == null) {
            return false;
        }
        for (int i = 0; i < matchedValues.length; i++) {
            if (value.equals(matchedValues[i])) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        multiple = null;
        name = TaglibAttributeKey.BEAN_KEY;
        property = null;
        savedBody = null;
        size = null;
        value = null;
        matchedValues = null;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getMultiple() {
        return multiple;
    }

    public void setMultiple(String multiple) {
        this.multiple = multiple;
    }

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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
