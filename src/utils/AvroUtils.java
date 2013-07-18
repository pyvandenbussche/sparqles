package utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;

public class AvroUtils {

        private static Map<String, Schema> schemas = new HashMap<String, Schema>();
	
        private AvroUtils(){}
	
        public static void addSchema(String name, Schema schema){
               schemas.put(name, schema);

        }

        public static Schema getSchema(String name){
               return schemas.get(name);

        }

        public static String resolveSchema(String sc){

                String result = sc;
                for(Map.Entry<String, Schema> entry : schemas.entrySet())
                      result = replace(result, entry.getKey(),
                                        entry.getValue().toString());
                return result;

        }

        static String replace(String str, String pattern, String replace) {

                int s = 0;
                int e = 0;
                StringBuffer result = new StringBuffer();
                while ((e = str.indexOf(pattern, s)) >= 0) {
                result.append(str.substring(s, e));
                result.append(replace);
                s = e+pattern.length();

        }
        result.append(str.substring(s));
        return result.toString();

}

public static Schema parseSchema(String schemaString){

        String completeSchema = resolveSchema(schemaString);
        Schema schema = Schema.parse(completeSchema);
        String name = schema.getFullName();
        schemas.put(name, schema);
        return schema;

}

public static Schema parseSchema(InputStream in)throws IOException {

    StringBuffer out = new StringBuffer();
    byte[] b = new byte[4096];
    for (int n; (n = in.read(b)) != -1;) {
     out.append(new String(b, 0, n));
    }
    return parseSchema(out.toString());

}

public static Schema parseSchema(File file)throws IOException {

        FileInputStream fis = new FileInputStream(file);
        return parseSchema(fis);
    }
}