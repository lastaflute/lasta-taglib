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
package org.lastaflute.taglib.bean;

import javax.servlet.jsp.JspException;

import org.lastaflute.taglib.base.BaseNonBodyTag;

/**
 * The tag for label resource.
 * <pre>
 * e.g.
 *  html:label key="labels.foo"
 *  html:label key="labels.foo|labels.list"
 * </pre>
 * @author jflute
 */
public class BeanCaptionTag extends BaseNonBodyTag {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String key;

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    public int doStartTag() throws JspException {
        getEnhanceLogic().write(pageContext, findLabelResourceChecked(key));
        return SKIP_BODY;
    }

    // ===================================================================================
    //                                                                      Error Identity
    //                                                                      ==============
    @Override
    protected String buildErrorIdentity() {
        return "key=" + key + " tag=" + getClass().getName();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
