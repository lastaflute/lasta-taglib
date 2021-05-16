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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.optional.OptionalThingConsumer;
import org.lastaflute.core.util.ContainerUtil;
import org.lastaflute.di.util.LdiStringUtil;
import org.lastaflute.taglib.base.BaseTouchableBodyTag;
import org.lastaflute.taglib.base.TaglibEnhanceLogic;
import org.lastaflute.taglib.exception.TaglibLinkActionNotFoundException;
import org.lastaflute.web.LastaWebKey;
import org.lastaflute.web.path.ActionFoundPathHandler;
import org.lastaflute.web.path.ActionPathResolver;
import org.lastaflute.web.path.MappingPathResource;
import org.lastaflute.web.ruts.config.ActionExecute;
import org.lastaflute.web.servlet.request.RequestManager;
import org.lastaflute.web.servlet.session.SessionManager;
import org.lastaflute.web.token.DoubleSubmitTokenMap;
import org.lastaflute.web.util.LaActionRuntimeUtil;
import org.lastaflute.web.util.LaRequestUtil;
import org.lastaflute.web.util.LaResponseUtil;

/**
 * @author modified by jflute (originated in Struts)
 */
public class HtmlLinkTag extends BaseTouchableBodyTag {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String text;
    protected String anchor;
    protected String href;
    protected String linkName;
    protected String name;
    protected String page;
    protected String action;
    protected String paramId;
    protected String paramName;
    protected String paramProperty;
    protected String paramScope;
    protected String property;
    protected String scope;
    protected String target;
    protected boolean transaction;
    protected String indexId;

    /** The language for linked contents (NullAllowd: if NotNull, add the path to lang) */
    protected String lang;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public HtmlLinkTag() {
        doDisabled = false;
    }

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    public int doStartTag() throws JspException {
        final StringBuilder sb = new StringBuilder("<a");
        prepareAttribute(sb, "name", getLinkName());
        if (getLinkName() == null || getHref() != null || getPage() != null || getAction() != null) {
            prepareAttribute(sb, "href", calculateURL());
        }
        prepareAttribute(sb, "target", getTarget());
        prepareAttribute(sb, "accesskey", getAccesskey());
        prepareAttribute(sb, "tabindex", getTabindex());
        sb.append(prepareStyles());
        sb.append(prepareEventHandlers());
        prepareOtherAttributes(sb);
        sb.append(">");
        getEnhanceLogic().write(pageContext, sb.toString());
        text = null;
        return EVAL_BODY_BUFFERED;
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
        // Prepare the textual content and ending element of this hyperlink
        final StringBuilder sb = new StringBuilder();
        sb.append(text != null ? text : "").append("</a>");
        getEnhanceLogic().write(pageContext, sb.toString());
        return EVAL_PAGE;
    }

    // ===================================================================================
    //                                                                       Calculate URL
    //                                                                       =============
    protected String calculateURL() throws JspException {
        if (href == null) {
            throw new IllegalStateException("The attribute 'href' is required but not found.");
        }
        if (href.indexOf(':') > -1) {
            throw new IllegalStateException("Unsupported colon in href: " + href);
        }
        final StringBuilder sb = new StringBuilder();
        final String hrefUrl = buildHrefUrl(href);
        sb.append(hrefUrl); // not null
        if (transaction) {
            final SessionManager manager = getRequestManager().getSessionManager();
            final String tokenKey = LastaWebKey.TRANSACTION_TOKEN_KEY;
            manager.getAttribute(tokenKey, DoubleSubmitTokenMap.class).ifPresent(new OptionalThingConsumer<DoubleSubmitTokenMap>() {
                @Override
                public void accept(DoubleSubmitTokenMap tokenMap) {
                    final Class<?> actionType = LaActionRuntimeUtil.getActionRuntime().getActionType();
                    tokenMap.get(actionType).ifPresent(new OptionalThingConsumer<String>() {
                        @Override
                        public void accept(String token) {
                            final String delimiter = hrefUrl.indexOf('?') >= 0 ? "&" : "?";
                            sb.append(delimiter).append(tokenKey).append("=").append(token);
                        }
                    });
                }
            });
        }
        // Copied from Struts TagUtils (and small adjustment)
        // Add anchor if requested (adding only here, duplicate if any anchor exists)
        if (anchor != null) {
            sb.append("#").append(getEnhanceLogic().encode(anchor));
        }
        // AbsolutePath & lang is not null
        if (LdiStringUtil.isNotBlank(lang)) {
            // absolute path only
            if (hrefUrl.startsWith("/")) {
                sb.insert(0, "/").insert(0, lang); // e.g) if lang is ja, /contents/about/ => /ja/contents/about/
            } else {
                throwLangIllegalArgumentException();
            }
        }
        return sb.toString();
    }

    protected String buildHrefUrl(String input) {
        final String contextPath = LaRequestUtil.getRequest().getContextPath();
        final StringBuilder sb = new StringBuilder();
        if (contextPath.length() > 1) {
            sb.append(contextPath);
        }
        if (LdiStringUtil.isEmpty(input)) {
            sb.append(calculateActionPathByJspPath(getRequestPath()));
        } else if (!input.startsWith("/")) { // add/, ../add/
            sb.append(calculateActionPathByJspPath(getRequestPath()));
            sb.append(appendSlashRearIfNeeds(input)); // rear slash is added automatically
        } else { // /member/list/
            resolveAbsolutePath(input, sb);
        }
        return LaResponseUtil.getResponse().encodeURL(sb.toString());
    }

