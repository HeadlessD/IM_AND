// automatically generated by the FlatBuffers compiler, do not modify

package userpack;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class T_UPDATE_USER_INFO_RQ extends Table {
  public static T_UPDATE_USER_INFO_RQ getRootAsT_UPDATE_USER_INFO_RQ(ByteBuffer _bb) { return getRootAsT_UPDATE_USER_INFO_RQ(_bb, new T_UPDATE_USER_INFO_RQ()); }
  public static T_UPDATE_USER_INFO_RQ getRootAsT_UPDATE_USER_INFO_RQ(ByteBuffer _bb, T_UPDATE_USER_INFO_RQ obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public T_UPDATE_USER_INFO_RQ __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public commonpack.S_RQ_HEAD sRqHead() { return sRqHead(new commonpack.S_RQ_HEAD()); }
  public commonpack.S_RQ_HEAD sRqHead(commonpack.S_RQ_HEAD obj) { int o = __offset(4); return o != 0 ? obj.__init(o + bb_pos, bb) : null; }
  public T_KEYINFO keyLstInfo(int j) { return keyLstInfo(new T_KEYINFO(), j); }
  public T_KEYINFO keyLstInfo(T_KEYINFO obj, int j) { int o = __offset(6); return o != 0 ? obj.__init(__indirect(__vector(o) + j * 4), bb) : null; }
  public int keyLstInfoLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }

  public static void startT_UPDATE_USER_INFO_RQ(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addSRqHead(FlatBufferBuilder builder, int sRqHeadOffset) { builder.addStruct(0, sRqHeadOffset, 0); }
  public static void addKeyLstInfo(FlatBufferBuilder builder, int keyLstInfoOffset) { builder.addOffset(1, keyLstInfoOffset, 0); }
  public static int createKeyLstInfoVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startKeyLstInfoVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endT_UPDATE_USER_INFO_RQ(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishT_UPDATE_USER_INFO_RQBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
}

