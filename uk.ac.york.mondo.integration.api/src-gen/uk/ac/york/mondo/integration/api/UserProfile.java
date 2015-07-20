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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-7-20")
public class UserProfile implements org.apache.thrift.TBase<UserProfile, UserProfile._Fields>, java.io.Serializable, Cloneable, Comparable<UserProfile> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("UserProfile");

  private static final org.apache.thrift.protocol.TField REAL_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("realName", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField ADMIN_FIELD_DESC = new org.apache.thrift.protocol.TField("admin", org.apache.thrift.protocol.TType.BOOL, (short)2);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new UserProfileStandardSchemeFactory());
    schemes.put(TupleScheme.class, new UserProfileTupleSchemeFactory());
  }

  public String realName; // required
  public boolean admin; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    REAL_NAME((short)1, "realName"),
    ADMIN((short)2, "admin");

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
        case 1: // REAL_NAME
          return REAL_NAME;
        case 2: // ADMIN
          return ADMIN;
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
  private static final int __ADMIN_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.REAL_NAME, new org.apache.thrift.meta_data.FieldMetaData("realName", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.ADMIN, new org.apache.thrift.meta_data.FieldMetaData("admin", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(UserProfile.class, metaDataMap);
  }

  public UserProfile() {
  }

  public UserProfile(
    String realName,
    boolean admin)
  {
    this();
    this.realName = realName;
    this.admin = admin;
    setAdminIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public UserProfile(UserProfile other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetRealName()) {
      this.realName = other.realName;
    }
    this.admin = other.admin;
  }

  public UserProfile deepCopy() {
    return new UserProfile(this);
  }

  @Override
  public void clear() {
    this.realName = null;
    setAdminIsSet(false);
    this.admin = false;
  }

  public String getRealName() {
    return this.realName;
  }

  public UserProfile setRealName(String realName) {
    this.realName = realName;
    return this;
  }

  public void unsetRealName() {
    this.realName = null;
  }

  /** Returns true if field realName is set (has been assigned a value) and false otherwise */
  public boolean isSetRealName() {
    return this.realName != null;
  }

  public void setRealNameIsSet(boolean value) {
    if (!value) {
      this.realName = null;
    }
  }

  public boolean isAdmin() {
    return this.admin;
  }

  public UserProfile setAdmin(boolean admin) {
    this.admin = admin;
    setAdminIsSet(true);
    return this;
  }

  public void unsetAdmin() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __ADMIN_ISSET_ID);
  }

  /** Returns true if field admin is set (has been assigned a value) and false otherwise */
  public boolean isSetAdmin() {
    return EncodingUtils.testBit(__isset_bitfield, __ADMIN_ISSET_ID);
  }

  public void setAdminIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __ADMIN_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case REAL_NAME:
      if (value == null) {
        unsetRealName();
      } else {
        setRealName((String)value);
      }
      break;

    case ADMIN:
      if (value == null) {
        unsetAdmin();
      } else {
        setAdmin((Boolean)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case REAL_NAME:
      return getRealName();

    case ADMIN:
      return Boolean.valueOf(isAdmin());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case REAL_NAME:
      return isSetRealName();
    case ADMIN:
      return isSetAdmin();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof UserProfile)
      return this.equals((UserProfile)that);
    return false;
  }

  public boolean equals(UserProfile that) {
    if (that == null)
      return false;

    boolean this_present_realName = true && this.isSetRealName();
    boolean that_present_realName = true && that.isSetRealName();
    if (this_present_realName || that_present_realName) {
      if (!(this_present_realName && that_present_realName))
        return false;
      if (!this.realName.equals(that.realName))
        return false;
    }

    boolean this_present_admin = true;
    boolean that_present_admin = true;
    if (this_present_admin || that_present_admin) {
      if (!(this_present_admin && that_present_admin))
        return false;
      if (this.admin != that.admin)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_realName = true && (isSetRealName());
    list.add(present_realName);
    if (present_realName)
      list.add(realName);

    boolean present_admin = true;
    list.add(present_admin);
    if (present_admin)
      list.add(admin);

    return list.hashCode();
  }

  @Override
  public int compareTo(UserProfile other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetRealName()).compareTo(other.isSetRealName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRealName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.realName, other.realName);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetAdmin()).compareTo(other.isSetAdmin());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAdmin()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.admin, other.admin);
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
    StringBuilder sb = new StringBuilder("UserProfile(");
    boolean first = true;

    sb.append("realName:");
    if (this.realName == null) {
      sb.append("null");
    } else {
      sb.append(this.realName);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("admin:");
    sb.append(this.admin);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (realName == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'realName' was not present! Struct: " + toString());
    }
    // alas, we cannot check 'admin' because it's a primitive and you chose the non-beans generator.
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

  private static class UserProfileStandardSchemeFactory implements SchemeFactory {
    public UserProfileStandardScheme getScheme() {
      return new UserProfileStandardScheme();
    }
  }

  private static class UserProfileStandardScheme extends StandardScheme<UserProfile> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, UserProfile struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // REAL_NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.realName = iprot.readString();
              struct.setRealNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // ADMIN
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.admin = iprot.readBool();
              struct.setAdminIsSet(true);
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
      if (!struct.isSetAdmin()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'admin' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, UserProfile struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.realName != null) {
        oprot.writeFieldBegin(REAL_NAME_FIELD_DESC);
        oprot.writeString(struct.realName);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(ADMIN_FIELD_DESC);
      oprot.writeBool(struct.admin);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class UserProfileTupleSchemeFactory implements SchemeFactory {
    public UserProfileTupleScheme getScheme() {
      return new UserProfileTupleScheme();
    }
  }

  private static class UserProfileTupleScheme extends TupleScheme<UserProfile> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, UserProfile struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.realName);
      oprot.writeBool(struct.admin);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, UserProfile struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.realName = iprot.readString();
      struct.setRealNameIsSet(true);
      struct.admin = iprot.readBool();
      struct.setAdminIsSet(true);
    }
  }

}

