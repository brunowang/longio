/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.context.parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

import com.alibaba.fastjson.JSONArray;
import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.transport.Connector;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class JsonArrayParameterParser implements ParameterParser {

	@Override
	public Object[] parse(MessageBlock<?> mb, Annotation[] meta, Parameter[] paras) {
		Object[] objs = new Object[paras.length];
		JSONArray body = (JSONArray) mb.getBody();
		JSONArray ja = body.getJSONArray(body.size()-1);
		for (int i = 0; i < paras.length; i++) {
			Parameter p = paras[i];
			if (p.getType() == MessageBlock.class) {
				objs[i] = mb;
				continue;
			}
			if (p.getType() == Connector.class) {
				objs[i] = mb.getConnector();
				continue;
			}
			
			objs[i] = parseObject(p.getType(), ja.get(i));
		}
		return objs;
	}

	private Object parseObject(Class<?> type, Object val) {
		if (type.isPrimitive()) {
			return val;
		}
		return val;
	}

}
