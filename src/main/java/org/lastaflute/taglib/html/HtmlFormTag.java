/*
 * Copyright 2015-2021 the original author or authors.
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

import org.dbflute.optional.OptionalThingFunction;
import org.lastaflute.taglib.base.BaseNonBodyTag;
import org.lastaflute.taglib.base.TaglibAttributeKey;
import org.lastaflute.web.LastaWebKey;
import org.lastaflute.web.ruts.config.ActionMapping;
import org.lastaflute.web.ruts.config.ModuleConfig;
import org.lastaflute.web.servlet.session.SessionManager;
import org.lastaflute.web.token.DoubleSubmitTokenMap;
import org.lastaflute.web.util.LaActionRuntimeUtil;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

/**
 * Split as basic form tag and mapping form tag, so see also MappingHtmlFormTag.
 * @author modified by jflute (originated in Struts)
 */
public abstract class HtmlFormTag extends BaseNonBodyTag {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;
    protected static final String lineEnd = "\n";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String action;
    protected ModuleConfig moduleConfig;
    protected String enctype;
    protected String focus = null;
    protected String focusIndex = null;
    protected ActionMapping mapping = null;
    protected String method = null;
    protected String onreset = null;
    protected String onsubmit = null;
    protected boolean scriptLanguage = true;
    protected String style = null;
    protected String styleClass = null;
    protected String styleId = null;
    protected String target = null;
    protected String beanName; // is form key, initialized at lookup()
    protected String acceptCharset = null;
    private boolean disabled = false;
    protected boolean readonly = false;

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    public int doStartTag() throws JspException {
        lookup();

        final StringBuilder sb = new StringBuilder();
        sb.append(renderFormStartElement());
        sb.append(renderToken());
        write(sb.toString());

        pageContext.setAttribute(TaglibAttributeKey.FORM_KEY, this, PageContext.REQUEST_SCOPE);

        initFormBean();
        return EVAL_BODY_INCLUDE;
    }

    protected abstract void lookup() throws JspException;

    protected abstract void initFormBean() throws JspException;

    protected String renderFormStartElement() {
        final StringBuilder sb = new StringBuilder("<form");
        renderName(sb);
        renderAttribute(sb, "method", getMethod() == null ? "post" : getMethod());
        renderAction(sb);
        renderAttribute(sb, "accept-charset", getAcceptCharset());
        renderAttribute(sb, "class", getStyleClass());
        renderAttribute(sb, "enctype", getEnctype());
        renderAttribute(sb, "onreset", getOnreset());
        renderAttribute(sb, "onsubmit", getOnsubmit());
        renderAttribute(sb, "style", getStyle());
        renderAttribute(sb, "target", getTarget());
        renderOtherAttributes(sb);
        sb.append(">"); // HTML5 style
        return sb.toString();
    }

    protected void renderName(StringBuilder sb) {
        renderAttribute(sb, "name", beanName);
        renderAttribute(sb, "id", getStyleId());
    }

    protected abstract void renderAction(StringBuilder sb);

    protected void renderOtherAttributes(StringBuilder s) {
    }

    protected String renderToken() {
        final String tokenKey = LastaWebKey.TRANSACTION_TOKEN_KEY;
        final SessionManager manager = getRequestManager().getSessionManager();
        return manager.getAttribute(tokenKey, DoubleSubmitTokenMap.class).map(new OptionalThingFunction<DoubleSubmitTokenMap, String>() {
            @Override
            public String apply(DoubleSubmitTokenMap tokenMap) {
                final Class<?> actionType = LaActionRuntimeUtil.getActionRuntime().getActionType();
                return tokenMap.get(actionType).map(new OptionalThingFunction<String, String>() {
                    @Override
                    public String apply(String token) {
                        final StringBuilder sb = new StringBuilder();
                        sb.append("<div><input type=\"hidden\" name=\"").append(tokenKey);
                        sb.append("\" value=\"").append(token).append("\">").append("</div>");
                        return sb.toString();
                    }
                }).orElse("");
            }
        }).orElse("");
    }

