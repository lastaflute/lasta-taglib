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
package org.dbflute.lasta.taglib.html;

import java.util.Iterator;

import javax.servlet.jsp.JspException;

import org.dbflute.lasta.taglib.base.BaseNonBodyTag;
import org.dbflute.lastaflute.web.LastaWebKey;
import org.dbflute.lastaflute.web.ruts.message.ActionMessage;
import org.dbflute.lastaflute.web.ruts.message.ActionMessages;

/**
 * @author modified by jflute (originated in Struts)
 */
public class HtmlErrorsTag extends BaseNonBodyTag {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String property; // item name of message, not required
    protected String header;
    protected String footer;
    protected String prefix;
    protected String suffix;

    /** The style class for the errors tag. (NullAllowed) */
    protected String styleClass;

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    public int doStartTag() throws JspException {
        final ActionMessages errors = extractActionErrors();
        if (errors.isEmpty()) {
            return EVAL_BODY_INCLUDE;
        }
        final String tagExp = buildTagExp(errors);
        write(tagExp);

        // clear temporary session message here
        // you can give message to next action when redirect safely
        //
        // time passed!
        // we then heard a voice coming out of nowhere...
        // errors on session are removed at next request at RequestProcessor#processCachedMessages()
        // so you don't need to remove them here
        //
        // time passed!
        // SessionManager may have external session so it needs to remove by session manager
        // also in other cases, it should not depend on Struts session handling
        clearSessionGlobalErrorsIfNeeds();

        return EVAL_BODY_INCLUDE;
    }

    protected ActionMessages extractActionErrors() throws JspException {
        return getEnhanceLogic().findActionMessages(pageContext, getMessagesAttributeKey());
    }

    protected String getMessagesAttributeKey() {
        return LastaWebKey.ACTION_ERRORS_KEY;
    }

    protected String buildTagExp(ActionMessages errors) throws JspException {
        final boolean headerPresent = present(getHeader());
        final boolean footerPresent = present(getFooter());
        final boolean prefixPresent = present(getPrefix());
        final boolean suffixPresent = present(getSuffix());
        final StringBuilder results = new StringBuilder();
        boolean headerDone = false;
        final Iterator<ActionMessage> reports = toMessageIterator(errors);
        while (reports.hasNext()) {
            final ActionMessage report = reports.next();
            if (!headerDone) {
                setupHeader(headerPresent, results);
                headerDone = true;
            }
            final String mainMessage = prepareMainMessage(report);
            if (!isSuppressMessageLine(results, report, mainMessage)) {
                if (prefixPresent) {
                    setupPrefix(results, mainMessage);
                }
                setupMessage(results, report, mainMessage);
                if (suffixPresent) {
                    setupSuffix(results, mainMessage);
                }
            }
        }
        if (headerDone && footerPresent) {
            setupFooter(results);
        }
        return results.toString();
    }

    protected Iterator<ActionMessage> toMessageIterator(ActionMessages errors) {
        return property != null ? errors.get(property) : errors.get();
    }

    protected String prepareMainMessage(ActionMessage report) throws JspException {
        return message(report);
    }

    protected boolean isSuppressMessageLine(StringBuilder results, ActionMessage report, String mainMessage) {
        return false; // you can add original rule by overriding
    }

    // -----------------------------------------------------
    //                                           Setup Parts
    //                                           -----------
    protected void setupHeader(boolean headerPresent, StringBuilder sb) throws JspException {
        if (styleClass != null) {
            sb.append("<ul class=\"" + styleClass + "\">");
        } else if (headerPresent) {
            sb.append(message(getHeader()));
        }
    }

    protected void setupPrefix(StringBuilder results, String mainMessage) throws JspException {
        results.append(message(getPrefix()));
    }

    protected void setupMessage(StringBuilder results, ActionMessage report, String mainMessage) throws JspException {
        if (mainMessage != null) {
            results.append(mainMessage);
        }
    }

    protected void setupSuffix(StringBuilder results, String mainMessage) throws JspException {
        results.append(message(getSuffix()));
    }

    protected void setupFooter(StringBuilder results) throws JspException {
        results.append(message(getFooter()));
    }

    // -----------------------------------------------------
    //                                        Global Message
    //                                        --------------
    protected void clearSessionGlobalErrorsIfNeeds() {
        getSessionManager().errors().clear();
    }

    // ===================================================================================
    //                                                                      Error Identity
    //                                                                      ==============
    @Override
    protected String buildErrorIdentity() {
        return "property=" + property + " tag=" + getClass().getName();
    }

    // ===================================================================================
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        property = null;
        header = null;
        footer = null;
        prefix = null;
        suffix = null;
        styleClass = null;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getProperty() {
        return this.property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getHeader() {
        return header == null ? "errors.header" : header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFooter() {
        return footer == null ? "errors.footer" : footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public String getPrefix() {
        return prefix == null ? "errors.prefix" : prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix == null ? "errors.suffix" : suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * Set the style class for the errors tag.
     * @param styleClass The string for style class. (NotNull)
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }
}
