/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.parsing;

import java.util.Properties;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 *
 * 属性解析器，静态工具类，构造器为private
 */
public class PropertyParser {

  private static final String KEY_PREFIX = "org.apache.ibatis.parsing.PropertyParser.";
  /**
   * The special property key that indicate whether enable a default value on placeholder.
   *
   * 是否开启占位符，默认false(ENABLE_DEFAULT_VALUE)。在property文件中key为org.apache.ibatis.parsing.PropertyParser.enable-default-value
   * <p>
   *   The default value is {@code false} (indicate disable a default value on placeholder)
   *   If you specify the {@code true}, you can specify key and default value on placeholder (e.g. {@code ${db.username:postgres}}).
   * </p>
   * @since 3.4.2
   */
  public static final String KEY_ENABLE_DEFAULT_VALUE = KEY_PREFIX + "enable-default-value";

  /**
   * The special property key that specify a separator for key and default value on placeholder.
   *
   * 占位符的分隔符，默认为':'(DEFAULT_VALUE_SEPARATOR)
   * <p>
   *   The default separator is {@code ":"}.
   * </p>
   * @since 3.4.2
   */
  public static final String KEY_DEFAULT_VALUE_SEPARATOR = KEY_PREFIX + "default-value-separator";

  private static final String ENABLE_DEFAULT_VALUE = "false";  //是否开启默认值功能
  private static final String DEFAULT_VALUE_SEPARATOR = ":";  //默认值的分隔符${db.username:postgres}，如果properties中没有db.useername，使用postgres为它的值

  private PropertyParser() {
    // Prevent Instantiation, 私有构造器，防止实例化
  }

  /**
   * 唯一主方法，将string中的占位符用Properties中的属性替换
   */
  public static String parse(String string, Properties variables) {
    VariableTokenHandler handler = new VariableTokenHandler(variables);
    GenericTokenParser parser = new GenericTokenParser("${", "}", handler);
    return parser.parse(string);
  }


  /**
   * 静态内部类，处理变量标记，如果开启默认值 varaible.get(xxx, defalutValue), 否则使用variable.get(xxx)，没有给一个${content}
   *
   * 主要是对一个占位符String（String都是占位符中的内容，${}或者#{}中的值）进行处理
   *
   * ==> 一个变量占位符处理
   */
  private static class VariableTokenHandler implements TokenHandler {
    private final Properties variables;  //Properties extends Hashtable<Object,Object>
    private final boolean enableDefaultValue;  //是否开启默认值
    private final String defaultValueSeparator;  //占位符中默认值的分隔符

    private VariableTokenHandler(Properties variables) {
      this.variables = variables;
      this.enableDefaultValue = Boolean.parseBoolean(getPropertyValue(KEY_ENABLE_DEFAULT_VALUE, ENABLE_DEFAULT_VALUE));  //Properties中都是String, 要进行类型转换
      this.defaultValueSeparator = getPropertyValue(KEY_DEFAULT_VALUE_SEPARATOR, DEFAULT_VALUE_SEPARATOR);
    }

    private String getPropertyValue(String key, String defaultValue) {
      return (variables == null) ? defaultValue : variables.getProperty(key, defaultValue);  //variables为空取默认值，否则取属性值
    }

    /**
     * TokenHandler接口中的方法，处理标志
     */
    @Override
    public String handleToken(String content) {
      if (variables != null) {
        String key = content;
        if (enableDefaultValue) {
          final int separatorIndex = content.indexOf(defaultValueSeparator);
          String defaultValue = null;
          if (separatorIndex >= 0) {
            key = content.substring(0, separatorIndex);
            defaultValue = content.substring(separatorIndex + defaultValueSeparator.length());
          }
          if (defaultValue != null) {
            return variables.getProperty(key, defaultValue);  //从vairable中取key,没有则为默认值
          }
        }
        if (variables.containsKey(key)) {  //variables中含有key值
          return variables.getProperty(key);
        }
      }
      return "${" + content + "}";
    }
  }

}
