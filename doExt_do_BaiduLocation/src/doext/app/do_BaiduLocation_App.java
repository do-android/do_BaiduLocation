package doext.app;
import android.content.Context;
import core.interfaces.DoIAppDelegate;

/**
 * APP启动的时候会执行onCreate方法；
 *
 */
public class do_BaiduLocation_App implements DoIAppDelegate {

	private static do_BaiduLocation_App instance;
	private String moduleTypeID;
	
	private do_BaiduLocation_App(){
		
	}
	
	public static do_BaiduLocation_App getInstance() {
		if(instance == null){
			instance = new do_BaiduLocation_App();
		}
		return instance;
	}
	
	@Override
	public void onCreate(Context context) {
		// ...do something
	}
	
	public String getModuleTypeID() {
		return moduleTypeID;
	}

	public void setModuleTypeID(String moduleTypeID) {
		this.moduleTypeID = moduleTypeID;
	}

	@Override
	public String getTypeID() {
		// TODO Auto-generated method stub
		return getModuleTypeID();
	}

}
