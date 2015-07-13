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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-7-13")
public class ModelElement implements org.apache.thrift.TBase<ModelElement, ModelElement._Fields>, java.io.Serializable, Cloneable, Comparable<ModelElement> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ModelElement");

  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField METAMODEL_URI_FIELD_DESC = new org.apache.thrift.protocol.TField("metamodelUri", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField TYPE_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("typeName", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField ATTRIBUTES_FIELD_DESC = new org.apache.thrift.protocol.TField("attributes", org.apache.thrift.protocol.TType.LIST, (short)4);
  private static final org.apache.thrift.protocol.TField REFERENCES_FIELD_DESC = new org.apache.thrift.protocol.TField("references", org.apache.thrift.protocol.TType.LIST, (short)5);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ModelElementStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ModelElementTupleSchemeFactory());
  }

  public String id; // required
  public String metamodelUri; // required
  public String typeName; // required
  public List<Slot> attributes; // required
  public List<Slot> references; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ID((short)1, "id"),
    METAMODEL_URI((short)2, "metamodelUri"),
    TYPE_NAME((short)3, "typeName"),
    ATTRIBUTES((short)4, "attributes"),
    REFERENCES((short)5, "references");

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
        case 1: // ID
          return ID;
        case 2: // METAMODEL_URI
          return METAMODEL_URI;
        case 3: // TYPE_NAME
          return TYPE_NAME;
        case 4: // ATTRIBUTES
          return ATTRIBUTES;
        case 5: // REFERENCES
          return REFERENCES;
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
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.METAMODEL_URI, new org.apache.thrift.meta_data.FieldMetaData("metamodelUri", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.TYPE_NAME, new org.apache.thrift.meta_data.FieldMetaData("typeName", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.ATTRIBUTES, new org.apache.thrift.meta_data.FieldMetaData("attributes", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Slot.class))));
    tmpMap.put(_Fields.REFERENCES, new org.apache.thrift.meta_data.FieldMetaData("references", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Slot.class))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ModelElement.class, metaDataMap);
  }

  public ModelElement() {
  }

  public ModelElement(
    String id,
    String metamodelUri,
    String typeName,
    List<Slot> attributes,
    List<Slot> references)
  {
    this();
    this.id = id;
    this.metamodelUri = metamodelUri;
    this.typeName = typeName;
    this.attributes = attributes;
    this.references = references;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ModelElement(ModelElement other) {
    if (other.isSetId()) {
      this.id = other.id;
    }
    if (other.isSetMetamodelUri()) {
      this.metamodelUri = other.metamodelUri;
    }
    if (other.isSetTypeName()) {
      this.typeName = other.typeName;
    }
    if (other.isSetAttributes()) {
      List<Slot> __this__attributes = new ArrayList<Slot>(other.attributes.size());
      for (Slot other_element : other.attributes) {
        __this__attributes.add(new Slot(other_element));
      }
      this.attributes = __this__attributes;
    }
    if (other.isSetReferences()) {
      List<Slot> __this__references = new ArrayList<Slot>(other.references.size());
      for (Slot other_element : other.references) {
        __this__references.add(new Slot(other_element));
      }
      this.references = __this__references;
    }
  }

  public ModelElement deepCopy() {
    return new ModelElement(this);
  }

  @Override
  public void clear() {
    this.id = null;
    this.metamodelUri = null;
    this.typeName = null;
    this.attributes = null;
    this.references = null;
  }

  public String getId() {
    return this.id;
  }

  public ModelElement setId(String id) {
    this.id = id;
    return this;
  }

  public void unsetId() {
    this.id = null;
  }

  /** Returns true if field id is set (has been assigned a value) and false otherwise */
  public boolean isSetId() {
    return this.id != null;
  }

  public void setIdIsSet(boolean value) {
    if (!value) {
      this.id = null;
    }
  }

  public String getMetamodelUri() {
    return this.metamodelUri;
  }

  public ModelElement setMetamodelUri(String metamodelUri) {
    this.metamodelUri = metamodelUri;
    return this;
  }

  public void unsetMetamodelUri() {
    this.metamodelUri = null;
  }

  /** Returns true if field metamodelUri is set (has been assigned a value) and false otherwise */
  public boolean isSetMetamodelUri() {
    return this.metamodelUri != null;
  }

  public void setMetamodelUriIsSet(boolean value) {
    if (!value) {
      this.metamodelUri = null;
    }
  }

  public String getTypeName() {
    return this.typeName;
  }

  public ModelElement setTypeName(String typeName) {
    this.typeName = typeName;
    return this;
  }

  public void unsetTypeName() {
    this.typeName = null;
  }

  /** Returns true if field typeName is set (has been assigned a value) and false otherwise */
  public boolean isSetTypeName() {
    return this.typeName != null;
  }

  public void setTypeNameIsSet(boolean value) {
    if (!value) {
      this.typeName = null;
    }
  }

  public int getAttributesSize() {
    return (this.attributes == null) ? 0 : this.attributes.size();
  }

  public java.util.Iterator<Slot> getAttributesIterator() {
    return (this.attributes == null) ? null : this.attributes.iterator();
  }

  public void addToAttributes(Slot elem) {
    if (this.attributes == null) {
      this.attributes = new ArrayList<Slot>();
    }
    this.attributes.add(elem);
  }

  public List<Slot> getAttributes() {
    return this.attributes;
  }

  public ModelElement setAttributes(List<Slot> attributes) {
    this.attributes = attributes;
    return this;
  }

  public void unsetAttributes() {
    this.attributes = null;
  }

  /** Returns true if field attributes is set (has been assigned a value) and false otherwise */
  public boolean isSetAttributes() {
    return this.attributes != null;
  }

  public void setAttributesIsSet(boolean value) {
    if (!value) {
      this.attributes = null;
    }
  }

  public int getReferencesSize() {
    return (this.references == null) ? 0 : this.references.size();
  }

  public java.util.Iterator<Slot> getReferencesIterator() {
    return (this.references == null) ? null : this.references.iterator();
  }

  public void addToReferences(Slot elem) {
    if (this.references == null) {
      this.references = new ArrayList<Slot>();
    }
    this.references.add(elem);
  }

  public List<Slot> getReferences() {
    return this.references;
  }

  public ModelElement setReferences(List<Slot> references) {
    this.references = references;
    return this;
  }

  public void unsetReferences() {
    this.references = null;
  }

  /** Returns true if field references is set (has been assigned a value) and false otherwise */
  public boolean isSetReferences() {
    return this.references != null;
  }

  public void setReferencesIsSet(boolean value) {
    if (!value) {
      this.references = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case ID:
      if (value == null) {
        unsetId();
      } else {
        setId((String)value);
      }
      break;

    case METAMODEL_URI:
      if (value == null) {
        unsetMetamodelUri();
      } else {
        setMetamodelUri((String)value);
      }
      break;

    case TYPE_NAME:
      if (value == null) {
        unsetTypeName();
      } else {
        setTypeName((String)value);
      }
      break;

    case ATTRIBUTES:
      if (value == null) {
        unsetAttributes();
      } else {
        setAttributes((List<Slot>)value);
      }
      break;

    case REFERENCES:
      if (value == null) {
        unsetReferences();
      } else {
        setReferences((List<Slot>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case ID:
      return getId();

    case METAMODEL_URI:
      return getMetamodelUri();

    case TYPE_NAME:
      return getTypeName();

    case ATTRIBUTES:
      return getAttributes();

    case REFERENCES:
      return getReferences();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case ID:
      return isSetId();
    case METAMODEL_URI:
      return isSetMetamodelUri();
    case TYPE_NAME:
      return isSetTypeName();
    case ATTRIBUTES:
      return isSetAttributes();
    case REFERENCES:
      return isSetReferences();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof ModelElement)
      return this.equals((ModelElement)that);
    return false;
  }

  public boolean equals(ModelElement that) {
    if (that == null)
      return false;

    boolean this_present_id = true && this.isSetId();
    boolean that_present_id = true && that.isSetId();
    if (this_present_id || that_present_id) {
      if (!(this_present_id && that_present_id))
        return false;
      if (!this.id.equals(that.id))
        return false;
    }

    boolean this_present_metamodelUri = true && this.isSetMetamodelUri();
    boolean that_present_metamodelUri = true && that.isSetMetamodelUri();
    if (this_present_metamodelUri || that_present_metamodelUri) {
      if (!(this_present_metamodelUri && that_present_metamodelUri))
        return false;
      if (!this.metamodelUri.equals(that.metamodelUri))
        return false;
    }

    boolean this_present_typeName = true && this.isSetTypeName();
    boolean that_present_typeName = true && that.isSetTypeName();
    if (this_present_typeName || that_present_typeName) {
      if (!(this_present_typeName && that_present_typeName))
        return false;
      if (!this.typeName.equals(that.typeName))
        return false;
    }

    boolean this_present_attributes = true && this.isSetAttributes();
    boolean that_present_attributes = true && that.isSetAttributes();
    if (this_present_attributes || that_present_attributes) {
      if (!(this_present_attributes && that_present_attributes))
        return false;
      if (!this.attributes.equals(that.attributes))
        return false;
    }

    boolean this_present_references = true && this.isSetReferences();
    boolean that_present_references = true && that.isSetReferences();
    if (this_present_references || that_present_references) {
      if (!(this_present_references && that_present_references))
        return false;
      if (!this.references.equals(that.references))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_id = true && (isSetId());
    list.add(present_id);
    if (present_id)
      list.add(id);

    boolean present_metamodelUri = true && (isSetMetamodelUri());
    list.add(present_metamodelUri);
    if (present_metamodelUri)
      list.add(metamodelUri);

    boolean present_typeName = true && (isSetTypeName());
    list.add(present_typeName);
    if (present_typeName)
      list.add(typeName);

    boolean present_attributes = true && (isSetAttributes());
    list.add(present_attributes);
    if (present_attributes)
      list.add(attributes);

    boolean present_references = true && (isSetReferences());
    list.add(present_references);
    if (present_references)
      list.add(references);

    return list.hashCode();
  }

  @Override
  public int compareTo(ModelElement other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetId()).compareTo(other.isSetId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.id, other.id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetMetamodelUri()).compareTo(other.isSetMetamodelUri());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMetamodelUri()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.metamodelUri, other.metamodelUri);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTypeName()).compareTo(other.isSetTypeName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTypeName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.typeName, other.typeName);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetAttributes()).compareTo(other.isSetAttributes());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAttributes()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.attributes, other.attributes);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetReferences()).compareTo(other.isSetReferences());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetReferences()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.references, other.references);
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
    StringBuilder sb = new StringBuilder("ModelElement(");
    boolean first = true;

    sb.append("id:");
    if (this.id == null) {
      sb.append("null");
    } else {
      sb.append(this.id);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("metamodelUri:");
    if (this.metamodelUri == null) {
      sb.append("null");
    } else {
      sb.append(this.metamodelUri);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("typeName:");
    if (this.typeName == null) {
      sb.append("null");
    } else {
      sb.append(this.typeName);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("attributes:");
    if (this.attributes == null) {
      sb.append("null");
    } else {
      sb.append(this.attributes);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("references:");
    if (this.references == null) {
      sb.append("null");
    } else {
      sb.append(this.references);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (id == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'id' was not present! Struct: " + toString());
    }
    if (metamodelUri == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'metamodelUri' was not present! Struct: " + toString());
    }
    if (typeName == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'typeName' was not present! Struct: " + toString());
    }
    if (attributes == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'attributes' was not present! Struct: " + toString());
    }
    if (references == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'references' was not present! Struct: " + toString());
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

  private static class ModelElementStandardSchemeFactory implements SchemeFactory {
    public ModelElementStandardScheme getScheme() {
      return new ModelElementStandardScheme();
    }
  }

  private static class ModelElementStandardScheme extends StandardScheme<ModelElement> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ModelElement struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.id = iprot.readString();
              struct.setIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // METAMODEL_URI
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.metamodelUri = iprot.readString();
              struct.setMetamodelUriIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // TYPE_NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.typeName = iprot.readString();
              struct.setTypeNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // ATTRIBUTES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list24 = iprot.readListBegin();
                struct.attributes = new ArrayList<Slot>(_list24.size);
                Slot _elem25;
                for (int _i26 = 0; _i26 < _list24.size; ++_i26)
                {
                  _elem25 = new Slot();
                  _elem25.read(iprot);
                  struct.attributes.add(_elem25);
                }
                iprot.readListEnd();
              }
              struct.setAttributesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // REFERENCES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list27 = iprot.readListBegin();
                struct.references = new ArrayList<Slot>(_list27.size);
                Slot _elem28;
                for (int _i29 = 0; _i29 < _list27.size; ++_i29)
                {
                  _elem28 = new Slot();
                  _elem28.read(iprot);
                  struct.references.add(_elem28);
                }
                iprot.readListEnd();
              }
              struct.setReferencesIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, ModelElement struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.id != null) {
        oprot.writeFieldBegin(ID_FIELD_DESC);
        oprot.writeString(struct.id);
        oprot.writeFieldEnd();
      }
      if (struct.metamodelUri != null) {
        oprot.writeFieldBegin(METAMODEL_URI_FIELD_DESC);
        oprot.writeString(struct.metamodelUri);
        oprot.writeFieldEnd();
      }
      if (struct.typeName != null) {
        oprot.writeFieldBegin(TYPE_NAME_FIELD_DESC);
        oprot.writeString(struct.typeName);
        oprot.writeFieldEnd();
      }
      if (struct.attributes != null) {
        oprot.writeFieldBegin(ATTRIBUTES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.attributes.size()));
          for (Slot _iter30 : struct.attributes)
          {
            _iter30.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.references != null) {
        oprot.writeFieldBegin(REFERENCES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.references.size()));
          for (Slot _iter31 : struct.references)
          {
            _iter31.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ModelElementTupleSchemeFactory implements SchemeFactory {
    public ModelElementTupleScheme getScheme() {
      return new ModelElementTupleScheme();
    }
  }

  private static class ModelElementTupleScheme extends TupleScheme<ModelElement> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ModelElement struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.id);
      oprot.writeString(struct.metamodelUri);
      oprot.writeString(struct.typeName);
      {
        oprot.writeI32(struct.attributes.size());
        for (Slot _iter32 : struct.attributes)
        {
          _iter32.write(oprot);
        }
      }
      {
        oprot.writeI32(struct.references.size());
        for (Slot _iter33 : struct.references)
        {
          _iter33.write(oprot);
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ModelElement struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.id = iprot.readString();
      struct.setIdIsSet(true);
      struct.metamodelUri = iprot.readString();
      struct.setMetamodelUriIsSet(true);
      struct.typeName = iprot.readString();
      struct.setTypeNameIsSet(true);
      {
        org.apache.thrift.protocol.TList _list34 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
        struct.attributes = new ArrayList<Slot>(_list34.size);
        Slot _elem35;
        for (int _i36 = 0; _i36 < _list34.size; ++_i36)
        {
          _elem35 = new Slot();
          _elem35.read(iprot);
          struct.attributes.add(_elem35);
        }
      }
      struct.setAttributesIsSet(true);
      {
        org.apache.thrift.protocol.TList _list37 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
        struct.references = new ArrayList<Slot>(_list37.size);
        Slot _elem38;
        for (int _i39 = 0; _i39 < _list37.size; ++_i39)
        {
          _elem38 = new Slot();
          _elem38.read(iprot);
          struct.references.add(_elem38);
        }
      }
      struct.setReferencesIsSet(true);
    }
  }

}

