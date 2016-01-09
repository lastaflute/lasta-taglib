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
package org.lastaflute.taglib.base;

import javax.servlet.jsp.JspException;

/**
 * @author modified by jflute (originated in Struts)
 */
public abstract class BaseTouchableFieldTag extends BaseTouchableInputTag {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String size;
    protected String accept;
    protected boolean redisplay = true;

    protected String type; // e.g. text, password

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    protected String getTagName() throws JspException {
        return "input";
    }

    @Override
    protected void prepareBasicInputAttribute(StringBuilder results) throws JspException {
        prepareAttribute(results, "type", type);
        prepareAttribute(results, "name", prepareName());
        prepareAttribute(results, "accesskey", getAccesskey());
        prepareAttribute(results, "accept", getAccept());
        prepareAttribute(results, "maxlength", getMaxlength());
        prepareAttribute(results, "size", getSize());
        prepareAttribute(results, "tabindex", getTabindex());
        prepareValue(results);
    }

    protected void prepareValue(StringBuilder results) throws JspException {
        results.append(" value=\"");
        if (value != null) {
            results.append(formatValue(value));
        } else if (redisplay || !"password".equals(type)) {
            // #later textTag redisplay catch and throw good error message
            results.append(formatValue(lookupProperty(name, property)));
        }
        results.append('"');
    }

    protected String formatValue(Object value) throws JspException {
        return value != null ? getEnhanceLogic().filter(value.toString()) : "";
    }

    @Override
    protected void prepareClosingInputAttribute(StringBuilder results) throws JspException {
        results.append(">"); // HTML5 style
    }

    // ===================================================================================
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        size = null;
        accept = null;
        redisplay = true;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getAccept() {
        return accept;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public boolean getRedisplay() {
        return redisplay;
    }

    public void setRedisplay(boolean redisplay) {
        this.redisplay = redisplay;
    }
}
