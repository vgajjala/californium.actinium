/*******************************************************************************
 * Copyright (c) 2014 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Martin Lanter - architect and initial implementation
 ******************************************************************************/
package org.eclipse.californium.actinium.jscoap;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * It is not possible to add further methods or fields to this class within
 * JavaScript (Rhino). If this is necessary, use AbstractJavaScriptResource.
 */
public class JavaScriptResource extends CoapResource implements JavaScriptCoapConstants {
	// Cannot extend ScriptableObject, because has to extend CoapResource
	// Cannot (reasonably) implement Scriptable, because we then have to implement all 16 methods like ScriptableObject

	public Function onget = null;
	public Function onpost = null;
	public Function onput = null;
	public Function ondelete = null;
	
	public JavaScriptResource() {
		super(null);
	}

	public JavaScriptResource(String resourceIdentifier) {
		super(resourceIdentifier);
	}
	
	public JavaScriptResource(String resourceIdentifier, boolean hidden) {
		super(resourceIdentifier, hidden);
	}
	
	@Override
	public void changed() {
		super.changed();
	}
	
	public Function getOnget() {
		return onget;
	}
	
	public Function getOnpost() {
		return onpost;
	}
	
	public Function getOnput() {
		return onput;
	}
	
	public Function getOndelete() {
		return ondelete;
	}
	
	public Object getThis() {
		return this;
	}
	
	@Override
	public void handleGET(CoapExchange exchange) {
		Function onget = getOnget();
		if (onget!=null) {
			performFunction(onget, new JavaScriptCoapExchange(exchange));
		} else {
			super.handleGET(exchange);
		}
	}

	@Override
	public void handlePOST(CoapExchange exchange) {
		Function onpost = getOnpost();
		if (onpost!=null) {
			performFunction(onpost, new JavaScriptCoapExchange(exchange));
		} else {
			super.handlePOST(exchange);
		}
	}

	@Override
	public void handlePUT(CoapExchange exchange) {
		Function onput = getOnput();
		if (onput!=null) {
			performFunction(onput, new JavaScriptCoapExchange(exchange));
		} else {
			super.handlePUT(exchange);
		}
	}

	@Override
	public void handleDELETE(CoapExchange exchange) {
		Function ondelete = getOndelete();
		if (ondelete!=null) {
			performFunction(ondelete, new JavaScriptCoapExchange(exchange));
		} else {
			super.handleDELETE(exchange);
		}
	}
	
	private void performFunction(Function fun, JavaScriptCoapExchange request) {
		try {
			Context cx = Context.enter();
			Scriptable prototype = ScriptableObject.getClassPrototype(fun, request.getClassName());
			request.setPrototype(prototype);
			Scriptable scope = fun.getParentScope();
			Object thisObj = getThis();
			fun.call(cx, fun, Context.toObject(thisObj, scope), new Object[] {request});
		} catch (RhinoException e) {
        	System.err.println("JavaScript error in ["+e.sourceName()+"#"+e.lineNumber()+"]: "+e.getCause().getMessage());
        	e.printStackTrace();
		} finally {
			Context.exit();
		}
	}
}
