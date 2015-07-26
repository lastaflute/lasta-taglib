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

import java.util.function.Supplier;

import javax.servlet.jsp.JspException;

import org.lastaflute.taglib.base.BaseNonBodyTag;
import org.lastaflute.taglib.base.TaglibEnhanceLogic;

/**
 * The tag for classification caption.
 * <pre>
 * e.g.
 *  bean:captionCls name="MemberStatus" value="FML"
 * </pre>
 * @author jflute
 */
public class BeanCaptionClsTag extends BaseNonBodyTag {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String name; // required
    protected String value; // required

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    public int doStartTag() throws JspException {
        final TaglibEnhanceLogic logic = getEnhanceLogic();
        final String alias = logic.findClassificationAlias(name, value, new Supplier<Object>() {
            public Object get() {
                return buildErrorIdentity();
            }
        }); // not lambda for Jetty6
        getEnhanceLogic().write(pageContext, alias);
        return SKIP_BODY;
    }

    // ===================================================================================
    //                                                                      Error Identity
    //                                                                      ==============
    @Override
    protected String buildErrorIdentity() {
        return "name=" + name + " value=" + value + " tag=" + getClass().getName();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
