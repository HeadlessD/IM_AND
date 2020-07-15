package com.qbao.newim.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.views.ChatAudioLeftView;
import com.qbao.newim.views.ChatAudioRightView;
import com.qbao.newim.views.ChatCardLeftView;
import com.qbao.newim.views.ChatCardRightView;
import com.qbao.newim.views.ChatHintTipsView;
import com.qbao.newim.views.ChatLinkLeftView;
import com.qbao.newim.views.ChatLinkRightView;
import com.qbao.newim.views.ChatLocationLeftView;
import com.qbao.newim.views.ChatLocationRightView;
import com.qbao.newim.views.ChatPictureLeftView;
import com.qbao.newim.views.ChatPictureRightView;
import com.qbao.newim.views.ChatRedLeftView;
import com.qbao.newim.views.ChatRedRightView;
import com.qbao.newim.views.ChatSmileyLeftView;
import com.qbao.newim.views.ChatSmileyRightView;
import com.qbao.newim.views.ChatTextLeftView;
import com.qbao.newim.views.ChatTextRightView;
import com.qbao.newim.views.ChatView;

import java.util.ArrayList;
import java.util.Collections;

import static com.qbao.newim.activity.NIMChatActivity.CHAT_PAGE_SIZE;

/**
 * Created by chenjian on 2017/3/21.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private ArrayList<BaseMessageModel> mLists;

    public static final int RECEIVE_TXT = 1;
    public static final int SEND_TXT = 2;
    public static final int RECEIVE_VOICE = 3;
    public static final int SEND_VOICE = 4;
    public static final int SEND_IMG = 5;
    public static final int RECEIVE_IMG = 6;
    public static final int SEND_GIF_IMG = 7;
    public static final int RECEIVE_GIF_IMG = 8;
    public static final int HINT_TIPS = 9;
    public static final int SEND_MAP = 10;
    public static final int RECEIVE_MAP = 11;
    public static final int SEND_CARD = 12;
    public static final int RECEIVE_CARD = 13;
    public static final int SEND_RED = 14;
    public static final int RECEIVE_RED = 15;
    public static final int SEND_LINK = 16;
    public static final int RECEIVE_LINK = 17;
    private Context mContext;
    private boolean is_show_nick;

    public ChatAdapter(Context context, ArrayList<BaseMessageModel> mLists) {
        this.mLists = mLists;
        this.mContext = context;
    }

    public boolean is_show_nick() {
        return is_show_nick;
    }

    public void setIs_show_nick(boolean is_show_nick) {
        this.is_show_nick = is_show_nick;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatViewHolder(getView(viewType));
    }

    private View getView(int viewType) {
        ChatView chatView;
        switch (viewType) {
            case RECEIVE_TXT:
                chatView = new ChatTextLeftView(mContext, this);
                break;
            case RECEIVE_VOICE:
                chatView = new ChatAudioLeftView(mContext, this);
                break;
            case SEND_TXT:
                chatView = new ChatTextRightView(mContext, this);
                break;
            case SEND_VOICE:
                chatView = new ChatAudioRightView(mContext, this);
                break;
            case SEND_IMG:
                chatView = new ChatPictureRightView(mContext, this);
                break;
            case RECEIVE_IMG:
                chatView = new ChatPictureLeftView(mContext, this);
                break;
            case SEND_GIF_IMG:
                chatView = new ChatSmileyRightView(mContext, this);
                break;
            case RECEIVE_GIF_IMG:
                chatView = new ChatSmileyLeftView(mContext, this);
                break;
            case HINT_TIPS:
                chatView = new ChatHintTipsView(mContext, this);
                break;
            case SEND_MAP:
                chatView = new ChatLocationRightView(mContext, this);
                break;
            case RECEIVE_MAP:
                chatView = new ChatLocationLeftView(mContext, this);
                break;
            case SEND_CARD:
                chatView = new ChatCardRightView(mContext, this);
                break;
            case RECEIVE_CARD:
                chatView = new ChatCardLeftView(mContext, this);
                break;
            case SEND_RED:
                chatView = new ChatRedRightView(mContext, this);
                break;
            case RECEIVE_RED:
                chatView = new ChatRedLeftView(mContext, this);
                break;
            case SEND_LINK:
                chatView = new ChatLinkRightView(mContext, this);
                break;
            case RECEIVE_LINK:
                chatView = new ChatLinkLeftView(mContext, this);
                break;
            default:
                throw new IllegalStateException("Invalid view type ID " + viewType);
        }

        return chatView;
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        BaseMessageModel messageModel = mLists.get(position);
        holder.chatView.setMessage(position, messageModel);
    }

    @Override
    public int getItemCount() {
        if (mLists == null) {
            return 0;
        }
        return mLists.size();
    }

    @Override
    public int getItemViewType(int position) {
        BaseMessageModel model = mLists.get(position);
        int type = -1;
        switch (model.m_type) {
            case MsgConstDef.MSG_M_TYPE.TEXT:
                if (model.s_type > 0) {
                    type = HINT_TIPS;
                } else {
                    type = model.is_self ? SEND_TXT : RECEIVE_TXT;
                }

                break;
            case MsgConstDef.MSG_M_TYPE.VOICE:
                type = model.is_self ? SEND_VOICE : RECEIVE_VOICE;
                break;
            case MsgConstDef.MSG_M_TYPE.IMAGE:
                type = model.is_self ? SEND_IMG : RECEIVE_IMG;
                break;
            case MsgConstDef.MSG_M_TYPE.SMILEY:
                type = model.is_self ? SEND_GIF_IMG : RECEIVE_GIF_IMG;
                break;
            case MsgConstDef.MSG_M_TYPE.MAP:
                type = model.is_self ? SEND_MAP : RECEIVE_MAP;
                break;
            case MsgConstDef.MSG_M_TYPE.JSON:
                if (model.s_type == MsgConstDef.MSG_S_TYPE.VCARD) {
                    type = model.is_self ? SEND_CARD : RECEIVE_CARD;
                } else if (model.s_type == MsgConstDef.MSG_S_TYPE.RED_PACKET) {
                    type = model.is_self ? SEND_RED : RECEIVE_RED;
                }
                break;
            case MsgConstDef.MSG_M_TYPE.HTML:
                type = model.is_self ? SEND_LINK : RECEIVE_LINK;
                break;
        }

        return type;
    }

    public BaseMessageModel getLastMsg() {
        BaseMessageModel item = null;
        if (mLists != null && !mLists.isEmpty()) {
            item = mLists.get(mLists.size() - 1);
        }

        return item;
    }

    /**
     * 检查该消息是否存在聊天对话
     *
     * @param item
     * @return
     */
    public boolean isChatMsgExist(BaseMessageModel item) {
        if (item == null) {
            return true;
        }

        final int count = getItemCount();
        for (int i = count - 1; i >= 0 && (count - i) <= CHAT_PAGE_SIZE; i--) {
            final BaseMessageModel tItem = getItem(i);
            if (tItem.message_id == item.message_id) {
                return true;
            }
        }

        return false;
    }

    // 添加一条消息
    public void addDataAtEnd(BaseMessageModel item) {
        mLists.add(item);
        notifyItemInserted(mLists.size() - 1);
    }

    public void updateItemView(int position) {
        notifyItemChanged(position);
    }

    public void deleteItemView(int position) {
        mLists.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeRemoved(position, mLists.size() - position);
    }

    /**
     * 添加一条信息
     *
     * @param item
     * @param sync 判断该item是否已经存在，如果存在就不添加
     */
    public boolean addDataAtEnd(BaseMessageModel item, boolean sort, boolean sync) {
        boolean isExist = false;
        if (sync) {
            isExist = isChatMsgExist(item);
        }

        if (!isExist) {
            mLists.add(item);
        }
        if (sort) {
            Collections.sort(mLists);
        }
        notifyDataSetChanged();

        return isExist;
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        ChatView chatView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            chatView = (ChatView) itemView;
        }
    }

    public BaseMessageModel getItem(int position) {
        if (mLists == null || mLists.isEmpty()) {
            return null;
        } else if (position >= mLists.size()) {
            return mLists.get(mLists.size() - 1);
        } else {
            return mLists.get(position);
        }
    }
}
