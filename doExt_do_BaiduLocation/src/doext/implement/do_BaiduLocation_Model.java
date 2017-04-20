package doext.implement;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult.AddressComponent;
import com.baidu.mapapi.utils.DistanceUtil;

import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.helper.DoTextHelper;
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
	private BDLocationListener mMyLocationListener;
	private GeoCoder mGeoCoder;

	public do_BaiduLocation_Model() throws Exception {
		super();
		SDKInitializer.initialize(DoServiceContainer.getPageViewFactory().getAppContext().getApplicationContext());
		// 定位初始化
		mLocClient = new LocationClient(DoServiceContainer.getPageViewFactory().getAppContext().getApplicationContext());
		mGeoCoder = GeoCoder.newInstance();
		mGetGeoCoderResultListener = new MyGetGeoCoderResultListener();
		mGeoCoder.setOnGetGeoCodeResultListener(mGetGeoCoderResultListener);
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
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("start".equals(_methodName)) {
			this.start(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("stop".equals(_methodName)) {
			this.stop(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("startScan".equals(_methodName)) {
			this.startScan(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("stopScan".equals(_methodName)) {
			this.stopScan(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("getDistance".equals(_methodName)) {
			this.getDistance(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
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
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		if ("locate".equals(_methodName)) {
			this.locate(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("geoCode".equals(_methodName)) {
			this.geoCode(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("reverseGeoCode".equals(_methodName)) {
			this.reverseGeoCode(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	@Override
	public void locate(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		stop();
		String _model = DoJsonHelper.getString(_dictParas, "model", "high");
		setLocationOption(_model, 300);
		mMyLocationListener = new MyLocationListener2(_scriptEngine, _callbackFuncName);
		mLocClient.registerLocationListener(mMyLocationListener);
		mLocClient.start();
	}

	@Override
	public void startScan(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		stop();
		String _model = DoJsonHelper.getString(_dictParas, "model", "high");
		int _span = DoJsonHelper.getInt(_dictParas, "span", 1000);
		if (_span < 1000) {
			_span = 1000;
		}
		setLocationOption(_model, _span);
		mMyLocationListener = new MyLocationListener();
		mLocClient.registerLocationListener(mMyLocationListener);
		mLocClient.start();
	}

	private void getDistance(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String _startPoint = DoJsonHelper.getString(_dictParas, "startPoint", null);
		String _endPoint = DoJsonHelper.getString(_dictParas, "endPoint", null);
		if (_startPoint == null || _endPoint == null) {
			throw new Exception("startPoint 或  endPoint 参数值不能为空！");
		}
		String[] _latLng1 = _startPoint.split(",");
		String[] _latLng2 = _endPoint.split(",");
		if (_latLng1 == null || _latLng2 == null || _latLng1.length != 2 || _latLng2.length != 2) {
			throw new Exception("startPoint 或  endPoint 参数值非法！");
		}
		double _p1_lat = DoTextHelper.strToDouble(_latLng1[0], 0);
		double _p1_lng = DoTextHelper.strToDouble(_latLng1[1], 0);
		double _p2_lat = DoTextHelper.strToDouble(_latLng2[0], 0);
		double _p2_lng = DoTextHelper.strToDouble(_latLng2[1], 0);

		LatLng _p1 = new LatLng(_p1_lat, _p1_lng);
		LatLng _p2 = new LatLng(_p2_lat, _p2_lng);
		double _distance = DistanceUtil.getDistance(_p1, _p2);

		_invokeResult.setResultFloat(_distance);
	}

	private void setLocationOption(String _model, int _span) {
		try {
			LocationClientOption option = new LocationClientOption();
			if ("high".equals(_model.trim())) {
				option.setLocationMode(LocationMode.Hight_Accuracy);
			} else if ("low".equals(_model.trim())) {
				option.setLocationMode(LocationMode.Battery_Saving);
			} else {
				option.setLocationMode(LocationMode.Device_Sensors);
			}

			option.setCoorType("bd09ll");
			option.setOpenGps(true);// 打开gps
			option.setScanSpan(_span); // scan < 1000 为主动定位，>= 1000 为定时定位
			option.setNeedDeviceDirect(true);
			option.setIsNeedAddress(true);
			mLocClient.setLocOption(option);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stopScan(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) {
		stop();
	}

	private void stop() {
		if (mLocClient != null && mLocClient.isStarted()) {
			mLocClient.stop();
			if (mMyLocationListener != null) {
				mLocClient.unRegisterLocationListener(mMyLocationListener);
				mMyLocationListener = null;
			}
		}
	}

	private class MyLocationListener implements BDLocationListener {
		private DoInvokeResult invokeResult;

		public MyLocationListener() {
			this.invokeResult = new DoInvokeResult(getUniqueKey());
		}

		@Override
		public void onReceiveLocation(BDLocation location) {
			try {
				if (BDLocation.TypeServerError == location.getLocType()) { // 定位失败
					invokeResult.setError("定位失败");
				} else {
					JSONObject _jsonNode = new JSONObject();
					_jsonNode.put("latitude", location.getLatitude() + "");
					_jsonNode.put("longitude", location.getLongitude() + "");
					_jsonNode.put("address", location.getAddrStr() + "");
					invokeResult.setResultNode(_jsonNode);
				}
			} catch (Exception e) {
				invokeResult.setException(e);
				DoServiceContainer.getLogEngine().writeError("do_BaiduLocation：startScan \n", e);
			} finally {
				getEventCenter().fireEvent("result", invokeResult);
			}

		}
	}

	private class MyLocationListener2 implements BDLocationListener {

		private DoIScriptEngine scriptEngine;
		private String callbackFuncName;

		public MyLocationListener2(DoIScriptEngine _scriptEngine, String _callbackFuncName) {
			this.scriptEngine = _scriptEngine;
			this.callbackFuncName = _callbackFuncName;
		}

		@Override
		public void onReceiveLocation(BDLocation location) {
			DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
			try {
				if (BDLocation.TypeServerError == location.getLocType()) { // 定位失败
					_invokeResult.setError("定位失败");
				} else {
					JSONObject _jsonNode = new JSONObject();
					_jsonNode.put("latitude", location.getLatitude() + "");
					_jsonNode.put("longitude", location.getLongitude() + "");
					_jsonNode.put("address", location.getAddrStr() + "");
					_invokeResult.setResultNode(_jsonNode);
				}
			} catch (Exception e) {
				_invokeResult.setException(e);
				DoServiceContainer.getLogEngine().writeError("do_BaiduLocation：locate \n", e);
			} finally {
				scriptEngine.callback(callbackFuncName, _invokeResult);
			}
		}

	}

	///////////////////////////////////
	@Override
	@Deprecated
	public void stop(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) {
		stop();
	}

	@Override
	@Deprecated
	public void start(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		stop();
		String _model = DoJsonHelper.getString(_dictParas, "model", "high");
		boolean _isLoop = DoJsonHelper.getBoolean(_dictParas, "isLoop", false);
		setLocationOption(_model, _isLoop);
		mLocClient.start();
		mMyLocationListener = new MyLocationListener();
		mLocClient.registerLocationListener(mMyLocationListener);
	}

	// 设置Option
	@Deprecated
	private void setLocationOption(String _model, boolean _isLoop) {
		try {
			LocationClientOption option = new LocationClientOption();
			if ("high".equals(_model.trim())) {
				option.setLocationMode(LocationMode.Hight_Accuracy);
			} else if ("low".equals(_model.trim())) {
				option.setLocationMode(LocationMode.Battery_Saving);
			} else {
				option.setLocationMode(LocationMode.Device_Sensors);
			}

			option.setCoorType("bd09ll");
			option.setOpenGps(true);// 打开gps
			option.setScanSpan(_isLoop ? 30000 : 300); // scan < 1000 为主动定位，>= 1000 为定时定位
			option.setNeedDeviceDirect(true);
			option.setIsNeedAddress(true);
			mLocClient.setLocOption(option);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void reverseGeoCode(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		Double _latitude = DoJsonHelper.getDouble(_dictParas, "latitude", -1);
		Double _longitude = DoJsonHelper.getDouble(_dictParas, "longitude", -1);
		DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
		if (_latitude > 0 && _longitude > 0) {
			LatLng _location = new LatLng(_latitude, _longitude);
			ReverseGeoCodeOption _option = new ReverseGeoCodeOption();
			_option.location(_location);
			mGetGeoCoderResultListener.init(_scriptEngine, _callbackFuncName, _invokeResult);
			mGeoCoder.reverseGeoCode(_option);
		} else {
			_invokeResult.setError("中心点经纬度不合法");
			_scriptEngine.callback(_callbackFuncName, _invokeResult);
		}
	}

	private void geoCode(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		String _city = DoJsonHelper.getString(_dictParas, "city", "");
		if (TextUtils.isEmpty(_city)) {
			throw new Exception("city参数值不能为空！");
		}
		String _address = DoJsonHelper.getString(_dictParas, "address", "");
		if (TextUtils.isEmpty(_address)) {
			throw new Exception("address参数值不能为空！");
		}
		DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
		GeoCodeOption _option = new GeoCodeOption();
		_option.city(_city);
		_option.address(_address);
		mGetGeoCoderResultListener.init(_scriptEngine, _callbackFuncName, _invokeResult);
		mGeoCoder.geocode(_option);

	}

	private MyGetGeoCoderResultListener mGetGeoCoderResultListener;

	private class MyGetGeoCoderResultListener implements OnGetGeoCoderResultListener {

		private DoIScriptEngine scriptEngine;
		private String callbackFuncName;
		private DoInvokeResult invokeResult;

		public void init(DoIScriptEngine _scriptEngine, String _callbackFuncName, DoInvokeResult _invokeResult) {
			this.scriptEngine = _scriptEngine;
			this.callbackFuncName = _callbackFuncName;
			this.invokeResult = _invokeResult;
		}

		@Override
		public void onGetGeoCodeResult(GeoCodeResult result) {
			JSONObject _resultNode = new JSONObject();
			try {
				LatLng _location = result.getLocation();
				_resultNode.put("latitude", _location.latitude); //纬度
				_resultNode.put("longitude", _location.longitude); //经度
			} catch (Exception e) {
			}
			invokeResult.setResultNode(_resultNode);
			scriptEngine.callback(callbackFuncName, invokeResult);
		}

		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			String _address = result.getAddress(); //具体地址
			AddressComponent _addressDetail = result.getAddressDetail();
			String _province = null; //省份名称
			String _city = null; //城市名称
			String _district = null; //区县名称
			String _streetName = null; //街道名称
			String _streetNumber = null; //街道号码
			if (_addressDetail != null) {
				_province = _addressDetail.province; //省份名称
				_city = _addressDetail.city; //城市名称
				_district = _addressDetail.district; //区县名称
				_streetName = _addressDetail.street; //街道名称
				_streetNumber = _addressDetail.streetNumber; //街道号码
			}

			JSONObject _resultNode = new JSONObject();
			try {
				_resultNode.put("address", _address);
				_resultNode.put("province", _province);
				_resultNode.put("city", _city);
				_resultNode.put("district", _district);
				_resultNode.put("streetName", _streetName);
				_resultNode.put("streetNumber", _streetNumber);
			} catch (JSONException e) {
			}

			List<PoiInfo> _poiInfos = result.getPoiList();
			JSONArray _pois = new JSONArray();
			if (_poiInfos != null) {
				for (int i = 0; i < _poiInfos.size(); i++) {
					PoiInfo poi = _poiInfos.get(i);
					JSONObject _obj = new JSONObject();
					try {
						_obj.put("id", poi.uid);
						_obj.put("name", poi.name);
						_obj.put("city", poi.city);
						_obj.put("isPano", poi.isPano);
						LatLng _location = poi.location;
						JSONObject _locationObj = new JSONObject();
						if (_location != null) {
							_locationObj.put("latitude", _location.latitude);
							_locationObj.put("longitude", _location.longitude);
						}
						_obj.put("location", _locationObj);
						_obj.put("address", poi.address);
					} catch (JSONException e) {
					}
					_pois.put(_obj);
				}
			}
			try {
				_resultNode.put("pois", _pois);
			} catch (JSONException e) {
			}
			invokeResult.setResultNode(_resultNode);
			scriptEngine.callback(callbackFuncName, invokeResult);
		}
	}
}