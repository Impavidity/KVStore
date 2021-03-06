/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2017-11-29")
public class RequestVoteResponse implements org.apache.thrift.TBase<RequestVoteResponse, RequestVoteResponse._Fields>, java.io.Serializable, Cloneable, Comparable<RequestVoteResponse> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("RequestVoteResponse");

  private static final org.apache.thrift.protocol.TField TERM_FIELD_DESC = new org.apache.thrift.protocol.TField("term", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField VOTE_GRANTED_FIELD_DESC = new org.apache.thrift.protocol.TField("voteGranted", org.apache.thrift.protocol.TType.BOOL, (short)2);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new RequestVoteResponseStandardSchemeFactory());
    schemes.put(TupleScheme.class, new RequestVoteResponseTupleSchemeFactory());
  }

  public int term; // required
  public boolean voteGranted; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    TERM((short)1, "term"),
    VOTE_GRANTED((short)2, "voteGranted");

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
        case 1: // TERM
          return TERM;
        case 2: // VOTE_GRANTED
          return VOTE_GRANTED;
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
  private static final int __TERM_ISSET_ID = 0;
  private static final int __VOTEGRANTED_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TERM, new org.apache.thrift.meta_data.FieldMetaData("term", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.VOTE_GRANTED, new org.apache.thrift.meta_data.FieldMetaData("voteGranted", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(RequestVoteResponse.class, metaDataMap);
  }

  public RequestVoteResponse() {
  }

  public RequestVoteResponse(
    int term,
    boolean voteGranted)
  {
    this();
    this.term = term;
    setTermIsSet(true);
    this.voteGranted = voteGranted;
    setVoteGrantedIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public RequestVoteResponse(RequestVoteResponse other) {
    __isset_bitfield = other.__isset_bitfield;
    this.term = other.term;
    this.voteGranted = other.voteGranted;
  }

  public RequestVoteResponse deepCopy() {
    return new RequestVoteResponse(this);
  }

  @Override
  public void clear() {
    setTermIsSet(false);
    this.term = 0;
    setVoteGrantedIsSet(false);
    this.voteGranted = false;
  }

  public int getTerm() {
    return this.term;
  }

  public RequestVoteResponse setTerm(int term) {
    this.term = term;
    setTermIsSet(true);
    return this;
  }

  public void unsetTerm() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TERM_ISSET_ID);
  }

  /** Returns true if field term is set (has been assigned a value) and false otherwise */
  public boolean isSetTerm() {
    return EncodingUtils.testBit(__isset_bitfield, __TERM_ISSET_ID);
  }

  public void setTermIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TERM_ISSET_ID, value);
  }

  public boolean isVoteGranted() {
    return this.voteGranted;
  }

  public RequestVoteResponse setVoteGranted(boolean voteGranted) {
    this.voteGranted = voteGranted;
    setVoteGrantedIsSet(true);
    return this;
  }

  public void unsetVoteGranted() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __VOTEGRANTED_ISSET_ID);
  }

  /** Returns true if field voteGranted is set (has been assigned a value) and false otherwise */
  public boolean isSetVoteGranted() {
    return EncodingUtils.testBit(__isset_bitfield, __VOTEGRANTED_ISSET_ID);
  }

  public void setVoteGrantedIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __VOTEGRANTED_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case TERM:
      if (value == null) {
        unsetTerm();
      } else {
        setTerm((Integer)value);
      }
      break;

    case VOTE_GRANTED:
      if (value == null) {
        unsetVoteGranted();
      } else {
        setVoteGranted((Boolean)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case TERM:
      return getTerm();

    case VOTE_GRANTED:
      return isVoteGranted();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case TERM:
      return isSetTerm();
    case VOTE_GRANTED:
      return isSetVoteGranted();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof RequestVoteResponse)
      return this.equals((RequestVoteResponse)that);
    return false;
  }

  public boolean equals(RequestVoteResponse that) {
    if (that == null)
      return false;

    boolean this_present_term = true;
    boolean that_present_term = true;
    if (this_present_term || that_present_term) {
      if (!(this_present_term && that_present_term))
        return false;
      if (this.term != that.term)
        return false;
    }

    boolean this_present_voteGranted = true;
    boolean that_present_voteGranted = true;
    if (this_present_voteGranted || that_present_voteGranted) {
      if (!(this_present_voteGranted && that_present_voteGranted))
        return false;
      if (this.voteGranted != that.voteGranted)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_term = true;
    list.add(present_term);
    if (present_term)
      list.add(term);

    boolean present_voteGranted = true;
    list.add(present_voteGranted);
    if (present_voteGranted)
      list.add(voteGranted);

    return list.hashCode();
  }

  @Override
  public int compareTo(RequestVoteResponse other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetTerm()).compareTo(other.isSetTerm());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTerm()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.term, other.term);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetVoteGranted()).compareTo(other.isSetVoteGranted());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetVoteGranted()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.voteGranted, other.voteGranted);
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
    StringBuilder sb = new StringBuilder("RequestVoteResponse(");
    boolean first = true;

    sb.append("term:");
    sb.append(this.term);
    first = false;
    if (!first) sb.append(", ");
    sb.append("voteGranted:");
    sb.append(this.voteGranted);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
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

  private static class RequestVoteResponseStandardSchemeFactory implements SchemeFactory {
    public RequestVoteResponseStandardScheme getScheme() {
      return new RequestVoteResponseStandardScheme();
    }
  }

  private static class RequestVoteResponseStandardScheme extends StandardScheme<RequestVoteResponse> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, RequestVoteResponse struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // TERM
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.term = iprot.readI32();
              struct.setTermIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // VOTE_GRANTED
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.voteGranted = iprot.readBool();
              struct.setVoteGrantedIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, RequestVoteResponse struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(TERM_FIELD_DESC);
      oprot.writeI32(struct.term);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(VOTE_GRANTED_FIELD_DESC);
      oprot.writeBool(struct.voteGranted);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class RequestVoteResponseTupleSchemeFactory implements SchemeFactory {
    public RequestVoteResponseTupleScheme getScheme() {
      return new RequestVoteResponseTupleScheme();
    }
  }

  private static class RequestVoteResponseTupleScheme extends TupleScheme<RequestVoteResponse> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, RequestVoteResponse struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetTerm()) {
        optionals.set(0);
      }
      if (struct.isSetVoteGranted()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetTerm()) {
        oprot.writeI32(struct.term);
      }
      if (struct.isSetVoteGranted()) {
        oprot.writeBool(struct.voteGranted);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, RequestVoteResponse struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.term = iprot.readI32();
        struct.setTermIsSet(true);
      }
      if (incoming.get(1)) {
        struct.voteGranted = iprot.readBool();
        struct.setVoteGrantedIsSet(true);
      }
    }
  }

}

