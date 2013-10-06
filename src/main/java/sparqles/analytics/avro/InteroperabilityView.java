/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package sparqles.analytics.avro;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class InteroperabilityView extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"InteroperabilityView\",\"namespace\":\"sparqles.analytics.avro\",\"fields\":[{\"name\":\"endpoint\",\"type\":{\"type\":\"record\",\"name\":\"Endpoint\",\"namespace\":\"sparqles.core\",\"fields\":[{\"name\":\"uri\",\"type\":\"string\"},{\"name\":\"datasets\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"Dataset\",\"fields\":[{\"name\":\"uri\",\"type\":\"string\"},{\"name\":\"label\",\"type\":\"string\"}]}}}]}},{\"name\":\"nbCompliantSPARQL1Features\",\"type\":\"int\"},{\"name\":\"nbCompliantSPARQL11Features\",\"type\":\"int\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public sparqles.core.Endpoint endpoint;
  @Deprecated public int nbCompliantSPARQL1Features;
  @Deprecated public int nbCompliantSPARQL11Features;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public InteroperabilityView() {}

  /**
   * All-args constructor.
   */
  public InteroperabilityView(sparqles.core.Endpoint endpoint, java.lang.Integer nbCompliantSPARQL1Features, java.lang.Integer nbCompliantSPARQL11Features) {
    this.endpoint = endpoint;
    this.nbCompliantSPARQL1Features = nbCompliantSPARQL1Features;
    this.nbCompliantSPARQL11Features = nbCompliantSPARQL11Features;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return endpoint;
    case 1: return nbCompliantSPARQL1Features;
    case 2: return nbCompliantSPARQL11Features;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: endpoint = (sparqles.core.Endpoint)value$; break;
    case 1: nbCompliantSPARQL1Features = (java.lang.Integer)value$; break;
    case 2: nbCompliantSPARQL11Features = (java.lang.Integer)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'endpoint' field.
   */
  public sparqles.core.Endpoint getEndpoint() {
    return endpoint;
  }

  /**
   * Sets the value of the 'endpoint' field.
   * @param value the value to set.
   */
  public void setEndpoint(sparqles.core.Endpoint value) {
    this.endpoint = value;
  }

  /**
   * Gets the value of the 'nbCompliantSPARQL1Features' field.
   */
  public java.lang.Integer getNbCompliantSPARQL1Features() {
    return nbCompliantSPARQL1Features;
  }

  /**
   * Sets the value of the 'nbCompliantSPARQL1Features' field.
   * @param value the value to set.
   */
  public void setNbCompliantSPARQL1Features(java.lang.Integer value) {
    this.nbCompliantSPARQL1Features = value;
  }

  /**
   * Gets the value of the 'nbCompliantSPARQL11Features' field.
   */
  public java.lang.Integer getNbCompliantSPARQL11Features() {
    return nbCompliantSPARQL11Features;
  }

  /**
   * Sets the value of the 'nbCompliantSPARQL11Features' field.
   * @param value the value to set.
   */
  public void setNbCompliantSPARQL11Features(java.lang.Integer value) {
    this.nbCompliantSPARQL11Features = value;
  }

  /** Creates a new InteroperabilityView RecordBuilder */
  public static sparqles.analytics.avro.InteroperabilityView.Builder newBuilder() {
    return new sparqles.analytics.avro.InteroperabilityView.Builder();
  }
  
  /** Creates a new InteroperabilityView RecordBuilder by copying an existing Builder */
  public static sparqles.analytics.avro.InteroperabilityView.Builder newBuilder(sparqles.analytics.avro.InteroperabilityView.Builder other) {
    return new sparqles.analytics.avro.InteroperabilityView.Builder(other);
  }
  
  /** Creates a new InteroperabilityView RecordBuilder by copying an existing InteroperabilityView instance */
  public static sparqles.analytics.avro.InteroperabilityView.Builder newBuilder(sparqles.analytics.avro.InteroperabilityView other) {
    return new sparqles.analytics.avro.InteroperabilityView.Builder(other);
  }
  
  /**
   * RecordBuilder for InteroperabilityView instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<InteroperabilityView>
    implements org.apache.avro.data.RecordBuilder<InteroperabilityView> {

    private sparqles.core.Endpoint endpoint;
    private int nbCompliantSPARQL1Features;
    private int nbCompliantSPARQL11Features;

    /** Creates a new Builder */
    private Builder() {
      super(sparqles.analytics.avro.InteroperabilityView.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(sparqles.analytics.avro.InteroperabilityView.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.endpoint)) {
        this.endpoint = data().deepCopy(fields()[0].schema(), other.endpoint);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.nbCompliantSPARQL1Features)) {
        this.nbCompliantSPARQL1Features = data().deepCopy(fields()[1].schema(), other.nbCompliantSPARQL1Features);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.nbCompliantSPARQL11Features)) {
        this.nbCompliantSPARQL11Features = data().deepCopy(fields()[2].schema(), other.nbCompliantSPARQL11Features);
        fieldSetFlags()[2] = true;
      }
    }
    
    /** Creates a Builder by copying an existing InteroperabilityView instance */
    private Builder(sparqles.analytics.avro.InteroperabilityView other) {
            super(sparqles.analytics.avro.InteroperabilityView.SCHEMA$);
      if (isValidValue(fields()[0], other.endpoint)) {
        this.endpoint = data().deepCopy(fields()[0].schema(), other.endpoint);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.nbCompliantSPARQL1Features)) {
        this.nbCompliantSPARQL1Features = data().deepCopy(fields()[1].schema(), other.nbCompliantSPARQL1Features);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.nbCompliantSPARQL11Features)) {
        this.nbCompliantSPARQL11Features = data().deepCopy(fields()[2].schema(), other.nbCompliantSPARQL11Features);
        fieldSetFlags()[2] = true;
      }
    }

    /** Gets the value of the 'endpoint' field */
    public sparqles.core.Endpoint getEndpoint() {
      return endpoint;
    }
    
    /** Sets the value of the 'endpoint' field */
    public sparqles.analytics.avro.InteroperabilityView.Builder setEndpoint(sparqles.core.Endpoint value) {
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
    public sparqles.analytics.avro.InteroperabilityView.Builder clearEndpoint() {
      endpoint = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'nbCompliantSPARQL1Features' field */
    public java.lang.Integer getNbCompliantSPARQL1Features() {
      return nbCompliantSPARQL1Features;
    }
    
    /** Sets the value of the 'nbCompliantSPARQL1Features' field */
    public sparqles.analytics.avro.InteroperabilityView.Builder setNbCompliantSPARQL1Features(int value) {
      validate(fields()[1], value);
      this.nbCompliantSPARQL1Features = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'nbCompliantSPARQL1Features' field has been set */
    public boolean hasNbCompliantSPARQL1Features() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'nbCompliantSPARQL1Features' field */
    public sparqles.analytics.avro.InteroperabilityView.Builder clearNbCompliantSPARQL1Features() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'nbCompliantSPARQL11Features' field */
    public java.lang.Integer getNbCompliantSPARQL11Features() {
      return nbCompliantSPARQL11Features;
    }
    
    /** Sets the value of the 'nbCompliantSPARQL11Features' field */
    public sparqles.analytics.avro.InteroperabilityView.Builder setNbCompliantSPARQL11Features(int value) {
      validate(fields()[2], value);
      this.nbCompliantSPARQL11Features = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'nbCompliantSPARQL11Features' field has been set */
    public boolean hasNbCompliantSPARQL11Features() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'nbCompliantSPARQL11Features' field */
    public sparqles.analytics.avro.InteroperabilityView.Builder clearNbCompliantSPARQL11Features() {
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    public InteroperabilityView build() {
      try {
        InteroperabilityView record = new InteroperabilityView();
        record.endpoint = fieldSetFlags()[0] ? this.endpoint : (sparqles.core.Endpoint) defaultValue(fields()[0]);
        record.nbCompliantSPARQL1Features = fieldSetFlags()[1] ? this.nbCompliantSPARQL1Features : (java.lang.Integer) defaultValue(fields()[1]);
        record.nbCompliantSPARQL11Features = fieldSetFlags()[2] ? this.nbCompliantSPARQL11Features : (java.lang.Integer) defaultValue(fields()[2]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
