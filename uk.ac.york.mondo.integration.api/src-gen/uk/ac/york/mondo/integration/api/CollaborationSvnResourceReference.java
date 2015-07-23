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
public class CollaborationSvnResourceReference implements org.apache.thrift.TBase<CollaborationSvnResourceReference, CollaborationSvnResourceReference._Fields>, java.io.Serializable, Cloneable, Comparable<CollaborationSvnResourceReference> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("CollaborationSvnResourceReference");

  private static final org.apache.thrift.protocol.TField REPOSITORY_URI_FIELD_DESC = new org.apache.thrift.protocol.TField("repositoryUri", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField REVISION_FIELD_DESC = new org.apache.thrift.protocol.TField("revision", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField FILE_PATH_FIELD_DESC = new org.apache.thrift.protocol.TField("filePath", org.apache.thrift.protocol.TType.STRING, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new CollaborationSvnResourceReferenceStandardSchemeFactory());
    schemes.put(TupleScheme.class, new CollaborationSvnResourceReferenceTupleSchemeFactory());
  }

  public String repositoryUri; // required
  public String revision; // required
  public String filePath; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    REPOSITORY_URI((short)1, "repositoryUri"),
    REVISION((short)2, "revision"),
    FILE_PATH((short)3, "filePath");

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
        case 1: // REPOSITORY_URI
          return REPOSITORY_URI;
        case 2: // REVISION
          return REVISION;
        case 3: // FILE_PATH
          return FILE_PATH;
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
    tmpMap.put(_Fields.REPOSITORY_URI, new org.apache.thrift.meta_data.FieldMetaData("repositoryUri", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.REVISION, new org.apache.thrift.meta_data.FieldMetaData("revision", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.FILE_PATH, new org.apache.thrift.meta_data.FieldMetaData("filePath", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(CollaborationSvnResourceReference.class, metaDataMap);
  }

  public CollaborationSvnResourceReference() {
  }

  public CollaborationSvnResourceReference(
    String repositoryUri,
    String revision,
    String filePath)
  {
    this();
    this.repositoryUri = repositoryUri;
    this.revision = revision;
    this.filePath = filePath;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public CollaborationSvnResourceReference(CollaborationSvnResourceReference other) {
    if (other.isSetRepositoryUri()) {
      this.repositoryUri = other.repositoryUri;
    }
    if (other.isSetRevision()) {
      this.revision = other.revision;
    }
    if (other.isSetFilePath()) {
      this.filePath = other.filePath;
    }
  }

  public CollaborationSvnResourceReference deepCopy() {
    return new CollaborationSvnResourceReference(this);
  }

  @Override
  public void clear() {
    this.repositoryUri = null;
    this.revision = null;
    this.filePath = null;
  }

  public String getRepositoryUri() {
    return this.repositoryUri;
  }

  public CollaborationSvnResourceReference setRepositoryUri(String repositoryUri) {
    this.repositoryUri = repositoryUri;
    return this;
  }

  public void unsetRepositoryUri() {
    this.repositoryUri = null;
  }

  /** Returns true if field repositoryUri is set (has been assigned a value) and false otherwise */
  public boolean isSetRepositoryUri() {
    return this.repositoryUri != null;
  }

  public void setRepositoryUriIsSet(boolean value) {
    if (!value) {
      this.repositoryUri = null;
    }
  }

  public String getRevision() {
    return this.revision;
  }

  public CollaborationSvnResourceReference setRevision(String revision) {
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

  public String getFilePath() {
    return this.filePath;
  }

  public CollaborationSvnResourceReference setFilePath(String filePath) {
    this.filePath = filePath;
    return this;
  }

  public void unsetFilePath() {
    this.filePath = null;
  }

  /** Returns true if field filePath is set (has been assigned a value) and false otherwise */
  public boolean isSetFilePath() {
    return this.filePath != null;
  }

  public void setFilePathIsSet(boolean value) {
    if (!value) {
      this.filePath = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case REPOSITORY_URI:
      if (value == null) {
        unsetRepositoryUri();
      } else {
        setRepositoryUri((String)value);
      }
      break;

    case REVISION:
      if (value == null) {
        unsetRevision();
      } else {
        setRevision((String)value);
      }
      break;

    case FILE_PATH:
      if (value == null) {
        unsetFilePath();
      } else {
        setFilePath((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case REPOSITORY_URI:
      return getRepositoryUri();

    case REVISION:
      return getRevision();

    case FILE_PATH:
      return getFilePath();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case REPOSITORY_URI:
      return isSetRepositoryUri();
    case REVISION:
      return isSetRevision();
    case FILE_PATH:
      return isSetFilePath();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof CollaborationSvnResourceReference)
      return this.equals((CollaborationSvnResourceReference)that);
    return false;
  }

  public boolean equals(CollaborationSvnResourceReference that) {
    if (that == null)
      return false;

    boolean this_present_repositoryUri = true && this.isSetRepositoryUri();
    boolean that_present_repositoryUri = true && that.isSetRepositoryUri();
    if (this_present_repositoryUri || that_present_repositoryUri) {
      if (!(this_present_repositoryUri && that_present_repositoryUri))
        return false;
      if (!this.repositoryUri.equals(that.repositoryUri))
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

    boolean this_present_filePath = true && this.isSetFilePath();
    boolean that_present_filePath = true && that.isSetFilePath();
    if (this_present_filePath || that_present_filePath) {
      if (!(this_present_filePath && that_present_filePath))
        return false;
      if (!this.filePath.equals(that.filePath))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_repositoryUri = true && (isSetRepositoryUri());
    list.add(present_repositoryUri);
    if (present_repositoryUri)
      list.add(repositoryUri);

    boolean present_revision = true && (isSetRevision());
    list.add(present_revision);
    if (present_revision)
      list.add(revision);

    boolean present_filePath = true && (isSetFilePath());
    list.add(present_filePath);
    if (present_filePath)
      list.add(filePath);

    return list.hashCode();
  }

  @Override
  public int compareTo(CollaborationSvnResourceReference other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetRepositoryUri()).compareTo(other.isSetRepositoryUri());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRepositoryUri()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.repositoryUri, other.repositoryUri);
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
    lastComparison = Boolean.valueOf(isSetFilePath()).compareTo(other.isSetFilePath());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFilePath()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.filePath, other.filePath);
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
    StringBuilder sb = new StringBuilder("CollaborationSvnResourceReference(");
    boolean first = true;

    sb.append("repositoryUri:");
    if (this.repositoryUri == null) {
      sb.append("null");
    } else {
      sb.append(this.repositoryUri);
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
    sb.append("filePath:");
    if (this.filePath == null) {
      sb.append("null");
    } else {
      sb.append(this.filePath);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (repositoryUri == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'repositoryUri' was not present! Struct: " + toString());
    }
    if (revision == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'revision' was not present! Struct: " + toString());
    }
    if (filePath == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'filePath' was not present! Struct: " + toString());
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

  private static class CollaborationSvnResourceReferenceStandardSchemeFactory implements SchemeFactory {
    public CollaborationSvnResourceReferenceStandardScheme getScheme() {
      return new CollaborationSvnResourceReferenceStandardScheme();
    }
  }

  private static class CollaborationSvnResourceReferenceStandardScheme extends StandardScheme<CollaborationSvnResourceReference> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, CollaborationSvnResourceReference struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // REPOSITORY_URI
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.repositoryUri = iprot.readString();
              struct.setRepositoryUriIsSet(true);
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
          case 3: // FILE_PATH
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.filePath = iprot.readString();
              struct.setFilePathIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, CollaborationSvnResourceReference struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.repositoryUri != null) {
        oprot.writeFieldBegin(REPOSITORY_URI_FIELD_DESC);
        oprot.writeString(struct.repositoryUri);
        oprot.writeFieldEnd();
      }
      if (struct.revision != null) {
        oprot.writeFieldBegin(REVISION_FIELD_DESC);
        oprot.writeString(struct.revision);
        oprot.writeFieldEnd();
      }
      if (struct.filePath != null) {
        oprot.writeFieldBegin(FILE_PATH_FIELD_DESC);
        oprot.writeString(struct.filePath);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class CollaborationSvnResourceReferenceTupleSchemeFactory implements SchemeFactory {
    public CollaborationSvnResourceReferenceTupleScheme getScheme() {
      return new CollaborationSvnResourceReferenceTupleScheme();
    }
  }

  private static class CollaborationSvnResourceReferenceTupleScheme extends TupleScheme<CollaborationSvnResourceReference> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, CollaborationSvnResourceReference struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.repositoryUri);
      oprot.writeString(struct.revision);
      oprot.writeString(struct.filePath);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, CollaborationSvnResourceReference struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.repositoryUri = iprot.readString();
      struct.setRepositoryUriIsSet(true);
      struct.revision = iprot.readString();
      struct.setRevisionIsSet(true);
      struct.filePath = iprot.readString();
      struct.setFilePathIsSet(true);
    }
  }

}

