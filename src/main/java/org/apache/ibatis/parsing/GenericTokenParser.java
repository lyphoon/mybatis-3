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

  public String parse(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    // search open token
    int start = text.indexOf(openToken, 0);
    if (start == -1) {
      return text;
    }
    char[] src = text.toCharArray();
    int offset = 0;

    final StringBuilder builder = new StringBuilder();  //存的是结果
    StringBuilder expression = null;  //存的是点位符中间的内容

    /**
     * 一次是一个开始占位与结束占位，同时将内容用TokenHandler进行处理并加入到结果中
     */
    while (start > -1) {
      if (start > 0 && src[start - 1] == '\\') {  //开始的转义不处理
        // this open token is escaped. remove the backslash and continue.
        builder.append(src, offset, start - offset - 1).append(openToken); //将转义换掉
        offset = start + openToken.length();  //offset变化 a
      } else {
        // found open token. let's search close token.
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
            expression.append(src, offset, end - offset - 1).append(closeToken);  //中间位，从offset到转义点
            offset = end + closeToken.length();  //修改offfset
            end = text.indexOf(closeToken, offset);  //再找结束标志，一次open要找到一个close
          } else {
            expression.append(src, offset, end - offset);
            offset = end + closeToken.length();
            break;
          }
        }
        if (end == -1) {
          // close token was not found.
          builder.append(src, start, src.length - start);
          offset = src.length;
        } else {
          builder.append(handler.handleToken(expression.toString()));  //将占位符的字面值转给TokenHandler处理
          offset = end + closeToken.length();
        }
      }

      start = text.indexOf(openToken, offset);  //下一次的start从offset位置开始,找第一个opentToken
    }
    if (offset < src.length) {  //结尾的内容
      builder.append(src, offset, src.length - offset);
    }
    return builder.toString();
  }
}
