/*
 * Copyright 2015-2024 the original author or authors.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.lastaflute.taglib.html.HtmlFormTag;
import org.lastaflute.web.LastaWebKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

/**
 * @author modified by jflute (originated in Struts)
 */
public abstract class BaseTouchableBodyTag extends BaseBodyTag {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(BaseTouchableBodyTag.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                 Navigation Management
    //                                 ---------------------
    protected String accesskey;
    protected String tabindex;

    // -----------------------------------------------------
    //                          Indexing ability for Iterate
    //                          ----------------------------
    protected boolean indexed;

    // -----------------------------------------------------
    //                                          Mouse Events
    //                                          ------------
    protected String onclick;
    protected String ondblclick;
    protected String onmouseover;
    protected String onmouseout;
    protected String onmousemove;
    protected String onmousedown;
    protected String onmouseup;

    // -----------------------------------------------------
    //                                       Keyboard Events
    //                                       ---------------
    protected String onkeydown;
    protected String onkeyup;
    protected String onkeypress;

    // -----------------------------------------------------
    //                                           Text Events
    //                                           -----------
    protected String onselect;
    protected String onchange;

    // -----------------------------------------------------
    //                               Focus Events and States
    //                               -----------------------
    protected String onblur;
    protected String onfocus;
    protected boolean disabled;
    protected boolean readonly;

    // -----------------------------------------------------
    //                                     CSS Style Support
    //                                     -----------------
    protected String style;
    protected String styleClass;
    protected String styleId;
    protected String errorKey = LastaWebKey.ACTION_ERRORS_KEY;
    protected String errorStyle;
    protected String errorStyleClass;
    protected String errorStyleId;

    // -----------------------------------------------------
    //                               Other Common Attributes
    //                               -----------------------
    protected String alt;
    protected String altKey;
    protected String title;
    protected String titleKey;

    // -----------------------------------------------------
    //                                     Internal Handling
    //                                     -----------------
    protected boolean doDisabled = true;
    protected boolean doReadonly;

    // -----------------------------------------------------
    //                                             JSTL Info
    //                                             ---------
    private Class<?> loopTagSupportClass;
    private Method loopTagSupportGetStatus;
    private Class<?> loopTagStatusClass;
    private Method loopTagStatusGetIndex;
    private boolean triedJstlInit;
    private boolean triedJstlSuccess;

    // ===================================================================================
    //                                                                   Prepare Attribute
    //                                                                   =================
    protected String prepareStyles() throws JspException {
        final StringBuilder sb = new StringBuilder();
        final boolean errorsExist = doErrorsExist();
        if (errorsExist && getErrorStyleId() != null) {
            prepareAttribute(sb, "id", getErrorStyleId());
        } else {
            prepareAttribute(sb, "id", getStyleId());
        }
        if (errorsExist && getErrorStyle() != null) {
            prepareAttribute(sb, "style", getErrorStyle());
        } else {
            prepareAttribute(sb, "style", getStyle());
        }
        if (errorsExist && getErrorStyleClass() != null) {
            prepareAttribute(sb, "class", getErrorStyleClass());
        } else {
            prepareAttribute(sb, "class", getStyleClass());
        }
        prepareAttribute(sb, "title", message(getTitle(), getTitleKey()));
        prepareAttribute(sb, "alt", message(getAlt(), getAltKey()));
        return sb.toString();
    }

    protected boolean doErrorsExist() throws JspException {
        boolean errorsExist = false;
        if (getErrorStyleId() != null || getErrorStyle() != null || getErrorStyleClass() != null) {
            final String actualName = prepareName();
            if (actualName != null) {
                errorsExist = getEnhanceLogic().findUserMessages(pageContext, errorKey).size(actualName) > 0;
            }
        }
        return errorsExist;
    }

    protected String prepareName() throws JspException {
        return null;
    }

    protected String message(String literal, String key) throws JspException {
        if (literal != null) {
            if (key != null) {
                String msg = "The literal and key should be either null: literal=" + literal + " key=" + key;
                throw new JspException(msg);
            } else {
                return literal;
            }
        } else {
            return key != null ? message(key) : null;
        }
    }

    protected String prepareEventHandlers() {
        final StringBuilder sb = new StringBuilder();
        prepareMouseEvents(sb);
        prepareKeyEvents(sb);
        prepareTextEvents(sb);
        prepareFocusEvents(sb);
        return sb.toString();
    }

    protected void prepareMouseEvents(StringBuilder sb) {
        prepareAttribute(sb, "onclick", getOnclick());
        prepareAttribute(sb, "ondblclick", getOndblclick());
        prepareAttribute(sb, "onmouseover", getOnmouseover());
        prepareAttribute(sb, "onmouseout", getOnmouseout());
        prepareAttribute(sb, "onmousemove", getOnmousemove());
        prepareAttribute(sb, "onmousedown", getOnmousedown());
        prepareAttribute(sb, "onmouseup", getOnmouseup());
    }

    protected void prepareKeyEvents(StringBuilder sb) {
        prepareAttribute(sb, "onkeydown", getOnkeydown());
        prepareAttribute(sb, "onkeyup", getOnkeyup());
        prepareAttribute(sb, "onkeypress", getOnkeypress());
    }

    protected void prepareTextEvents(StringBuilder sb) {
        prepareAttribute(sb, "onselect", getOnselect());
        prepareAttribute(sb, "onchange", getOnchange());
    }

    protected void prepareFocusEvents(StringBuilder sb) {
        prepareAttribute(sb, "onblur", getOnblur());
        prepareAttribute(sb, "onfocus", getOnfocus());
        HtmlFormTag formTag = null;
        if ((doDisabled && !getDisabled()) || (doReadonly && !getReadonly())) {
            formTag = (HtmlFormTag) pageContext.getAttribute(TaglibAttributeKey.FORM_KEY, PageContext.REQUEST_SCOPE);
        }
        if (doDisabled) {
            final boolean formDisabled = formTag == null ? false : formTag.isDisabled();
            if (formDisabled || getDisabled()) {
                sb.append(" disabled=\"disabled\"");
            }
        }
        if (doReadonly) {
            final boolean formReadOnly = formTag == null ? false : formTag.isReadonly();
            if (formReadOnly || getReadonly()) {
                sb.append(" readonly=\"readonly\"");
            }
        }
    }

    protected void prepareOtherAttributes(StringBuilder sb) {
    }

    protected void prepareAttribute(StringBuilder sb, String name, Object value) {
        if (value != null) {
            sb.append(" ").append(name).append("=\"").append(value).append("\"");
        }
    }

    // ===================================================================================
    //                                                                         Index Value
    //                                                                         ===========
    protected void prepareIndex(StringBuilder sb, String name) throws JspException {
        if (name != null) {
            sb.append(name);
        }
        sb.append("[").append(getIndexValue()).append("]");
        if (name != null) {
            sb.append(".");
        }
    }

    protected int getIndexValue() throws JspException {
        final Integer jstlLoopIndex = getJstlLoopIndex();
        if (jstlLoopIndex == null) {
            throw new JspException("Not nested in JSTL loop.");
        }
        return jstlLoopIndex.intValue();
    }

    protected Integer getJstlLoopIndex() {
        if (!triedJstlInit) {
            triedJstlInit = true;
            try {
                loopTagSupportClass = applicationClass("jakarta.servlet.jsp.jstl.core.LoopTagSupport");
                loopTagSupportGetStatus = loopTagSupportClass.getDeclaredMethod("getLoopStatus", (Class<?>[]) null);
                loopTagStatusClass = applicationClass("jakarta.servlet.jsp.jstl.core.LoopTagStatus");
                loopTagStatusGetIndex = loopTagStatusClass.getDeclaredMethod("getIndex", (Class<?>[]) null);
                triedJstlSuccess = true;
            } catch (ClassNotFoundException ignored) { // means JSTL not loaded
            } catch (NoSuchMethodException ignored) {}
        }
        if (triedJstlSuccess) {
            try {
                final Object loopTag = findAncestorWithClass(this, loopTagSupportClass);
                if (loopTag == null) {
                    return null;
                }
                final Object status = loopTagSupportGetStatus.invoke(loopTag, (Object[]) null);
                return (Integer) loopTagStatusGetIndex.invoke(status, (Object[]) null);
            } catch (IllegalAccessException e) {
                logger.error(e.getMessage(), e);
            } catch (IllegalArgumentException e) {
                logger.error(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                logger.error(e.getMessage(), e);
            } catch (NullPointerException e) {
                logger.error(e.getMessage(), e);
            } catch (ExceptionInInitializerError e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    protected Class<?> applicationClass(String className) throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        return classLoader.loadClass(className);
    }

    // ===================================================================================
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        accesskey = null;
        alt = null;
        altKey = null;
        errorKey = LastaWebKey.ACTION_ERRORS_KEY;
        errorStyle = null;
        errorStyleClass = null;
        errorStyleId = null;
        indexed = false;
        onclick = null;
        ondblclick = null;
        onmouseover = null;
        onmouseout = null;
        onmousemove = null;
        onmousedown = null;
        onmouseup = null;
        onkeydown = null;
        onkeyup = null;
        onkeypress = null;
        onselect = null;
        onchange = null;
        onblur = null;
        onfocus = null;
        disabled = false;
        readonly = false;
        style = null;
        styleClass = null;
        styleId = null;
        tabindex = null;
        title = null;
        titleKey = null;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                 Navigation Management
    //                                 ---------------------
    public void setAccesskey(String accessKey) {
        this.accesskey = accessKey;
    }

    public String getAccesskey() {
        return accesskey;
    }

    public void setTabindex(String tabIndex) {
        this.tabindex = tabIndex;
    }

    public String getTabindex() {
        return tabindex;
    }

    // -----------------------------------------------------
    //                          Indexing ability for Iterate
    //                          ----------------------------
    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public boolean getIndexed() {
        return indexed;
    }

    // -----------------------------------------------------
    //                                          Mouse Events
    //                                          ------------
    public void setOnclick(String onClick) {
        this.onclick = onClick;
    }

    public String getOnclick() {
        return onclick;
    }

    public void setOndblclick(String onDblClick) {
        this.ondblclick = onDblClick;
    }

    public String getOndblclick() {
        return ondblclick;
    }

    public void setOnmousedown(String onMouseDown) {
        this.onmousedown = onMouseDown;
    }

    public String getOnmousedown() {
        return onmousedown;
    }

    public void setOnmouseup(String onMouseUp) {
        this.onmouseup = onMouseUp;
    }

    public String getOnmouseup() {
        return onmouseup;
    }

    public void setOnmousemove(String onMouseMove) {
        this.onmousemove = onMouseMove;
    }

    public String getOnmousemove() {
        return onmousemove;
    }

    public void setOnmouseover(String onMouseOver) {
        this.onmouseover = onMouseOver;
    }

    public String getOnmouseover() {
        return onmouseover;
    }

    public void setOnmouseout(String onMouseOut) {
        this.onmouseout = onMouseOut;
    }

    public String getOnmouseout() {
        return onmouseout;
    }

    // -----------------------------------------------------
    //                                       Keyboard Events
    //                                       ---------------
    public void setOnkeydown(String onKeyDown) {
        this.onkeydown = onKeyDown;
    }

    public String getOnkeydown() {
        return onkeydown;
    }

    public void setOnkeyup(String onKeyUp) {
        this.onkeyup = onKeyUp;
    }

    public String getOnkeyup() {
        return onkeyup;
    }

    public void setOnkeypress(String onKeyPress) {
        this.onkeypress = onKeyPress;
    }

    public String getOnkeypress() {
        return onkeypress;
    }

    // -----------------------------------------------------
    //                                           Text Events
    //                                           -----------
    public void setOnchange(String onChange) {
        this.onchange = onChange;
    }

    public String getOnchange() {
        return onchange;
    }

    public void setOnselect(String onSelect) {
        this.onselect = onSelect;
    }

    public String getOnselect() {
        return onselect;
    }

    // -----------------------------------------------------
    //                               Focus Events and States
    //                               -----------------------
    public void setOnblur(String onBlur) {
        this.onblur = onBlur;
    }

    public String getOnblur() {
        return onblur;
    }

    public void setOnfocus(String onFocus) {
        this.onfocus = onFocus;
    }

    public String getOnfocus() {
        return onfocus;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean getReadonly() {
        return readonly;
    }

    // -----------------------------------------------------
    //                                     CSS Style Support
    //                                     -----------------
    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleId(String styleId) {
        this.styleId = styleId;
    }

    public String getStyleId() {
        return styleId;
    }

    public String getErrorKey() {
        return errorKey;
    }

    public void setErrorKey(String errorKey) {
        this.errorKey = errorKey;
    }

    public String getErrorStyle() {
        return errorStyle;
    }

    public void setErrorStyle(String errorStyle) {
        this.errorStyle = errorStyle;
    }

    public String getErrorStyleClass() {
        return errorStyleClass;
    }

    public void setErrorStyleClass(String errorStyleClass) {
        this.errorStyleClass = errorStyleClass;
    }

    public String getErrorStyleId() {
        return errorStyleId;
    }

    public void setErrorStyleId(String errorStyleId) {
        this.errorStyleId = errorStyleId;
    }

    // -----------------------------------------------------
    //                               Other Common Attributes
    //                               -----------------------
    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getAltKey() {
        return altKey;
    }

    public void setAltKey(String altKey) {
        this.altKey = altKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }
}
