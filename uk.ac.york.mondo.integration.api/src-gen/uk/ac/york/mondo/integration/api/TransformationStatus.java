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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-7-29")
public class TransformationStatus implements org.apache.thrift.TBase<TransformationStatus, TransformationStatus._Fields>, java.io.Serializable, Cloneable, Comparable<TransformationStatus> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TransformationStatus");

  private static final org.apache.thrift.protocol.TField FINISHED_FIELD_DESC = new org.apache.thrift.protocol.TField("finished", org.apache.thrift.protocol.TType.BOOL, (short)1);
  private static final org.apache.thrift.protocol.TField ELAPSED_FIELD_DESC = new org.apache.thrift.protocol.TField("elapsed", org.apache.thrift.protocol.TType.I64, (short)2);
  private static final org.apache.thrift.protocol.TField ERROR_FIELD_DESC = new org.apache.thrift.protocol.TField("error", org.apache.thrift.protocol.TType.STRING, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TransformationStatusStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TransformationStatusTupleSchemeFactory());
  }

  public boolean finished; // required
  public long elapsed; // required
  public String error; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    FINISHED((short)1, "finished"),
    ELAPSED((short)2, "elapsed"),
    ERROR((short)3, "error");

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
        case 1: // FINISHED
          return FINISHED;
        case 2: // ELAPSED
          return ELAPSED;
        case 3: // ERROR
          return ERROR;
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
  private static final int __FINISHED_ISSET_ID = 0;
  private static final int __ELAPSED_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.FINISHED, new org.apache.thrift.meta_data.FieldMetaData("finished", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    tmpMap.put(_Fields.ELAPSED, new org.apache.thrift.meta_data.FieldMetaData("elapsed", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.ERROR, new org.apache.thrift.meta_data.FieldMetaData("error", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TransformationStatus.class, metaDataMap);
  }

  public TransformationStatus() {
  }

  public TransformationStatus(
    boolean finished,
    long elapsed,
    String error)
  {
    this();
    this.finished = finished;
    setFinishedIsSet(true);
    this.elapsed = elapsed;
    setElapsedIsSet(true);
    this.error = error;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TransformationStatus(TransformationStatus other) {
    __isset_bitfield = other.__isset_bitfield;
    this.finished = other.finished;
    this.elapsed = other.elapsed;
    if (other.isSetError()) {
      this.error = other.error;
    }
  }

  public TransformationStatus deepCopy() {
    return new TransformationStatus(this);
  }

  @Override
  public void clear() {
    setFinishedIsSet(false);
    this.finished = false;
    setElapsedIsSet(false);
    this.elapsed = 0;
    this.error = null;
  }

  public boolean isFinished() {
    return this.finished;
  }

  public TransformationStatus setFinished(boolean finished) {
    this.finished = finished;
    setFinishedIsSet(true);
    return this;
  }

  public void unsetFinished() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __FINISHED_ISSET_ID);
  }

  /** Returns true if field finished is set (has been assigned a value) and false otherwise */
  public boolean isSetFinished() {
    return EncodingUtils.testBit(__isset_bitfield, __FINISHED_ISSET_ID);
  }

  public void setFinishedIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __FINISHED_ISSET_ID, value);
  }

  public long getElapsed() {
    return this.elapsed;
  }

  public TransformationStatus setElapsed(long elapsed) {
    this.elapsed = elapsed;
    setElapsedIsSet(true);
    return this;
  }

  public void unsetElapsed() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __ELAPSED_ISSET_ID);
  }

  /** Returns true if field elapsed is set (has been assigned a value) and false otherwise */
  public boolean isSetElapsed() {
    return EncodingUtils.testBit(__isset_bitfield, __ELAPSED_ISSET_ID);
  }

  public void setElapsedIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __ELAPSED_ISSET_ID, value);
  }

  public String getError() {
    return this.error;
  }

  public TransformationStatus setError(String error) {
    this.error = error;
    return this;
  }

  public void unsetError() {
    this.error = null;
  }

  /** Returns true if field error is set (has been assigned a value) and false otherwise */
  public boolean isSetError() {
    return this.error != null;
  }

  public void setErrorIsSet(boolean value) {
    if (!value) {
      this.error = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case FINISHED:
      if (value == null) {
        unsetFinished();
      } else {
        setFinished((Boolean)value);
      }
      break;

    case ELAPSED:
      if (value == null) {
        unsetElapsed();
      } else {
        setElapsed((Long)value);
      }
      break;

    case ERROR:
      if (value == null) {
        unsetError();
      } else {
        setError((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case FINISHED:
      return Boolean.valueOf(isFinished());

    case ELAPSED:
      return Long.valueOf(getElapsed());

    case ERROR:
      return getError();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case FINISHED:
      return isSetFinished();
    case ELAPSED:
      return isSetElapsed();
    case ERROR:
      return isSetError();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TransformationStatus)
      return this.equals((TransformationStatus)that);
    return false;
  }

  public boolean equals(TransformationStatus that) {
    if (that == null)
      return false;

    boolean this_present_finished = true;
    boolean that_present_finished = true;
    if (this_present_finished || that_present_finished) {
      if (!(this_present_finished && that_present_finished))
        return false;
      if (this.finished != that.finished)
        return false;
    }

    boolean this_present_elapsed = true;
    boolean that_present_elapsed = true;
    if (this_present_elapsed || that_present_elapsed) {
      if (!(this_present_elapsed && that_present_elapsed))
        return false;
      if (this.elapsed != that.elapsed)
        return false;
    }

    boolean this_present_error = true && this.isSetError();
    boolean that_present_error = true && that.isSetError();
    if (this_present_error || that_present_error) {
      if (!(this_present_error && that_present_error))
        return false;
      if (!this.error.equals(that.error))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_finished = true;
    list.add(present_finished);
    if (present_finished)
      list.add(finished);

    boolean present_elapsed = true;
    list.add(present_elapsed);
    if (present_elapsed)
      list.add(elapsed);

    boolean present_error = true && (isSetError());
    list.add(present_error);
    if (present_error)
      list.add(error);

    return list.hashCode();
  }

  @Override
  public int compareTo(TransformationStatus other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetFinished()).compareTo(other.isSetFinished());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFinished()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.finished, other.finished);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetElapsed()).compareTo(other.isSetElapsed());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetElapsed()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.elapsed, other.elapsed);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetError()).compareTo(other.isSetError());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetError()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.error, other.error);
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
    StringBuilder sb = new StringBuilder("TransformationStatus(");
    boolean first = true;

    sb.append("finished:");
    sb.append(this.finished);
    first = false;
    if (!first) sb.append(", ");
    sb.append("elapsed:");
    sb.append(this.elapsed);
    first = false;
    if (!first) sb.append(", ");
    sb.append("error:");
    if (this.error == null) {
      sb.append("null");
    } else {
      sb.append(this.error);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // alas, we cannot check 'finished' because it's a primitive and you chose the non-beans generator.
    // alas, we cannot check 'elapsed' because it's a primitive and you chose the non-beans generator.
    if (error == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'error' was not present! Struct: " + toString());
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
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class TransformationStatusStandardSchemeFactory implements SchemeFactory {
    public TransformationStatusStandardScheme getScheme() {
      return new TransformationStatusStandardScheme();
    }
  }

  private static class TransformationStatusStandardScheme extends StandardScheme<TransformationStatus> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TransformationStatus struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // FINISHED
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.finished = iprot.readBool();
              struct.setFinishedIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // ELAPSED
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.elapsed = iprot.readI64();
              struct.setElapsedIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // ERROR
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.error = iprot.readString();
              struct.setErrorIsSet(true);
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
      if (!struct.isSetFinished()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'finished' was not found in serialized data! Struct: " + toString());
      }
      if (!struct.isSetElapsed()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'elapsed' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TransformationStatus struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(FINISHED_FIELD_DESC);
      oprot.writeBool(struct.finished);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(ELAPSED_FIELD_DESC);
      oprot.writeI64(struct.elapsed);
      oprot.writeFieldEnd();
      if (struct.error != null) {
        oprot.writeFieldBegin(ERROR_FIELD_DESC);
        oprot.writeString(struct.error);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TransformationStatusTupleSchemeFactory implements SchemeFactory {
    public TransformationStatusTupleScheme getScheme() {
      return new TransformationStatusTupleScheme();
    }
  }

  private static class TransformationStatusTupleScheme extends TupleScheme<TransformationStatus> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TransformationStatus struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeBool(struct.finished);
      oprot.writeI64(struct.elapsed);
      oprot.writeString(struct.error);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TransformationStatus struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.finished = iprot.readBool();
      struct.setFinishedIsSet(true);
      struct.elapsed = iprot.readI64();
      struct.setElapsedIsSet(true);
      struct.error = iprot.readString();
      struct.setErrorIsSet(true);
    }
  }

}

