/**
 *    Copyright 2009-2017 the original author or authors.
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

/**
 * @author Clinton Begin
 *
 * 通用占位符解析器，一个String中可能有多个占位符，其中每一个占位符的解析是由TokenHandler处理的
 */
public class GenericTokenParser {

  private final String openToken;
  private final String closeToken;
  private final TokenHandler handler;

  public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
    this.openToken = openToken;
    this.closeToken = closeToken;
    this.handler = handler;
  }

  /**
   * 主方法 !!
   */
  public String parse(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    // search open token
    int start = text.indexOf(openToken, 0);
    if (start == -1) {
      return text;
    }
    char[] src = text.toCharArray();  //字符数组，方便以下标定位一个字符
    int offset = 0;

    final StringBuilder builder = new StringBuilder();  //存的是结果
    StringBuilder expression = null;  //存的是点位符中间的内容，它由TokenHandler处理

    /**
     * 一次要处理完一个开始占位及对应的一个结束占位，它们之前的内容用TokenHandler进行处理并加入到结果中
     */
    while (start > -1) {
      if (start > 0 && src[start - 1] == '\\') {  //开始占位标记如果是转义的，不认为是一个占位标记放弃这一次处理，但是要将结果拼接，将下一次的起始偏移offset设置
        // this open token is escaped. remove the backslash and continue.
        builder.append(src, offset, start - offset - 1).append(openToken); //将转义换掉
        offset = start + openToken.length();  //offset变化 a
      } else {
        // found open token. let's search close token. 找到开始占位标记，处理expression了
        if (expression == null) {
          expression = new StringBuilder();  //没有new 一个，这时第一次循环
        } else {
          expression.setLength(0);  //有，清空，这时已经不是第一次循环
        }

        builder.append(src, offset, start - offset);  //起始点为offset, 参考a
        offset = start + openToken.length(); //a
        int end = text.indexOf(closeToken, offset);  //从offset开始找结束标志
        while (end > -1) {
          if (end > offset && src[end - 1] == '\\') { //结束为转义
            // this close token is escaped. remove the backslash and continue.
            expression.append(src, offset, end - offset - 1).append(closeToken);  //中间位（expression），从offset到转义点
            offset = end + closeToken.length();  //修改offfset
            end = text.indexOf(closeToken, offset);  //再找结束标志，一次open要找到一个close
          } else {
            expression.append(src, offset, end - offset);
            offset = end + closeToken.length();
            break;  //找到了本次的结束占位标记
          }
        }

        if (end == -1) {  //本次有开始占位符，没有结束占位符
          // close token was not found.
          builder.append(src, start, src.length - start);
          offset = src.length;
          //break;
        } else {
          builder.append(handler.handleToken(expression.toString()));  //将占位符的字面值转给TokenHandler处理
          offset = end + closeToken.length();  //可以不要
        }
      }

      start = text.indexOf(openToken, offset);  //下一次查找start从offset位置开始,找第一个opentToken
    }

    if (offset < src.length) {  //end之后的结尾内容
      builder.append(src, offset, src.length - offset);
    }
    return builder.toString();
  }
}
