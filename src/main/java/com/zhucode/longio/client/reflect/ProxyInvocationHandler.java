/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.client.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.zhucode.longio.annotation.LsAutowired;
import com.zhucode.longio.callback.CallbackDispatcher;
import com.zhucode.longio.callback.InvocationTask;
import com.zhucode.longio.client.parameter.CMDParser;
import com.zhucode.longio.client.parameter.ParameterPacker;
import com.zhucode.longio.client.parameter.ParameterPackerFactory;
import com.zhucode.longio.client.parameter.UidParser;
import com.zhucode.longio.conf.AppLookup;
import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.message.MessageCallback;
import com.zhucode.longio.message.MessageSerial;
import com.zhucode.longio.transport.Beginpoint;
import com.zhucode.longio.transport.Connector;
import com.zhucode.longio.transport.ProtocolType;
import com.zhucode.longio.transport.TransportType;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class ProxyInvocationHandler implements InvocationHandler {

	private static ParameterPackerFactory ppf = new ParameterPackerFactory();
	
	private Map<Method, MethodInfo> methods;
	
	private ParameterPacker<?> packer;

	private Connector connector;

	private CallbackDispatcher dispatcher;
	
	private Class<?> proxyCls;
	
	private Beginpoint beginPoint;
	
	private AppLookup appLookup;
	
	public ProxyInvocationHandler(Connector connector, AppLookup appLookup, Class<?> requiredType, List<MethodInfo> methods) {
		this.connector = connector;
		this.dispatcher = connector.getCallbackDispatcher();
		this.proxyCls = requiredType;
		this.methods =  new HashMap<Method, MethodInfo>();
		for (MethodInfo mi : methods) {
			this.methods.put(mi.getMethod(), mi);
		}
		this.appLookup = appLookup;
		initBeginpointAndPacker();
	}
	

	private void initBeginpointAndPacker() {
		LsAutowired lsa = this.proxyCls.getAnnotation(LsAutowired.class);
		String host = lsa.ip();
		int port = lsa.port();
		String app = lsa.app();
		TransportType tt = lsa.tt();
		ProtocolType pt = lsa.pt();
		
		if (appLookup.parseHost(app) != null) {
			host = appLookup.parseHost(app);
		}
		
		if (appLookup.parsePort(app) != 0) {
			port = appLookup.parsePort(app);
		}
		
		this.beginPoint = new Beginpoint(connector, app, host, port, tt, pt);
		
		this.packer = ppf.getPacker(pt);
	}


	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		MethodInfo mi = methods.get(method);
		Object po = packer.pack(mi, args);
		
		MessageBlock<?> mb = new MessageBlock<Object>(po);
		mb.setCmd(mi.getCmd());
		mb.setSerial(MessageSerial.newSerial());
		
		int uid = UidParser.parseUid(mi.getMethod(), args);
		int cmd = CMDParser.parseCMD(mi.getMethod(), args);
		
		if (uid > 0) {
			mb.setUid(uid);
		}
		
		if (cmd > 0) {
			mb.setCmd(cmd);
		}
		
		Callable<MessageBlock<?>> call = new Callable<MessageBlock<?>>() {

			@Override
			public MessageBlock<?> call() throws Exception {
				try {
					beginPoint.send(mb);
				} catch (Exception e) {
					mb.setBody(null);
					mb.setStatus(500);
					dispatcher.setReturnValue(mb);
					e.printStackTrace();
				}
				return null;
			}
		};
		
		InvocationTask<MessageBlock<?>> task = new InvocationTask<MessageBlock<?>>(call);
		if (args != null && args.length > 0 && args[args.length-1] instanceof MessageCallback) {
			MessageCallback callback = (MessageCallback)args[args.length-1];
			this.dispatcher.registCallback(mb.getSerial(), task, callback, mi.getTimeout());
			return null;
		} else {
			this.dispatcher.registTask(mb.getSerial(), task);

			try {
				MessageBlock<?> ret = task.get(mi.getTimeout(), TimeUnit.MILLISECONDS);
				return packer.unpack(mi.getMethod().getReturnType(), mi.getMethod().getGenericReturnType(), ret.getBody());
			} catch (Exception e) {
				this.dispatcher.unregist(mb.getSerial());
				e.printStackTrace();
			}
			
			throw new Exception("server invoke timeout");
		}
		
		
	}

}
