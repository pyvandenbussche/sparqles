/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package sparqles.avro.core;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class Robots extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Robots\",\"namespace\":\"sparqles.avro.core\",\"fields\":[{\"name\":\"endpoint\",\"type\":{\"type\":\"record\",\"name\":\"Endpoint\",\"namespace\":\"sparqles.avro\",\"fields\":[{\"name\":\"uri\",\"type\":\"string\"},{\"name\":\"datasets\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"Dataset\",\"fields\":[{\"name\":\"uri\",\"type\":\"string\"},{\"name\":\"label\",\"type\":\"string\"}]}}}]}},{\"name\":\"content\",\"type\":[\"string\",\"null\"]},{\"name\":\"exception\",\"type\":[\"string\",\"null\"]},{\"name\":\"respCode\",\"type\":\"int\"},{\"name\":\"timestamp\",\"type\":\"long\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public sparqles.avro.Endpoint endpoint;
  @Deprecated public java.lang.CharSequence content;
  @Deprecated public java.lang.CharSequence exception;
  @Deprecated public int respCode;
  @Deprecated public long timestamp;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public Robots() {}

  /**
   * All-args constructor.
   */
  public Robots(sparqles.avro.Endpoint endpoint, java.lang.CharSequence content, java.lang.CharSequence exception, java.lang.Integer respCode, java.lang.Long timestamp) {
    this.endpoint = endpoint;
    this.content = content;
    this.exception = exception;
    this.respCode = respCode;
    this.timestamp = timestamp;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return endpoint;
    case 1: return content;
    case 2: return exception;
    case 3: return respCode;
    case 4: return timestamp;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: endpoint = (sparqles.avro.Endpoint)value$; break;
    case 1: content = (java.lang.CharSequence)value$; break;
    case 2: exception = (java.lang.CharSequence)value$; break;
    case 3: respCode = (java.lang.Integer)value$; break;
    case 4: timestamp = (java.lang.Long)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'endpoint' field.
   */
  public sparqles.avro.Endpoint getEndpoint() {
    return endpoint;
  }

  /**
   * Sets the value of the 'endpoint' field.
   * @param value the value to set.
   */
  public void setEndpoint(sparqles.avro.Endpoint value) {
    this.endpoint = value;
  }

  /**
   * Gets the value of the 'content' field.
   */
  public java.lang.CharSequence getContent() {
    return content;
  }

  /**
   * Sets the value of the 'content' field.
   * @param value the value to set.
   */
  public void setContent(java.lang.CharSequence value) {
    this.content = value;
  }

  /**
   * Gets the value of the 'exception' field.
   */
  public java.lang.CharSequence getException() {
    return exception;
  }

  /**
   * Sets the value of the 'exception' field.
   * @param value the value to set.
   */
  public void setException(java.lang.CharSequence value) {
    this.exception = value;
  }

  /**
   * Gets the value of the 'respCode' field.
   */
  public java.lang.Integer getRespCode() {
    return respCode;
  }

  /**
   * Sets the value of the 'respCode' field.
   * @param value the value to set.
   */
  public void setRespCode(java.lang.Integer value) {
    this.respCode = value;
  }

  /**
   * Gets the value of the 'timestamp' field.
   */
  public java.lang.Long getTimestamp() {
    return timestamp;
  }

  /**
   * Sets the value of the 'timestamp' field.
   * @param value the value to set.
   */
  public void setTimestamp(java.lang.Long value) {
    this.timestamp = value;
  }

  /** Creates a new Robots RecordBuilder */
  public static sparqles.avro.core.Robots.Builder newBuilder() {
    return new sparqles.avro.core.Robots.Builder();
  }
  
  /** Creates a new Robots RecordBuilder by copying an existing Builder */
  public static sparqles.avro.core.Robots.Builder newBuilder(sparqles.avro.core.Robots.Builder other) {
    return new sparqles.avro.core.Robots.Builder(other);
  }
  
  /** Creates a new Robots RecordBuilder by copying an existing Robots instance */
  public static sparqles.avro.core.Robots.Builder newBuilder(sparqles.avro.core.Robots other) {
    return new sparqles.avro.core.Robots.Builder(other);
  }
  
  /**
   * RecordBuilder for Robots instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Robots>
    implements org.apache.avro.data.RecordBuilder<Robots> {

    private sparqles.avro.Endpoint endpoint;
    private java.lang.CharSequence content;
    private java.lang.CharSequence exception;
    private int respCode;
    private long timestamp;

    /** Creates a new Builder */
    private Builder() {
      super(sparqles.avro.core.Robots.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(sparqles.avro.core.Robots.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.endpoint)) {
        this.endpoint = data().deepCopy(fields()[0].schema(), other.endpoint);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.content)) {
        this.content = data().deepCopy(fields()[1].schema(), other.content);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.exception)) {
        this.exception = data().deepCopy(fields()[2].schema(), other.exception);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.respCode)) {
        this.respCode = data().deepCopy(fields()[3].schema(), other.respCode);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.timestamp)) {
        this.timestamp = data().deepCopy(fields()[4].schema(), other.timestamp);
        fieldSetFlags()[4] = true;
      }
    }
    
    /** Creates a Builder by copying an existing Robots instance */
    private Builder(sparqles.avro.core.Robots other) {
            super(sparqles.avro.core.Robots.SCHEMA$);
      if (isValidValue(fields()[0], other.endpoint)) {
        this.endpoint = data().deepCopy(fields()[0].schema(), other.endpoint);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.content)) {
        this.content = data().deepCopy(fields()[1].schema(), other.content);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.exception)) {
        this.exception = data().deepCopy(fields()[2].schema(), other.exception);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.respCode)) {
        this.respCode = data().deepCopy(fields()[3].schema(), other.respCode);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.timestamp)) {
        this.timestamp = data().deepCopy(fields()[4].schema(), other.timestamp);
        fieldSetFlags()[4] = true;
      }
    }

    /** Gets the value of the 'endpoint' field */
    public sparqles.avro.Endpoint getEndpoint() {
      return endpoint;
    }
    
    /** Sets the value of the 'endpoint' field */
    public sparqles.avro.core.Robots.Builder setEndpoint(sparqles.avro.Endpoint value) {
      validate(fields()[0], value);
      this.endpoint = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'endpoint' field has been set */
    public boolean hasEndpoint() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'endpoint' field */
    public sparqles.avro.core.Robots.Builder clearEndpoint() {
      endpoint = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'content' field */
    public java.lang.CharSequence getContent() {
      return content;
    }
    
    /** Sets the value of the 'content' field */
    public sparqles.avro.core.Robots.Builder setContent(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.content = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'content' field has been set */
    public boolean hasContent() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'content' field */
    public sparqles.avro.core.Robots.Builder clearContent() {
      content = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'exception' field */
    public java.lang.CharSequence getException() {
      return exception;
    }
    
    /** Sets the value of the 'exception' field */
    public sparqles.avro.core.Robots.Builder setException(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.exception = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'exception' field has been set */
    public boolean hasException() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'exception' field */
    public sparqles.avro.core.Robots.Builder clearException() {
      exception = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'respCode' field */
    public java.lang.Integer getRespCode() {
      return respCode;
    }
    
    /** Sets the value of the 'respCode' field */
    public sparqles.avro.core.Robots.Builder setRespCode(int value) {
      validate(fields()[3], value);
      this.respCode = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'respCode' field has been set */
    public boolean hasRespCode() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'respCode' field */
    public sparqles.avro.core.Robots.Builder clearRespCode() {
      fieldSetFlags()[3] = false;
      return this;
    }

    /** Gets the value of the 'timestamp' field */
    public java.lang.Long getTimestamp() {
      return timestamp;
    }
    
    /** Sets the value of the 'timestamp' field */
    public sparqles.avro.core.Robots.Builder setTimestamp(long value) {
      validate(fields()[4], value);
      this.timestamp = value;
      fieldSetFlags()[4] = true;
      return this; 
    }
    
    /** Checks whether the 'timestamp' field has been set */
    public boolean hasTimestamp() {
      return fieldSetFlags()[4];
    }
    
    /** Clears the value of the 'timestamp' field */
    public sparqles.avro.core.Robots.Builder clearTimestamp() {
      fieldSetFlags()[4] = false;
      return this;
    }

    @Override
    public Robots build() {
      try {
        Robots record = new Robots();
        record.endpoint = fieldSetFlags()[0] ? this.endpoint : (sparqles.avro.Endpoint) defaultValue(fields()[0]);
        record.content = fieldSetFlags()[1] ? this.content : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.exception = fieldSetFlags()[2] ? this.exception : (java.lang.CharSequence) defaultValue(fields()[2]);
        record.respCode = fieldSetFlags()[3] ? this.respCode : (java.lang.Integer) defaultValue(fields()[3]);
        record.timestamp = fieldSetFlags()[4] ? this.timestamp : (java.lang.Long) defaultValue(fields()[4]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
