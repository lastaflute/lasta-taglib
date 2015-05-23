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
package org.lastaflute.taglib.html;

import java.util.Iterator;

import javax.servlet.jsp.JspException;

import org.lastaflute.taglib.base.BaseBodyTag;
import org.lastaflute.web.LastaWebKey;
import org.lastaflute.web.ruts.message.ActionMessage;
import org.lastaflute.web.ruts.message.ActionMessages;

/**
 * @author modified by jflute (originated in Struts)
 */
public class HtmlInfoTag extends BaseBodyTag {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // id is required, to use message in content
    protected String property; // item name of message, not required
    protected String header;
    protected String footer;

    // -----------------------------------------------------
    //                                     Internal Handling
    //                                     -----------------
    protected Iterator<ActionMessage> iterator;
    protected boolean processed;

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    public int doStartTag() throws JspException {
        processed = false;
        final ActionMessages messages = extractActionMessages();
        iterator = toMessageIterator(messages != null ? messages : new ActionMessages());
        if (!iterator.hasNext()) {
            return SKIP_BODY;
        }
        pageContext.setAttribute(id, message(iterator.next()));
        if (header != null) {
            write(message(header));
        }
        processed = true;
        return EVAL_BODY_BUFFERED;
    }

    protected ActionMessages extractActionMessages() throws JspException {
        return getEnhanceLogic().findActionMessages(pageContext, getMessagesAttributeKey());
    }

    protected String getMessagesAttributeKey() {
        return LastaWebKey.ACTION_INFO_KEY;
    }

    protected Iterator<ActionMessage> toMessageIterator(ActionMessages messages) {
        return property != null ? messages.accessByIteratorOf(property) : messages.accessByFlatIterator();
    }

    // ===================================================================================
    //                                                                          After Body
    //                                                                          ==========
    @Override
    public int doAfterBody() throws JspException {
        if (bodyContent != null) {
            writePrevious(bodyContent.getString());
            bodyContent.clearBody();
        }
        if (iterator.hasNext()) {
            pageContext.setAttribute(id, message(iterator.next()));
            return EVAL_BODY_AGAIN;
        } else {
            return SKIP_BODY;
        }
    }

    // ===================================================================================
    //                                                                             End Tag
    //                                                                             =======
    @Override
    public int doEndTag() throws JspException {
        if (processed && footer != null) {
            write(message(footer));
        }
        clearSessionGlobalInfoIfNeeds(); // *see comment on errors tag for the details
        return EVAL_PAGE;
    }

    protected void clearSessionGlobalInfoIfNeeds() {
        getSessionManager().info().clear();
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
        id = null;
        property = null;
        header = null;
        footer = null;
        iterator = null;
        processed = false;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getProperty() {
        return (this.property);
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getHeader() {
        return (this.header);
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFooter() {
        return (this.footer);
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }
}
