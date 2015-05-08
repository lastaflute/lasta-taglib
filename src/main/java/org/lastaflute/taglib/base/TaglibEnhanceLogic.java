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
package org.lastaflute.taglib.base;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.jdbc.ClassificationMeta;
import org.dbflute.util.DfStringUtil;
import org.lastaflute.core.direction.FwAssistantDirector;
import org.lastaflute.core.message.MessageManager;
import org.lastaflute.core.message.exception.MessageKeyNotFoundException;
import org.lastaflute.core.util.ContainerUtil;
import org.lastaflute.db.dbflute.classification.ListedClassificationProvider;
import org.lastaflute.db.dbflute.exception.ProvidedClassificationNotFoundException;
import org.lastaflute.di.helper.beans.BeanDesc;
import org.lastaflute.di.helper.beans.PropertyDesc;
import org.lastaflute.di.helper.beans.factory.BeanDescFactory;
import org.lastaflute.taglib.exception.TaglibAutocompleteInvalidValueException;
import org.lastaflute.taglib.exception.TaglibBeanPropertyNotFoundException;
import org.lastaflute.taglib.exception.TaglibClassificationNotFoundException;
import org.lastaflute.taglib.exception.TaglibLabelsResourceNotFoundException;
import org.lastaflute.taglib.exception.TaglibMessagesResourceNotFoundException;
import org.lastaflute.taglib.function.LaFunctions;
import org.lastaflute.web.exception.FormPropertyNotFoundException;
import org.lastaflute.web.ruts.VirtualActionForm;
import org.lastaflute.web.ruts.message.ActionMessage;
import org.lastaflute.web.ruts.message.ActionMessages;
import org.lastaflute.web.ruts.message.objective.ObjectiveMessageResources;
import org.lastaflute.web.servlet.request.RequestManager;

/**
 * @author modified by jflute (originated in Struts)
 */
public class TaglibEnhanceLogic {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final TaglibEnhanceLogic instance = new TaglibEnhanceLogic();
    protected static final Map<String, Integer> scopes = new HashMap<String, Integer>();
    static {
        scopes.put("page", new Integer(PageContext.PAGE_SCOPE));
        scopes.put("request", new Integer(PageContext.REQUEST_SCOPE));
        scopes.put("session", new Integer(PageContext.SESSION_SCOPE));
        scopes.put("application", new Integer(PageContext.APPLICATION_SCOPE));
    }

    /** The key prefix for labels of message resources, which contains dot at last. */
    protected static final String LABELS_KEY_PREFIX = ObjectiveMessageResources.LABELS_KEY_PREFIX;

    /** The key prefix for messages of message resources, which contains dot at last. */
    protected static final String MESSAGES_KEY_PREFIX = ObjectiveMessageResources.MESSAGES_KEY_PREFIX;

    /** The empty string array for empty result. (NotNull) */
    protected static final String[] EMPTY_STRING_ARRAY = new String[] {};

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected TaglibEnhanceLogic() {
    }

    public static TaglibEnhanceLogic getInstance() {
        return instance;
    }

    // ===================================================================================
    //                                                                         Basic Logic
    //                                                                         ===========
    public String encode(String url) {
        return LaFunctions.u(url);
    }

    public String filter(String value) {
        if (value == null || value.length() == 0) {
            return value;
        }
        StringBuilder sb = null;
        String filtered = null;
        for (int i = 0; i < value.length(); i++) {
            filtered = null;
            switch (value.charAt(i)) {
            case '<':
                filtered = "&lt;";
                break;
            case '>':
                filtered = "&gt;";
                break;
            case '&':
                filtered = "&amp;";
                break;
            case '"':
                filtered = "&quot;";
                break;
            case '\'':
                filtered = "&#39;";
                break;
            }
            if (sb == null) {
                if (filtered != null) {
                    sb = new StringBuilder(value.length() + 50);
                    sb.append(i > 0 ? value.substring(0, i) : "").append(filtered);
                }
            } else {
                sb.append(filtered != null ? filtered : value.charAt(i));
            }
        }
        return sb != null ? sb.toString() : value;
    }

    // ===================================================================================
    //                                                                       Bean Handling
    //                                                                       =============
    // -----------------------------------------------------
    //                                  Lookup Bean/Property
    //                                  --------------------
    public Object lookupBean(PageContext pageContext, String beanName, String scope, Supplier<Object> callerInfo) throws JspException {
        final Object bean = scope != null ? pageContext.getAttribute(beanName, getScope(scope)) : pageContext.findAttribute(beanName);
        if (bean == null) {
            // TODO jflute lastaflute: [E] fitting: JspException error message
            throw new JspException("Not found the bean: beanName=" + beanName);
        }
        return bean;
    }

