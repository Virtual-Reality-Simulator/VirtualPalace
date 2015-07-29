package kr.poturns.virtualpalace.communication;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Map;
import java.util.Set;

/**
 * Created by Myungjin Kim on 2015-07-30.
 * <p/>
 * Wearable Device 와 Mobile Device 간 통신을 도와주는 클래스
 */
public class WearableCommunicator implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<MessageApi.SendMessageResult> {
    private static final String TAG = "WearableCommunicator";
    //public static final String SEND_DATA_CAPABILITY_NAME = "send_data";
    //public static final String SEND_DATA_MESSAGE_PATH = "/send_data";

    private GoogleApiClient mGoogleApiClient;
    private MessageApi.MessageListener messageListener;

    private final String[] CAPABILITY_NAMES;
    private final ArrayMap<String, String> NODE_ID_MAP;
    private final Context context;

    public WearableCommunicator(Context context) {
        this.context = context;

        CAPABILITY_NAMES = context.getResources().getStringArray(R.array.android_wear_capabilities);
        //ResourcesUtils.get(context, "android_wear_capabilities", "kr.poturns.util");
        NODE_ID_MAP = new ArrayMap<String, String>(CAPABILITY_NAMES.length);

        initGoogleApiClient();

    }


    public void setMessageListener(MessageApi.MessageListener messageListener) {
        this.messageListener = messageListener;

        if (mGoogleApiClient != null && messageListener != null)
            Wearable.MessageApi.removeListener(mGoogleApiClient, messageListener);

        if (isConnected())
            Wearable.MessageApi.addListener(mGoogleApiClient, messageListener);

    }

    //*************** Lifecycle helper method ***************

    /**
     * GoogleApiClient 와 연결한다.
     */
    public final void connect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * GoogleApiClient 와 연결 해제한다.
     */
    public final void disconnect() {
        if (mGoogleApiClient != null) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, messageListener);
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * GoogleApiClient 와 연결 해제하고, 리소스를 정리한다.
     */
    public final void destroy() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.unregisterConnectionCallbacks(this);
            mGoogleApiClient.unregisterConnectionFailedListener(this);
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }

    }

    /**
     * GoogleApiClient 와 연결 되었는 지 확인한다.
     *
     * @return 연결 여부
     */
    public final boolean isConnected() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }


    //*************** google api Listener ***************

    /**
     * GoogleApiClient 객체를 생성하고, 초기화한다.
     */
    protected final void initGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public final void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected: " + connectionHint);

        if (messageListener != null)
            Wearable.MessageApi.addListener(mGoogleApiClient, messageListener);

        setupCapability();
    }

    @Override
    public final void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed: " + result);
    }

    /**
     * GoogleApiClient 에서 연결 가능한 노드를 설정한다.
     */
    private void setupCapability() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                CapabilityApi.GetAllCapabilitiesResult result = Wearable.CapabilityApi.getAllCapabilities(mGoogleApiClient, CapabilityApi.FILTER_REACHABLE).await();

                updateCapability(result.getAllCapabilities());
            }
        });
    }

    /**
     * GoogleApiClient 에서 연결 가능한 노드 정보를 업데이트 한다.
     */
    private void updateCapability(Map<String, CapabilityInfo> capabilityInfoMap) {

        for (String capabilityName : CAPABILITY_NAMES) {
            if (capabilityInfoMap.containsKey(capabilityName)) {
                CapabilityInfo capabilityInfo = capabilityInfoMap.get(capabilityName);
                Set<Node> connectedNodes = capabilityInfo.getNodes();

                NODE_ID_MAP.put('/' + capabilityName, pickBestNodeId(connectedNodes));
            }
        }

    }

    /**
     * 가장 근처에 있는 노드를 선택한다.
     */
    private static String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;

        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    /**
     * 상대 Device 에 메시지를 보낸다.
     *
     * @param message 보낼 메시지
     */
    public final boolean sendMessage(String message) {
        return sendMessage(NODE_ID_MAP.get(NODE_ID_MAP.keyAt(0)), message);
    }

    /**
     * 상대 Device 에 메시지를 보낸다.
     *
     * @param path    메시지를 보낼 노드 path
     * @param message 보낼 메시지
     */
    public final boolean sendMessage(String path, String message) {
        return sendMessage(NODE_ID_MAP.get(path), path, message);
    }


    /**
     * 상대 Device 에 메시지를 보낸다.
     *
     * @param nodeId  메시지를 보낼 노드 ID
     * @param path    메시지를 보낼 노드 path
     * @param message 보낼 메시지
     */
    private boolean sendMessage(final String nodeId, final String path, final String message) {
        if (nodeId != null) {
            AsyncTask.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            Wearable.MessageApi
                                    .sendMessage(mGoogleApiClient, nodeId, path, message.getBytes())
                                    .setResultCallback(WearableCommunicator.this);
                        }
                    }
            );

            return true;

        } else
            return false;

    }

    @Override
    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
        if (!sendMessageResult.getStatus().isSuccess()) {
            // Failed to send message
            Log.e(TAG, "send message : fail - " + sendMessageResult);
        } else {
            Log.d(TAG, "send message : success - " + sendMessageResult);
        }
    }

}

