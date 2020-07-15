package com.qbao.newim.processor;

import com.google.flatbuffers.FlatBufferBuilder;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.NetConstDef;
import com.qbao.newim.constdef.PackTypeDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMGroupUserManager;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.network.IProcessInterface;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import commonpack.USER_BASE_INFO;
import grouppack.T_GROUP_BASE_INFO;
import grouppack.T_GROUP_DETAIL_INFO_RQ;
import grouppack.T_GROUP_DETAIL_INFO_RS;
import grouppack.T_GROUP_LIST_IDS_RQ;
import grouppack.T_GROUP_LIST_IDS_RS;
import grouppack.T_GROUP_LIST_RQ;
import grouppack.T_GROUP_LIST_RS;
import grouppack.T_GROUP_RELATION_USER_INFO;


/**
 * Created by chenjian on 2017/6/19.
 */

public class GroupListProcessor extends BaseProcessor {
    private static final String TAG = GroupListProcessor.class.getSimpleName();
    private ProcessorGroupListRS p_group_list_rs = new ProcessorGroupListRS();
    private ProcessorGroupDetailRS p_group_detail_rs = new ProcessorGroupDetailRS();
    private ProcessorGroupIdRS p_group_id_rs = new ProcessorGroupIdRS();
    private ArrayList<IMGroupInfo> group_info_list = new ArrayList<>();
    private int nListIndex;
    private int nDetailIndex;
    private boolean isMember = true;

    public GroupListProcessor() {
        Init();
    }

