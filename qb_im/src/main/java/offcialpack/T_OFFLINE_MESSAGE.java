// automatically generated by the FlatBuffers compiler, do not modify

package offcialpack;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class T_OFFLINE_MESSAGE extends Table {
  public static T_OFFLINE_MESSAGE getRootAsT_OFFLINE_MESSAGE(ByteBuffer _bb) { return getRootAsT_OFFLINE_MESSAGE(_bb, new T_OFFLINE_MESSAGE()); }
  public static T_OFFLINE_MESSAGE getRootAsT_OFFLINE_MESSAGE(ByteBuffer _bb, T_OFFLINE_MESSAGE obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public T_OFFLINE_MESSAGE __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public long messageId() { int o = __offset(4); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  public commonpack.S_MSG sMsg() { return sMsg(new commonpack.S_MSG()); }
  public commonpack.S_MSG sMsg(commonpack.S_MSG obj) { int o = __offset(6); return o != 0 ? obj.__init(__indirect(o + bb_pos), bb) : null; }

  public static int createT_OFFLINE_MESSAGE(FlatBufferBuilder builder,
      long message_id,
      int s_msgOffset) {
    builder.startObject(2);
    T_OFFLINE_MESSAGE.addMessageId(builder, message_id);
    T_OFFLINE_MESSAGE.addSMsg(builder, s_msgOffset);
    return T_OFFLINE_MESSAGE.endT_OFFLINE_MESSAGE(builder);
  }

  public static void startT_OFFLINE_MESSAGE(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addMessageId(FlatBufferBuilder builder, long messageId) { builder.addLong(0, messageId, 0); }
  public static void addSMsg(FlatBufferBuilder builder, int sMsgOffset) { builder.addOffset(1, sMsgOffset, 0); }
  public static int endT_OFFLINE_MESSAGE(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

