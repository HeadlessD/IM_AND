package com.qbao.newim.processor;

import com.google.flatbuffers.FlatBufferBuilder;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.PackTypeDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.IMUserInfo;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.network.IProcessInterface;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;

import java.nio.ByteBuffer;

import userpack.T_KEYINFO;
import userpack.T_UPDATE_USER_INFO_RS;

/**
 * Created by chenjian on 2017/5/18.
 */

public class UserUpdateProcessor extends BaseProcessor{

    private static final String TAG = UserUpdateProcessor.class.getSimpleName();
    private ProcessorUploadUserRS p_upload_user_rs = new ProcessorUploadUserRS();

    public UserUpdateProcessor() {
        Init();
    }

    @Override
    protected void Init() {
        super.Init();
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_USER_CHANGE_RS, p_upload_user_rs);
    }

    public boolean uploadUserInfo(IMUserInfo info, boolean bAddToTcp) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        info.SerializeUpload(builder, bAddToTcp);
        super.SendRQ(PackTypeDef.NEW_DEF_USER_CHANGE_RQ, builder);
        return true;
    }

    class ProcessorUploadUserRS implements IProcessInterface{

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_UPDATE_USER_INFO_RS user_rs = T_UPDATE_USER_INFO_RS.getRootAsT_UPDATE_USER_INFO_RS(byte_buffer);

            if(BodyEmpty(user_rs)) {
                Logger.error(TAG, "T_UPDATE_USER_INFO_RS is null");
                return -1;
            }

            if(false == CheckHead(user_rs.sRsHead())) {
                Logger.error(TAG, "client get update user info rs head is null");
                int result = user_rs.sRsHead().result();
                String error = Integer.toHexString(result);
                Logger.error(TAG, error);
                return -1;
            }

            IMUserInfo userInfo = new IMUserInfo();
            int length = user_rs.keyLstInfoLength();
            for (int i = 0; i < length; i++) {
                T_KEYINFO key_info = user_rs.keyLstInfo(i);
                boolean result = userInfo.UnSerializeKey(key_info);
                if (!result) {
                    Logger.error(TAG, "recv error head = " + user_rs.sRsHead());
                    return -1;
                }
            }

            NIMUserInfoManager.getInstance().AddIMUser(NIMUserInfoManager.getInstance().GetSelfUserId(), userInfo);
            //通知状态机状态完成
            DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.USER_INFO, null);
            return 1;
        }
    }
}
