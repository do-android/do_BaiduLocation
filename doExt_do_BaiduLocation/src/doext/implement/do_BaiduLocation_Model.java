package doext.implement;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import core.DoServiceContainer;
import core.helper.jsonparse.DoJsonNode;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.define.do_BaiduLocation_IMethod;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现do_BaiduLocation_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_BaiduLocation_Model extends DoSingletonModule implements do_BaiduLocation_IMethod {

	private LocationClient mLocClient;
	private MyLocationListener mMyLocationListener;

	public do_BaiduLocation_Model() throws Exception {
		super();
		// 定位初始化
		mLocClient = new LocationClient(DoServiceContainer.getPageViewFactory().getAppContext().getApplicationContext());
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V）
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, DoJsonNode _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("stop".equals(_methodName)) {
			this.stop(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	@Override
	public void stop(DoJsonNode _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) {
		if (mLocClient != null && mLocClient.isStarted()) {
			mLocClient.stop();
		}
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V）
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, DoJsonNode _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		if ("getLocation".equals(_methodName)) {
			this.getLocation(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 获取当前位置信息；
	 * 
	 * @throws Exception
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void getLocation(DoJsonNode _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		String _model = _dictParas.getOneText("model", "gps");
		String _type = _dictParas.getOneText("type", "bd-0911");
		int _scanSpan = _dictParas.getOneInteger("scanSpan", 5000);
		setLocationOption(_model, _type, _scanSpan);
		mLocClient.start();
		DoInvokeResult _invokeResult = new DoInvokeResult(this.getUniqueKey());
		mMyLocationListener = new MyLocationListener(_scriptEngine, _invokeResult, _callbackFuncName, _type);
		mLocClient.registerLocationListener(mMyLocationListener);
	}

	// 设置Option
	private void setLocationOption(String _model, String _type, int _scanSpan) {
		try {
			LocationClientOption option = new LocationClientOption();
			if ("accuracy".equals(_model.trim())) {
				option.setLocationMode(LocationMode.Hight_Accuracy);
			} else if ("lowpower".equals(_model.trim())) {
				option.setLocationMode(LocationMode.Battery_Saving);
			} else {
				option.setLocationMode(LocationMode.Device_Sensors);
			}

			if ("gcj-02".equals(_type.trim())) {
				option.setCoorType("gcj02");
			} else {
				option.setCoorType("bd09ll");
			}
			option.setOpenGps(true);// 打开gps
			option.setScanSpan(_scanSpan);
			option.setNeedDeviceDirect(true);
			option.setIsNeedAddress(true);
			mLocClient.setLocOption(option);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class MyLocationListener implements BDLocationListener {

		private DoInvokeResult invokeResult;
		private String type;
		private String callbackFuncName;
		private DoIScriptEngine scriptEngine;

		public MyLocationListener(DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult, String _callbackFuncName, String _type) {
			this.invokeResult = _invokeResult;
			this.callbackFuncName = _callbackFuncName;
			this.type = _type;
			this.scriptEngine = _scriptEngine;
		}

		@Override
		public void onReceiveLocation(BDLocation location) {
			try {
				if (BDLocation.TypeServerError == location.getLocType()) { // 定位失败
					invokeResult.setError("定位失败");
				} else {
					DoJsonNode _jsonNode = new DoJsonNode();
					_jsonNode.setOneInteger("code", location.getLocType());
					_jsonNode.setOneText("type", type);
					_jsonNode.setOneText("latitude", location.getLatitude() + "");
					_jsonNode.setOneText("longitude", location.getLongitude() + "");
					_jsonNode.setOneText("address", location.getAddrStr() + "");
					invokeResult.setResultNode(_jsonNode);
				}
			} catch (Exception e) {
				invokeResult.setException(e);
				DoServiceContainer.getLogEngine().writeError("do_BaiduLocation：getLocation \n", e);
			} finally {
				scriptEngine.callback(callbackFuncName, invokeResult);
			}
		}
	}

}