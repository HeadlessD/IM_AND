// automatically generated by the FlatBuffers compiler, do not modify

package offcialpack;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class T_SERVER_OFFCIAL_PRIVATE_MESSAGE_RS extends Table {
  public static T_SERVER_OFFCIAL_PRIVATE_MESSAGE_RS getRootAsT_SERVER_OFFCIAL_PRIVATE_MESSAGE_RS(ByteBuffer _bb) { return getRootAsT_SERVER_OFFCIAL_PRIVATE_MESSAGE_RS(_bb, new T_SERVER_OFFCIAL_PRIVATE_MESSAGE_RS()); }
  public static T_SERVER_OFFCIAL_PRIVATE_MESSAGE_RS getRootAsT_SERVER_OFFCIAL_PRIVATE_MESSAGE_RS(ByteBuffer _bb, T_SERVER_OFFCIAL_PRIVATE_MESSAGE_RS obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public T_SERVER_OFFCIAL_PRIVATE_MESSAGE_RS __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public commonpack.S_RS_HEAD sRsHead() { return sRsHead(new commonpack.S_RS_HEAD()); }
  public commonpack.S_RS_HEAD sRsHead(commonpack.S_RS_HEAD obj) { int o = __offset(4); return o != 0 ? obj.__init(o + bb_pos, bb) : null; }
  public long messageId() { int o = __offset(6); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  public long offcialId() { int o = __offset(8); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  public commonpack.S_MSG sMsg() { return sMsg(new commonpack.S_MSG()); }
  public commonpack.S_MSG sMsg(commonpack.S_MSG obj) { int o = __offset(10); return o != 0 ? obj.__init(__indirect(o + bb_pos), bb) : null; }

  public static void startT_SERVER_OFFCIAL_PRIVATE_MESSAGE_RS(FlatBufferBuilder builder) { builder.startObject(4); }
  public static void addSRsHead(FlatBufferBuilder builder, int sRsHeadOffset) { builder.addStruct(0, sRsHeadOffset, 0); }
  public static void addMessageId(FlatBufferBuilder builder, long messageId) { builder.addLong(1, messageId, 0); }
  public static void addOffcialId(FlatBufferBuilder builder, long offcialId) { builder.addLong(2, offcialId, 0); }
  public static void addSMsg(FlatBufferBuilder builder, int sMsgOffset) { builder.addOffset(3, sMsgOffset, 0); }
  public static int endT_SERVER_OFFCIAL_PRIVATE_MESSAGE_RS(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishT_SERVER_OFFCIAL_PRIVATE_MESSAGE_RSBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
}

