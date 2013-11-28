/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package sparqles.avro.discovery;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class QueryInfo extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"QueryInfo\",\"namespace\":\"sparqles.avro.discovery\",\"fields\":[{\"name\":\"URL\",\"type\":\"string\"},{\"name\":\"Operation\",\"type\":\"string\"},{\"name\":\"Exception\",\"type\":[\"string\",\"null\"]},{\"name\":\"allowedByRobotsTXT\",\"type\":\"boolean\",\"default\":true},{\"name\":\"Results\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.CharSequence URL;
  @Deprecated public java.lang.CharSequence Operation;
  @Deprecated public java.lang.CharSequence Exception;
  @Deprecated public boolean allowedByRobotsTXT;
  @Deprecated public java.util.List<java.lang.CharSequence> Results;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public QueryInfo() {}

  /**
   * All-args constructor.
   */
  public QueryInfo(java.lang.CharSequence URL, java.lang.CharSequence Operation, java.lang.CharSequence Exception, java.lang.Boolean allowedByRobotsTXT, java.util.List<java.lang.CharSequence> Results) {
    this.URL = URL;
    this.Operation = Operation;
    this.Exception = Exception;
    this.allowedByRobotsTXT = allowedByRobotsTXT;
    this.Results = Results;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return URL;
    case 1: return Operation;
    case 2: return Exception;
    case 3: return allowedByRobotsTXT;
    case 4: return Results;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: URL = (java.lang.CharSequence)value$; break;
    case 1: Operation = (java.lang.CharSequence)value$; break;
    case 2: Exception = (java.lang.CharSequence)value$; break;
    case 3: allowedByRobotsTXT = (java.lang.Boolean)value$; break;
    case 4: Results = (java.util.List<java.lang.CharSequence>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'URL' field.
   */
  public java.lang.CharSequence getURL() {
    return URL;
  }

  /**
   * Sets the value of the 'URL' field.
   * @param value the value to set.
   */
  public void setURL(java.lang.CharSequence value) {
    this.URL = value;
  }

  /**
   * Gets the value of the 'Operation' field.
   */
  public java.lang.CharSequence getOperation() {
    return Operation;
  }

  /**
   * Sets the value of the 'Operation' field.
   * @param value the value to set.
   */
  public void setOperation(java.lang.CharSequence value) {
    this.Operation = value;
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

  /**
   * Gets the value of the 'allowedByRobotsTXT' field.
   */
  public java.lang.Boolean getAllowedByRobotsTXT() {
    return allowedByRobotsTXT;
  }

  /**
   * Sets the value of the 'allowedByRobotsTXT' field.
   * @param value the value to set.
   */
  public void setAllowedByRobotsTXT(java.lang.Boolean value) {
    this.allowedByRobotsTXT = value;
  }

  /**
   * Gets the value of the 'Results' field.
   */
  public java.util.List<java.lang.CharSequence> getResults() {
    return Results;
  }

  /**
   * Sets the value of the 'Results' field.
   * @param value the value to set.
   */
  public void setResults(java.util.List<java.lang.CharSequence> value) {
    this.Results = value;
  }

  /** Creates a new QueryInfo RecordBuilder */
  public static sparqles.avro.discovery.QueryInfo.Builder newBuilder() {
    return new sparqles.avro.discovery.QueryInfo.Builder();
  }
  
  /** Creates a new QueryInfo RecordBuilder by copying an existing Builder */
  public static sparqles.avro.discovery.QueryInfo.Builder newBuilder(sparqles.avro.discovery.QueryInfo.Builder other) {
    return new sparqles.avro.discovery.QueryInfo.Builder(other);
  }
  
  /** Creates a new QueryInfo RecordBuilder by copying an existing QueryInfo instance */
  public static sparqles.avro.discovery.QueryInfo.Builder newBuilder(sparqles.avro.discovery.QueryInfo other) {
    return new sparqles.avro.discovery.QueryInfo.Builder(other);
  }
  
  /**
   * RecordBuilder for QueryInfo instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<QueryInfo>
    implements org.apache.avro.data.RecordBuilder<QueryInfo> {

    private java.lang.CharSequence URL;
    private java.lang.CharSequence Operation;
    private java.lang.CharSequence Exception;
    private boolean allowedByRobotsTXT;
    private java.util.List<java.lang.CharSequence> Results;

    /** Creates a new Builder */
    private Builder() {
      super(sparqles.avro.discovery.QueryInfo.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(sparqles.avro.discovery.QueryInfo.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.URL)) {
        this.URL = data().deepCopy(fields()[0].schema(), other.URL);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.Operation)) {
        this.Operation = data().deepCopy(fields()[1].schema(), other.Operation);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.Exception)) {
        this.Exception = data().deepCopy(fields()[2].schema(), other.Exception);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.allowedByRobotsTXT)) {
        this.allowedByRobotsTXT = data().deepCopy(fields()[3].schema(), other.allowedByRobotsTXT);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.Results)) {
        this.Results = data().deepCopy(fields()[4].schema(), other.Results);
        fieldSetFlags()[4] = true;
      }
    }
    
    /** Creates a Builder by copying an existing QueryInfo instance */
    private Builder(sparqles.avro.discovery.QueryInfo other) {
            super(sparqles.avro.discovery.QueryInfo.SCHEMA$);
      if (isValidValue(fields()[0], other.URL)) {
        this.URL = data().deepCopy(fields()[0].schema(), other.URL);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.Operation)) {
        this.Operation = data().deepCopy(fields()[1].schema(), other.Operation);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.Exception)) {
        this.Exception = data().deepCopy(fields()[2].schema(), other.Exception);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.allowedByRobotsTXT)) {
        this.allowedByRobotsTXT = data().deepCopy(fields()[3].schema(), other.allowedByRobotsTXT);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.Results)) {
        this.Results = data().deepCopy(fields()[4].schema(), other.Results);
        fieldSetFlags()[4] = true;
      }
    }

    /** Gets the value of the 'URL' field */
    public java.lang.CharSequence getURL() {
      return URL;
    }
    
    /** Sets the value of the 'URL' field */
    public sparqles.avro.discovery.QueryInfo.Builder setURL(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.URL = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'URL' field has been set */
    public boolean hasURL() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'URL' field */
    public sparqles.avro.discovery.QueryInfo.Builder clearURL() {
      URL = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'Operation' field */
    public java.lang.CharSequence getOperation() {
      return Operation;
    }
    
    /** Sets the value of the 'Operation' field */
    public sparqles.avro.discovery.QueryInfo.Builder setOperation(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.Operation = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'Operation' field has been set */
    public boolean hasOperation() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'Operation' field */
    public sparqles.avro.discovery.QueryInfo.Builder clearOperation() {
      Operation = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'Exception' field */
    public java.lang.CharSequence getException() {
      return Exception;
    }
    
    /** Sets the value of the 'Exception' field */
    public sparqles.avro.discovery.QueryInfo.Builder setException(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.Exception = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'Exception' field has been set */
    public boolean hasException() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'Exception' field */
    public sparqles.avro.discovery.QueryInfo.Builder clearException() {
      Exception = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'allowedByRobotsTXT' field */
    public java.lang.Boolean getAllowedByRobotsTXT() {
      return allowedByRobotsTXT;
    }
    
    /** Sets the value of the 'allowedByRobotsTXT' field */
    public sparqles.avro.discovery.QueryInfo.Builder setAllowedByRobotsTXT(boolean value) {
      validate(fields()[3], value);
      this.allowedByRobotsTXT = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'allowedByRobotsTXT' field has been set */
    public boolean hasAllowedByRobotsTXT() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'allowedByRobotsTXT' field */
    public sparqles.avro.discovery.QueryInfo.Builder clearAllowedByRobotsTXT() {
      fieldSetFlags()[3] = false;
      return this;
    }

    /** Gets the value of the 'Results' field */
    public java.util.List<java.lang.CharSequence> getResults() {
      return Results;
    }
    
    /** Sets the value of the 'Results' field */
    public sparqles.avro.discovery.QueryInfo.Builder setResults(java.util.List<java.lang.CharSequence> value) {
      validate(fields()[4], value);
      this.Results = value;
      fieldSetFlags()[4] = true;
      return this; 
    }
    
    /** Checks whether the 'Results' field has been set */
    public boolean hasResults() {
      return fieldSetFlags()[4];
    }
    
    /** Clears the value of the 'Results' field */
    public sparqles.avro.discovery.QueryInfo.Builder clearResults() {
      Results = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    @Override
    public QueryInfo build() {
      try {
        QueryInfo record = new QueryInfo();
        record.URL = fieldSetFlags()[0] ? this.URL : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.Operation = fieldSetFlags()[1] ? this.Operation : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.Exception = fieldSetFlags()[2] ? this.Exception : (java.lang.CharSequence) defaultValue(fields()[2]);
        record.allowedByRobotsTXT = fieldSetFlags()[3] ? this.allowedByRobotsTXT : (java.lang.Boolean) defaultValue(fields()[3]);
        record.Results = fieldSetFlags()[4] ? this.Results : (java.util.List<java.lang.CharSequence>) defaultValue(fields()[4]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
