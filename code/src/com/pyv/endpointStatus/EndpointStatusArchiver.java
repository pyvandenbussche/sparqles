package com.pyv.endpointStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.n3.N3Writer;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.trig.TriGWriter;
import org.openrdf.rio.trix.TriXWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.memory.MemoryStore;

public class EndpointStatusArchiver {
	
	private static String TRIPLE_STORE_URL= null;
	private static String OUTPUT_ARCHIVES_PATH = null;
	
	public static void main(String[] args){
		
		/* LOAD PROPERTIES */
	 	try {
			Properties prop = new Properties();
			String fileName = "endpointStatus.config";
			InputStream is = new FileInputStream(fileName);
			prop.load(is);
			TRIPLE_STORE_URL= prop.getProperty("TRIPLE_STORE_URL");
			OUTPUT_ARCHIVES_PATH= prop.getProperty("OUTPUT_ARCHIVES_PATH");
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		/* Historisation des triplets fenetre 1 mois */
		new EndpointStatusArchiver(TRIPLE_STORE_URL,OUTPUT_ARCHIVES_PATH,1);
		
	}
	
	public  EndpointStatusArchiver(String tripleStoreURI, String outputDir,int monthWindow){
		extractAll(TRIPLE_STORE_URL,outputDir);
	}
	
	
	private void archiveLastMonth(String tripleStoreURI, String outputDir){
//		tripleStoreURI="http://labs.mondeca.com/endpoint/ends";
		 Calendar begin = Calendar.getInstance();
		 Calendar end = Calendar.getInstance();
		 
		 begin.set(2012, 3, 1);
		 end.set(2012, 6, 1);
		 
		 		 
		 begin.set(Calendar.HOUR_OF_DAY,0);
		 begin.set(Calendar.MINUTE,0);
		 begin.set(Calendar.SECOND, 0);
		 
		 end.set(Calendar.HOUR_OF_DAY,0);
		 end.set(Calendar.MINUTE,0);
		 end.set(Calendar.SECOND, 0);
		 
		 
//		 end.add(Calendar.MONTH, -monthWindow);
//		 begin.add(Calendar.MONTH, -monthWindow);
		 DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+01:00'");
		 DateFormat sdf2 = new SimpleDateFormat("yyyy-MM");
		 		 
		 end.add(Calendar.MONTH, -1);
		 String filePath = outputDir+"archive_"+sdf2.format(begin.getTime())+"_to_"+sdf2.format(end.getTime())+".n3";
		 String fileZipPath = outputDir+"archive_"+sdf2.format(begin.getTime())+"_to_"+sdf2.format(end.getTime())+".zip";
		 end.add(Calendar.MONTH, +1);
		 System.out.println(filePath);
		 File file = new File(filePath);
		 File fileZip = new File(fileZipPath);
		 
		 
		 
		 try {
				
				HTTPRepository rep = new HTTPRepository(tripleStoreURI);
				rep.initialize();
				RepositoryConnection con = rep.getConnection();
		 
			 /* fetch the list of dataset URI */
			 List<String> datasetList = new ArrayList<String>();
			 StringBuilder query= new StringBuilder();
//			 query.append("SELECT DISTINCT ?datasetURI  WHERE { ?datasetURI a <http://rdfs.org/ns/void#Dataset>.} ORDER BY ?datasetURI"); 
			 query.append("SELECT DISTINCT ?datasetURI  WHERE { ?datasetURI ends:status ?status.} ORDER BY ?datasetURI"); 
			 
			 TupleQueryResult resultTuple = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ query.toString()).evaluate();	
				while (resultTuple.hasNext()) {
					BindingSet bindingSet = resultTuple.next();
					String datasetURI= bindingSet.getBinding("datasetURI").getValue().toString();
//					if(!datasetURI.endsWith("-8000"))datasetList.add(datasetURI);
					datasetList.add(datasetURI);
				}
				
			 /* for each dataset, build a graph */ 
				
			Repository memRepository = new SailRepository(new MemoryStore());
			memRepository.initialize();
			RepositoryConnection memCon = memRepository.getConnection();
			int cpt=0;
			for (int i = 0; i < datasetList.size(); i++) {
				
				String datasetURI = datasetList.get(i);
				System.out.println(datasetURI);
//				query= new StringBuilder();
//				query.append(" CONSTRUCT{<"+datasetURI+"> "+ "ends:status ?status.\n");
//				query.append("<"+datasetURI+">"+ " rdf:type ?dType. \n");
//				query.append("<"+datasetURI+">"+ " void:sparqlEndpoint ?dEndpoint. \n");
//				query.append("<"+datasetURI+">"+ " dcterms:title ?dTitle. \n");
//				query.append("<"+datasetURI+">"+ " dcterms:identifier ?dIdentifier. \n");
//				query.append(" ?status dcterms:date ?statusDate.\n");
//				query.append(" ?status ends:statusIsAvailable ?avail.\n");
//				query.append(" ?status dcterms:description ?descr.\n");
//				query.append(" ?status ends:responseTime ?avail.\n");
//				query.append(" ?status a ends:EndpointStatus.\n");
//				query.append(" }WHERE{\n");
//				query.append("<"+datasetURI+">"+ " ends:status ?status. \n");
//				query.append("?status dcterms:date ?statusDate. \n");
//				query.append("FILTER ( ?statusDate < \""+sdf.format(end.getTime())+"\"^^xsd:dateTime && ?statusDate >= \""+sdf.format(begin.getTime())+"\"^^xsd:dateTime) \n");
//				query.append("<"+datasetURI+">"+ " rdf:type ?dType. \n");
//				query.append("<"+datasetURI+">"+ " void:sparqlEndpoint ?dEndpoint. \n");
//				query.append("<"+datasetURI+">"+ " dcterms:title ?dTitle. \n");
//				query.append("<"+datasetURI+">"+ " dcterms:identifier ?dIdentifier. \n");
//				query.append("?status ends:statusIsAvailable ?avail. \n");
//				query.append("OPTIONAL{?status dcterms:description ?descr.} \n");
//				query.append("OPTIONAL{?status ends:responseTime ?avail.} \n}");
//				
////				query.append(" DELETE DATA{\n");
////				query.append("<"+datasetURI+"> "+ "?dp ?do." +
////						" <"+datasetURI+">"+ " ends:status ?status. \n");
////				query.append("?status ?sp ?so. \n}");
//				
//				System.out.println(query);
//				System.out.println(i+" / "+datasetList.size());
//				GraphQueryResult graphQueryResult = con.prepareGraphQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ query.toString()).evaluate();
//				memCon.add(graphQueryResult, (Resource)null);
			}
//			System.out.println(datasetList.size());
		 
		 
		 /* write the graph in a file */
		 
		 if(!fileZip.exists()){
			if(file.exists())file.delete();
			file.createNewFile();
			 
			WriteRepositoryToFile(file, memCon, RDFFormat.N3);
			
			//zip file
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(fileZip));
			FileInputStream in = new FileInputStream(file);
			byte[] buf = new byte[4096];
			// Add ZIP entry to output stream.
			out.putNextEntry(new ZipEntry(file.getName()));
			
			// Transfer bytes from the file to the ZIP file
			int len;
			while ((len = in.read(buf)) > 0) {
			    out.write(buf, 0, len);
			}
			
			// Complete the entry
			out.closeEntry();
			in.close();
			out.flush();
			out.close();
			
			System.out.println("Archivage du dernier mois effectue");
								
		        
		 }
		        
		        
		        
		        
//				//remove old statements
//		        int cptdel=0;
//		        System.out.println("nombre statements a supprimer: "+graph.size());
//		        con.setAutoCommit(false);
//		        for ( Iterator<Statement> st = graph.iterator(); st.hasNext(); ) {
//		        	Statement s = st.next();
//		        	con.remove(s, s.getContext() );
//		        	cptdel++;
////		        	System.out.println("nombre statements supprimes: "+cptdel);
//		        	if(cptdel%100000==0){
//		        		con.commit();
//		        		System.out.println("nombre statements supprimes: "+cptdel);
//		        	}
//				}
//		        con.commit();
		        
//		        con.remove(graph, ((Resource)null) );
//				con.commit();
//			System.out.println("Suppression dernier mois effectue");
			
			con.close();
			file.delete();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void extractAll(String tripleStoreURI, String outputDir){
		try {
			
			Calendar begin = Calendar.getInstance();
			 Calendar end = Calendar.getInstance();
			 
			 begin.set(2012, 6, 1);
			 end.set(2012, 9, 1);
			 
			 		 
			 begin.set(Calendar.HOUR_OF_DAY,0);
			 begin.set(Calendar.MINUTE,0);
			 begin.set(Calendar.SECOND, 0);
			 
			 end.set(Calendar.HOUR_OF_DAY,0);
			 end.set(Calendar.MINUTE,0);
			 end.set(Calendar.SECOND, 0);
			 
			 
//			 end.add(Calendar.MONTH, -monthWindow);
//			 begin.add(Calendar.MONTH, -monthWindow);
			 DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+01:00'");
			 DateFormat sdf2 = new SimpleDateFormat("yyyy-MM");
			 		 
			 end.add(Calendar.MONTH, -1);
			 String filePath = outputDir+"archive_"+sdf2.format(begin.getTime())+"_to_"+sdf2.format(end.getTime())+".n3";
			 String fileZipPath = outputDir+"archive_"+sdf2.format(begin.getTime())+"_to_"+sdf2.format(end.getTime())+".zip";
			 end.add(Calendar.MONTH, +1);
			 System.out.println(filePath);
			 File file = new File(filePath);
			 File fileZip = new File(fileZipPath);
			
		
			
				HTTPRepository rep = new HTTPRepository(tripleStoreURI);
				rep.initialize();
				RepositoryConnection con = rep.getConnection();
			 
				 /* fetch the list of dataset URI */
				 List<String> datasetList = new ArrayList<String>();
				 StringBuilder query= new StringBuilder();
				 query.append("SELECT DISTINCT ?datasetURI  WHERE { ?datasetURI a <http://rdfs.org/ns/void#Dataset>.} ORDER BY ?datasetURI"); 
//				 query.append("SELECT DISTINCT ?datasetURI  WHERE { ?datasetURI ends:status ?status.} ORDER BY ?datasetURI"); 
				 
				 TupleQueryResult resultTuple = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ query.toString()).evaluate();	
					while (resultTuple.hasNext()) {
						BindingSet bindingSet = resultTuple.next();
						String datasetURI= bindingSet.getBinding("datasetURI").getValue().toString();
		//				if(!datasetURI.endsWith("-8000"))datasetList.add(datasetURI);
						datasetList.add(datasetURI);
						System.out.println(datasetURI);
					}
					
				 /* for each dataset, build a graph */ 
					
				Repository memRepository = new SailRepository(new MemoryStore());
				memRepository.initialize();
				RepositoryConnection memCon = memRepository.getConnection();
				int cpt=0;
				for (int i = 0; i < datasetList.size(); i++) {
					
					String datasetURI = datasetList.get(i);
					System.out.println(datasetURI);
					query= new StringBuilder();
					query.append(" CONSTRUCT{<"+datasetURI+"> "+ "ends:status ?status.\n");
					query.append("<"+datasetURI+">"+ " rdf:type ?dType. \n");
					query.append("<"+datasetURI+">"+ " void:sparqlEndpoint ?dEndpoint. \n");
					query.append("<"+datasetURI+">"+ " dcterms:title ?dTitle. \n");
					query.append("<"+datasetURI+">"+ " dcterms:identifier ?dIdentifier. \n");
					query.append(" ?status dcterms:date ?statusDate.\n");
					query.append(" ?status ends:statusIsAvailable ?avail.\n");
					query.append(" ?status dcterms:description ?descr.\n");
					query.append(" ?status ends:responseTime ?respTime.\n");
					query.append(" ?status a ends:EndpointStatus.\n");
					query.append(" }WHERE{\n");
					query.append("<"+datasetURI+">"+ " ends:status ?status. \n");
					query.append("?status dcterms:date ?statusDate. \n");
					query.append("FILTER ( ?statusDate < \""+sdf.format(end.getTime())+"\"^^xsd:dateTime && ?statusDate >= \""+sdf.format(begin.getTime())+"\"^^xsd:dateTime) \n");
					query.append("<"+datasetURI+">"+ " rdf:type ?dType. \n");
					query.append("<"+datasetURI+">"+ " void:sparqlEndpoint ?dEndpoint. \n");
					query.append("<"+datasetURI+">"+ " dcterms:title ?dTitle. \n");
					query.append("<"+datasetURI+">"+ " dcterms:identifier ?dIdentifier. \n");
					query.append("?status ends:statusIsAvailable ?avail. \n");
					query.append("OPTIONAL{?status dcterms:description ?descr.} \n");
					query.append("OPTIONAL{?status ends:responseTime ?respTime.} \n}");

					System.out.println(query);
					System.out.println(i+" / "+datasetList.size());
					GraphQueryResult graphQueryResult = con.prepareGraphQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ query.toString()).evaluate();
					memCon.add(graphQueryResult, (Resource)null);
				}
		//		System.out.println(datasetList.size());
			 
			 
			 /* write the graph in a file */
			 
			 if(!fileZip.exists()){
				if(file.exists())file.delete();
				file.createNewFile();
				 
				WriteRepositoryToFile(file, memCon, RDFFormat.N3);
				
				//zip file
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(fileZip));
				FileInputStream in = new FileInputStream(file);
				byte[] buf = new byte[4096];
				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(file.getName()));
				
				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
				    out.write(buf, 0, len);
				}
				
				// Complete the entry
				out.closeEntry();
				in.close();
				out.flush();
				out.close();
				file.delete();
				
				System.out.println("Archivage total effectue");
									
			        
			 }
		 con.close();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		 
	
	
	
	public static void WriteRepositoryToFile(File file ,RepositoryConnection con, RDFFormat format){
		try {
			
			if(file.exists())file.delete();
			file.createNewFile();
			FileOutputStream fileOut = new FileOutputStream(file);
			RDFWriter writer=null;
			if(format==null)writer = new RDFXMLWriter(fileOut);
			else if(format.equals(RDFFormat.RDFXML))writer = new RDFXMLWriter(fileOut);
			else if(format.equals(RDFFormat.N3))writer = new N3Writer(fileOut);
			else if(format.equals(RDFFormat.NTRIPLES))writer = new NTriplesWriter(fileOut);
			else if(format.equals(RDFFormat.TURTLE))writer = new TurtleWriter(fileOut);
			else if(format.equals(RDFFormat.TRIX))writer = new TriXWriter(fileOut);
			else if(format.equals(RDFFormat.TRIG))writer = new TriGWriter(fileOut);
//						rdfwriter.setBaseURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			 
			con.export(writer, ((Resource)null) );
			fileOut.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
