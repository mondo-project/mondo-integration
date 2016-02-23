/**
 * Autogenerated by Thrift Compiler (0.9.3)
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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2016-02-12")
public class HawkFileRemovalEvent implements org.apache.thrift.TBase<HawkFileRemovalEvent, HawkFileRemovalEvent._Fields>, java.io.Serializable, Cloneable, Comparable<HawkFileRemovalEvent> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("HawkFileRemovalEvent");

  private static final org.apache.thrift.protocol.TField VCS_ITEM_FIELD_DESC = new org.apache.thrift.protocol.TField("vcsItem", org.apache.thrift.protocol.TType.STRUCT, (short)1);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new HawkFileRemovalEventStandardSchemeFactory());
    schemes.put(TupleScheme.class, new HawkFileRemovalEventTupleSchemeFactory());
  }

  public CommitItem vcsItem; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    VCS_ITEM((short)1, "vcsItem");

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
        case 1: // VCS_ITEM
          return VCS_ITEM;
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
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.VCS_ITEM, new org.apache.thrift.meta_data.FieldMetaData("vcsItem", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, CommitItem.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(HawkFileRemovalEvent.class, metaDataMap);
  }

  public HawkFileRemovalEvent() {
  }

  public HawkFileRemovalEvent(
    CommitItem vcsItem)
  {
    this();
    this.vcsItem = vcsItem;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public HawkFileRemovalEvent(HawkFileRemovalEvent other) {
    if (other.isSetVcsItem()) {
      this.vcsItem = new CommitItem(other.vcsItem);
    }
  }

  public HawkFileRemovalEvent deepCopy() {
    return new HawkFileRemovalEvent(this);
  }

  @Override
  public void clear() {
    this.vcsItem = null;
  }

  public CommitItem getVcsItem() {
    return this.vcsItem;
  }

  public HawkFileRemovalEvent setVcsItem(CommitItem vcsItem) {
    this.vcsItem = vcsItem;
    return this;
  }

  public void unsetVcsItem() {
    this.vcsItem = null;
  }

  /** Returns true if field vcsItem is set (has been assigned a value) and false otherwise */
  public boolean isSetVcsItem() {
    return this.vcsItem != null;
  }

  public void setVcsItemIsSet(boolean value) {
    if (!value) {
      this.vcsItem = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case VCS_ITEM:
      if (value == null) {
        unsetVcsItem();
      } else {
        setVcsItem((CommitItem)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case VCS_ITEM:
      return getVcsItem();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case VCS_ITEM:
      return isSetVcsItem();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof HawkFileRemovalEvent)
      return this.equals((HawkFileRemovalEvent)that);
    return false;
  }

  public boolean equals(HawkFileRemovalEvent that) {
    if (that == null)
      return false;

    boolean this_present_vcsItem = true && this.isSetVcsItem();
    boolean that_present_vcsItem = true && that.isSetVcsItem();
    if (this_present_vcsItem || that_present_vcsItem) {
      if (!(this_present_vcsItem && that_present_vcsItem))
        return false;
      if (!this.vcsItem.equals(that.vcsItem))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_vcsItem = true && (isSetVcsItem());
    list.add(present_vcsItem);
    if (present_vcsItem)
      list.add(vcsItem);

    return list.hashCode();
  }

  @Override
  public int compareTo(HawkFileRemovalEvent other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetVcsItem()).compareTo(other.isSetVcsItem());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetVcsItem()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.vcsItem, other.vcsItem);
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
    StringBuilder sb = new StringBuilder("HawkFileRemovalEvent(");
    boolean first = true;

    sb.append("vcsItem:");
    if (this.vcsItem == null) {
      sb.append("null");
    } else {
      sb.append(this.vcsItem);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (vcsItem == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'vcsItem' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
    if (vcsItem != null) {
      vcsItem.validate();
    }
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

  private static class HawkFileRemovalEventStandardSchemeFactory implements SchemeFactory {
    public HawkFileRemovalEventStandardScheme getScheme() {
      return new HawkFileRemovalEventStandardScheme();
    }
  }

  private static class HawkFileRemovalEventStandardScheme extends StandardScheme<HawkFileRemovalEvent> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, HawkFileRemovalEvent struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // VCS_ITEM
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.vcsItem = new CommitItem();
              struct.vcsItem.read(iprot);
              struct.setVcsItemIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, HawkFileRemovalEvent struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.vcsItem != null) {
        oprot.writeFieldBegin(VCS_ITEM_FIELD_DESC);
        struct.vcsItem.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class HawkFileRemovalEventTupleSchemeFactory implements SchemeFactory {
    public HawkFileRemovalEventTupleScheme getScheme() {
      return new HawkFileRemovalEventTupleScheme();
    }
  }

  private static class HawkFileRemovalEventTupleScheme extends TupleScheme<HawkFileRemovalEvent> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, HawkFileRemovalEvent struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      struct.vcsItem.write(oprot);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, HawkFileRemovalEvent struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.vcsItem = new CommitItem();
      struct.vcsItem.read(iprot);
      struct.setVcsItemIsSet(true);
    }
  }

}

