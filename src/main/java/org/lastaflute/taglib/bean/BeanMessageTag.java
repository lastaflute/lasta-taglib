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
package org.lastaflute.taglib.bean;

import javax.servlet.jsp.JspException;

import org.lastaflute.taglib.base.BaseNonBodyTag;

/**
 * @author modified by jflute (originated in Struts)
 */
public class BeanMessageTag extends BaseNonBodyTag {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String key; // required
    protected String arg0;
    protected String arg1;
    protected String arg2;
    protected String arg3;
    protected String arg4;

    // ===================================================================================
    //                                                                           Start Tag
    //                                                                           =========
    @Override
    public int doStartTag() throws JspException {
        write(message(key, new Object[] { arg0, arg1, arg2, arg3, arg4 }));
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
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        key = null;
        arg0 = null;
        arg1 = null;
        arg2 = null;
        arg3 = null;
        arg4 = null;
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

    public String getArg0() {
        return arg0;
    }

    public void setArg0(String arg0) {
        this.arg0 = arg0;
    }

    public String getArg1() {
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public void setArg2(String arg2) {
        this.arg2 = arg2;
    }

    public String getArg3() {
        return arg3;
    }

    public void setArg3(String arg3) {
        this.arg3 = arg3;
    }

    public String getArg4() {
        return arg4;
    }

    public void setArg4(String arg4) {
        this.arg4 = arg4;
    }
}
