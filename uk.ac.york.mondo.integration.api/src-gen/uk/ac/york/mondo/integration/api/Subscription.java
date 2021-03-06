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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2016-04-28")
public class Subscription implements org.apache.thrift.TBase<Subscription, Subscription._Fields>, java.io.Serializable, Cloneable, Comparable<Subscription> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Subscription");

  private static final org.apache.thrift.protocol.TField HOST_FIELD_DESC = new org.apache.thrift.protocol.TField("host", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField PORT_FIELD_DESC = new org.apache.thrift.protocol.TField("port", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField QUEUE_ADDRESS_FIELD_DESC = new org.apache.thrift.protocol.TField("queueAddress", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField QUEUE_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("queueName", org.apache.thrift.protocol.TType.STRING, (short)4);
  private static final org.apache.thrift.protocol.TField SSL_REQUIRED_FIELD_DESC = new org.apache.thrift.protocol.TField("sslRequired", org.apache.thrift.protocol.TType.BOOL, (short)5);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new SubscriptionStandardSchemeFactory());
    schemes.put(TupleScheme.class, new SubscriptionTupleSchemeFactory());
  }

  public String host; // required
  public int port; // required
  public String queueAddress; // required
  public String queueName; // required
  public boolean sslRequired; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    HOST((short)1, "host"),
    PORT((short)2, "port"),
    QUEUE_ADDRESS((short)3, "queueAddress"),
    QUEUE_NAME((short)4, "queueName"),
    SSL_REQUIRED((short)5, "sslRequired");

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
        case 1: // HOST
          return HOST;
        case 2: // PORT
          return PORT;
        case 3: // QUEUE_ADDRESS
          return QUEUE_ADDRESS;
        case 4: // QUEUE_NAME
          return QUEUE_NAME;
        case 5: // SSL_REQUIRED
          return SSL_REQUIRED;
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
  private static final int __PORT_ISSET_ID = 0;
  private static final int __SSLREQUIRED_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.HOST, new org.apache.thrift.meta_data.FieldMetaData("host", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.PORT, new org.apache.thrift.meta_data.FieldMetaData("port", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.QUEUE_ADDRESS, new org.apache.thrift.meta_data.FieldMetaData("queueAddress", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.QUEUE_NAME, new org.apache.thrift.meta_data.FieldMetaData("queueName", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.SSL_REQUIRED, new org.apache.thrift.meta_data.FieldMetaData("sslRequired", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Subscription.class, metaDataMap);
  }

  public Subscription() {
    this.sslRequired = false;

  }

  public Subscription(
    String host,
    int port,
    String queueAddress,
    String queueName,
    boolean sslRequired)
  {
    this();
    this.host = host;
    this.port = port;
    setPortIsSet(true);
    this.queueAddress = queueAddress;
    this.queueName = queueName;
    this.sslRequired = sslRequired;
    setSslRequiredIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public Subscription(Subscription other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetHost()) {
      this.host = other.host;
    }
    this.port = other.port;
    if (other.isSetQueueAddress()) {
      this.queueAddress = other.queueAddress;
    }
    if (other.isSetQueueName()) {
      this.queueName = other.queueName;
    }
    this.sslRequired = other.sslRequired;
  }

  public Subscription deepCopy() {
    return new Subscription(this);
  }

  @Override
  public void clear() {
    this.host = null;
    setPortIsSet(false);
    this.port = 0;
    this.queueAddress = null;
    this.queueName = null;
    this.sslRequired = false;

  }

  public String getHost() {
    return this.host;
  }

  public Subscription setHost(String host) {
    this.host = host;
    return this;
  }

  public void unsetHost() {
    this.host = null;
  }

  /** Returns true if field host is set (has been assigned a value) and false otherwise */
  public boolean isSetHost() {
    return this.host != null;
  }

  public void setHostIsSet(boolean value) {
    if (!value) {
      this.host = null;
    }
  }

  public int getPort() {
    return this.port;
  }

  public Subscription setPort(int port) {
    this.port = port;
    setPortIsSet(true);
    return this;
  }

  public void unsetPort() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __PORT_ISSET_ID);
  }

  /** Returns true if field port is set (has been assigned a value) and false otherwise */
  public boolean isSetPort() {
    return EncodingUtils.testBit(__isset_bitfield, __PORT_ISSET_ID);
  }

  public void setPortIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __PORT_ISSET_ID, value);
  }

  public String getQueueAddress() {
    return this.queueAddress;
  }

  public Subscription setQueueAddress(String queueAddress) {
    this.queueAddress = queueAddress;
    return this;
  }

  public void unsetQueueAddress() {
    this.queueAddress = null;
  }

  /** Returns true if field queueAddress is set (has been assigned a value) and false otherwise */
  public boolean isSetQueueAddress() {
    return this.queueAddress != null;
  }

  public void setQueueAddressIsSet(boolean value) {
    if (!value) {
      this.queueAddress = null;
    }
  }

  public String getQueueName() {
    return this.queueName;
  }

  public Subscription setQueueName(String queueName) {
    this.queueName = queueName;
    return this;
  }

  public void unsetQueueName() {
    this.queueName = null;
  }

  /** Returns true if field queueName is set (has been assigned a value) and false otherwise */
  public boolean isSetQueueName() {
    return this.queueName != null;
  }

  public void setQueueNameIsSet(boolean value) {
    if (!value) {
      this.queueName = null;
    }
  }

  public boolean isSslRequired() {
    return this.sslRequired;
  }

  public Subscription setSslRequired(boolean sslRequired) {
    this.sslRequired = sslRequired;
    setSslRequiredIsSet(true);
    return this;
  }

  public void unsetSslRequired() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SSLREQUIRED_ISSET_ID);
  }

  /** Returns true if field sslRequired is set (has been assigned a value) and false otherwise */
  public boolean isSetSslRequired() {
    return EncodingUtils.testBit(__isset_bitfield, __SSLREQUIRED_ISSET_ID);
  }

  public void setSslRequiredIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SSLREQUIRED_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case HOST:
      if (value == null) {
        unsetHost();
      } else {
        setHost((String)value);
      }
      break;

    case PORT:
      if (value == null) {
        unsetPort();
      } else {
        setPort((Integer)value);
      }
      break;

    case QUEUE_ADDRESS:
      if (value == null) {
        unsetQueueAddress();
      } else {
        setQueueAddress((String)value);
      }
      break;

    case QUEUE_NAME:
      if (value == null) {
        unsetQueueName();
      } else {
        setQueueName((String)value);
      }
      break;

    case SSL_REQUIRED:
      if (value == null) {
        unsetSslRequired();
      } else {
        setSslRequired((Boolean)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case HOST:
      return getHost();

    case PORT:
      return getPort();

    case QUEUE_ADDRESS:
      return getQueueAddress();

    case QUEUE_NAME:
      return getQueueName();

    case SSL_REQUIRED:
      return isSslRequired();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case HOST:
      return isSetHost();
    case PORT:
      return isSetPort();
    case QUEUE_ADDRESS:
      return isSetQueueAddress();
    case QUEUE_NAME:
      return isSetQueueName();
    case SSL_REQUIRED:
      return isSetSslRequired();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof Subscription)
      return this.equals((Subscription)that);
    return false;
  }

  public boolean equals(Subscription that) {
    if (that == null)
      return false;

    boolean this_present_host = true && this.isSetHost();
    boolean that_present_host = true && that.isSetHost();
    if (this_present_host || that_present_host) {
      if (!(this_present_host && that_present_host))
        return false;
      if (!this.host.equals(that.host))
        return false;
    }

    boolean this_present_port = true;
    boolean that_present_port = true;
    if (this_present_port || that_present_port) {
      if (!(this_present_port && that_present_port))
        return false;
      if (this.port != that.port)
        return false;
    }

    boolean this_present_queueAddress = true && this.isSetQueueAddress();
    boolean that_present_queueAddress = true && that.isSetQueueAddress();
    if (this_present_queueAddress || that_present_queueAddress) {
      if (!(this_present_queueAddress && that_present_queueAddress))
        return false;
      if (!this.queueAddress.equals(that.queueAddress))
        return false;
    }

    boolean this_present_queueName = true && this.isSetQueueName();
    boolean that_present_queueName = true && that.isSetQueueName();
    if (this_present_queueName || that_present_queueName) {
      if (!(this_present_queueName && that_present_queueName))
        return false;
      if (!this.queueName.equals(that.queueName))
        return false;
    }

    boolean this_present_sslRequired = true;
    boolean that_present_sslRequired = true;
    if (this_present_sslRequired || that_present_sslRequired) {
      if (!(this_present_sslRequired && that_present_sslRequired))
        return false;
      if (this.sslRequired != that.sslRequired)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_host = true && (isSetHost());
    list.add(present_host);
    if (present_host)
      list.add(host);

    boolean present_port = true;
    list.add(present_port);
    if (present_port)
      list.add(port);

    boolean present_queueAddress = true && (isSetQueueAddress());
    list.add(present_queueAddress);
    if (present_queueAddress)
      list.add(queueAddress);

    boolean present_queueName = true && (isSetQueueName());
    list.add(present_queueName);
    if (present_queueName)
      list.add(queueName);

    boolean present_sslRequired = true;
    list.add(present_sslRequired);
    if (present_sslRequired)
      list.add(sslRequired);

    return list.hashCode();
  }

  @Override
  public int compareTo(Subscription other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetHost()).compareTo(other.isSetHost());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetHost()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.host, other.host);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetPort()).compareTo(other.isSetPort());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPort()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.port, other.port);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetQueueAddress()).compareTo(other.isSetQueueAddress());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetQueueAddress()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.queueAddress, other.queueAddress);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetQueueName()).compareTo(other.isSetQueueName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetQueueName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.queueName, other.queueName);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSslRequired()).compareTo(other.isSetSslRequired());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSslRequired()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sslRequired, other.sslRequired);
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
    StringBuilder sb = new StringBuilder("Subscription(");
    boolean first = true;

    sb.append("host:");
    if (this.host == null) {
      sb.append("null");
    } else {
      sb.append(this.host);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("port:");
    sb.append(this.port);
    first = false;
    if (!first) sb.append(", ");
    sb.append("queueAddress:");
    if (this.queueAddress == null) {
      sb.append("null");
    } else {
      sb.append(this.queueAddress);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("queueName:");
    if (this.queueName == null) {
      sb.append("null");
    } else {
      sb.append(this.queueName);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("sslRequired:");
    sb.append(this.sslRequired);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (host == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'host' was not present! Struct: " + toString());
    }
    // alas, we cannot check 'port' because it's a primitive and you chose the non-beans generator.
    if (queueAddress == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'queueAddress' was not present! Struct: " + toString());
    }
    if (queueName == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'queueName' was not present! Struct: " + toString());
    }
    // alas, we cannot check 'sslRequired' because it's a primitive and you chose the non-beans generator.
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

  private static class SubscriptionStandardSchemeFactory implements SchemeFactory {
    public SubscriptionStandardScheme getScheme() {
      return new SubscriptionStandardScheme();
    }
  }

  private static class SubscriptionStandardScheme extends StandardScheme<Subscription> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, Subscription struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // HOST
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.host = iprot.readString();
              struct.setHostIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // PORT
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.port = iprot.readI32();
              struct.setPortIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // QUEUE_ADDRESS
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.queueAddress = iprot.readString();
              struct.setQueueAddressIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // QUEUE_NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.queueName = iprot.readString();
              struct.setQueueNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // SSL_REQUIRED
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.sslRequired = iprot.readBool();
              struct.setSslRequiredIsSet(true);
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
      if (!struct.isSetPort()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'port' was not found in serialized data! Struct: " + toString());
      }
      if (!struct.isSetSslRequired()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'sslRequired' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, Subscription struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.host != null) {
        oprot.writeFieldBegin(HOST_FIELD_DESC);
        oprot.writeString(struct.host);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(PORT_FIELD_DESC);
      oprot.writeI32(struct.port);
      oprot.writeFieldEnd();
      if (struct.queueAddress != null) {
        oprot.writeFieldBegin(QUEUE_ADDRESS_FIELD_DESC);
        oprot.writeString(struct.queueAddress);
        oprot.writeFieldEnd();
      }
      if (struct.queueName != null) {
        oprot.writeFieldBegin(QUEUE_NAME_FIELD_DESC);
        oprot.writeString(struct.queueName);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(SSL_REQUIRED_FIELD_DESC);
      oprot.writeBool(struct.sslRequired);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class SubscriptionTupleSchemeFactory implements SchemeFactory {
    public SubscriptionTupleScheme getScheme() {
      return new SubscriptionTupleScheme();
    }
  }

  private static class SubscriptionTupleScheme extends TupleScheme<Subscription> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, Subscription struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.host);
      oprot.writeI32(struct.port);
      oprot.writeString(struct.queueAddress);
      oprot.writeString(struct.queueName);
      oprot.writeBool(struct.sslRequired);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, Subscription struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.host = iprot.readString();
      struct.setHostIsSet(true);
      struct.port = iprot.readI32();
      struct.setPortIsSet(true);
      struct.queueAddress = iprot.readString();
      struct.setQueueAddressIsSet(true);
      struct.queueName = iprot.readString();
      struct.setQueueNameIsSet(true);
      struct.sslRequired = iprot.readBool();
      struct.setSslRequiredIsSet(true);
    }
  }

}

