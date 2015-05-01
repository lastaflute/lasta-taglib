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
package org.dbflute.lasta.taglib.base;

import java.util.function.Supplier;

import javax.servlet.jsp.JspException;

/**
 * @author modified by jflute (originated in Struts)
 */
public abstract class BaseTouchableInputTag extends BaseTouchableBodyTag {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String name = TaglibAttributeKey.BEAN_KEY;
    protected String property; // required
    protected String maxlength;
    protected String rows;
    protected String value;
    protected String placeholder;
    protected String autocomplete;

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    public int doStartTag() throws JspException {
        write(renderInputElement());
        return EVAL_BODY_BUFFERED;
    }

    protected String renderInputElement() throws JspException {
        final StringBuilder sb = new StringBuilder();
        sb.append("<").append(getTagName());
        prepareBasicInputAttribute(sb);
        sb.append(prepareEventHandlers());
        sb.append(prepareStyles());
        prepareHtml5Attribute(sb);
        prepareOtherAttributes(sb);
        prepareDynamicAttributes(sb);
        prepareClosingInputAttribute(sb);
        return sb.toString();
    }

    protected abstract String getTagName() throws JspException;

    protected abstract void prepareBasicInputAttribute(StringBuilder sb) throws JspException;

    protected void prepareHtml5Attribute(StringBuilder results) {
        prepareAttribute(results, "placeholder", resolvePlaceholderResource(getPlaceholder()));
        prepareAttribute(results, "autocomplete", resolveAutocompleteResource(getAutocomplete()));
    }

    protected abstract void prepareClosingInputAttribute(StringBuilder sb) throws JspException;

    // ===================================================================================
    //                                                                             End Tag
    //                                                                             =======
    @Override
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

    // ===================================================================================
    //                                                                        Prepare Name
    //                                                                        ============
    @Override
    protected String prepareName() throws JspException {
        if (property == null) {
            return null;
        }
        if (indexed) {
            final StringBuilder results = new StringBuilder();
            prepareIndex(results, name);
            results.append(property);
            return results.toString();
        }
        return property;
    }

    // ===================================================================================
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        name = TaglibAttributeKey.BEAN_KEY;
        property = null;
        maxlength = null;
        rows = null;
        value = null;
        placeholder = null;
        autocomplete = null;
    }

    // ===================================================================================
    //                                                                             Enhance
    //                                                                             =======
    protected String resolvePlaceholderResource(String placeholder) {
        return getEnhanceLogic().resolveLabelResource(pageContext, placeholder, new Supplier<Object>() {
            public Object get() {
                return buildErrorIdentity();
            }
        });
    }

    protected String resolveAutocompleteResource(String label) {
        return getEnhanceLogic().resolveAutocompleteResource(pageContext, label, new Supplier<Object>() {
            public Object get() {
                return buildErrorIdentity();
            }
        });
    }

    // ===================================================================================
    //                                                                             Enhance
    //                                                                             =======
    @Override
    public void setTitle(String title) { // for label use
        final TaglibEnhanceLogic tablibLogic = getEnhanceLogic();
        super.setTitle(tablibLogic.resolveLabelResource(pageContext, title, new Supplier<Object>() {
            public Object get() {
                return buildErrorIdentity();
            }
        }));
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

    public String getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(String maxlength) {
        this.maxlength = maxlength;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getRows() {
        return rows;
    }

    public void setRows(String rows) {
        this.rows = rows;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getAutocomplete() {
        return autocomplete;
    }

    public void setAutocomplete(String autocomplete) {
        this.autocomplete = autocomplete;
    }
}