    protected void resolveAbsolutePath(String input, StringBuilder sb) {
        final int paramMarkIndex = input.indexOf('?');
        final String path = paramMarkIndex >= 0 ? input.substring(0, paramMarkIndex) : input;
        final String queryString = paramMarkIndex >= 0 ? input.substring(paramMarkIndex) : "";
        final ActionPathResolver resolver = getActionResolver();
        try {
            final ActionFoundPathHandler handler = createActionPathHandler(sb, path, queryString);
            final boolean handled = resolver.handleActionPath(path, handler).isPathHandled();
            if (!handled) {
                throwLinkActionNotFoundException(path, queryString);
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                final String msg = "Failed to handle action path: " + path;
                throw new IllegalStateException(msg, e);
            }
        }
    }

    protected ActionFoundPathHandler createActionPathHandler(final StringBuilder sb, final String path, final String queryString) {
        return new ActionFoundPathHandler() {
            @Override
            public boolean handleActionPath(MappingPathResource pathResource, String actionName, String paramPath, ActionExecute execute)
                    throws IOException, ServletException {
                // not use actionPath because the path may have prefix
                // see the form tag class for the details
                sb.append(appendSlashRearIfNeeds(path)); // rear slash is added automatically
                sb.append(queryString);
                return true;
            }
        }; // not lambda for jetty6
    }

    protected String appendSlashRearIfNeeds(final String str) {
        if (str.contains("?")) { // contains query string so rear slash unneeded
            return str;
        }
        return str + (!str.endsWith("/") ? "/" : "");
    }

    protected void throwLinkActionNotFoundException(final String path, final String queryString) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the action for the action path of link tag.");
        br.addItem("Requested JSP Path");
        br.addElement(getEnhanceLogic().getRequestJspPath());
        br.addItem("Action Path");
        br.addElement(path);
        br.addItem("Query String");
        br.addElement(queryString);
        br.addItem("Defined Href");
        br.addElement(href);
        final String msg = br.buildExceptionMessage();
        throw new TaglibLinkActionNotFoundException(msg);
    }

    protected void throwLangIllegalArgumentException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("When you specify the lang, Please make relative path.");
        br.addItem("Requested JSP Path");
        br.addElement(getEnhanceLogic().getRequestJspPath());
        br.addItem("Defined Href");
        br.addElement(href);
        final String msg = br.buildExceptionMessage();
        throw new IllegalArgumentException(msg);
    }

    // ===================================================================================
    //                                                                         Action Path
    //                                                                         ===========
    protected String calculateActionPath() {
        // cannot use request path of routing origin here
        // see the form tag class for the details
        //final String routingOriginRequestPathAndQuery = getRoutingOriginRequestPathAndQuery();
        //if (routingOriginRequestPathAndQuery != null) { // first priority
        //  return routingOriginRequestPathAndQuery;
        //}
        return calculateActionPathByJspPath(getRequestPath());
    }

    protected String calculateActionPathByJspPath(String requestPath) {
        return getActionResolver().calculateActionPathByJspPath(requestPath);
    }

    protected String getRequestPath() {
        return getRequestManager().getRequestPath();
    }

    // ===================================================================================
    //                                                                           Component
    //                                                                           =========
    protected RequestManager getRequestManager() {
        return ContainerUtil.getComponent(RequestManager.class);
    }

    protected ActionPathResolver getActionResolver() {
        return ContainerUtil.getComponent(ActionPathResolver.class);
    }

    // ===================================================================================
    //                                                                    Various Override
    //                                                                    ================
    @Override
    protected String buildErrorIdentity() {
        return "property=" + property + " tag=" + getClass().getName();
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected TaglibEnhanceLogic getEnhanceLogic() {
        return TaglibEnhanceLogic.getInstance();
    }

    // ===================================================================================
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        anchor = null;
        href = null;
        linkName = null;
        name = null;
        page = null;
        action = null;
        paramId = null;
        paramName = null;
        paramProperty = null;
        paramScope = null;
        property = null;
        scope = null;
        target = null;
        text = null;
        transaction = false;
        indexId = null;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getAnchor() {
        return this.anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public String getHref() {
        return this.href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getLinkName() {
        return (this.linkName);
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPage() {
        return (this.page);
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getAction() {
        return (this.action);
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getParamId() {
        return (this.paramId);
    }

    public void setParamId(String paramId) {
        this.paramId = paramId;
    }

    public String getParamName() {
        return (this.paramName);
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamProperty() {
        return (this.paramProperty);
    }

    public void setParamProperty(String paramProperty) {
        this.paramProperty = paramProperty;
    }

    public String getParamScope() {
        return (this.paramScope);
    }

    public void setParamScope(String paramScope) {
        this.paramScope = paramScope;
    }

    public String getProperty() {
        return (this.property);
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getScope() {
        return (this.scope);
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getTarget() {
        return (this.target);
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean getTransaction() {
        return (this.transaction);
    }

    public void setTransaction(boolean transaction) {
        this.transaction = transaction;
    }

    public String getIndexId() {
        return (this.indexId);
    }

    public void setIndexId(String indexId) {
        this.indexId = indexId;
    }
}
