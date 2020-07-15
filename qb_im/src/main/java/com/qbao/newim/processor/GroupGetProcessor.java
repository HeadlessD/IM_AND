package com.qbao.newim.processor;

import com.google.flatbuffers.FlatBufferBuilder;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.NetConstDef;
import com.qbao.newim.constdef.PackTypeDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMRemarkDetail;
import com.qbao.newim.model.NIMGroupCreateInfo;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.network.IProcessInterface;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;

import java.nio.ByteBuffer;

import grouppack.T_GET_BATCH_GROUP_INFO_RQ;
import grouppack.T_GET_BATCH_GROUP_INFO_RS;
import grouppack.T_GROUP_BASE_INFO;
import grouppack.T_GROUP_REMARK_DETAIL_RQ;
import grouppack.T_GROUP_REMARK_DETAIL_RS;
import grouppack.T_GROUP_SAVE_CHANGE_RQ;
import grouppack.T_GROUP_SAVE_CHANGE_RS;
import grouppack.T_GROUP_SCAN_RQ;
import grouppack.T_GROUP_SCAN_RS;
import grouppack.T_GROUP_TYPE_LIST_RQ;
import grouppack.T_GROUP_TYPE_LIST_RS;

/**
 * Created by chenjian on 2017/5/23.
 */

public class GroupGetProcessor extends BaseProcessor{
    private static final String TAG = GroupGetProcessor.class.getSimpleName();
    private ProcessorGroupDetailRS p_group_detail_rs = new ProcessorGroupDetailRS();
    private ProcessorGroupRemarkRS p_remark_rs = new ProcessorGroupRemarkRS();
    private ProcessorGetGroupCreateRS p_group_create_rs = new ProcessorGetGroupCreateRS();
    private ProcessorSaveContactRS p_group_save_rs = new ProcessorSaveContactRS();
    private ProcessorScanGroupRS p_group_scan_rs = new ProcessorScanGroupRS();

    public GroupGetProcessor() {
        Init();
    }

