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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-9-25")
public class CommitItem implements org.apache.thrift.TBase<CommitItem, CommitItem._Fields>, java.io.Serializable, Cloneable, Comparable<CommitItem> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("CommitItem");

  private static final org.apache.thrift.protocol.TField REPO_URL_FIELD_DESC = new org.apache.thrift.protocol.TField("repoURL", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField REVISION_FIELD_DESC = new org.apache.thrift.protocol.TField("revision", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField PATH_FIELD_DESC = new org.apache.thrift.protocol.TField("path", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("type", org.apache.thrift.protocol.TType.I32, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new CommitItemStandardSchemeFactory());
    schemes.put(TupleScheme.class, new CommitItemTupleSchemeFactory());
  }

  public String repoURL; // required
  public String revision; // required
  public String path; // required
  /**
   * 
   * @see CommitItemChangeType
   */
  public CommitItemChangeType type; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    REPO_URL((short)1, "repoURL"),
    REVISION((short)2, "revision"),
    PATH((short)3, "path"),
    /**
     * 
     * @see CommitItemChangeType
     */
    TYPE((short)4, "type");

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
        case 1: // REPO_URL
          return REPO_URL;
        case 2: // REVISION
          return REVISION;
        case 3: // PATH
          return PATH;
        case 4: // TYPE
          return TYPE;
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
    tmpMap.put(_Fields.REPO_URL, new org.apache.thrift.meta_data.FieldMetaData("repoURL", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.REVISION, new org.apache.thrift.meta_data.FieldMetaData("revision", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.PATH, new org.apache.thrift.meta_data.FieldMetaData("path", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.TYPE, new org.apache.thrift.meta_data.FieldMetaData("type", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, CommitItemChangeType.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(CommitItem.class, metaDataMap);
  }

  public CommitItem() {
  }

  public CommitItem(
    String repoURL,
    String revision,
    String path,
    CommitItemChangeType type)
  {
    this();
    this.repoURL = repoURL;
    this.revision = revision;
    this.path = path;
    this.type = type;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public CommitItem(CommitItem other) {
    if (other.isSetRepoURL()) {
      this.repoURL = other.repoURL;
    }
    if (other.isSetRevision()) {
      this.revision = other.revision;
    }
    if (other.isSetPath()) {
      this.path = other.path;
    }
    if (other.isSetType()) {
      this.type = other.type;
    }
  }

  public CommitItem deepCopy() {
    return new CommitItem(this);
  }

  @Override
  public void clear() {
    this.repoURL = null;
    this.revision = null;
    this.path = null;
    this.type = null;
  }

  public String getRepoURL() {
    return this.repoURL;
  }

  public CommitItem setRepoURL(String repoURL) {
    this.repoURL = repoURL;
    return this;
  }

  public void unsetRepoURL() {
    this.repoURL = null;
  }

  /** Returns true if field repoURL is set (has been assigned a value) and false otherwise */
  public boolean isSetRepoURL() {
    return this.repoURL != null;
  }

  public void setRepoURLIsSet(boolean value) {
    if (!value) {
      this.repoURL = null;
    }
  }

  public String getRevision() {
    return this.revision;
  }

  public CommitItem setRevision(String revision) {
    this.revision = revision;
    return this;
  }

  public void unsetRevision() {
    this.revision = null;
  }

  /** Returns true if field revision is set (has been assigned a value) and false otherwise */
  public boolean isSetRevision() {
    return this.revision != null;
  }

  public void setRevisionIsSet(boolean value) {
    if (!value) {
      this.revision = null;
    }
  }

  public String getPath() {
    return this.path;
  }

  public CommitItem setPath(String path) {
    this.path = path;
    return this;
  }

  public void unsetPath() {
    this.path = null;
  }

  /** Returns true if field path is set (has been assigned a value) and false otherwise */
  public boolean isSetPath() {
    return this.path != null;
  }

  public void setPathIsSet(boolean value) {
    if (!value) {
      this.path = null;
    }
  }

  /**
   * 
   * @see CommitItemChangeType
   */
  public CommitItemChangeType getType() {
    return this.type;
  }

  /**
   * 
   * @see CommitItemChangeType
   */
  public CommitItem setType(CommitItemChangeType type) {
    this.type = type;
    return this;
  }

  public void unsetType() {
    this.type = null;
  }

  /** Returns true if field type is set (has been assigned a value) and false otherwise */
  public boolean isSetType() {
    return this.type != null;
  }

  public void setTypeIsSet(boolean value) {
    if (!value) {
      this.type = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case REPO_URL:
      if (value == null) {
        unsetRepoURL();
      } else {
        setRepoURL((String)value);
      }
      break;

    case REVISION:
      if (value == null) {
        unsetRevision();
      } else {
        setRevision((String)value);
      }
      break;

    case PATH:
      if (value == null) {
        unsetPath();
      } else {
        setPath((String)value);
      }
      break;

    case TYPE:
      if (value == null) {
        unsetType();
      } else {
        setType((CommitItemChangeType)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case REPO_URL:
      return getRepoURL();

    case REVISION:
      return getRevision();

    case PATH:
      return getPath();

    case TYPE:
      return getType();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case REPO_URL:
      return isSetRepoURL();
    case REVISION:
      return isSetRevision();
    case PATH:
      return isSetPath();
    case TYPE:
      return isSetType();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof CommitItem)
      return this.equals((CommitItem)that);
    return false;
  }

  public boolean equals(CommitItem that) {
    if (that == null)
      return false;

    boolean this_present_repoURL = true && this.isSetRepoURL();
    boolean that_present_repoURL = true && that.isSetRepoURL();
    if (this_present_repoURL || that_present_repoURL) {
      if (!(this_present_repoURL && that_present_repoURL))
        return false;
      if (!this.repoURL.equals(that.repoURL))
        return false;
    }

    boolean this_present_revision = true && this.isSetRevision();
    boolean that_present_revision = true && that.isSetRevision();
    if (this_present_revision || that_present_revision) {
      if (!(this_present_revision && that_present_revision))
        return false;
      if (!this.revision.equals(that.revision))
        return false;
    }

    boolean this_present_path = true && this.isSetPath();
    boolean that_present_path = true && that.isSetPath();
    if (this_present_path || that_present_path) {
      if (!(this_present_path && that_present_path))
        return false;
      if (!this.path.equals(that.path))
        return false;
    }

    boolean this_present_type = true && this.isSetType();
    boolean that_present_type = true && that.isSetType();
    if (this_present_type || that_present_type) {
      if (!(this_present_type && that_present_type))
        return false;
      if (!this.type.equals(that.type))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_repoURL = true && (isSetRepoURL());
    list.add(present_repoURL);
    if (present_repoURL)
      list.add(repoURL);

    boolean present_revision = true && (isSetRevision());
    list.add(present_revision);
    if (present_revision)
      list.add(revision);

    boolean present_path = true && (isSetPath());
    list.add(present_path);
    if (present_path)
      list.add(path);

    boolean present_type = true && (isSetType());
    list.add(present_type);
    if (present_type)
      list.add(type.getValue());

    return list.hashCode();
  }

  @Override
  public int compareTo(CommitItem other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetRepoURL()).compareTo(other.isSetRepoURL());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRepoURL()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.repoURL, other.repoURL);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetRevision()).compareTo(other.isSetRevision());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRevision()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.revision, other.revision);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetPath()).compareTo(other.isSetPath());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPath()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.path, other.path);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetType()).compareTo(other.isSetType());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetType()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.type, other.type);
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
    StringBuilder sb = new StringBuilder("CommitItem(");
    boolean first = true;

    sb.append("repoURL:");
    if (this.repoURL == null) {
      sb.append("null");
    } else {
      sb.append(this.repoURL);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("revision:");
    if (this.revision == null) {
      sb.append("null");
    } else {
      sb.append(this.revision);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("path:");
    if (this.path == null) {
      sb.append("null");
    } else {
      sb.append(this.path);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("type:");
    if (this.type == null) {
      sb.append("null");
    } else {
      sb.append(this.type);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (repoURL == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'repoURL' was not present! Struct: " + toString());
    }
    if (revision == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'revision' was not present! Struct: " + toString());
    }
    if (path == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'path' was not present! Struct: " + toString());
    }
    if (type == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'type' was not present! Struct: " + toString());
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

  private static class CommitItemStandardSchemeFactory implements SchemeFactory {
    public CommitItemStandardScheme getScheme() {
      return new CommitItemStandardScheme();
    }
  }

  private static class CommitItemStandardScheme extends StandardScheme<CommitItem> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, CommitItem struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // REPO_URL
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.repoURL = iprot.readString();
              struct.setRepoURLIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // REVISION
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.revision = iprot.readString();
              struct.setRevisionIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // PATH
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.path = iprot.readString();
              struct.setPathIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // TYPE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.type = uk.ac.york.mondo.integration.api.CommitItemChangeType.findByValue(iprot.readI32());
              struct.setTypeIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, CommitItem struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.repoURL != null) {
        oprot.writeFieldBegin(REPO_URL_FIELD_DESC);
        oprot.writeString(struct.repoURL);
        oprot.writeFieldEnd();
      }
      if (struct.revision != null) {
        oprot.writeFieldBegin(REVISION_FIELD_DESC);
        oprot.writeString(struct.revision);
        oprot.writeFieldEnd();
      }
      if (struct.path != null) {
        oprot.writeFieldBegin(PATH_FIELD_DESC);
        oprot.writeString(struct.path);
        oprot.writeFieldEnd();
      }
      if (struct.type != null) {
        oprot.writeFieldBegin(TYPE_FIELD_DESC);
        oprot.writeI32(struct.type.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class CommitItemTupleSchemeFactory implements SchemeFactory {
    public CommitItemTupleScheme getScheme() {
      return new CommitItemTupleScheme();
    }
  }

  private static class CommitItemTupleScheme extends TupleScheme<CommitItem> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, CommitItem struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.repoURL);
      oprot.writeString(struct.revision);
      oprot.writeString(struct.path);
      oprot.writeI32(struct.type.getValue());
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, CommitItem struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.repoURL = iprot.readString();
      struct.setRepoURLIsSet(true);
      struct.revision = iprot.readString();
      struct.setRevisionIsSet(true);
      struct.path = iprot.readString();
      struct.setPathIsSet(true);
      struct.type = uk.ac.york.mondo.integration.api.CommitItemChangeType.findByValue(iprot.readI32());
      struct.setTypeIsSet(true);
    }
  }

}

