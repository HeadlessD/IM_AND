package com.qbao.newim.manager;

import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.qbao.newim.configure.Constants;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.util.BaseUtil;

import java.util.List;

/**
 * Created by chenjian on 2017/4/12.
 */

public class ChatMsgBuildManager {
    public static final int LONG_PICTURE = 101;
    public static final int WIDE_PICTURE = 102;

    public static BaseMessageModel buildChatTypeMsg(int chat_type) {
        BaseMessageModel model = null;
        switch (chat_type) {
            case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
                ScMessageModel sc_model = new ScMessageModel();
                sc_model.send_user_name = NIMUserInfoManager.getInstance().GetSelfUserName();
                model = sc_model;
                break;
            case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                model = new GcMessageModel();
                break;
            case MsgConstDef.MSG_CHAT_TYPE.PUBLIC:
                model = new ScMessageModel();
                break;
            case MsgConstDef.MSG_CHAT_TYPE.BUSINESS:
                model = new ScMessageModel();
                break;
        }

        if (model != null) {
            model.app_id = Constants.APP_ID;
            model.msg_time = BaseUtil.GetServerTime();
            model.msg_status = MsgConstDef.MSG_STATUS.SENDING;
            model.is_self = true;
        }

        return model;
    }

    /**
     * 构建普通文字消息
     * @param content 消息内容
     * @return
     */
    public static BaseMessageModel buildTextMsg(int chat_type, String content) {
        BaseMessageModel msg = buildChatTypeMsg(chat_type);
        msg.m_type = MsgConstDef.MSG_M_TYPE.TEXT;
        msg.s_type = MsgConstDef.MSG_S_TYPE.INVALID;
        msg.msg_content = content;
        return msg;
    }

    /**
     * 构建位置消息
     * @param content 消息内容
     * @return
     */
    public static BaseMessageModel buildLocationMsg(int chat_type, String content) {
        BaseMessageModel msg = buildChatTypeMsg(chat_type);
        msg.m_type = MsgConstDef.MSG_M_TYPE.MAP;
        msg.s_type = MsgConstDef.MSG_S_TYPE.INVALID;
        msg.msg_content = content;
        return msg;
    }

    /**
     * 构建红包消息
     * @param content 消息内容
     * @return
     */
    public static BaseMessageModel buildRedMsg(int chat_type, String content) {
        BaseMessageModel msg = buildChatTypeMsg(chat_type);
        msg.m_type = MsgConstDef.MSG_M_TYPE.JSON;
        msg.s_type = MsgConstDef.MSG_S_TYPE.RED_PACKET;
        msg.msg_content = content;
        return msg;
    }

    /**
     * 构建链接消息
     * @param content 消息内容
     * @return
     */
    public static BaseMessageModel buildLinkMsg(int chat_type, String content) {
        BaseMessageModel msg = buildChatTypeMsg(chat_type);
        msg.m_type = MsgConstDef.MSG_M_TYPE.HTML;
        msg.msg_content = content;
        return msg;
    }

    /**
     * 构建名片消息
     * @param content 消息内容
     * @return
     */
    public static BaseMessageModel buildCardMsg(int chat_type, String content) {
        BaseMessageModel msg = buildChatTypeMsg(chat_type);
        msg.m_type = MsgConstDef.MSG_M_TYPE.JSON;
        msg.s_type = MsgConstDef.MSG_S_TYPE.VCARD;
        msg.msg_content = content;
        return msg;
    }

    /**
     * 构建语音消息，content表示语音连接地址，ext_type表示语音时长
     * @param audioDuration
     * @param audioPath
     * @return
     */
    public static BaseMessageModel buildAudioMsg(int chat_type, int audioDuration, String audioPath) {
        BaseMessageModel msg = buildChatTypeMsg(chat_type);
        msg.m_type = MsgConstDef.MSG_M_TYPE.VOICE;
        msg.audio_path = audioPath;
        msg.ext_type = audioDuration;
        return msg;
    }

