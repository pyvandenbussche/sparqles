/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package sparqles.core.discovery;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class VoidResult extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"VoidResult\",\"namespace\":\"sparqles.core.discovery\",\"fields\":[{\"name\":\"voidFile\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"Exception\",\"type\":[\"string\",\"null\"]}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.util.List<java.lang.CharSequence> voidFile;
  @Deprecated public java.lang.CharSequence Exception;

  /**
   * Default constructor.
   */
  public VoidResult() {}

  /**
   * All-args constructor.
   */
  public VoidResult(java.util.List<java.lang.CharSequence> voidFile, java.lang.CharSequence Exception) {
    this.voidFile = voidFile;
    this.Exception = Exception;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return voidFile;
    case 1: return Exception;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: voidFile = (java.util.List<java.lang.CharSequence>)value$; break;
    case 1: Exception = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'voidFile' field.
   */
  public java.util.List<java.lang.CharSequence> getVoidFile() {
    return voidFile;
  }

  /**
   * Sets the value of the 'voidFile' field.
   * @param value the value to set.
   */
  public void setVoidFile(java.util.List<java.lang.CharSequence> value) {
    this.voidFile = value;
  }

  /**
   * Gets the value of the 'Exception' field.
   */
  public java.lang.CharSequence getException() {
    return Exception;
  }

  /**
   * Sets the value of the 'Exception' field.
   * @param value the value to set.
   */
  public void setException(java.lang.CharSequence value) {
    this.Exception = value;
  }

  /** Creates a new VoidResult RecordBuilder */
  public static sparqles.core.discovery.VoidResult.Builder newBuilder() {
    return new sparqles.core.discovery.VoidResult.Builder();
  }
  
  /** Creates a new VoidResult RecordBuilder by copying an existing Builder */
  public static sparqles.core.discovery.VoidResult.Builder newBuilder(sparqles.core.discovery.VoidResult.Builder other) {
    return new sparqles.core.discovery.VoidResult.Builder(other);
  }
  
  /** Creates a new VoidResult RecordBuilder by copying an existing VoidResult instance */
  public static sparqles.core.discovery.VoidResult.Builder newBuilder(sparqles.core.discovery.VoidResult other) {
    return new sparqles.core.discovery.VoidResult.Builder(other);
  }
  
  /**
   * RecordBuilder for VoidResult instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<VoidResult>
    implements org.apache.avro.data.RecordBuilder<VoidResult> {

    private java.util.List<java.lang.CharSequence> voidFile;
    private java.lang.CharSequence Exception;

    /** Creates a new Builder */
    private Builder() {
      super(sparqles.core.discovery.VoidResult.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(sparqles.core.discovery.VoidResult.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing VoidResult instance */
    private Builder(sparqles.core.discovery.VoidResult other) {
            super(sparqles.core.discovery.VoidResult.SCHEMA$);
      if (isValidValue(fields()[0], other.voidFile)) {
        this.voidFile = data().deepCopy(fields()[0].schema(), other.voidFile);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.Exception)) {
        this.Exception = data().deepCopy(fields()[1].schema(), other.Exception);
        fieldSetFlags()[1] = true;
      }
    }

    /** Gets the value of the 'voidFile' field */
    public java.util.List<java.lang.CharSequence> getVoidFile() {
      return voidFile;
    }
    
    /** Sets the value of the 'voidFile' field */
    public sparqles.core.discovery.VoidResult.Builder setVoidFile(java.util.List<java.lang.CharSequence> value) {
      validate(fields()[0], value);
      this.voidFile = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'voidFile' field has been set */
    public boolean hasVoidFile() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'voidFile' field */
    public sparqles.core.discovery.VoidResult.Builder clearVoidFile() {
      voidFile = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'Exception' field */
    public java.lang.CharSequence getException() {
      return Exception;
    }
    
    /** Sets the value of the 'Exception' field */
    public sparqles.core.discovery.VoidResult.Builder setException(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.Exception = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'Exception' field has been set */
    public boolean hasException() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'Exception' field */
    public sparqles.core.discovery.VoidResult.Builder clearException() {
      Exception = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    public VoidResult build() {
      try {
        VoidResult record = new VoidResult();
        record.voidFile = fieldSetFlags()[0] ? this.voidFile : (java.util.List<java.lang.CharSequence>) defaultValue(fields()[0]);
        record.Exception = fieldSetFlags()[1] ? this.Exception : (java.lang.CharSequence) defaultValue(fields()[1]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}