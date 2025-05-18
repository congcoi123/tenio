/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.tenio.core.bootstrap.test;

import com.tenio.core.bootstrap.annotation.Autowired;
import com.tenio.core.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.AutowiredQualifier;
import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.bootstrap.bean.TestBeanClass;
import com.tenio.core.bootstrap.test.factory.TestBeanCommon;
import com.tenio.core.bootstrap.test.impl.TestClassAlone;
import com.tenio.core.bootstrap.test.impl.TestClassCCopy;
import com.tenio.core.bootstrap.test.inf.TestInterfaceA;
import com.tenio.core.bootstrap.test.inf.TestInterfaceB;
import com.tenio.core.bootstrap.test.inf.TestInterfaceC;

@Component
public class BootstrapComponent {

  @Autowired
  public TestInterfaceA a;

  /**
   * This declaration should not throw any exceptions while scanning packages
   */
  @AutowiredAcceptNull
  public TestInterfaceB b;

  @Autowired
  @AutowiredQualifier(
      clazz = TestClassCCopy.class
  )
  public TestInterfaceC c;

  @Autowired
  public TestClassAlone alone;

  @Autowired
  public TestBeanClass bean;

  @Autowired
  public TestBeanCommon beanCommon;

  @Autowired
  @AutowiredQualifier(
      name = "commonA"
  )
  public TestBeanCommon beanCommonA;

  @Autowired
  @AutowiredQualifier(
      name = "commonB"
  )
  public TestBeanCommon beanCommonB;
}
