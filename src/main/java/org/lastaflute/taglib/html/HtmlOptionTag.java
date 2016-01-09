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

import org.lastaflute.taglib.base.BaseBodyTag;
import org.lastaflute.taglib.base.TaglibEnhanceLogic;

/**
 * @author modified by jflute (originated in Struts)
 */
public class HtmlOptionTag extends BaseBodyTag {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String value; // required
    protected String key; // message key for content
    protected boolean disabled = false;
    private String style;
    private String styleClass;
    protected String styleId;
    protected String text; // content body

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    public int doStartTag() throws JspException {
        text = null;
        return EVAL_BODY_BUFFERED;
    }

    // ===================================================================================
    //                                                                          After Body
    //                                                                          ==========
    @Override
    public int doAfterBody() throws JspException {
        if (bodyContent != null) {
            final String content = bodyContent.getString();
            if (content != null && content.trim().length() > 0) {
                this.text = content.trim();
            }
        }
        return SKIP_BODY;
    }

    // ===================================================================================
    //                                                                             End Tag
    //                                                                             =======
    @Override
    public int doEndTag() throws JspException {
        write(renderOptionElement());
        return EVAL_PAGE;
    }

    protected String renderOptionElement() throws JspException {
        final StringBuilder sb = new StringBuilder("<option value=\"");
        sb.append(value);
        sb.append("\"");
        if (disabled) {
            sb.append(" disabled=\"disabled\"");
        }
        if (selectTag().isMatched(value)) {
            sb.append(" selected=\"selected\"");
        }
        if (style != null) {
            sb.append(" style=\"").append(style).append("\"");
        }
        if (styleId != null) {
            sb.append(" id=\"").append(styleId).append("\"");
        }
        if (styleClass != null) {
            sb.append(" class=\"").append(styleClass).append("\"");
        }
        sb.append(">").append(text()).append("</option>");
        return sb.toString();
    }

    protected HtmlSelectTag selectTag() throws JspException {
        return HtmlSelectTag.selectTag(pageContext, new Supplier<String>() {
            public String get() {
                return "value=" + value + " text=" + text;
            }
        }); // not lambda for Jetty6
    }

    protected String text() throws JspException {
        if (text != null) {
            return text;
        }
        if (key != null) {
            return message(key);
        }
        return value;
    }

    // ===================================================================================
    //                                                                       Enhance Logic
    //                                                                       =============
    protected TaglibEnhanceLogic getEnhanceLogic() {
        return TaglibEnhanceLogic.getInstance();
    }

    // ===================================================================================
    //                                                                      Error Identity
    //                                                                      ==============
    @Override
    protected String buildErrorIdentity() {
        return "value=" + value + " text=" + text + " tag=" + getClass().getName();
    }

    // ===================================================================================
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        value = null;
        key = null;
        disabled = false;
        style = null;
        styleClass = null;
        text = null;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean getDisabled() {
        return this.disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public String getStyleId() {
        return this.styleId;
    }

    public void setStyleId(String styleId) {
        this.styleId = styleId;
    }
}
