// automatically generated by the FlatBuffers compiler, do not modify

package scpack;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class T_CHAT_GET_OFFLINE_MESSAGE_RQ extends Table {
  public static T_CHAT_GET_OFFLINE_MESSAGE_RQ getRootAsT_CHAT_GET_OFFLINE_MESSAGE_RQ(ByteBuffer _bb) { return getRootAsT_CHAT_GET_OFFLINE_MESSAGE_RQ(_bb, new T_CHAT_GET_OFFLINE_MESSAGE_RQ()); }
  public static T_CHAT_GET_OFFLINE_MESSAGE_RQ getRootAsT_CHAT_GET_OFFLINE_MESSAGE_RQ(ByteBuffer _bb, T_CHAT_GET_OFFLINE_MESSAGE_RQ obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public T_CHAT_GET_OFFLINE_MESSAGE_RQ __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public commonpack.S_RQ_HEAD sRqHead() { return sRqHead(new commonpack.S_RQ_HEAD()); }
  public commonpack.S_RQ_HEAD sRqHead(commonpack.S_RQ_HEAD obj) { int o = __offset(4); return o != 0 ? obj.__init(o + bb_pos, bb) : null; }
  public long nextMessageId() { int o = __offset(6); return o != 0 ? bb.getLong(o + bb_pos) : 0; }

  public static void startT_CHAT_GET_OFFLINE_MESSAGE_RQ(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addSRqHead(FlatBufferBuilder builder, int sRqHeadOffset) { builder.addStruct(0, sRqHeadOffset, 0); }
  public static void addNextMessageId(FlatBufferBuilder builder, long nextMessageId) { builder.addLong(1, nextMessageId, 0); }
  public static int endT_CHAT_GET_OFFLINE_MESSAGE_RQ(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishT_CHAT_GET_OFFLINE_MESSAGE_RQBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
}

