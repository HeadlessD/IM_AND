// automatically generated by the FlatBuffers compiler, do not modify

package friendpack;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class T_FRIEND_LIST_RS extends Table {
  public static T_FRIEND_LIST_RS getRootAsT_FRIEND_LIST_RS(ByteBuffer _bb) { return getRootAsT_FRIEND_LIST_RS(_bb, new T_FRIEND_LIST_RS()); }
  public static T_FRIEND_LIST_RS getRootAsT_FRIEND_LIST_RS(ByteBuffer _bb, T_FRIEND_LIST_RS obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public T_FRIEND_LIST_RS __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public commonpack.S_RS_HEAD sRsHead() { return sRsHead(new commonpack.S_RS_HEAD()); }
  public commonpack.S_RS_HEAD sRsHead(commonpack.S_RS_HEAD obj) { int o = __offset(4); return o != 0 ? obj.__init(o + bb_pos, bb) : null; }
  public friendpack.T_FREIND_INFO friendList(int j) { return friendList(new friendpack.T_FREIND_INFO(), j); }
  public friendpack.T_FREIND_INFO friendList(friendpack.T_FREIND_INFO obj, int j) { int o = __offset(6); return o != 0 ? obj.__init(__indirect(__vector(o) + j * 4), bb) : null; }
  public int friendListLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public long token() { int o = __offset(8); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  public int msgSource() { int o = __offset(10); return o != 0 ? bb.getInt(o + bb_pos) : 0; }

  public static void startT_FRIEND_LIST_RS(FlatBufferBuilder builder) { builder.startObject(4); }
  public static void addSRsHead(FlatBufferBuilder builder, int sRsHeadOffset) { builder.addStruct(0, sRsHeadOffset, 0); }
  public static void addFriendList(FlatBufferBuilder builder, int friendListOffset) { builder.addOffset(1, friendListOffset, 0); }
  public static int createFriendListVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startFriendListVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addToken(FlatBufferBuilder builder, long token) { builder.addLong(2, token, 0); }
  public static void addMsgSource(FlatBufferBuilder builder, int msgSource) { builder.addInt(3, msgSource, 0); }
  public static int endT_FRIEND_LIST_RS(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishT_FRIEND_LIST_RSBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
}