    protected void renderAttribute(StringBuilder sb, String attribute, String value) {
        if (value != null) {
            sb.append(" ").append(attribute).append("=\"").append(value).append("\"");
        }
    }

    // ===================================================================================
    //                                                                             End Tag
    //                                                                             =======
    @Override
    public int doEndTag() throws JspException {
        pageContext.removeAttribute(TaglibAttributeKey.BEAN_KEY, PageContext.REQUEST_SCOPE);
        pageContext.removeAttribute(TaglibAttributeKey.FORM_KEY, PageContext.REQUEST_SCOPE);

        final StringBuilder sb = new StringBuilder("</form>");
        if (focus != null) {
            sb.append(renderFocusJavascript());
        }
        write(sb.toString());
        return EVAL_PAGE;
    }

    protected String renderFocusJavascript() {
        final StringBuilder results = new StringBuilder();
        results.append(lineEnd);
        results.append("<script type=\"text/javascript\"");
        if (this.scriptLanguage) {
            results.append(" language=\"JavaScript\"");
        }
        results.append(">");
        results.append(lineEnd);
        results.append("  <!--");
        results.append(lineEnd);
        final StringBuilder focusControl = new StringBuilder("document.forms[\"");
        focusControl.append(beanName);
        focusControl.append("\"].elements[\"");
        focusControl.append(this.focus);
        focusControl.append("\"]");

        results.append("  var focusControl = ");
        results.append(focusControl.toString());
        results.append(";");
        results.append(lineEnd);
        results.append(lineEnd);
        results.append("  if (focusControl.type != \"hidden\" && !focusControl.disabled) {");
        results.append(lineEnd);

        String index = "";
        if (this.focusIndex != null) {
            StringBuilder sb = new StringBuilder("[");
            sb.append(this.focusIndex);
            sb.append("]");
            index = sb.toString();
        }
        results.append("     focusControl");
        results.append(index);
        results.append(".focus();");
        results.append(lineEnd);
        results.append("  }");
        results.append(lineEnd);

        results.append("  // -->");
        results.append(lineEnd);
        results.append("</script>");
        results.append(lineEnd);
        return results.toString();
    }

    // ===================================================================================
    //                                                                       Enhance Logic
    //                                                                       =============
    @Override
    protected String buildErrorIdentity() {
        return "action=" + action + " method=" + method;
    }

    // ===================================================================================
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        action = null;
        moduleConfig = null;
        enctype = null;
        disabled = false;
        focus = null;
        focusIndex = null;
        mapping = null;
        method = null;
        onreset = null;
        onsubmit = null;
        readonly = false;
        style = null;
        styleClass = null;
        styleId = null;
        target = null;
        acceptCharset = null;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getBeanName() {
        return beanName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEnctype() {
        return enctype;
    }

    public void setEnctype(String enctype) {
        this.enctype = enctype;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getOnreset() {
        return onreset;
    }

    public void setOnreset(String onReset) {
        this.onreset = onReset;
    }

    public String getOnsubmit() {
        return onsubmit;
    }

    public void setOnsubmit(String onSubmit) {
        this.onsubmit = onSubmit;
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
        return styleId;
    }

    public void setStyleId(String styleId) {
        this.styleId = styleId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getAcceptCharset() {
        return acceptCharset;
    }

    public void setAcceptCharset(String acceptCharset) {
        this.acceptCharset = acceptCharset;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public String getFocusIndex() {
        return focusIndex;
    }

    public void setFocusIndex(String focusIndex) {
        this.focusIndex = focusIndex;
    }

    public boolean getScriptLanguage() {
        return scriptLanguage;
    }

    public void setScriptLanguage(boolean scriptLanguage) {
        this.scriptLanguage = scriptLanguage;
    }
}
