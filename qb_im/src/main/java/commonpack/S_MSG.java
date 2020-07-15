// automatically generated by the FlatBuffers compiler, do not modify

package commonpack;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class S_MSG extends Table {
  public static S_MSG getRootAsS_MSG(ByteBuffer _bb) { return getRootAsS_MSG(_bb, new S_MSG()); }
  public static S_MSG getRootAsS_MSG(ByteBuffer _bb, S_MSG obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public S_MSG __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public short appId() { int o = __offset(4); return o != 0 ? bb.getShort(o + bb_pos) : 0; }
  public int sessionId() { int o = __offset(6); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public short chatType() { int o = __offset(8); return o != 0 ? bb.getShort(o + bb_pos) : 0; }
  public int mType() { int o = __offset(10); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public int sType() { int o = __offset(12); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public int extType() { int o = __offset(14); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public String msgContent() { int o = __offset(16); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer msgContentAsByteBuffer() { return __vector_as_bytebuffer(16, 1); }
  public long msgTime() { int o = __offset(18); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  public String sendUserName() { int o = __offset(20); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer sendUserNameAsByteBuffer() { return __vector_as_bytebuffer(20, 1); }

  public static int createS_MSG(FlatBufferBuilder builder,
      short app_id,
      int session_id,
      short chat_type,
      int m_type,
      int s_type,
      int ext_type,
      int msg_contentOffset,
      long msg_time,
      int send_user_nameOffset) {
    builder.startObject(9);
    S_MSG.addMsgTime(builder, msg_time);
    S_MSG.addSendUserName(builder, send_user_nameOffset);
    S_MSG.addMsgContent(builder, msg_contentOffset);
    S_MSG.addExtType(builder, ext_type);
    S_MSG.addSType(builder, s_type);
    S_MSG.addMType(builder, m_type);
    S_MSG.addSessionId(builder, session_id);
    S_MSG.addChatType(builder, chat_type);
    S_MSG.addAppId(builder, app_id);
    return S_MSG.endS_MSG(builder);
  }

  public static void startS_MSG(FlatBufferBuilder builder) { builder.startObject(9); }
  public static void addAppId(FlatBufferBuilder builder, short appId) { builder.addShort(0, appId, 0); }
  public static void addSessionId(FlatBufferBuilder builder, int sessionId) { builder.addInt(1, sessionId, 0); }
  public static void addChatType(FlatBufferBuilder builder, short chatType) { builder.addShort(2, chatType, 0); }
  public static void addMType(FlatBufferBuilder builder, int mType) { builder.addInt(3, mType, 0); }
  public static void addSType(FlatBufferBuilder builder, int sType) { builder.addInt(4, sType, 0); }
  public static void addExtType(FlatBufferBuilder builder, int extType) { builder.addInt(5, extType, 0); }
  public static void addMsgContent(FlatBufferBuilder builder, int msgContentOffset) { builder.addOffset(6, msgContentOffset, 0); }
  public static void addMsgTime(FlatBufferBuilder builder, long msgTime) { builder.addLong(7, msgTime, 0); }
  public static void addSendUserName(FlatBufferBuilder builder, int sendUserNameOffset) { builder.addOffset(8, sendUserNameOffset, 0); }
  public static int endS_MSG(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