    @Override
    protected void Init() {
        super.Init();
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_LIST_RS, p_group_list_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_DETAIL_INFO_RS, p_group_detail_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_LIST_IDS_RS, p_group_id_rs);
    }

    @Override
    protected void ProcessEvent() {
    }

    public boolean sendGroupListRQ() {
        FlatBufferBuilder bufferBuilder = new FlatBufferBuilder();
        T_GROUP_LIST_RQ.startT_GROUP_LIST_RQ(bufferBuilder);
        T_GROUP_LIST_RQ.addGroupListIndex(bufferBuilder, nListIndex);
        super.SendRQ(PackTypeDef.NEW_DEF_GROUP_LIST_RQ, bufferBuilder);
        return true;
    }

    public boolean sendGroupIdRQ() {
        FlatBufferBuilder bufferBuilder = new FlatBufferBuilder();
        T_GROUP_LIST_IDS_RQ.startT_GROUP_LIST_IDS_RQ(bufferBuilder);
        super.SendRQ(PackTypeDef.NEW_DEF_GROUP_LIST_IDS_RQ, bufferBuilder);
        return true;
    }

    public boolean sendGroupDetailRQ(long group_id) {
        FlatBufferBuilder bufferBuilder = new FlatBufferBuilder();
        T_GROUP_DETAIL_INFO_RQ.startT_GROUP_DETAIL_INFO_RQ(bufferBuilder);
        T_GROUP_DETAIL_INFO_RQ.addGroupId(bufferBuilder, group_id);
        T_GROUP_DETAIL_INFO_RQ.addGroupMemberIndex(bufferBuilder, nDetailIndex);
        super.SendRQ(PackTypeDef.NEW_DEF_GROUP_DETAIL_INFO_RQ, bufferBuilder);
        return true;
    }

    class ProcessorGroupListRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GROUP_LIST_RS group_rs = T_GROUP_LIST_RS.getRootAsT_GROUP_LIST_RS(byte_buffer);

            if (BodyEmpty(group_rs)) {
                Logger.error(TAG, "T_GROUP_LIST_RS is null");
                return -1;
            }

            if (false == CheckHead(group_rs.sRsHead())) {
                Logger.error(TAG, "client get group list rs head is null");
                return -1;
            }

            int index = group_rs.groupListIndex();
            nListIndex = group_rs.groupListIndex();
            int length = group_rs.groupInfoListLength();
            for (int i = 0; i < length; i++) {
                T_GROUP_BASE_INFO t_base = group_rs.groupInfoList(i);
                IMGroupInfo info = new IMGroupInfo();
                boolean result = info.UnSerialize(t_base);
                if (!result) {
                    break;
                }
                info.isMember = 1;
                NIMGroupInfoManager.getInstance().AddGroup(info);
                group_info_list.add(info);
            }

            if (index == -1) {
                //通知状态机状态完成
                DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.GROUP_INFO, null);
                return 1;
            }

            sendGroupListRQ();
            return 1;
        }
    }

    class ProcessorGroupIdRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GROUP_LIST_IDS_RS list_ids_rs = T_GROUP_LIST_IDS_RS.getRootAsT_GROUP_LIST_IDS_RS(byte_buffer);

            if (BodyEmpty(list_ids_rs)) {
                Logger.error(TAG, "T_GROUP_LIST_RS is null");
                return -1;
            }

            if (false == CheckHead(list_ids_rs.sRsHead())) {
                Logger.error(TAG, "client get group list rs head is null");
                return -1;
            }

            // 当前群id个数大于本地，则要进行添加并请求群详情，如果等于则不管，小于就要删除本地
            int server_len = list_ids_rs.groupInfoListLength();
            ArrayList<Long> exist_list = NIMGroupInfoManager.getInstance().getIdList();
            ArrayList<Long> new_list = new ArrayList<>();
            for (int i = 0; i < server_len; i++) {
                T_GROUP_RELATION_USER_INFO info = list_ids_rs.groupInfoList(i);
                boolean contain = NIMGroupInfoManager.getInstance().isGroupExist(info.groupId());
                if (contain) {
                    exist_list.remove(info.groupId());
                } else {
                    IMGroupInfo groupInfo = new IMGroupInfo();
                    groupInfo.group_id = info.groupId();
                    groupInfo.is_save_contact = info.saveType() == 1;
                    groupInfo.notify_type = info.messageStatus();
                    NIMGroupInfoManager.getInstance().AddGroup(groupInfo);
                    new_list.add(info.groupId());
                }
            }

            // 循环完，发现本地还有服务器不存在的，则更新
            if (exist_list.size() > 0) {
                for (Long id : exist_list) {
                    IMGroupInfo groupInfo = NIMGroupInfoManager.getInstance().getGroupInfo(id);
                    groupInfo.isMember = 0;
                    NIMGroupInfoManager.getInstance().updateGroup(groupInfo);
                }
            }
            // 循环完，发现还有新的id，本地不存在，则去请求详情
            if (new_list.size() > 0) {
                GroupGetProcessor processor = GlobalProcessor.getInstance().getGroupGetProcessor();
                List<Long> id_list = new ArrayList<>();
                for (int i = 1; i <= new_list.size(); i++) {
                    id_list.add(new_list.get(i - 1));
                    if (i % GlobalVariable.GROUP_DETAIL_COUNT == 0 || i == new_list.size()) {
                        long[] ids = new long[id_list.size()];
                        for (int j = 0; j < id_list.size(); j++) {
                            ids[j] = id_list.get(j);
                        }
                        processor.SendGroupInfoRQ(ids);
                        id_list.clear();
                    }
                }
                return 1;
            } else {
                //通知状态机状态完成
                DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.GROUP_INFO, null);
            }

            return 1;
        }
    }

    class ProcessorGroupDetailRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GROUP_DETAIL_INFO_RS group_rs = T_GROUP_DETAIL_INFO_RS.getRootAsT_GROUP_DETAIL_INFO_RS(byte_buffer);

            if (BodyEmpty(group_rs)) {
                Logger.error(TAG, "T_GROUP_DETAIL_INFO_RS is null");
                return -1;
            }

            if (false == CheckHead(group_rs.sRsHead())) {
                Logger.error(TAG, "client get group detail rs head is null");
                return -1;
            }

            int length = group_rs.listMembersInfoLength();
            long group_id = group_rs.groupId();
            ArrayList<IMGroupUserInfo> scan_show_list = new ArrayList<>();

            for (int i = 0; i < length; i++) {
                USER_BASE_INFO t_base = group_rs.listMembersInfo(i);
                IMGroupUserInfo user_info = new IMGroupUserInfo();
                boolean result = user_info.UnSerialize(t_base);
                if (!result) {
                    break;
                }

                // 当前非群成员，请求一次即可，如果是成员需要存数据
                if (!isMember) {
                    scan_show_list.add(user_info);
                }
                user_info.setGroup_id(group_id);

                NIMGroupUserManager.getInstance().AddGroupUser(group_id, user_info);
            }

            // 访问一次
            if (!isMember) {
                isMember = true;
                DataObserver.Notify(DataConstDef.EVENT_SCAN_GROUP_USER, scan_show_list, null);
                return 1;
            }

            nDetailIndex = group_rs.groupMemberIndex();
            if (nDetailIndex == -1) {
                DataObserver.Notify(DataConstDef.EVENT_GROUP_DETAIL, null, null);
                nDetailIndex = 0;
                return -1;
            }

            sendGroupDetailRQ(group_rs.groupId());
            return 1;
        }
    }

    public void setMember(boolean member) {
        isMember = member;
    }
}