    @Override
    protected void Init() {
        super.Init();
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_BATCH_INFO_RS, p_group_detail_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_REMARK_DETAIL_RS, p_remark_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_TYPE_LIST_RS, p_group_create_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_SAVE_CHANGE_RS, p_group_save_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_SCAN_RS, p_group_scan_rs);
    }

    public boolean SendRemarkDetailRQ(long group_id) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        T_GROUP_REMARK_DETAIL_RQ.startT_GROUP_REMARK_DETAIL_RQ(builder);
        T_GROUP_REMARK_DETAIL_RQ.addGroupId(builder, group_id);
        super.SendRQ(PackTypeDef.NEW_DEF_GROUP_REMARK_DETAIL_RQ, builder);
        return true;
    }

    public boolean SendGroupInfoRQ(long[] group_id) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int offset = T_GET_BATCH_GROUP_INFO_RQ.createListGroupIdVector(builder, group_id);
        T_GET_BATCH_GROUP_INFO_RQ.startT_GET_BATCH_GROUP_INFO_RQ(builder);
        T_GET_BATCH_GROUP_INFO_RQ.addListGroupId(builder, offset);
        super.SendRQ(PackTypeDef.NEW_DEF_GROUP_BATCH_INFO_RQ, builder);
        return true;
    }

    public boolean SendGroupCreateInfoRQ() {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        T_GROUP_TYPE_LIST_RQ.startT_GROUP_TYPE_LIST_RQ(builder);
        super.SendRQ(PackTypeDef.NEW_DEF_GROUP_TYPE_LIST_RQ, builder);
        return true;
    }

    public boolean SendSaveContactRQ(long group_id, byte type) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        T_GROUP_SAVE_CHANGE_RQ.startT_GROUP_SAVE_CHANGE_RQ(builder);
        T_GROUP_SAVE_CHANGE_RQ.addGroupId(builder, group_id);
        T_GROUP_SAVE_CHANGE_RQ.addSaveType(builder, type);
        super.SendRQ(PackTypeDef.NEW_DEF_GROUP_SAVE_CHANGE_RQ, builder);
        return true;
    }

    public boolean SendScanGroupRQ(long group_id, long user_id) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        T_GROUP_SCAN_RQ.startT_GROUP_SCAN_RQ(builder);
        T_GROUP_SCAN_RQ.addGroupId(builder, group_id);
        T_GROUP_SCAN_RQ.addUserIdShare(builder, user_id);
        super.SendRQ(PackTypeDef.NEW_DEF_GROUP_SCAN_RQ, builder);
        return true;
    }

    class ProcessorGroupDetailRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GET_BATCH_GROUP_INFO_RS t_group_rs = T_GET_BATCH_GROUP_INFO_RS.getRootAsT_GET_BATCH_GROUP_INFO_RS(byte_buffer);

            if(BodyEmpty(t_group_rs)) {
                Logger.error(TAG, "T_GROUP_GET_INFO_RS is null");
                return -1;
            }

            if(false == CheckHead(t_group_rs.sRsHead())) {
                Logger.error(TAG, "client get group info rs head is null");
                return -1;
            }

            int length = t_group_rs.listGroupIdLength();
            for (int i = 0; i < length; i++) {
                IMGroupInfo groupInfo = new IMGroupInfo();
                T_GROUP_BASE_INFO base_info = t_group_rs.listGroupInfo(i);
                boolean result = groupInfo.UnSerialize(base_info);
                if (!result) {
                    Logger.error(TAG, "recv error head = " + t_group_rs.sRsHead());
                    continue;
                }
                groupInfo.isMember = 1;
                NIMGroupInfoManager.getInstance().AddGroup(groupInfo);

            }

            //通知状态机状态完成
            DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.GROUP_INFO, null);
            return 1;
        }
    }

    class ProcessorGroupRemarkRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GROUP_REMARK_DETAIL_RS remark_detail_rs = T_GROUP_REMARK_DETAIL_RS.getRootAsT_GROUP_REMARK_DETAIL_RS(byte_buffer);

            if(BodyEmpty(remark_detail_rs)) {
                Logger.error(TAG, "T_GROUP_GET_INFO_RS is null");
                return -1;
            }

            if(false == CheckHead(remark_detail_rs.sRsHead())) {
                Logger.error(TAG, "client get group info rs head is null");
                return -1;
            }

            IMRemarkDetail remark = new IMRemarkDetail();
            boolean result = remark.UnSerialize(remark_detail_rs);
            if (!result) {
                Logger.error(TAG, "recv error head = " + remark_detail_rs.sRsHead());
                return -1;
            }

            DataObserver.Notify(DataConstDef.EVENT_REMARK_DETAIL, remark, true);
            return 1;
        }
    }

    class ProcessorGetGroupCreateRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GROUP_TYPE_LIST_RS list_rs = T_GROUP_TYPE_LIST_RS.getRootAsT_GROUP_TYPE_LIST_RS(byte_buffer);

            if(BodyEmpty(list_rs))
            {
                Logger.error(TAG, "T_GROUP_CREATE_INFO_RS is null");
                return -1;
            }

            if(false == CheckHead(list_rs.sRsHead()))
            {
                Logger.error(TAG, "client get group create info rs head is null");
                return -1;
            }

            NIMGroupCreateInfo info = new NIMGroupCreateInfo();
            info.UnSerialize(list_rs.listGroupTypeInfo(0));

            DataObserver.Notify(DataConstDef.EVENT_GROUP_CREATE_TYPE, info, null);
            return 1;
        }
    }

    class ProcessorSaveContactRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GROUP_SAVE_CHANGE_RS save_change_rs = T_GROUP_SAVE_CHANGE_RS.getRootAsT_GROUP_SAVE_CHANGE_RS(byte_buffer);

            if(BodyEmpty(save_change_rs)) {
                Logger.error(TAG, "T_GROUP_SAVE_CHANGE_RS is null");
                DataObserver.Notify(DataConstDef.EVENT_SAVE_CONTACT, null, null);
                return -1;
            }

            if(false == CheckHead(save_change_rs.sRsHead())) {
                Logger.error(TAG, "client get group save contact rs head is null");
                DataObserver.Notify(DataConstDef.EVENT_SAVE_CONTACT, null, null);
                return -1;
            }

            IMGroupInfo groupInfo = NIMGroupInfoManager.getInstance().getGroupInfo(save_change_rs.groupId());
            if (groupInfo != null) {
                groupInfo.is_save_contact = save_change_rs.saveType() == 1;
                NIMGroupInfoManager.getInstance().updateGroup(groupInfo);
                DataObserver.Notify(DataConstDef.EVENT_SAVE_CONTACT, groupInfo.group_id, groupInfo.is_save_contact);
            }
            return 1;
        }
    }

    class ProcessorScanGroupRS implements IProcessInterface {
        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GROUP_SCAN_RS scan_rs = T_GROUP_SCAN_RS.getRootAsT_GROUP_SCAN_RS(byte_buffer);

            if(BodyEmpty(scan_rs)) {
                Logger.error(TAG, "T_GROUP_SAVE_CHANGE_RS is null");
                DataObserver.Notify(DataConstDef.EVENT_SCAN_GROUP, null, null);
                return -1;
            }

            if(false == CheckHead(scan_rs.sRsHead())) {
                Logger.error(TAG, "client get group save contact rs head is null");
                DataObserver.Notify(DataConstDef.EVENT_SCAN_GROUP, null, null);
                return -1;
            }

            IMGroupInfo groupInfo = new IMGroupInfo();
            groupInfo.UnSerialize(scan_rs.groupInfo());
            groupInfo.isMember = scan_rs.isMember();
            DataObserver.Notify(DataConstDef.EVENT_SCAN_GROUP, groupInfo, scan_rs.userIdShare());
            return 1;
        }
    }

}
