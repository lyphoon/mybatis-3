/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.logging.slf4j;

import org.apache.ibatis.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 *
 * 它不是正常的适配器模式!!
 *
 *
 *
 * 使用适配器设计模式，它是Slf4j log的适配器，它持有被适配的对象Log(Slf4j日志对象)
 * 在构造器中，构造相应的被适配对象
 */
public class Slf4jImpl implements Log {

  private Log log;  //mybatis的Log接口

  /**
   * slf4j 1.6之前与之后的Logger是使用了不同的接口。对于不同版本的sl4j，都有一个相应的适配器对象，
   * Slf4jLocationAwareLoggerImpl为1.6版本之后的适配器，Slf4jLoggerImpl为1.6之前的适配器。
   *
   * 判断版本，找到相应的适配器，因为适配器本身都实现同一个接口Log(多态)，将不同的版本对应的适配器给Log log并持有，
   * 在本类中，Log接口的所有方法，都是使用这个log实现。
   *
   * 其实不同版本的适配器，内部也会持有被适配的对象，这里是Logger logger（通过构造器传入），
   * 这样，Log所有方法，都会调用logger的方法，因为不同版本的适配器都是通过logger实现Log接口
   */
  public Slf4jImpl(String clazz) {
    Logger logger = LoggerFactory.getLogger(clazz);  //sl4j

    if (logger instanceof LocationAwareLogger) {
      try {
        // check for slf4j >= 1.6 method signature
        logger.getClass().getMethod("log", Marker.class, String.class, int.class, String.class, Object[].class, Throwable.class);
        log = new Slf4jLocationAwareLoggerImpl((LocationAwareLogger) logger);
        return;
      } catch (SecurityException e) {
        // fail-back to Slf4jLoggerImpl
      } catch (NoSuchMethodException e) {
        // fail-back to Slf4jLoggerImpl
      }
    }

    // Logger is not LocationAwareLogger or slf4j version < 1.6
    log = new Slf4jLoggerImpl(logger);
  }

  @Override
  public boolean isDebugEnabled() {
    return log.isDebugEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return log.isTraceEnabled();
  }

  @Override
  public void error(String s, Throwable e) {
    log.error(s, e);
  }

  @Override
  public void error(String s) {
    log.error(s);
  }

  @Override
  public void debug(String s) {
    log.debug(s);
  }

  @Override
  public void trace(String s) {
    log.trace(s);
  }

  @Override
  public void warn(String s) {
    log.warn(s);
  }

}
