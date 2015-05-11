package doext.define;

import core.helper.jsonparse.DoJsonNode;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;

/**
 * 声明自定义扩展组件方法
 */
public interface do_BaiduLocation_IMethod {
	void getLocation(DoJsonNode _dictParas,DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception ;
	void stop(DoJsonNode _dictParas,DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception ;
}