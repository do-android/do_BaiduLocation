package doext.define;

import org.json.JSONObject;

import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;

/**
 * 声明自定义扩展组件方法
 */
public interface do_BaiduLocation_IMethod {
	void start(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception;
	void stop(JSONObject _dictParas,DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception ;
	void stopScan(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult);
	void startScan(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception;
	void locate(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception;
}