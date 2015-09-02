package kr.poturns.virtualpalace.controller;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LongSparseArray;

import com.unity3d.player.UnityPlayer;

import kr.poturns.virtualpalace.annotation.UnityApi;

import static kr.poturns.virtualpalace.input.IControllerCommands.REQUEST_CALLBACK_FROM_UNITY;
import static kr.poturns.virtualpalace.input.IControllerCommands.REQUEST_MESSAGE_FROM_UNITY;

/**
 * <b> ANDROID - UNITY 간 통신 클래스 </b>
 *
 * @author Myungjin.Kim
 * @author Yeonho.Kim
 */
public final class AndroidUnityBridge {

    // * * * S I N G L E T O N * * * //
    private static AndroidUnityBridge sInstance;

    public static AndroidUnityBridge getInstance(PalaceApplication app) {
        if (sInstance == null)
            sInstance = new AndroidUnityBridge(app);
        return sInstance;
    }


    // * * * C O N S T A N T S * * * //
    public static final String BUNDLE_KEY_ID = "id";
    public static final String BUNDLE_KEY_MESSAGE_JSON = "json";


    private final Object LOCK = new Object();
    private final PalaceMaster mMasterF;
    private final Handler mRequestHandlerF;

    private final LongSparseArray<IAndroidUnityCallback> mCallbackMapF;

    // * * * C O N S T A N T S * * * //


    // * * * C O N S T R U C T O R S * * * //
    private AndroidUnityBridge(PalaceApplication app) {
        mMasterF = PalaceMaster.getInstance(app);
        mRequestHandlerF = mMasterF.getRequestHandler();

        mCallbackMapF = new LongSparseArray<IAndroidUnityCallback>();
    }


    // * * * M E T H O D S * * * //

    /**
     * UNITY 에서 ANDROID 에 요청을 보낸다.
     *
     * @param jsonMessage 요청의 세부 사항이 Json형태로 기술되어 있는 문자열
     * @param callback    요청에 대한 응답을 받을 콜백
     * @return 요청이 접수되었을 경우, TRUE
     */
    @UnityApi
    public boolean requestCallbackToAndroid(String jsonMessage, IAndroidUnityCallback callback) {
        long id;
        synchronized (LOCK) {
            id = System.currentTimeMillis();
            mCallbackMapF.put(id, callback);
        }

        Bundle bundle = new Bundle();
        bundle.putLong(BUNDLE_KEY_ID, id);
        bundle.putString(BUNDLE_KEY_MESSAGE_JSON, jsonMessage);

        Message.obtain(mMasterF.getRequestHandler(),
                REQUEST_CALLBACK_FROM_UNITY, bundle).sendToTarget();

        return true;
    }

    /**
     * UNITY 에서 요청한 ID 에 해당하는 결과를 콜백메소드로 반환한다.
     *
     * @param id         콜백의 id
     * @param jsonResult 요청에 대한 결과값이 Json형태로 기술된 문자열
     */
    public synchronized void respondCallbackToUnity(long id, String jsonResult) {
        IAndroidUnityCallback callback = mCallbackMapF.get(id);
        if (callback != null) {
            callback.onCallback(jsonResult);
            mCallbackMapF.remove(id);
        }
    }

    /**
     * ANDROID 에서 UNITY 에 요청을 보낸다.
     *
     * @param jsonMessage 요청의 세부 사항이 Json형태로 기술되어 있는 문자열
     * @param callback    요청에 대한 응답을 받을 콜백
     * @return 요청이 접수되었을 경우, TRUE
     */
    public boolean requestCallbackToUnity(String jsonMessage, IAndroidUnityCallback callback) {
        long id;
        synchronized (LOCK) {
            id = System.currentTimeMillis();

            mCallbackMapF.put(id, callback);
        }

        //TODO 어떠한 GameObject로 전달해야 하는지 알 수 없음.
        //UnityPlayer.UnitySendMessage();
        return true;
    }

    /**
     * ANDROID 에서 요청한 ID 에 해당하는 결과를 콜백메소드로 반환한다.
     *
     * @param id         콜백의 id
     * @param jsonResult 요청에 대한 결과값이 Json형태로 기술된 문자열
     */
    @UnityApi
    public synchronized void respondCallbackToAndroid(long id, String jsonResult) {
        IAndroidUnityCallback callback = mCallbackMapF.get(id);
        if (callback != null) {
            callback.onCallback(jsonResult);
            mCallbackMapF.remove(id);
        }
    }

    /**
     * UNITY 에서 단일 메시지를 ANDROID 로 전송한다.
     *
     * @param jsonMessage 전송할 Json 메시지
     * @return 메시지가 정상적으로 전송되었을 때, TRUE
     */
    @UnityApi
    public synchronized boolean sendSingleMessageToAndroid(String jsonMessage) {
        Message.obtain(mMasterF.getRequestHandler(),
                REQUEST_MESSAGE_FROM_UNITY, jsonMessage).sendToTarget();

        return true;
    }

    /**
     * ANDROID 에서 단일 메시지를 UNITY 로 전송한다.
     *
     * @param target gameObject 이름
     * @param func   gameObject에 존재하는 함수 이름
     * @param param  함수의 매개변수
     */
    public synchronized void sendSingleMessageToUnity(String target, String func, String param) {
        UnityPlayer.UnitySendMessage(target, func, param);
    }


    // * * * I N N E R  C L A S S E S * * * //

    /**
     * Android-Unity간 요청에 대한 응답을 정의하는 클래스
     */
    public interface IAndroidUnityCallback {

        /**
         * 요청에 대한 응답
         *
         * @param json 요청에 대한 응답이 Json형태로 기술 된 문자열
         */
        public void onCallback(String json);
    }

}

