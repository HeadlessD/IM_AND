// automatically generated by the FlatBuffers compiler, do not modify

package userpack;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class T_USER_COMPLAINT_RQ extends Table {
  public static T_USER_COMPLAINT_RQ getRootAsT_USER_COMPLAINT_RQ(ByteBuffer _bb) { return getRootAsT_USER_COMPLAINT_RQ(_bb, new T_USER_COMPLAINT_RQ()); }
  public static T_USER_COMPLAINT_RQ getRootAsT_USER_COMPLAINT_RQ(ByteBuffer _bb, T_USER_COMPLAINT_RQ obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public T_USER_COMPLAINT_RQ __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public commonpack.S_RQ_HEAD sRqHead() { return sRqHead(new commonpack.S_RQ_HEAD()); }
  public commonpack.S_RQ_HEAD sRqHead(commonpack.S_RQ_HEAD obj) { int o = __offset(4); return o != 0 ? obj.__init(o + bb_pos, bb) : null; }
  public long userId() { int o = __offset(6); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  public byte type() { int o = __offset(8); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public String reason() { int o = __offset(10); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer reasonAsByteBuffer() { return __vector_as_bytebuffer(10, 1); }

  public static void startT_USER_COMPLAINT_RQ(FlatBufferBuilder builder) { builder.startObject(4); }
  public static void addSRqHead(FlatBufferBuilder builder, int sRqHeadOffset) { builder.addStruct(0, sRqHeadOffset, 0); }
  public static void addUserId(FlatBufferBuilder builder, long userId) { builder.addLong(1, userId, 0); }
  public static void addType(FlatBufferBuilder builder, byte type) { builder.addByte(2, type, 0); }
  public static void addReason(FlatBufferBuilder builder, int reasonOffset) { builder.addOffset(3, reasonOffset, 0); }
  public static int endT_USER_COMPLAINT_RQ(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishT_USER_COMPLAINT_RQBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
}

