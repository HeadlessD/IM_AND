// automatically generated by the FlatBuffers compiler, do not modify

package friendpack;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class T_FREIND_INFO extends Table {
  public static T_FREIND_INFO getRootAsT_FREIND_INFO(ByteBuffer _bb) { return getRootAsT_FREIND_INFO(_bb, new T_FREIND_INFO()); }
  public static T_FREIND_INFO getRootAsT_FREIND_INFO(ByteBuffer _bb, T_FREIND_INFO obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public T_FREIND_INFO __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public long userId() { int o = __offset(4); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  public byte sourceType() { int o = __offset(6); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public String remarkName() { int o = __offset(8); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer remarkNameAsByteBuffer() { return __vector_as_bytebuffer(8, 1); }
  public String opMsg() { int o = __offset(10); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer opMsgAsByteBuffer() { return __vector_as_bytebuffer(10, 1); }
  public int optType() { int o = __offset(12); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public int blackType() { int o = __offset(14); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public long opTime() { int o = __offset(16); return o != 0 ? bb.getLong(o + bb_pos) : 0; }

  public static int createT_FREIND_INFO(FlatBufferBuilder builder,
      long user_id,
      byte source_type,
      int remark_nameOffset,
      int op_msgOffset,
      int opt_type,
      int black_type,
      long op_time) {
    builder.startObject(7);
    T_FREIND_INFO.addOpTime(builder, op_time);
    T_FREIND_INFO.addUserId(builder, user_id);
    T_FREIND_INFO.addBlackType(builder, black_type);
    T_FREIND_INFO.addOptType(builder, opt_type);
    T_FREIND_INFO.addOpMsg(builder, op_msgOffset);
    T_FREIND_INFO.addRemarkName(builder, remark_nameOffset);
    T_FREIND_INFO.addSourceType(builder, source_type);
    return T_FREIND_INFO.endT_FREIND_INFO(builder);
  }

  public static void startT_FREIND_INFO(FlatBufferBuilder builder) { builder.startObject(7); }
  public static void addUserId(FlatBufferBuilder builder, long userId) { builder.addLong(0, userId, 0); }
  public static void addSourceType(FlatBufferBuilder builder, byte sourceType) { builder.addByte(1, sourceType, 0); }
  public static void addRemarkName(FlatBufferBuilder builder, int remarkNameOffset) { builder.addOffset(2, remarkNameOffset, 0); }
  public static void addOpMsg(FlatBufferBuilder builder, int opMsgOffset) { builder.addOffset(3, opMsgOffset, 0); }
  public static void addOptType(FlatBufferBuilder builder, int optType) { builder.addInt(4, optType, 0); }
  public static void addBlackType(FlatBufferBuilder builder, int blackType) { builder.addInt(5, blackType, 0); }
  public static void addOpTime(FlatBufferBuilder builder, long opTime) { builder.addLong(6, opTime, 0); }
  public static int endT_FREIND_INFO(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishT_FREIND_INFOBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
}

