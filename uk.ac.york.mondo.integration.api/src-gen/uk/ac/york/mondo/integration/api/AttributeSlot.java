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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-7-23")
public class AttributeSlot implements org.apache.thrift.TBase<AttributeSlot, AttributeSlot._Fields>, java.io.Serializable, Cloneable, Comparable<AttributeSlot> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("AttributeSlot");

  private static final org.apache.thrift.protocol.TField NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("name", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField IS_SET_FIELD_DESC = new org.apache.thrift.protocol.TField("isSet", org.apache.thrift.protocol.TType.BOOL, (short)2);
  private static final org.apache.thrift.protocol.TField VALUES_FIELD_DESC = new org.apache.thrift.protocol.TField("values", org.apache.thrift.protocol.TType.STRUCT, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new AttributeSlotStandardSchemeFactory());
    schemes.put(TupleScheme.class, new AttributeSlotTupleSchemeFactory());
  }

  public String name; // required
  public boolean isSet; // required
  public ScalarList values; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    NAME((short)1, "name"),
    IS_SET((short)2, "isSet"),
    VALUES((short)3, "values");

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
        case 2: // IS_SET
          return IS_SET;
        case 3: // VALUES
          return VALUES;
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
  private static final int __ISSET_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.VALUES};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("name", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.IS_SET, new org.apache.thrift.meta_data.FieldMetaData("isSet", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    tmpMap.put(_Fields.VALUES, new org.apache.thrift.meta_data.FieldMetaData("values", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ScalarList.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(AttributeSlot.class, metaDataMap);
  }

  public AttributeSlot() {
  }

  public AttributeSlot(
    String name,
    boolean isSet)
  {
    this();
    this.name = name;
    this.isSet = isSet;
    setIsSetIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public AttributeSlot(AttributeSlot other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetName()) {
      this.name = other.name;
    }
    this.isSet = other.isSet;
    if (other.isSetValues()) {
      this.values = new ScalarList(other.values);
    }
  }

  public AttributeSlot deepCopy() {
    return new AttributeSlot(this);
  }

  @Override
  public void clear() {
    this.name = null;
    setIsSetIsSet(false);
    this.isSet = false;
    this.values = null;
  }

  public String getName() {
    return this.name;
  }

  public AttributeSlot setName(String name) {
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

  public boolean isIsSet() {
    return this.isSet;
  }

  public AttributeSlot setIsSet(boolean isSet) {
    this.isSet = isSet;
    setIsSetIsSet(true);
    return this;
  }

  public void unsetIsSet() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __ISSET_ISSET_ID);
  }

  /** Returns true if field isSet is set (has been assigned a value) and false otherwise */
  public boolean isSetIsSet() {
    return EncodingUtils.testBit(__isset_bitfield, __ISSET_ISSET_ID);
  }

  public void setIsSetIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __ISSET_ISSET_ID, value);
  }

  public ScalarList getValues() {
    return this.values;
  }

  public AttributeSlot setValues(ScalarList values) {
    this.values = values;
    return this;
  }

  public void unsetValues() {
    this.values = null;
  }

  /** Returns true if field values is set (has been assigned a value) and false otherwise */
  public boolean isSetValues() {
    return this.values != null;
  }

  public void setValuesIsSet(boolean value) {
    if (!value) {
      this.values = null;
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

    case IS_SET:
      if (value == null) {
        unsetIsSet();
      } else {
        setIsSet((Boolean)value);
      }
      break;

    case VALUES:
      if (value == null) {
        unsetValues();
      } else {
        setValues((ScalarList)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case NAME:
      return getName();

    case IS_SET:
      return Boolean.valueOf(isIsSet());

    case VALUES:
      return getValues();

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
    case IS_SET:
      return isSetIsSet();
    case VALUES:
      return isSetValues();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof AttributeSlot)
      return this.equals((AttributeSlot)that);
    return false;
  }

  public boolean equals(AttributeSlot that) {
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

    boolean this_present_isSet = true;
    boolean that_present_isSet = true;
    if (this_present_isSet || that_present_isSet) {
      if (!(this_present_isSet && that_present_isSet))
        return false;
      if (this.isSet != that.isSet)
        return false;
    }

    boolean this_present_values = true && this.isSetValues();
    boolean that_present_values = true && that.isSetValues();
    if (this_present_values || that_present_values) {
      if (!(this_present_values && that_present_values))
        return false;
      if (!this.values.equals(that.values))
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

    boolean present_isSet = true;
    list.add(present_isSet);
    if (present_isSet)
      list.add(isSet);

    boolean present_values = true && (isSetValues());
    list.add(present_values);
    if (present_values)
      list.add(values);

    return list.hashCode();
  }

  @Override
  public int compareTo(AttributeSlot other) {
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
    lastComparison = Boolean.valueOf(isSetIsSet()).compareTo(other.isSetIsSet());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetIsSet()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.isSet, other.isSet);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetValues()).compareTo(other.isSetValues());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetValues()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.values, other.values);
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
    StringBuilder sb = new StringBuilder("AttributeSlot(");
    boolean first = true;

    sb.append("name:");
    if (this.name == null) {
      sb.append("null");
    } else {
      sb.append(this.name);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("isSet:");
    sb.append(this.isSet);
    first = false;
    if (isSetValues()) {
      if (!first) sb.append(", ");
      sb.append("values:");
      if (this.values == null) {
        sb.append("null");
      } else {
        sb.append(this.values);
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
    // alas, we cannot check 'isSet' because it's a primitive and you chose the non-beans generator.
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
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class AttributeSlotStandardSchemeFactory implements SchemeFactory {
    public AttributeSlotStandardScheme getScheme() {
      return new AttributeSlotStandardScheme();
    }
  }

  private static class AttributeSlotStandardScheme extends StandardScheme<AttributeSlot> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, AttributeSlot struct) throws org.apache.thrift.TException {
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
          case 2: // IS_SET
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.isSet = iprot.readBool();
              struct.setIsSetIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // VALUES
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.values = new ScalarList();
              struct.values.read(iprot);
              struct.setValuesIsSet(true);
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
      if (!struct.isSetIsSet()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'isSet' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, AttributeSlot struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.name != null) {
        oprot.writeFieldBegin(NAME_FIELD_DESC);
        oprot.writeString(struct.name);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(IS_SET_FIELD_DESC);
      oprot.writeBool(struct.isSet);
      oprot.writeFieldEnd();
      if (struct.values != null) {
        if (struct.isSetValues()) {
          oprot.writeFieldBegin(VALUES_FIELD_DESC);
          struct.values.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class AttributeSlotTupleSchemeFactory implements SchemeFactory {
    public AttributeSlotTupleScheme getScheme() {
      return new AttributeSlotTupleScheme();
    }
  }

  private static class AttributeSlotTupleScheme extends TupleScheme<AttributeSlot> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, AttributeSlot struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.name);
      oprot.writeBool(struct.isSet);
      BitSet optionals = new BitSet();
      if (struct.isSetValues()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetValues()) {
        struct.values.write(oprot);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, AttributeSlot struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.name = iprot.readString();
      struct.setNameIsSet(true);
      struct.isSet = iprot.readBool();
      struct.setIsSetIsSet(true);
      BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        struct.values = new ScalarList();
        struct.values.read(iprot);
        struct.setValuesIsSet(true);
      }
    }
  }

}

