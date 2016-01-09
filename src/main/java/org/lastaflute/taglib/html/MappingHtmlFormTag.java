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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.optional.OptionalThingConsumer;
import org.lastaflute.core.direction.FwAssistantDirector;
import org.lastaflute.core.util.ContainerUtil;
import org.lastaflute.taglib.base.TaglibAttributeKey;
import org.lastaflute.taglib.exception.TaglibFormActionNotFoundException;
import org.lastaflute.taglib.exception.TaglibFormBeanNotFoundException;
import org.lastaflute.web.LastaWebKey;
import org.lastaflute.web.exception.ActionFormNotFoundException;
import org.lastaflute.web.path.ActionFoundPathHandler;
import org.lastaflute.web.path.ActionPathResolver;
import org.lastaflute.web.ruts.VirtualForm;
import org.lastaflute.web.ruts.config.ActionExecute;
import org.lastaflute.web.ruts.config.ActionFormMeta;
import org.lastaflute.web.servlet.request.RequestManager;
import org.lastaflute.web.util.LaActionExecuteUtil;
import org.lastaflute.web.util.LaModuleConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 */
public class MappingHtmlFormTag extends HtmlFormTag {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(MappingHtmlFormTag.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String onkeypress;
    protected String onkeyup;
    protected String onkeydown;

    // ===================================================================================
    //                                                                             Look Up
    //                                                                             =======
    @Override
    protected void lookup() throws JspException {
        setupModuleConfig();
        setupActionForNow();

        final int paramMarkIndex = action.indexOf('?');
        final String path = paramMarkIndex >= 0 ? action.substring(0, paramMarkIndex) : action;
        final String queryString = paramMarkIndex >= 0 ? action.substring(paramMarkIndex) : "";

        final ActionPathResolver resolver = getActionResolver();
        try {
            final ActionFoundPathHandler handler = createActionPathHandler(path, queryString);
            final boolean handled = resolver.handleActionPath(path, handler);
            if (!handled) {
                throwFormActionNotFoundException(path, queryString);
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof JspException) {
                throw (JspException) e;
            } else {
                String msg = "Failed to handle action path: " + path;
                throw new IllegalStateException(msg, e);
            }
        }
        checkActionMappingExistence();
        setupBeanInfo();
    }

    protected void setupModuleConfig() throws JspException {
        moduleConfig = LaModuleConfigUtil.findModuleConfig(pageContext.getRequest());
    }

    protected void setupActionForNow() {
        if (isInternalDebug()) {
            debugInternally("...Setting up action for now");
            debugInternally("  requestPath    = " + getRequestManager().getRequestPath());
            debugInternally("  action (first) = " + action);
        }
        if (action == null) {
            action = calculateActionPath();
        } else if (!action.startsWith("/")) {
            action = calculateActionPath() + action;
        }
        if (isInternalDebug()) {
            debugInternally("  action (calc)  = " + action);
        }
    }

    protected ActionFoundPathHandler createActionPathHandler(final String path, final String queryString) {
        return new ActionFoundPathHandler() {
            public boolean handleActionPath(String requestPath, String actionName, String paramPath, ActionExecute configByParam)
                    throws IOException, ServletException {
                return processActionMapping(path, queryString, actionName, paramPath, configByParam);
            }
        };
    }

    protected void throwFormActionNotFoundException(String path, String queryString) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the action for the action path of form tag.");
        br.addItem("Action Path");
        br.addElement(path);
        br.addItem("Query String");
        br.addElement(queryString);
        final String msg = br.buildExceptionMessage();
        throw new TaglibFormActionNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                         Action Path
    //                                                                         ===========
    protected String calculateActionPath() {
        // cannot use this because URL parameter is contained in the path e.g. /member/edit/1001/
        // you should remove the parameter '1001' by additional logic when you use this
        //final String routingOriginRequestPathAndQuery = getRoutingOriginRequestPathAndQuery();
        //if (routingOriginRequestPathAndQuery != null) { // first priority
        //  return routingOriginRequestPathAndQuery;
        //}
        final String requestPath = getRequestPath();
        return calculateActionPathByJspPath(requestPath);
    }

    protected String calculateActionPathByJspPath(String requestPath) {
        final ActionPathResolver resolver = getActionResolver();
        return resolver.calculateActionPathByJspPath(requestPath);
    }

    // ===================================================================================
    //                                                                      Action Mapping
    //                                                                      ==============
    protected boolean processActionMapping(String path, String queryString, String actionKey, String paramPath,
            ActionExecute configByParam) {
        if (isInternalDebug()) {
            debugInternally("...Processing action mapping");
        }
        mapping = moduleConfig.findActionMapping(actionKey).get();
        if (isInternalDebug()) {
            debugInternally("  actionKey   = " + actionKey);
            debugInternally("  paramPath   = " + paramPath);
            debugInternally("  queryString = " + queryString);
        }
        if (configByParam == null) {
            // not use actionPath because the path may have prefix
            // instead, specified or calculated path (by request) is set here
            // the path will be resolved at routing filter
            action = appendSlashRearIfNeeds(path) + appendQuestionFrontIfNeeds(queryString);

            if (isInternalDebug()) {
                debugInternally(" -> processed (empty paramPath): action=" + action);
            }
        } else {
            if (isInternalDebug()) {
                debugInternally(" -> processed (execute config): action=" + action);
            }
        }
        return true;
    }

    protected String appendSlashRearIfNeeds(String str) {
        return str + (!str.endsWith("/") ? "/" : "");
    }

    protected String appendQuestionFrontIfNeeds(String str) {
        return (!str.equals("") ? "?" : "") + str;
    }

    protected void checkActionMappingExistence() throws JspException {
        if (mapping == null) {
            String msg = "Not found the action mapping in the form: action=" + action;
            throw new JspException(msg);
        }
    }

    // ===================================================================================
    //                                                                           Bean Info
    //                                                                           =========
    protected void setupBeanInfo() throws JspException { // from lookup()
        final int requestScope = PageContext.REQUEST_SCOPE;
        final Object pushedForm = pageContext.getAttribute(LastaWebKey.PUSHED_ACTION_FORM_KEY, requestScope);
        if (pushedForm != null) {
            beanName = ((VirtualForm) pushedForm).getFormMeta().getFormKey();
            pageContext.setAttribute(beanName, pushedForm, requestScope); // translate
            return;
        }
        final ActionExecute execute = LaActionExecuteUtil.getActionExecute();
        try {
            execute.getFormMeta().alwaysPresent(new OptionalThingConsumer<ActionFormMeta>() {
                public void accept(ActionFormMeta meta) {
                    beanName = meta.getFormKey();
                }
            });
        } catch (ActionFormNotFoundException e) {
            throwTaglibFormBeanNotFoundException(execute, e);
        }
    }

    protected void throwTaglibFormBeanNotFoundException(ActionExecute execute, ActionFormNotFoundException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the action form for the form tag.");
        br.addItem("Advice");
        br.addElement("ActionForm should be defined when you use la:form tag.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    public HtmlResponse index() {");
        br.addElement("        return asHtml(path_Sea_SeaJsp); // *NG");
        br.addElement("    }");
        br.addElement("  (o): (case of initial value from GET parameter)");
        br.addElement("    public HtmlResponse index(SeaForm form) { // OK");
        br.addElement("        return asHtml(path_Sea_SeaJsp);");
        br.addElement("    }");
        br.addElement("  (o): (case of no initial value, empty display)");
        br.addElement("    public HtmlResponse index() {");
        br.addElement("        return asHtml(path_Sea_SeaJsp).useForm(SeaForm.class); // OK");
        br.addElement("    }");
        br.addItem("Action Mapping");
        br.addElement(mapping);
        br.addItem("Action Execute");
        br.addElement(execute);
        br.addItem("Bean Name");
        br.addElement(beanName);
        br.addItem("Action Attribute");
        br.addElement(action);
        final String msg = br.buildExceptionMessage();
        throw new TaglibFormBeanNotFoundException(msg, cause);
    }

    // ===================================================================================
    //                                                                           Form Bean
    //                                                                           =========
    @Override
    protected void initFormBean() throws JspException { // from doStartTag()
        final int requestScope = PageContext.REQUEST_SCOPE;
        final Object bean = pageContext.getAttribute(beanName, requestScope);
        if (bean == null) {
            throwActionFormNotFoundException();
        }
        // set as default key to use it in inside tag
        pageContext.setAttribute(TaglibAttributeKey.BEAN_KEY, bean, requestScope);
    }

    protected void throwActionFormNotFoundException() throws JspException {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the action form for the form tag.");
        br.addItem("Advice");
        br.addElement("ActionForm should be defined when you use la:form tag.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    public HtmlResponse index() {");
        br.addElement("        return asHtml(path_Sea_SeaJsp); // *NG");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    public HtmlResponse index(SeaForm form) { // OK");
        br.addElement("        return asHtml(path_Sea_SeaJsp);");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    public HtmlResponse index() {");
        br.addElement("        return asHtml(path_Sea_SeaJsp).useForm(SeaForm.class); // OK");
        br.addElement("    }");
        br.addItem("Action Mapping");
        br.addElement(mapping);
        br.addItem("Action Execute");
        br.addElement(LaActionExecuteUtil.getActionExecute());
        br.addItem("Bean Name");
        br.addElement(beanName);
        br.addItem("Action Attribute");
        br.addElement(action);
        final String msg = br.buildExceptionMessage();
        throw new ActionFormNotFoundException(msg);
    }

    @Override
    protected void renderAction(StringBuilder results) {
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
        results.append(" action=\"");
        final String contextPath = request.getContextPath();
        final StringBuilder value = new StringBuilder();
        if (contextPath.length() > 1) {
            value.append(contextPath);
        }
        value.append(action);
        results.append(response.encodeURL(value.toString()));
        results.append("\"");
    }

    // ===================================================================================
    //                                                                     Other Attribute
    //                                                                     ===============
    @Override
    protected void renderOtherAttributes(StringBuilder results) {
        super.renderOtherAttributes(results);
        renderAttribute(results, "onkeypress", onkeypress);
        renderAttribute(results, "onkeyup", onkeyup);
        renderAttribute(results, "onkeydown", onkeydown);
    }

    // ===================================================================================
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        onkeypress = null;
        onkeyup = null;
        onkeydown = null;
    }

    // ===================================================================================
    //                                                                      Internal Debug
    //                                                                      ==============
    protected boolean isInternalDebug() {
        return logger.isDebugEnabled() && isFrameworkDebugEnabled();
    }

    protected void debugInternally(String msg) {
        logger.debug(msg);
    }

    // ===================================================================================
    //                                                                           Component
    //                                                                           =========
    protected FwAssistantDirector getAssistantDirector() {
        return ContainerUtil.getComponent(FwAssistantDirector.class);
    }

    protected RequestManager getRequestManager() {
        return ContainerUtil.getComponent(RequestManager.class);
    }

    protected ActionPathResolver getActionResolver() {
        return ContainerUtil.getComponent(ActionPathResolver.class);
    }

    // -----------------------------------------------------
    //                                    Component Behavior
    //                                    ------------------
    protected boolean isFrameworkDebugEnabled() {
        return getAssistantDirector().assistCoreDirection().isFrameworkDebug();
    }

    // cannot use this because... see the Action Path calculation logic
    //protected String getRoutingOriginRequestPathAndQuery() {
    //  final RequestManager requestManager = getRequestManager();
    //  return requestManager.getRoutingOriginRequestPathAndQuery();
    //}

    protected String getRequestPath() {
        return getRequestManager().getRequestPath();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getOnkeypress() {
        return onkeypress;
    }

    public void setOnkeypress(String onkeypress) {
        this.onkeypress = onkeypress;
    }

    public String getOnkeyup() {
        return onkeyup;
    }

    public void setOnkeyup(String onkeyup) {
        this.onkeyup = onkeyup;
    }

    public String getOnkeydown() {
        return onkeydown;
    }

    public void setOnkeydown(String onkeydown) {
        this.onkeydown = onkeydown;
    }
}
