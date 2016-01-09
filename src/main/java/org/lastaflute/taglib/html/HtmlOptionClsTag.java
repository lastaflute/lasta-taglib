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

import org.dbflute.jdbc.Classification;
import org.dbflute.jdbc.ClassificationMeta;
import org.lastaflute.taglib.base.BaseNonBodyTag;
import org.lastaflute.taglib.base.TaglibEnhanceLogic;

/**
 * @author modified by jflute (originated in Struts)
 */
public class HtmlOptionClsTag extends BaseNonBodyTag {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String name; // required
    private String style;
    private String styleClass;

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    // ===================================================================================
    //                                                                             End Tag
    //                                                                             =======
    @Override
    public int doEndTag() throws JspException {
        final TaglibEnhanceLogic logic = getEnhanceLogic();
        final ClassificationMeta meta = logic.findClassificationMeta(name, new Supplier<Object>() {
            public Object get() {
                return buildErrorIdentity();
            }
        }); // not lambda for Jetty6
        final StringBuilder sb = new StringBuilder();
        final HtmlSelectTag selectTag = selectTag();
        for (Classification cls : meta.listAll()) { // #pending classification group (see lasta thymeleaf)
            final String code = cls.code();
            final String alias = logic.findClassificationAlias(cls);
            addOption(sb, code, alias, selectTag.isMatched(code));
        }
        write(sb.toString());
        return EVAL_PAGE;
    }

    protected void addOption(StringBuilder sb, String value, String label, boolean matched) {
        sb.append("<option value=\"");
        sb.append(getEnhanceLogic().filter(value));
        sb.append("\"");
        if (matched) {
            sb.append(" selected=\"selected\"");
        }
        if (style != null) {
            sb.append(" style=\"");
            sb.append(style);
            sb.append("\"");
        }
        if (styleClass != null) {
            sb.append(" class=\"");
            sb.append(styleClass);
            sb.append("\"");
        }
        sb.append(">");
        sb.append(getEnhanceLogic().filter(label));
        sb.append("</option>\n");
    }

    protected HtmlSelectTag selectTag() throws JspException {
        return HtmlSelectTag.selectTag(pageContext, new Supplier<String>() {
            public String get() {
                return "name=" + name;
            }
        }); // not lambda for Jetty6
    }

    // ===================================================================================
    //                                                                      Error Identity
    //                                                                      ==============
    @Override
    protected String buildErrorIdentity() {
        try {
            return "name=" + name + " select_tag=" + selectTag().getProperty();
        } catch (JspException e) {
            return "name=" + name + " select_tag=" + e.getMessage();
        }
    }

    // ===================================================================================
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        name = null;
        style = null;
        styleClass = null;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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
}
