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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-9-15")
public class ContainerSlot implements org.apache.thrift.TBase<ContainerSlot, ContainerSlot._Fields>, java.io.Serializable, Cloneable, Comparable<ContainerSlot> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ContainerSlot");

  private static final org.apache.thrift.protocol.TField NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("name", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField ELEMENTS_FIELD_DESC = new org.apache.thrift.protocol.TField("elements", org.apache.thrift.protocol.TType.LIST, (short)2);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ContainerSlotStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ContainerSlotTupleSchemeFactory());
  }

  public String name; // required
  public List<ModelElement> elements; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    NAME((short)1, "name"),
    ELEMENTS((short)2, "elements");

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
        case 2: // ELEMENTS
          return ELEMENTS;
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
    tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("name", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.ELEMENTS, new org.apache.thrift.meta_data.FieldMetaData("elements", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ModelElement.class))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ContainerSlot.class, metaDataMap);
  }

  public ContainerSlot() {
  }

  public ContainerSlot(
    String name,
    List<ModelElement> elements)
  {
    this();
    this.name = name;
    this.elements = elements;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ContainerSlot(ContainerSlot other) {
    if (other.isSetName()) {
      this.name = other.name;
    }
    if (other.isSetElements()) {
      List<ModelElement> __this__elements = new ArrayList<ModelElement>(other.elements.size());
      for (ModelElement other_element : other.elements) {
        __this__elements.add(new ModelElement(other_element));
      }
      this.elements = __this__elements;
    }
  }

  public ContainerSlot deepCopy() {
    return new ContainerSlot(this);
  }

  @Override
  public void clear() {
    this.name = null;
    this.elements = null;
  }

  public String getName() {
    return this.name;
  }

  public ContainerSlot setName(String name) {
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

  public int getElementsSize() {
    return (this.elements == null) ? 0 : this.elements.size();
  }

  public java.util.Iterator<ModelElement> getElementsIterator() {
    return (this.elements == null) ? null : this.elements.iterator();
  }

  public void addToElements(ModelElement elem) {
    if (this.elements == null) {
      this.elements = new ArrayList<ModelElement>();
    }
    this.elements.add(elem);
  }

  public List<ModelElement> getElements() {
    return this.elements;
  }

  public ContainerSlot setElements(List<ModelElement> elements) {
    this.elements = elements;
    return this;
  }

  public void unsetElements() {
    this.elements = null;
  }

  /** Returns true if field elements is set (has been assigned a value) and false otherwise */
  public boolean isSetElements() {
    return this.elements != null;
  }

  public void setElementsIsSet(boolean value) {
    if (!value) {
      this.elements = null;
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

    case ELEMENTS:
      if (value == null) {
        unsetElements();
      } else {
        setElements((List<ModelElement>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case NAME:
      return getName();

    case ELEMENTS:
      return getElements();

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
    case ELEMENTS:
      return isSetElements();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof ContainerSlot)
      return this.equals((ContainerSlot)that);
    return false;
  }

  public boolean equals(ContainerSlot that) {
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

    boolean this_present_elements = true && this.isSetElements();
    boolean that_present_elements = true && that.isSetElements();
    if (this_present_elements || that_present_elements) {
      if (!(this_present_elements && that_present_elements))
        return false;
      if (!this.elements.equals(that.elements))
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

    boolean present_elements = true && (isSetElements());
    list.add(present_elements);
    if (present_elements)
      list.add(elements);

    return list.hashCode();
  }

  @Override
  public int compareTo(ContainerSlot other) {
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
    lastComparison = Boolean.valueOf(isSetElements()).compareTo(other.isSetElements());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetElements()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.elements, other.elements);
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
    StringBuilder sb = new StringBuilder("ContainerSlot(");
    boolean first = true;

    sb.append("name:");
    if (this.name == null) {
      sb.append("null");
    } else {
      sb.append(this.name);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("elements:");
    if (this.elements == null) {
      sb.append("null");
    } else {
      sb.append(this.elements);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (name == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'name' was not present! Struct: " + toString());
    }
    if (elements == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'elements' was not present! Struct: " + toString());
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

  private static class ContainerSlotStandardSchemeFactory implements SchemeFactory {
    public ContainerSlotStandardScheme getScheme() {
      return new ContainerSlotStandardScheme();
    }
  }

  private static class ContainerSlotStandardScheme extends StandardScheme<ContainerSlot> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ContainerSlot struct) throws org.apache.thrift.TException {
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
          case 2: // ELEMENTS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list112 = iprot.readListBegin();
                struct.elements = new ArrayList<ModelElement>(_list112.size);
                ModelElement _elem113;
                for (int _i114 = 0; _i114 < _list112.size; ++_i114)
                {
                  _elem113 = new ModelElement();
                  _elem113.read(iprot);
                  struct.elements.add(_elem113);
                }
                iprot.readListEnd();
              }
              struct.setElementsIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, ContainerSlot struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.name != null) {
        oprot.writeFieldBegin(NAME_FIELD_DESC);
        oprot.writeString(struct.name);
        oprot.writeFieldEnd();
      }
      if (struct.elements != null) {
        oprot.writeFieldBegin(ELEMENTS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.elements.size()));
          for (ModelElement _iter115 : struct.elements)
          {
            _iter115.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ContainerSlotTupleSchemeFactory implements SchemeFactory {
    public ContainerSlotTupleScheme getScheme() {
      return new ContainerSlotTupleScheme();
    }
  }

  private static class ContainerSlotTupleScheme extends TupleScheme<ContainerSlot> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ContainerSlot struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.name);
      {
        oprot.writeI32(struct.elements.size());
        for (ModelElement _iter116 : struct.elements)
        {
          _iter116.write(oprot);
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ContainerSlot struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.name = iprot.readString();
      struct.setNameIsSet(true);
      {
        org.apache.thrift.protocol.TList _list117 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
        struct.elements = new ArrayList<ModelElement>(_list117.size);
        ModelElement _elem118;
        for (int _i119 = 0; _i119 < _list117.size; ++_i119)
        {
          _elem118 = new ModelElement();
          _elem118.read(iprot);
          struct.elements.add(_elem118);
        }
      }
      struct.setElementsIsSet(true);
    }
  }

}