    public static BaseMessageModel buildPicMsg(int chat_type, String picPath) {
        BaseMessageModel msg = buildChatTypeMsg(chat_type);
        msg.m_type = MsgConstDef.MSG_M_TYPE.IMAGE;
        msg.pic_path = picPath;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picPath, options);
        if (options.outWidth == 0 || options.outHeight == 0) {
            return null;
        }
        int scale =  (100 * options.outHeight) / options.outWidth;
        if (scale > 1) {
            msg.ext_type = LONG_PICTURE;
        } else {
            msg.ext_type = WIDE_PICTURE;
        }
        return msg;
    }

    public static BaseMessageModel buildGifMsg(int chat_type, String text) {
        BaseMessageModel msg = buildChatTypeMsg(chat_type);
        msg.m_type = MsgConstDef.MSG_M_TYPE.SMILEY;
        msg.msg_content = text;
        return msg;
    }


    public static void buildGroupTipsMsg(GcMessageModel operateMode)
    {
        operateMode.m_type = MsgConstDef.MSG_M_TYPE.TEXT;
        operateMode.s_type = MsgConstDef.MSG_S_TYPE.TIP;
        operateMode.msg_status = MsgConstDef.MSG_STATUS.UNREAD;

        boolean is_self = false;

        if (operateMode.user_info_list != null && operateMode.user_info_list.size() > 0)
        {
            if (operateMode.user_id == NIMUserInfoManager.getInstance().GetSelfUserId())
            {
                is_self = true;
            }
        }

        List<IMGroupUserInfo> group_list;
        switch (operateMode.big_msg_type)
        {
            case MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_CREATE:
                group_list = operateMode.user_info_list;
                if (group_list == null || group_list.size() < 2)
                {
                    return ;
                }

                String invite_name;
                if (is_self)
                {
                    invite_name = "你";
                }
                else
                {
                    String name = getFriendRemarkName(operateMode.user_id);
                    invite_name = handString(TextUtils.isEmpty(name) ? operateMode.operate_user_name : name);
                }

                String content_name = "\"";
                for (int i = 0; i < group_list.size(); i++)
                {
                    IMGroupUserInfo groupUserInfo = group_list.get(i);
                    if (groupUserInfo.user_id == operateMode.user_id)
                    {
                        continue;
                    }
                    boolean contain_self = groupUserInfo.user_id == NIMUserInfoManager.getInstance().GetSelfUserId();
                    if (contain_self)
                    {
                        content_name += "你";
                    }
                    else
                    {
                        String show_name = getFriendRemarkName(groupUserInfo.user_id);
                        content_name += TextUtils.isEmpty(show_name) ? groupUserInfo.user_nick_name : show_name;
                    }

                    if (i < group_list.size() - 1)
                        content_name += "、";
                }

                content_name += "\"";
                operateMode.msg_content += invite_name + "邀请" + content_name + "加入了群聊";
                break;
            case MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ADD_USER:
                String suffix;
                group_list = operateMode.user_info_list;
                if (group_list == null || group_list.isEmpty())
                {
                    return ;
                }

                String add_name;
                if (is_self)
                {
                    add_name = "你";
                }
                else
                {
                    String name = getFriendRemarkName(operateMode.user_id);
                    add_name = handString(TextUtils.isEmpty(name) ? operateMode.operate_user_name : name);
                }

                String tip_name = "";
                // 当前需要经过群主同意
                boolean is_need_agree = operateMode.message_old_id > 0;

                for (int i = 0; i < group_list.size(); i++)
                {
                    IMGroupUserInfo userInfo = group_list.get(i);
                    if (is_need_agree)
                    {
                        userInfo.need_agree = is_need_agree;
                        NIMGroupUserManager.getInstance().AddGroupUser(operateMode.group_id, userInfo);
                    }
                    boolean contain_self = userInfo.user_id == NIMUserInfoManager.getInstance().GetSelfUserId();
                    if (contain_self)
                    {
                        tip_name += "你";
                    }
                    else
                    {
                        String show_name = getFriendRemarkName(userInfo.user_id);
                        tip_name += handString(TextUtils.isEmpty(show_name) ? userInfo.user_nick_name : show_name);
                    }

                    if (i < group_list.size() - 1)
                        tip_name += "、";
                }

                if (is_need_agree)
                {
                    suffix = add_name + " 同意 " + tip_name ;
                }
                else
                {
                    suffix =  add_name + " 邀请 " + tip_name ;
                }
                suffix +=" 进群";
                operateMode.msg_content = suffix;
                break;
            case MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_KICK_USER:
                group_list = operateMode.user_info_list;
                if (group_list == null || group_list.isEmpty())
                {
                    return ;
                }

                // 当前操作者是否是同一个人
                boolean self = group_list.get(0).user_id == operateMode.user_id;
                boolean contain_self = false;
                String operate_name = getFriendRemarkName(operateMode.user_id);

                // 当前操作踢的人是自己,表示退群
                if (self && operateMode.user_id == NIMUserInfoManager.getInstance().GetSelfUserId())
                {
                    return ;
                }

                if (self)
                {
                    operateMode.msg_content = handString(operateMode.operate_user_name) + "退出了该群";
                    NIMGroupUserManager.getInstance().deleteGroupUser(operateMode.group_id, operateMode.user_id);
                }
                else
                {
                    String kick_name;
                    if (is_self)
                    {
                        kick_name = "你";
                    }
                    else
                    {
                        kick_name = handString(TextUtils.isEmpty(operate_name) ? operateMode.operate_user_name : operate_name);
                    }

                    String member = "";
                    for (int i = 0; i < group_list.size(); i++)
                    {
                        IMGroupUserInfo userInfo = group_list.get(i);
                        String name = getFriendRemarkName(userInfo.user_id);
                        member += handString(TextUtils.isEmpty(name) ? userInfo.user_nick_name : name);
                        NIMGroupUserManager.getInstance().deleteGroupUser(operateMode.group_id, userInfo.user_id);
                        contain_self = userInfo.user_id == NIMUserInfoManager.getInstance().GetSelfUserId();
                        if (i < group_list.size() - 1)
                            member += "、";
                    }

                    if (contain_self)
                    {
                        operateMode.msg_content = "你被" + (TextUtils.isEmpty(operate_name) ? operateMode.operate_user_name : operate_name) + "移出群聊";
                    }
                    else
                    {
                        operateMode.msg_content = kick_name + "将 " + member + "移出群聊";
                    }
                }

                // 当前操作者是原先群主时，更新群信息
                IMGroupInfo origin_info = NIMGroupInfoManager.getInstance().getGroupInfo(operateMode.group_id);
                if (origin_info == null)
                {
                    return ;
                }
                NIMGroupInfoManager.getInstance().updateGroup(origin_info);
                // 非群主不可见
                if (NIMUserInfoManager.getInstance().GetSelfUserId() == origin_info.group_manager_user_id)
                {
                    contain_self = true;
                }
                if (!contain_self)
                {
                    return ;
                }

                break;
           case MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ENTER_AGREE:
               operateMode.msg_content = "群主已启用 \"群聊邀请确认\" , 群成员需群主确认才能邀请朋友进群。";
                break;
            case MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ENTER_DEFAULT:
                operateMode.msg_content = "群主恢复默认进群方式。";
                break;
            case MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_MODIFY_GROUP_NAME:
                if (is_self) {
                    operateMode.msg_content = "你修改群名为" + handString(operateMode.group_modify_content);
                } else {
                    String name = getFriendRemarkName(operateMode.user_id);
                    operateMode.msg_content = handString(TextUtils.isEmpty(name) ? operateMode.operate_user_name : name) +
                            "修改群名为" + handString(operateMode.group_modify_content);
                }


                break;
            case MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ADD_USER_AGREE:
                // 当前不是群主，不需要显示
                boolean is_manager = NIMGroupInfoManager.getInstance().checkUserIsGroupManager(operateMode.group_id, NIMUserInfoManager.getInstance().GetSelfUserId());
                if (!is_manager)
                {
                    return ;
                }

                operateMode.s_type = MsgConstDef.MSG_S_TYPE.GROUP_NEED_AGREE;
                group_list = operateMode.user_info_list;
                if (group_list == null || group_list.isEmpty())
                {
                    return ;
                }

                String name = getFriendRemarkName(operateMode.user_id);
                operateMode.msg_content = handString(TextUtils.isEmpty(name) ? operateMode.operate_user_name : name)
                        + "想邀请" + group_list.size() + "位朋友进群";
                break;
            case MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_MODIFY_GROUP_REMARK:
                IMGroupInfo remark_Info = NIMGroupInfoManager.getInstance().getGroupInfo(operateMode.group_id);
                remark_Info.group_remark = operateMode.group_modify_content;
                operateMode.msg_content = "群公告更新：" + handString(remark_Info.group_remark);
                break;
            case MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_SCAN_ADD_USER:
                if (operateMode.user_info_list.isEmpty()) {
                    return ;
                }

                IMGroupUserInfo join_group_user = operateMode.user_info_list.get(0);
                NIMGroupUserManager.getInstance().AddGroupUser(operateMode.group_id, join_group_user);
                String share_name = getFriendRemarkName(operateMode.user_id);
                String scan_name = getFriendRemarkName(join_group_user.user_id);

                if (join_group_user.user_id == NIMUserInfoManager.getInstance().GetSelfUserId()) {
                    operateMode.msg_content = "你通过扫描加入群聊";
                } else if (operateMode.user_id == NIMUserInfoManager.getInstance().GetSelfUserId()){
                    operateMode.msg_content = handString(TextUtils.isEmpty(scan_name) ? join_group_user.user_nick_name
                    : scan_name) + "通过扫描你分享的二维码加入群聊";
                } else {
                    operateMode.msg_content = handString(TextUtils.isEmpty(scan_name) ? join_group_user.user_nick_name
                            : scan_name) + "通过扫描 "+ handString(TextUtils.isEmpty(share_name) ? operateMode.operate_user_name
                            : share_name) + "分享的二维码加入群聊";
                }

                break;
        }
    }

    private static String handString(String str) {
        return "\"" + str +"\"";
    }

    private static String getFriendRemarkName(long id) {
        IMFriendInfo friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(id);
        if (friendInfo != null && !TextUtils.isEmpty(friendInfo.remark_name)) {
            return friendInfo.remark_name;
        }

        return "";
    }

    public static String HandleRichText(int type, String msg_content, int s_type) {
        String txt = "";
        switch (type) {
            case MsgConstDef.MSG_M_TYPE.IMAGE:
                txt = "[图片]";
                break;
            case MsgConstDef.MSG_M_TYPE.HTML:
                txt = "[链接]";
                break;
            case MsgConstDef.MSG_M_TYPE.MAP:
                txt = "[位置]";
                break;
            case MsgConstDef.MSG_M_TYPE.VOICE:
                txt = "[语音]";
                break;
            case MsgConstDef.MSG_M_TYPE.TEXT:
                txt = msg_content;
                break;
            case MsgConstDef.MSG_M_TYPE.SMILEY:
                txt = "[动画表情]";
                break;
            case MsgConstDef.MSG_M_TYPE.JSON:
                if (s_type == MsgConstDef.MSG_S_TYPE.VCARD) {
                    txt = "[名片]";
                } else if (s_type == MsgConstDef.MSG_S_TYPE.RED_PACKET) {
                    txt = "[红包]";
                }
                break;
        }

        return txt;
    }

    public static String GenWaiterTips(long b_id, long w_id, String name)
    {
        return w_id == b_id ? "商家" : "小二" + name +  "为您服务";
    }
}