    public Object lookupProperty(PageContext pageContext, String beanName, String property, String scope, Supplier<Object> callerInfo)
            throws JspException {
        final Object bean = lookupBean(pageContext, beanName, scope, callerInfo);
        if (bean == null) {
            String msg = "Not found the bean: name=" + beanName + " property=" + property + " scope=" + scope;
            throw new JspException(msg);
        }
        return getProperty(bean, property, callerInfo);
    }

    protected int getScope(String scopeName) throws JspException {
        final String lowerCase = scopeName.toLowerCase();
        final Integer scope = scopes.get(lowerCase);
        if (scope == null) {
            String msg = "Not found the scope: specified=" + lowerCase + " existing=" + scopes.keySet();
            throw new JspException(msg);
        }
        return scope.intValue();
    }

    // -----------------------------------------------------
    //                                        Specified Bean
    //                                        --------------
    public <VALUE> VALUE getProperty(Object bean, String property, Supplier<Object> callerInfo) {
        if (bean instanceof VirtualActionForm) {
            return getFormPropertyValue((VirtualActionForm) bean, property, callerInfo);
        }
        final BeanDesc beanDesc = BeanDescFactory.getBeanDesc(bean.getClass());
        final PropertyDesc propertyDesc = beanDesc.getPropertyDesc(property);
        if (propertyDesc == null) {
            String msg = "Not found the property: property=" + property + " caller=" + callerInfo.get();
            throw new TaglibBeanPropertyNotFoundException(msg);
        }
        @SuppressWarnings("unchecked")
        final VALUE found = (VALUE) propertyDesc.getValue(bean);
        return found;
    }

    public String[] getPropertyAsStringArray(Object bean, String property, Supplier<Object> callerInfo) {
        final Object propertyValue = getProperty(bean, property, callerInfo);
        if (propertyValue == null) {
            return (String[]) EMPTY_STRING_ARRAY;
        }
        if (propertyValue instanceof Collection<?>) {
            @SuppressWarnings("unchecked")
            final Collection<Object> col = (Collection<Object>) propertyValue;
            final String[] ary = new String[col.size()];
            int index = 0;
            for (Object obj : col) {
                ary[index] = obj != null ? obj.toString() : null;
                ++index;
            }
            return ary;
        } else if (propertyValue.getClass().isArray()) {
            final int len = Array.getLength(propertyValue);
            final String[] ary = new String[len];
            for (int i = 0; i < len; i++) {
                final Object element = Array.get(propertyValue, i);
                ary[i] = element != null ? element.toString() : null;
            }
            return ary;
        } else {
            final String[] ary = new String[1];
            ary[0] = propertyValue.toString();
            return ary;
        }
    }

