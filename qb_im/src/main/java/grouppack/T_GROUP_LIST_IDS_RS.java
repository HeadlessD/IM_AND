// automatically generated by the FlatBuffers compiler, do not modify

package grouppack;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class T_GROUP_LIST_IDS_RS extends Table {
  public static T_GROUP_LIST_IDS_RS getRootAsT_GROUP_LIST_IDS_RS(ByteBuffer _bb) { return getRootAsT_GROUP_LIST_IDS_RS(_bb, new T_GROUP_LIST_IDS_RS()); }
  public static T_GROUP_LIST_IDS_RS getRootAsT_GROUP_LIST_IDS_RS(ByteBuffer _bb, T_GROUP_LIST_IDS_RS obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public T_GROUP_LIST_IDS_RS __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public commonpack.S_RS_HEAD sRsHead() { return sRsHead(new commonpack.S_RS_HEAD()); }
  public commonpack.S_RS_HEAD sRsHead(commonpack.S_RS_HEAD obj) { int o = __offset(4); return o != 0 ? obj.__init(o + bb_pos, bb) : null; }
  public grouppack.T_GROUP_RELATION_USER_INFO groupInfoList(int j) { return groupInfoList(new grouppack.T_GROUP_RELATION_USER_INFO(), j); }
  public grouppack.T_GROUP_RELATION_USER_INFO groupInfoList(grouppack.T_GROUP_RELATION_USER_INFO obj, int j) { int o = __offset(6); return o != 0 ? obj.__init(__indirect(__vector(o) + j * 4), bb) : null; }
  public int groupInfoListLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public int groupListIndex() { int o = __offset(8); return o != 0 ? bb.getInt(o + bb_pos) : 0; }

  public static void startT_GROUP_LIST_IDS_RS(FlatBufferBuilder builder) { builder.startObject(3); }
  public static void addSRsHead(FlatBufferBuilder builder, int sRsHeadOffset) { builder.addStruct(0, sRsHeadOffset, 0); }
  public static void addGroupInfoList(FlatBufferBuilder builder, int groupInfoListOffset) { builder.addOffset(1, groupInfoListOffset, 0); }
  public static int createGroupInfoListVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startGroupInfoListVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addGroupListIndex(FlatBufferBuilder builder, int groupListIndex) { builder.addInt(2, groupListIndex, 0); }
  public static int endT_GROUP_LIST_IDS_RS(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishT_GROUP_LIST_IDS_RSBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
}

