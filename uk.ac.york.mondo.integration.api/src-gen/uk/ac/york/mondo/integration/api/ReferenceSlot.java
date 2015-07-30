/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package uk.ac.york.mondo.integration.api;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-7-30")
public class ReferenceSlot implements org.apache.thrift.TBase<ReferenceSlot, ReferenceSlot._Fields>, java.io.Serializable, Cloneable, Comparable<ReferenceSlot> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ReferenceSlot");

  private static final org.apache.thrift.protocol.TField NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("name", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField POSITIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("positions", org.apache.thrift.protocol.TType.LIST, (short)2);
  private static final org.apache.thrift.protocol.TField IDS_FIELD_DESC = new org.apache.thrift.protocol.TField("ids", org.apache.thrift.protocol.TType.LIST, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ReferenceSlotStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ReferenceSlotTupleSchemeFactory());
  }

  public String name; // required
  public List<Integer> positions; // optional
  public List<String> ids; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    NAME((short)1, "name"),
    POSITIONS((short)2, "positions"),
    IDS((short)3, "ids");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // NAME
          return NAME;
        case 2: // POSITIONS
          return POSITIONS;
        case 3: // IDS
          return IDS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final _Fields optionals[] = {_Fields.POSITIONS,_Fields.IDS};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("name", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.POSITIONS, new org.apache.thrift.meta_data.FieldMetaData("positions", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32))));
    tmpMap.put(_Fields.IDS, new org.apache.thrift.meta_data.FieldMetaData("ids", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ReferenceSlot.class, metaDataMap);
  }

  public ReferenceSlot() {
  }

  public ReferenceSlot(
    String name)
  {
    this();
    this.name = name;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ReferenceSlot(ReferenceSlot other) {
    if (other.isSetName()) {
      this.name = other.name;
    }
    if (other.isSetPositions()) {
      List<Integer> __this__positions = new ArrayList<Integer>(other.positions);
      this.positions = __this__positions;
    }
    if (other.isSetIds()) {
      List<String> __this__ids = new ArrayList<String>(other.ids);
      this.ids = __this__ids;
    }
  }

  public ReferenceSlot deepCopy() {
    return new ReferenceSlot(this);
  }

  @Override
  public void clear() {
    this.name = null;
    this.positions = null;
    this.ids = null;
  }

  public String getName() {
    return this.name;
  }

  public ReferenceSlot setName(String name) {
    this.name = name;
    return this;
  }

  public void unsetName() {
    this.name = null;
  }

  /** Returns true if field name is set (has been assigned a value) and false otherwise */
  public boolean isSetName() {
    return this.name != null;
  }

  public void setNameIsSet(boolean value) {
    if (!value) {
      this.name = null;
    }
  }

  public int getPositionsSize() {
    return (this.positions == null) ? 0 : this.positions.size();
  }

  public java.util.Iterator<Integer> getPositionsIterator() {
    return (this.positions == null) ? null : this.positions.iterator();
  }

  public void addToPositions(int elem) {
    if (this.positions == null) {
      this.positions = new ArrayList<Integer>();
    }
    this.positions.add(elem);
  }

  public List<Integer> getPositions() {
    return this.positions;
  }

  public ReferenceSlot setPositions(List<Integer> positions) {
    this.positions = positions;
    return this;
  }

  public void unsetPositions() {
    this.positions = null;
  }

  /** Returns true if field positions is set (has been assigned a value) and false otherwise */
  public boolean isSetPositions() {
    return this.positions != null;
  }

  public void setPositionsIsSet(boolean value) {
    if (!value) {
      this.positions = null;
    }
  }

  public int getIdsSize() {
    return (this.ids == null) ? 0 : this.ids.size();
  }

  public java.util.Iterator<String> getIdsIterator() {
    return (this.ids == null) ? null : this.ids.iterator();
  }

  public void addToIds(String elem) {
    if (this.ids == null) {
      this.ids = new ArrayList<String>();
    }
    this.ids.add(elem);
  }

  public List<String> getIds() {
    return this.ids;
  }

  public ReferenceSlot setIds(List<String> ids) {
    this.ids = ids;
    return this;
  }

  public void unsetIds() {
    this.ids = null;
  }

  /** Returns true if field ids is set (has been assigned a value) and false otherwise */
  public boolean isSetIds() {
    return this.ids != null;
  }

  public void setIdsIsSet(boolean value) {
    if (!value) {
      this.ids = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case NAME:
      if (value == null) {
        unsetName();
      } else {
        setName((String)value);
      }
      break;

    case POSITIONS:
      if (value == null) {
        unsetPositions();
      } else {
        setPositions((List<Integer>)value);
      }
      break;

    case IDS:
      if (value == null) {
        unsetIds();
      } else {
        setIds((List<String>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case NAME:
      return getName();

    case POSITIONS:
      return getPositions();

    case IDS:
      return getIds();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case NAME:
      return isSetName();
    case POSITIONS:
      return isSetPositions();
    case IDS:
      return isSetIds();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof ReferenceSlot)
      return this.equals((ReferenceSlot)that);
    return false;
  }

  public boolean equals(ReferenceSlot that) {
    if (that == null)
      return false;

    boolean this_present_name = true && this.isSetName();
    boolean that_present_name = true && that.isSetName();
    if (this_present_name || that_present_name) {
      if (!(this_present_name && that_present_name))
        return false;
      if (!this.name.equals(that.name))
        return false;
    }

    boolean this_present_positions = true && this.isSetPositions();
    boolean that_present_positions = true && that.isSetPositions();
    if (this_present_positions || that_present_positions) {
      if (!(this_present_positions && that_present_positions))
        return false;
      if (!this.positions.equals(that.positions))
        return false;
    }

    boolean this_present_ids = true && this.isSetIds();
    boolean that_present_ids = true && that.isSetIds();
    if (this_present_ids || that_present_ids) {
      if (!(this_present_ids && that_present_ids))
        return false;
      if (!this.ids.equals(that.ids))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_name = true && (isSetName());
    list.add(present_name);
    if (present_name)
      list.add(name);

    boolean present_positions = true && (isSetPositions());
    list.add(present_positions);
    if (present_positions)
      list.add(positions);

    boolean present_ids = true && (isSetIds());
    list.add(present_ids);
    if (present_ids)
      list.add(ids);

    return list.hashCode();
  }

  @Override
  public int compareTo(ReferenceSlot other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetName()).compareTo(other.isSetName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.name, other.name);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetPositions()).compareTo(other.isSetPositions());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPositions()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.positions, other.positions);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetIds()).compareTo(other.isSetIds());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetIds()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ids, other.ids);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ReferenceSlot(");
    boolean first = true;

    sb.append("name:");
    if (this.name == null) {
      sb.append("null");
    } else {
      sb.append(this.name);
    }
    first = false;
    if (isSetPositions()) {
      if (!first) sb.append(", ");
      sb.append("positions:");
      if (this.positions == null) {
        sb.append("null");
      } else {
        sb.append(this.positions);
      }
      first = false;
    }
    if (isSetIds()) {
    if (!first) sb.append(", ");
    sb.append("ids:");
    if (this.ids == null) {
      sb.append("null");
    } else {
      sb.append(this.ids);
    }
    first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (name == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'name' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class ReferenceSlotStandardSchemeFactory implements SchemeFactory {
    public ReferenceSlotStandardScheme getScheme() {
      return new ReferenceSlotStandardScheme();
    }
  }

  private static class ReferenceSlotStandardScheme extends StandardScheme<ReferenceSlot> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ReferenceSlot struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.name = iprot.readString();
              struct.setNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // POSITIONS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list8 = iprot.readListBegin();
                struct.positions = new ArrayList<Integer>(_list8.size);
                int _elem9;
                for (int _i10 = 0; _i10 < _list8.size; ++_i10)
                {
                  _elem9 = iprot.readI32();
                  struct.positions.add(_elem9);
                }
                iprot.readListEnd();
              }
              struct.setPositionsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // IDS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list11 = iprot.readListBegin();
                struct.ids = new ArrayList<String>(_list11.size);
                String _elem12;
                for (int _i13 = 0; _i13 < _list11.size; ++_i13)
                {
                  _elem12 = iprot.readString();
                  struct.ids.add(_elem12);
                }
                iprot.readListEnd();
              }
              struct.setIdsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, ReferenceSlot struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.name != null) {
        oprot.writeFieldBegin(NAME_FIELD_DESC);
        oprot.writeString(struct.name);
        oprot.writeFieldEnd();
      }
      if (struct.positions != null) {
        if (struct.isSetPositions()) {
          oprot.writeFieldBegin(POSITIONS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, struct.positions.size()));
            for (int _iter14 : struct.positions)
            {
              oprot.writeI32(_iter14);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.ids != null) {
        if (struct.isSetIds()) {
        oprot.writeFieldBegin(IDS_FIELD_DESC);
        {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.ids.size()));
            for (String _iter15 : struct.ids)
          {
              oprot.writeString(_iter15);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ReferenceSlotTupleSchemeFactory implements SchemeFactory {
    public ReferenceSlotTupleScheme getScheme() {
      return new ReferenceSlotTupleScheme();
    }
  }

  private static class ReferenceSlotTupleScheme extends TupleScheme<ReferenceSlot> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ReferenceSlot struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.name);
      BitSet optionals = new BitSet();
      if (struct.isSetPositions()) {
        optionals.set(0);
      }
      if (struct.isSetIds()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetPositions()) {
        {
          oprot.writeI32(struct.positions.size());
          for (int _iter16 : struct.positions)
          {
            oprot.writeI32(_iter16);
          }
        }
      }
      if (struct.isSetIds()) {
      {
        oprot.writeI32(struct.ids.size());
          for (String _iter17 : struct.ids)
        {
            oprot.writeString(_iter17);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ReferenceSlot struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.name = iprot.readString();
      struct.setNameIsSet(true);
      BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list18 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, iprot.readI32());
          struct.positions = new ArrayList<Integer>(_list18.size);
          int _elem19;
          for (int _i20 = 0; _i20 < _list18.size; ++_i20)
      {
            _elem19 = iprot.readI32();
            struct.positions.add(_elem19);
          }
        }
        struct.setPositionsIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list21 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.ids = new ArrayList<String>(_list21.size);
          String _elem22;
          for (int _i23 = 0; _i23 < _list21.size; ++_i23)
          {
            _elem22 = iprot.readString();
            struct.ids.add(_elem22);
        }
      }
      struct.setIdsIsSet(true);
    }
  }
  }

}