    protected <VALUE> VALUE getFormPropertyValue(VirtualActionForm form, String property, Supplier<Object> callerInfo) {
        try {
            @SuppressWarnings("unchecked")
            final VALUE found = (VALUE) form.getPropertyValue(property);
            return found;
        } catch (FormPropertyNotFoundException e) {
            String msg = "Not found the taglib bean property: caller=" + callerInfo.get();
            throw new TaglibBeanPropertyNotFoundException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                      Action Mapping
    //                                                                      ==============
    public ActionMessages findActionMessages(PageContext pageContext, String paramName) throws JspException {
        final ActionMessages messages = (ActionMessages) pageContext.findAttribute(paramName);
        return messages != null ? messages : new ActionMessages(); // if null, empty
    }

    // ===================================================================================
    //                                                                             Message
    //                                                                             =======
    public boolean present(PageContext pageContext, String key) {
        return getMessageManager().findMessage(getUserLocale(), key).isPresent();
    }

    public String message(PageContext pageContext, String key, Supplier<Object> callerInfo) {
        return message(pageContext, key, null, callerInfo);
    }

    public String message(PageContext pageContext, ActionMessage report, Supplier<Object> callerInfo) {
        final String key = report.getKey();
        return report.isResource() ? message(pageContext, key, report.getValues(), callerInfo) : key;
    }

    public String message(PageContext pageContext, String key, Object[] args, Supplier<Object> callerInfo) {
        final MessageManager manager = getMessageManager();
        final Locale locale = getUserLocale();
        try {
            return args != null ? manager.getMessage(locale, key, args) : manager.getMessage(locale, key);
        } catch (MessageKeyNotFoundException e) {
            throwMessagesResourceNotFoundException(key, locale, callerInfo, e);
            return null; // unreachable
        }
    }

    protected void throwMessagesResourceNotFoundException(String resourceKey, Locale locale, Supplier<Object> callerInfo,
            MessageKeyNotFoundException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the resource for message by the key.");
        br.addItem("Requested JSP Path");
        br.addElement(getRequestJspPath());
        br.addItem("Target Taglib");
        br.addElement(callerInfo.get());
        br.addItem("Resource Key");
        br.addElement(resourceKey);
        br.addItem("User Locale");
        br.addElement(locale);
        final String msg = br.buildExceptionMessage();
        throw new TaglibMessagesResourceNotFoundException(msg, cause);
    }

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    public void write(PageContext pageContext, String text) throws JspException {
        final JspWriter writer = pageContext.getOut();
        try {
            writer.print(text);
        } catch (IOException e) {
            String msg = "Failed to print the text by the writer: writer=" + writer + " text=" + text;
            throw new IllegalStateException(msg, e);
        }
    }

    public void writePrevious(PageContext pageContext, String text) throws JspException {
        JspWriter writer = pageContext.getOut();
        if (writer instanceof BodyContent) {
            writer = ((BodyContent) writer).getEnclosingWriter();
        }
        try {
            writer.print(text);
        } catch (IOException e) {
            String msg = "Failed to print the text by the writer: writer=" + writer + " text=" + text;
            throw new IllegalStateException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                       Auto Complete
    //                                                                       =============
    /**
     * @param pageContext The context of page. (NotNull)
     * @param value The value of autocomplete attribute, 'on' or 'off'. (NullAllowed: if null, returns null)
     * @param callerInfo The supplier of caller type for exception message. (NotNull)
     * @return The resolved value of autocomplete. (NullAllowed: when the autocomplete is null)
     * @throws BrTaglibLabelsResourceNotFoundException When the resource key is not found in the resource if the key is for label.
     */
    public String resolveAutocompleteResource(PageContext pageContext, String value, Supplier<Object> callerInfo) {
        if (value != null && !value.equals("on") && !value.equals("off")) {
            throwAutocompleteInvalidValueException(value, callerInfo);
        }
        return value;
    }

    protected void throwAutocompleteInvalidValueException(String value, Supplier<Object> callerInfo) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Invalid value for autocomplete attribute.");
        br.addItem("Requested JSP Path");
        br.addElement(getRequestJspPath());
        br.addItem("Target Taglib");
        br.addElement(callerInfo.get());
        br.addItem("Invalid Value");
        br.addElement(value);
        br.addItem("Expected Value");
        br.addElement("'on' or 'off'");
        final String msg = br.buildExceptionMessage();
        throw new TaglibAutocompleteInvalidValueException(msg);
    }

    // ===================================================================================
    //                                                                      Label Resource
    //                                                                      ==============
    /**
     * Resolve the label resource by message resources.
     * @param pageContext The context of page. (NotNull)
     * @param label The label value, might be resource key. (NullAllowed: if null, returns null)
     * @param callerInfo The supplier of caller type for exception message. (NotNull)
     * @return The resolved value of label. (NullAllowed: when the label is null)
     * @throws TaglibLabelsResourceNotFoundException When the resource key is not found in the resource if the key is for label.
     */
    public String resolveLabelResource(PageContext pageContext, String label, Supplier<Object> callerInfo) {
        final String found = findLabelResourceIfNeeds(pageContext, label, callerInfo);
        return found != null ? found : label;
    }

    /**
     * Find the label resource if the key is for label.
     * @param pageContext The context of page. (NotNull)
     * @param resourceKey The resource key of label. (NullAllowed: if null, returns null)
     * @param callerInfo The supplier of caller type for exception message. (NotNull)
     * @return The resolved value of label. (NullAllowed: when not found)
     * @throws TaglibLabelsResourceNotFoundException When the resource key is not found in the resource if the key is for label.
     */
    public String findLabelResourceIfNeeds(PageContext pageContext, String resourceKey, Supplier<Object> callerInfo) {
        if (resourceKey == null) {
            return null;
        }
        if (isLabelsResource(resourceKey)) {
            final List<String> keyList = DfStringUtil.splitListTrimmed(resourceKey, "|");
            final StringBuilder sb = new StringBuilder();
            for (String key : keyList) {
                final String resolved = message(pageContext, key, null, callerInfo);
                if (resolved == null) {
                    throwLabelsResourceNotFoundException(key, callerInfo);
                }
                sb.append(resolved);
            }
            return sb.toString();
        }
        return null;
    }

    /**
     * Is the resource for label?
     * @param resouce The value of resource. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isLabelsResource(String resouce) {
        return resouce.startsWith(LABELS_KEY_PREFIX) || resouce.startsWith(MESSAGES_KEY_PREFIX);
    }

    /**
     * Find the label resource with checking the resource is for label or not.
     * @param pageContext The context of page. (NotNull)
     * @param resourceKey The resource key of label. (NotNull: if null, exception)
     * @param callerInfo The supplier of caller type for exception message. (NotNull)
     * @return The resolved value of label. (NotNull: exception when not found)
     * @throws TaglibLabelsResourceNotFoundException When the resource key is not for label or null.
     */
    public String findLabelResourceChecked(PageContext pageContext, String resourceKey, Supplier<Object> callerInfo) {
        final String resource = findLabelResourceIfNeeds(pageContext, resourceKey, callerInfo);
        if (resource == null) {
            throwLabelsResourceNotFoundException(resourceKey, callerInfo);
        }
        return resource;
    }

    protected void throwLabelsResourceNotFoundException(String resourceKey, Supplier<Object> callerInfo) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the resource for label by the key.");
        br.addItem("Requested JSP Path");
        br.addElement(getRequestJspPath());
        br.addItem("Target Taglib");
        br.addElement(callerInfo.get());
        br.addItem("Resource Key");
        br.addElement(resourceKey);
        final String msg = br.buildExceptionMessage();
        throw new TaglibLabelsResourceNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                   Dynamic Attribute
    //                                                                   =================
    public void addDynamicAttribute(Set<DynamicTagAttribute> dynamicAttributes, String key, Object value) {
        if (key != null && key.trim().length() > 0) {
            dynamicAttributes.add(createDynamicTagAttribute(key, value));
        }
    }

    protected DynamicTagAttribute createDynamicTagAttribute(String key, Object value) {
        return new DynamicTagAttribute(key, value != null ? value.toString() : null);
    }

    public String buildDynamicAttributeExp(Set<DynamicTagAttribute> dynamicAttributes) {
        final StringBuilder sb = new StringBuilder();
        for (DynamicTagAttribute attribute : dynamicAttributes) {
            final Object val = attribute.getValue();
            if (val == null) {
                sb.append(" " + attribute.getKey());
            } else {
                sb.append(" " + attribute.getKey() + "=\"");
                sb.append(escapeInnerDoubleQuote(attribute.getValue().toString()) + "\"");
            }
        }
        return sb.toString();
    }

    protected String escapeInnerDoubleQuote(String str) {
        if (str == null || str.trim().length() == 0) {
            return str;
        }
        final String filtered;
        if (str.contains("\\") || str.contains("\"")) {
            final CharArrayWriter caw = new CharArrayWriter(128);
            final char[] chars = str.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '\\') {
                    caw.append(chars[i]);
                } else if (chars[i] == '"') {
                    caw.append('\\');
                }
                caw.append(chars[i]);
            }
            filtered = caw.toString();
        } else {
            filtered = str;
        }
        return filtered;
    }

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    public ListedClassificationProvider getListedClassificationProvider() {
        return getAssistantDirector().assistDbDirection().assistListedClassificationProvider();
    }

    public ClassificationMeta provideClassificationMeta(ListedClassificationProvider provider, String classificationName,
            Supplier<Object> callerInfo) {
        try {
            return provider.provide(classificationName);
        } catch (ProvidedClassificationNotFoundException e) {
            throwClassificationNotFoundException(classificationName, callerInfo);
            return null; // unreachable
        }
    }

    protected void throwClassificationNotFoundException(String classificationName, Supplier<Object> callerInfo) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the classification for the list.");
        br.addItem("Requested JSP Path");
        br.addElement(getRequestJspPath());
        br.addItem("Target Taglib");
        br.addElement(callerInfo.get());
        br.addItem("Classification Name");
        br.addElement(classificationName);
        final String msg = br.buildExceptionMessage();
        throw new TaglibClassificationNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                         User Locale
    //                                                                         ===========
    public Locale getUserLocale() {
        return getRequestManager().getUserLocale();
    }

    // ===================================================================================
    //                                                                  Requested JSP Path
    //                                                                  ==================
    public String getRequestJspPath() {
        return getRequestManager().getRequestPath();
    }

    // ===================================================================================
    //                                                                           Component
    //                                                                           =========
    protected FwAssistantDirector getAssistantDirector() {
        return getComponent(FwAssistantDirector.class);
    }

    protected MessageManager getMessageManager() {
        return getComponent(MessageManager.class);
    }

    protected RequestManager getRequestManager() {
        return getComponent(RequestManager.class);
    }

    protected <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) {
        return ContainerUtil.getComponent(type);
    }
}
