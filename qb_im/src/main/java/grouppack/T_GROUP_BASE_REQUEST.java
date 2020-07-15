// automatically generated by the FlatBuffers compiler, do not modify

package grouppack;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class T_GROUP_BASE_REQUEST extends Table {
  public static T_GROUP_BASE_REQUEST getRootAsT_GROUP_BASE_REQUEST(ByteBuffer _bb) { return getRootAsT_GROUP_BASE_REQUEST(_bb, new T_GROUP_BASE_REQUEST()); }
  public static T_GROUP_BASE_REQUEST getRootAsT_GROUP_BASE_REQUEST(ByteBuffer _bb, T_GROUP_BASE_REQUEST obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public T_GROUP_BASE_REQUEST __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public long groupId() { int o = __offset(4); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  public long nextMessageId() { int o = __offset(6); return o != 0 ? bb.getLong(o + bb_pos) : 0; }

  public static int createT_GROUP_BASE_REQUEST(FlatBufferBuilder builder,
      long group_id,
      long next_message_id) {
    builder.startObject(2);
    T_GROUP_BASE_REQUEST.addNextMessageId(builder, next_message_id);
    T_GROUP_BASE_REQUEST.addGroupId(builder, group_id);
    return T_GROUP_BASE_REQUEST.endT_GROUP_BASE_REQUEST(builder);
  }

  public static void startT_GROUP_BASE_REQUEST(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addGroupId(FlatBufferBuilder builder, long groupId) { builder.addLong(0, groupId, 0); }
  public static void addNextMessageId(FlatBufferBuilder builder, long nextMessageId) { builder.addLong(1, nextMessageId, 0); }
  public static int endT_GROUP_BASE_REQUEST(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishT_GROUP_BASE_REQUESTBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
}

