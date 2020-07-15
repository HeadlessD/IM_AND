package com.qbao.newim.constdef;
import java.util.HashMap;

public class ErrorDetail
{
    private static HashMap<Integer, String> error_detail_map = new HashMap<>();
    public static String GetErrorDetail(int error_code)
    {
        if(!error_detail_map.containsKey(error_code))
        {
            return "未知的错误信息:" + error_code;
        }
        return error_detail_map.get(error_code);
    }

    public static void Init()
    {
        //////////////////////////////////////sys start//////////////////////////////////////
        error_detail_map.put(ErrorCodeDef.RET_SYS_DISCON_USER_KICKED,"此账号在另一个浏览器登陆了");
        error_detail_map.put(ErrorCodeDef.RET_UNPACK_FAILED_RESULT,"服务器解包是失败");
        error_detail_map.put(ErrorCodeDef.RET_PLATFORM_ERROR,"错误的登录平台");
        error_detail_map.put(ErrorCodeDef.RET_REQ_FAST_ERROR,"请求过于频繁");
        error_detail_map.put(ErrorCodeDef.RET_REQ_REDIS_ERROR,"redis操作失败");
        error_detail_map.put(ErrorCodeDef.RET_SYS_PACK_TYPE_INVALID,"转发包失败");
        //////////////////////////////////////sys end///////////////////////////////////////

        //////////////////////////////////////user start//////////////////////////////////////
        error_detail_map.put(ErrorCodeDef.RET_USERINFO_BASE,"用户不存在");
        error_detail_map.put(ErrorCodeDef.RET_ADDUSER_BASE,"用户已存在");
        error_detail_map.put(ErrorCodeDef.RET_UPDATEUSERINFO_BASE,"用户属性不存在");
        error_detail_map.put(ErrorCodeDef.RET_GETUSERINFO_BASE,"用户信息已是最新");
        error_detail_map.put(ErrorCodeDef.RET_NOATTRIBUTE_BASE,"上传用户信息属性不全(user_name和mobile为必填项)");
        error_detail_map.put(ErrorCodeDef.RET_USERNAME_BASE,"user_name已存在");
        error_detail_map.put(ErrorCodeDef.RET_MOBILE_BASE,"mobile已存在");
        error_detail_map.put(ErrorCodeDef.RET_MAIL_BASE,"mail已存在");
        error_detail_map.put(ErrorCodeDef.RET_COMPLAINTTYPE_BASE,"举报类型错误");
        //////////////////////////////////////user end///////////////////////////////////////

        //////////////////////////////////////chat start//////////////////////////////////////
        error_detail_map.put(ErrorCodeDef.RET_CHAT_UPLOAD_METHOD,"上传方式不正确");
        error_detail_map.put(ErrorCodeDef.RET_CHAT_UPLOAD_TYPE,"上传格式不正确");
        error_detail_map.put(ErrorCodeDef.RET_CHAT_UPLOAD_RESULT,"上传失败");
        error_detail_map.put(ErrorCodeDef.RET_CHAT_SINGLE_STATUS_OP_USER_ID_INVALID,"对端用户id错误");
        error_detail_map.put(ErrorCodeDef.RET_CHAT_MSG_CONTENT_MAX,"消息内容过长");
        //////////////////////////////////////chat end///////////////////////////////////////

        //////////////////////////////////////friend start//////////////////////////////////////
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_ALREADY_EXISTED,"好友已存在");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_LIST_ERROR,"查询好友列表失败");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_ADD_ERROR,"添加好友失败");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_DEL_ERROR,"删除好友失败");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_MODIFY_ERROR,"修改好友信息失败");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_CONFIRM_TIMEOUT_ERROR,"好友确认请求超时");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_AGREE_ERROR,"好友同意失败");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_REFUSE_ERROR,"好友拒绝失败");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_BLACK_LIST_ERROR,"黑名单设置失败");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_HAS_BLACK_ERROR,"添加好友失败，是对方的黑名单用户");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_SETTING_STATUS_ERROR,"设置状态失败");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_HAVE_BLACK_ERROR,"已经在黑名单里");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_BE_DELETE_ERROR,"被删除好友，恢复好友关系");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_RELATION_ERROR,"等待好友处理");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_MAXCNT_ERROR,"好友数目达到上限");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_PEERMAXCNT_ERROR,"对端好友数达到上限");
        error_detail_map.put(ErrorCodeDef.RET_FRIEND_REMARK_ERROR,"好友备注名过长");
        //////////////////////////////////////friend end///////////////////////////////////////

        //////////////////////////////////////group start//////////////////////////////////////
        error_detail_map.put(ErrorCodeDef.RET_CREATE_USER_LIST_EMPTY,"建群用户列表为空");
        error_detail_map.put(ErrorCodeDef.RET_OPERATE_TYPE_ERROR,"用户操作无效");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_ID_INVALID,"群组不存在");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_OPREATE_USER_ID_INVALID,"用户不是群主");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_USER_NOT_JOIN,"用户未加入群");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_USER_HAS_JOIN,"用户已经加入该群");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_OPERATE_INFO_ERROR,"踢人或者邀请用户信息错误");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_CREATE_MAX_COUNT,"建群超过默认最大数");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_INVITE_FAILED,"添加用户失败");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_KICK_FAILED,"踢人失败");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_LEADER_CHANGE_SELF,"已经是群主");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_LEADER_NAME_IS_NIL,"被转让用户参数有误");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_AGREE_DEFAULT,"群当前为默认加入");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_AGREE_USER,"群当前为需要群主同意");
        error_detail_map.put(ErrorCodeDef.DEF_GROUP_AGREE_OLD_MESSAGE_ID_INVALID,"群主同意失败");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_ADD_ERROR,"添加群组失败");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_ADD_EXSIST,"当前群组已经存在");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_MEMBER_LIST_ERROR,"获取群成员列表失败");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_MEMBER_CHANGE_ERROR,"群主转让失败");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_ADD_MESSAGE_ERROR,"群消息保存失败");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_MAX_LIMIT_ERROR,"群人数已上线");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_SINGLE_LIMIT_ERROR,"单次拉人上线");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_MODIFY_REMARK_ERROR,"修改群备注失败");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_MESSAGE_ID_INVALID,"群聊消息id无效");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_BATCH_GET_LIST_EMPTY,"批量获取群信息列表为空");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_BATCH_LIST_INVALID,"批量获取群信息列表过大");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_OFFLINE_MAX_COUNT,"批量获取群离线超过上限");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_MODIFY_NAME_ERROR,"修改群名称失败名称过长");
        error_detail_map.put(ErrorCodeDef.RET_GROUP_MODIFY_NICK_NAME_ERROR,"修改群成员昵称失败昵称过长");
        //////////////////////////////////////group end///////////////////////////////////////

        //////////////////////////////////////offcial start//////////////////////////////////////
        error_detail_map.put(ErrorCodeDef.RET_OFFCIALMSG_BASE,"公众号消息发送过于频繁");
        error_detail_map.put(ErrorCodeDef.RET_OFFCIALNAME_BASE,"公众号用户名不能为空");
        error_detail_map.put(ErrorCodeDef.RET_OFFCIALMSG_CONTENT_BASE,"公众号消息结构不全");
        //////////////////////////////////////offcial end///////////////////////////////////////

        //////////////////////////////////////business start//////////////////////////////////////
        //////////////////////////////////////business end///////////////////////////////////////

    }
}
