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
package org.lastaflute.taglib.base;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.lastaflute.core.message.UserMessage;
import org.lastaflute.core.util.ContainerUtil;
import org.lastaflute.web.servlet.request.RequestManager;
import org.lastaflute.web.servlet.session.SessionManager;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.DynamicAttributes;
import jakarta.servlet.jsp.tagext.TagSupport;

/**
 * @author modified by jflute (originated in Struts)
 */
public abstract class BaseNonBodyTag extends TagSupport implements DynamicAttributes {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;
    protected static final String[] EMPTY_STRING_ARRAY = new String[0];

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                               Control
    //                                               -------
    protected Set<DynamicTagAttribute> dynamicAttributes; // lazy loaded

    // ===================================================================================
    //                                                                   Dynamic Attribute
    //                                                                   =================
    @Override
    public void setDynamicAttribute(String uri, String localName, Object value) throws JspException {
        getEnhanceLogic().addDynamicAttribute(getDynamicAttributes(), localName, value);
    }

    protected void prepareDynamicAttributes(StringBuilder sb) {
        sb.append(buildDynamicAttributeExp());
        clearDynamicAttributes(); // taglib instance may be shared with other tags so need to clear
    }

    protected String buildDynamicAttributeExp() {
        return getEnhanceLogic().buildDynamicAttributeExp(getDynamicAttributes());
    }

    protected Set<DynamicTagAttribute> getDynamicAttributes() {
        if (dynamicAttributes == null) {
            dynamicAttributes = new LinkedHashSet<DynamicTagAttribute>(4);
        }
        return dynamicAttributes;
    }

    protected void clearDynamicAttributes() {
        if (dynamicAttributes != null) {
            dynamicAttributes.clear();
            dynamicAttributes = null;
        }
    }

    // ===================================================================================
    //                                                                       Enhance Logic
    //                                                                       =============
    protected boolean present(String key) throws JspException {
        return getEnhanceLogic().present(pageContext, key);
    }

    protected String message(String key) throws JspException {
        return getEnhanceLogic().message(pageContext, key, prepareCallerInfo());
    }

    protected String message(String key, Object[] args) throws JspException {
        return getEnhanceLogic().message(pageContext, key, args, prepareCallerInfo());
    }

    protected String message(UserMessage report) throws JspException {
        return getEnhanceLogic().message(pageContext, report, prepareCallerInfo());
    }

    protected void write(String text) throws JspException {
        getEnhanceLogic().write(pageContext, text);
    }

    protected void writePrevious(String text) throws JspException {
        getEnhanceLogic().writePrevious(pageContext, text);
    }

    protected Object lookupBean(String beanName) throws JspException {
        return getEnhanceLogic().lookupBean(pageContext, beanName, null, prepareCallerInfo());
    }

    protected Object lookupProperty(String beanName, String property) throws JspException {
        return getEnhanceLogic().lookupProperty(pageContext, beanName, property, null, prepareCallerInfo()); // no lambda for jetty6
    }

    protected String findLabelResourceChecked(String label) {
        return getEnhanceLogic().findLabelResourceChecked(pageContext, label, prepareCallerInfo());
    }

    protected Supplier<Object> prepareCallerInfo() {
        return new Supplier<Object>() {
            public Object get() {
                return buildErrorIdentity();
            }
        };
    }

    protected String getElementClose() {
        return ">";
    }

    protected abstract String buildErrorIdentity();

    protected TaglibEnhanceLogic getEnhanceLogic() {
        return TaglibEnhanceLogic.getInstance();
    }

    // ===================================================================================
    //                                                                           Component
    //                                                                           =========
    protected RequestManager getRequestManager() {
        return getComponent(RequestManager.class);
    }

    protected SessionManager getSessionManager() {
        return getComponent(SessionManager.class);
    }

    protected <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) {
        return ContainerUtil.getComponent(type);
    }

    // ===================================================================================
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        clearDynamicAttributes();
    }
}
