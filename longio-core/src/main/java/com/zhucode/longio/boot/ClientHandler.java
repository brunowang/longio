/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.boot;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.zhucode.longio.App;
import com.zhucode.longio.Callback;
import com.zhucode.longio.Request;
import com.zhucode.longio.Response;
import com.zhucode.longio.core.client.CallbackFutureRouter;

/**
 * @author zhu jinxian
 * @date  2016年08月13日
 * 
 */
public abstract class ClientHandler {
	
	
	protected static CallbackFutureRouter router = new CallbackFutureRouter();
		
	public void handleResponse(Response response) {
		router.route(response);
	}
	
	public CallbackFutureRouter getRouter() {
		return router;
	}
	
	public void registerFuture(long serial, CompletableFuture<Response> future) {
		router.registerFuture(serial, future);
	}

	public void registerCallback(long serial, Callback callback, int timeout) {
		router.registerCallback(serial, callback, timeout);
	}

	public void timeoutFuture(long serial) {
		router.timeoutFuture(serial);
	}

	public abstract void connect(String app);
	public abstract boolean writeRequest(String app, Request request);
	
}
