package io.openim.android.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openim.android.sdk.internal.log.LogcatHelper;
import io.openim.android.sdk.listener.BaseImpl;
import io.openim.android.sdk.listener.ConnectListener;
import io.openim.android.sdk.listener.InitCallback;
import io.openim.android.sdk.listener.InitSDKListener;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.manager.ConversationManager;
import io.openim.android.sdk.manager.FriendshipManager;
import io.openim.android.sdk.manager.GroupManager;
import io.openim.android.sdk.manager.MessageManager;
import io.openim.android.sdk.models.UserInfo;
import io.openim.android.sdk.user.Credential;
import io.openim.android.sdk.util.CollectionUtils;
import io.openim.android.sdk.util.JsonUtil;
import io.openim.android.sdk.util.Predicates;
import open_im_sdk.Open_im_sdk;

public class OpenIMClient {

    private OpenIMClientImpl mClientImpl;
    //    public ImManager imManager;
    public ConversationManager conversationManager;
    public FriendshipManager friendshipManager;
    public GroupManager groupManager;
    public MessageManager messageManager;

    private OpenIMClient() {
        mClientImpl = new OpenIMClientImpl();
//        imManager = new ImManager();
        // TODO: to be remove, use service interface via getters instead
        conversationManager = mClientImpl.conversationManager;
        friendshipManager = mClientImpl.friendshipManager;
        groupManager = mClientImpl.groupManager;
        messageManager = mClientImpl.messageManager;
    }

    private static class Singleton {
        private static final OpenIMClient INSTANCE = new OpenIMClient();
    }

    public static OpenIMClient getInstance() {
        return Singleton.INSTANCE;
    }

    public void init(@NonNull OpenIMConfig config, @Nullable InitCallback callback) {
        Predicates.requireNonNull(config);

        String conf = config.toJson();
        LogcatHelper.logDInDebug(String.format("init config: %s", conf));
        mClientImpl.init(conf, callback);
    }

    /**
     * 初始化sdk
     * 注：在创建图片，语音，视频，文件等需要路径参数的消息体时，
     * 如果选择的是非全路径方法如：createSoundMessage（全路径方法为：createSoundMessageFromFullPath）,
     * 需要将文件自行拷贝到dbPath目录下，如果此时文件路径为 apath+"/sound/a.mp3"，则参数path的值为：/sound/a.mp3。
     * 如果选择的全路径方法，路径为你文件的实际路径不需要再拷贝。
     *
     * @param platform {@link io.openim.android.sdk.enums.Platform}
     * @param ipApi    SDK的API接口地址。如：http:xxx:10000
     * @param ipWs     SDK的web socket地址。如： ws:xxx:17778
     * @param dbPath   数据存储路径
     * @param listener SDK初始化监听
     */
    public void initSDK(int platform, String ipApi, String ipWs, String dbPath, InitSDKListener listener) {
        String conf = JsonUtil.toString(CollectionUtils.simpleMapOf("platform", platform, "ipApi", ipApi, "ipWs", ipWs, "dbDir", dbPath));
        LogcatHelper.logDInDebug(String.format("init config: %s", conf));
        mClientImpl.init(conf, new InitCallback() {
            @Override
            public void onSucceed() {
            }

            @Override
            public void onFailed(@NonNull Throwable throwable) {
            }
        });
        if (Predicates.isNull(listener)) {
            return;
        }
        mClientImpl.registerConnListener(new ConnectListener() {
            @Override
            public void onConnectFailed(long code, String error) {
                Predicates.requireNonNull(listener).onConnectFailed(code, error);
            }

            @Override
            public void onConnectSuccess() {
                Predicates.requireNonNull(listener).onConnectSuccess();
            }

            @Override
            public void onConnecting() {
                Predicates.requireNonNull(listener).onConnecting();
            }

            @Override
            public void onKickedOffline() {
                Predicates.requireNonNull(listener).onKickedOffline();
            }

            @Override
            public void onSelfInfoUpdated(UserInfo info) {
                Predicates.requireNonNull(listener).onSelfInfoUpdated(info);
            }

            @Override
            public void onUserTokenExpired() {
                Predicates.requireNonNull(listener).onUserTokenExpired();
            }
        });
    }

    public void registerConnectListener(@Nullable ConnectListener listener) {
        if (Predicates.isNull(listener)) {
            return;
        }
        mClientImpl.registerConnListener(listener);
    }

    /**
     * Un-init SDK
     */
    public void unInitSDK() {
        mClientImpl.release();
    }

    /**
     * Login
     *
     * @param credential user credential
     * @param callback   callback String
     */
    public void login(@NonNull Credential credential, OnBase<String> callback) {
        Predicates.requireNonNull(credential);

        mClientImpl.login(credential, callback);
    }

    /**
     * Logout
     */
    public void logout(OnBase<String> base) {
        mClientImpl.logout(base);
    }

    /**
     * 查询登录状态
     */
    public long getLoginStatus() {
        return Open_im_sdk.getLoginStatus();
    }

    /**
     * 当前登录uid
     */
    public String getLoginUid() {
        return Open_im_sdk.getLoginUid();
    }

    /**
     * 根据uid 批量查询用户信息
     *
     * @param uidList 用户id列表
     * @param base    callback List<{@link UserInfo}>
     */
    public void getUsersInfo(OnBase<List<UserInfo>> base, List<String> uidList) {
        Open_im_sdk.getUsersInfo(JsonUtil.toString(uidList), BaseImpl.arrayBase(base, UserInfo.class));
    }

    /**
     * 修改资料
     *
     * @param name   名字
     * @param icon   头像
     * @param gender 性别
     * @param mobile 手机号
     * @param birth  出生日期
     * @param email  邮箱
     * @param base   callback String
     */
    public void setSelfInfo(OnBase<String> base, String name, String icon, int gender, String mobile, String birth, String email) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("icon", icon);
        map.put("gender", gender);
        map.put("mobile", mobile);
        map.put("birth", birth);
        map.put("email", email);
        Open_im_sdk.setSelfInfo(JsonUtil.toString(map), BaseImpl.stringBase(base));
    }

    public void forceSyncLoginUerInfo() {
        Open_im_sdk.forceSyncLoginUerInfo();
    }

    public void forceReConn() {
        Open_im_sdk.forceReConn();
    }
}

