package core.availability;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.openrdf.model.Literal;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;

import com.pyv.endpointStatus.objects.AnalyticsPerWeek;
import com.pyv.endpointStatus.objects.DatasetAnalytics;


public class EndpointStatusAnalytics {
	
	
	private String CKAN_SPARQL_ENDPOINT = null;
	private String CKAN_PACKAGE_PATH = null;
	private String TRIPLE_STORE_URL= null;
	private String TEMPLATE_HTML_PAGE_PATH= null;
	private String TEMPLATE_DETAILS_HTML_PAGE_PATH = null;
	private String OUTPUT_HTML_PAGE_PATH = null;
	private String OUTPUT_STATS_AVAILABILITY = null;
	private String OUTPUT_RSS_PAGE_PATH = null;
	private String OUTPUT_RSS_OVERALL_FEED_NAME = null;
	private String OUTPUT_DETAILS_HTML_PAGE_PATH = null;
	private String OUTPUT_HTML_PAGE_NAME = null;
	private String HOMEPAGE = null;
	
	 public static void main(String[] args) {new EndpointStatusAnalytics();}
	 public EndpointStatusAnalytics() {
		 
		 /*INITIALISATION*/
		 	/* LOAD PROPERTIES */
			 	try {
					Properties prop = new Properties();
					String fileName = "endpointStatus.config";
					InputStream is = new FileInputStream(fileName);
					prop.load(is);
					CKAN_SPARQL_ENDPOINT= prop.getProperty("CKAN_SPARQL_ENDPOINT");
					CKAN_PACKAGE_PATH= prop.getProperty("CKAN_PACKAGE_PATH");
					TRIPLE_STORE_URL= prop.getProperty("TRIPLE_STORE_URL");
					TEMPLATE_HTML_PAGE_PATH= prop.getProperty("TEMPLATE_HTML_PAGE_PATH");
					TEMPLATE_DETAILS_HTML_PAGE_PATH= prop.getProperty("TEMPLATE_DETAILS_HTML_PAGE_PATH");
					OUTPUT_HTML_PAGE_PATH= prop.getProperty("OUTPUT_HTML_PAGE_PATH");
					OUTPUT_STATS_AVAILABILITY= prop.getProperty("OUTPUT_STATS_AVAILABILITY");
					OUTPUT_RSS_PAGE_PATH= prop.getProperty("OUTPUT_RSS_PAGE_PATH");
					OUTPUT_RSS_OVERALL_FEED_NAME= prop.getProperty("OUTPUT_RSS_OVERALL_FEED_NAME");
					OUTPUT_DETAILS_HTML_PAGE_PATH= prop.getProperty("OUTPUT_DETAILS_HTML_PAGE_PATH");
					OUTPUT_HTML_PAGE_NAME= prop.getProperty("OUTPUT_HTML_PAGE_NAME");
					HOMEPAGE= prop.getProperty("HOMEPAGE");
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			 	
			 	
			 	/* Define the timeframe */
			 	
			 	
			 	 Calendar begin = Calendar.getInstance();
				 begin.set(2011,1,1);  
				 begin.set(Calendar.HOUR_OF_DAY,0);
				 begin.set(Calendar.MINUTE,0);
				 begin.set(Calendar.SECOND, 0);
				 
				 Calendar end = Calendar.getInstance();
				 end.set(2012,10,1);
				 
				 
				 
				
//				EvolutionWeekByWeek(begin, end);
				
				List<DatasetAnalytics> datasetsAnalytics =StatPerEndpointPerMonth(begin,end);
//			    Top10(datasetsAnalytics);
//			    DeadEndpointsLastMonth(datasetsAnalytics);
				CSVAvailPerEndpointPerMonth(datasetsAnalytics);
	    }
	 
	 
	
	 
	 
	 /*
	  *	pour chaque semaine, regarde le nombre total d'endpoint et construit un histogramme avec le nbre de gris, rouge, orange et vert. 
	  */
	 private void EvolutionWeekByWeek( Calendar begin,  Calendar end){
		 //store the begin date so we can reinitialize the value afterwards
		 int tempYear = begin.get(Calendar.YEAR);
		 int tempMonth = begin.get(Calendar.MONTH);
		 int tempDay = begin.get(Calendar.DAY_OF_MONTH);
		 
		 try {
			HTTPRepository rep = new HTTPRepository(TRIPLE_STORE_URL);
			rep.initialize();
			RepositoryConnection con = rep.getConnection();
				
			
			 int min = -1;
			 int max = -1;
			 while(begin.before(end)){
				 DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+01:00'");
				 
				 String query="SELECT (count(distinct ?dataset) as ?count) \n" +
					"WHERE{ " +
						"?dataset a void:Dataset. \n" +
						"?dataset ends:status ?status. " +
						"?status dcterms:date ?statusDate. \n" +
						"?status ends:statusIsAvailable ?availability. \n" +
						"FILTER ( ?statusDate > \""+sdf.format(begin.getTime())+"\"^^xsd:dateTime ) \n";
//				 System.out.print(sdf.format(begin.getTime())+"\t");
				 begin.add(Calendar.WEEK_OF_YEAR, 1);
				 	query+="FILTER ( ?statusDate < \""+sdf.format(begin.getTime())+"\"^^xsd:dateTime ) \n"+			 
					"} ORDER BY ?statusDate";
				 	
//				 	System.out.println(query);
				 
				 	TupleQueryResult results = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ query).evaluate();
//				 	 ResultSet resultSet2 = EndpointStatusUtil.executeQuery(EndpointStatusUtil.PREFIXES+query,TRIPLE_STORE_URL);
				 	for ( ; results.hasNext() ; ){
						BindingSet bind = results.next() ;
//						System.out.println(sdf.format(begin.getTime())+"\t"+((Literal)bind.getValue("count")).intValue());
						int nbTotal=((Literal)bind.getValue("count")).intValue();
						if(min==-1 || min>nbTotal)min=nbTotal;
						if(max==-1 || max<nbTotal)max=nbTotal;
					        
						System.out.println("{x:new Date("+begin.get(Calendar.YEAR)+","+begin.get(Calendar.MONTH)+","+begin.get(Calendar.DAY_OF_MONTH)+"), " +
								"y:"+nbTotal+"},");
						
						}
				 	con.close();
			 }
			 System.out.println("var Ymin = "+min+";");
			 System.out.println("var Ymax = "+max+";");
			 		
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		begin.set(Calendar.YEAR,tempYear);
		begin.set(Calendar.MONTH,tempMonth);
		begin.set(Calendar.DAY_OF_MONTH,tempDay);
	 }
	 
	 
	 /*
	  * pour chaque endpoint, regarder chaque semaine date d'introduction. 
	  * date de fin. 
	  * pourcentage dispo par semaine. nbre de tests effectués par semaine
	  *  */
	 private List<DatasetAnalytics> StatPerEndpointPerMonth( Calendar begin,  Calendar end){
		//store the begin date so we can reinitialize the value afterwards
		 int tempYear = begin.get(Calendar.YEAR);
		 int tempMonth = begin.get(Calendar.MONTH);
		 int tempDay = begin.get(Calendar.DAY_OF_MONTH);
		 
		 try {
				HTTPRepository rep = new HTTPRepository(TRIPLE_STORE_URL);
				rep.initialize();
				RepositoryConnection con = rep.getConnection();
				
				List<DatasetAnalytics> datasetsAnalytics = new ArrayList<DatasetAnalytics>(300);
				
				List<String> endpointList = new ArrayList<String>();
				
				//on récupère la liste des datasets
				 String queryDatasets="SELECT distinct ?endpoint \n" +
					"WHERE{ \n" +
					"	   ?dataset void:sparqlEndpoint ?endpoint. "+
					"} ORDER BY ?endpoint";
				 TupleQueryResult resultsDatasets = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ queryDatasets).evaluate();
				 for ( ; resultsDatasets.hasNext() ; ){
						BindingSet bindDataset = resultsDatasets.next() ;
						String endpoint = bindDataset.getValue("endpoint").stringValue().toString();
						if(!endpointList.contains(endpoint.trim()))endpointList.add(endpoint.trim());
				 }
				 System.out.println("endpoint size:"+endpointList.size());
				 
				 for (int i = 0; i < endpointList.size(); i++) {
					String endpoint = endpointList.get(i);
						DatasetAnalytics da = new DatasetAnalytics();
						da.setDatasetURI(endpoint);
						da.setDatasetTitle(endpoint);
						da.setDatasetId(endpoint);
						da.setEndpointURL(endpoint);
						
						 DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+01:00'");
//						 int min = -1;
//						 int max = -1;
						 int nbTestsTot=0;
						 int nbTestsTrueTot=0;
						 System.out.println(endpoint);
						 while(begin.before(end)){
							
							 int nbTestsWeek=0;
							 int nbTestsTrueWeek=0;
							 String query="SELECT ?availability \n" +
								"WHERE{ " +
									"?datasetURI void:sparqlEndpoint \""+endpoint+"\". \n" +
									"?datasetURI ends:status ?status. " +
									"?status dcterms:date ?statusDate. \n" +
									"?status ends:statusIsAvailable ?availability. \n" +
									"FILTER ( ?statusDate > \""+sdf.format(begin.getTime())+"\"^^xsd:dateTime ) \n";
//							 System.out.print(sdf.format(begin.getTime())+"\t");
							 begin.add(Calendar.MONTH, 1);
							 	query+="FILTER ( ?statusDate < \""+sdf.format(begin.getTime())+"\"^^xsd:dateTime ) \n"+			 
								"} ORDER BY ?statusDate";
							 	
//							 	System.out.println(query);
							 
							 	TupleQueryResult results = con.prepareTupleQuery(QueryLanguage.SPARQL, EndpointStatusUtil.PREFIXES+ query).evaluate();
//							 	 ResultSet resultSet2 = EndpointStatusUtil.executeQuery(EndpointStatusUtil.PREFIXES+query,TRIPLE_STORE_URL);
							 	
							 	for ( ; results.hasNext() ; ){
									BindingSet bind = results.next() ;
									nbTestsWeek++;
									nbTestsTot++;
									if(((Literal)bind.getValue("availability")).booleanValue()==true){
										nbTestsTrueTot++;
										nbTestsTrueWeek++;
									}
								}
							 	con.close();
							 	AnalyticsPerWeek a = new AnalyticsPerWeek();
							 	a.setDate(begin.get(Calendar.YEAR)+"_"+begin.get(Calendar.MONTH)+"_"+begin.get(Calendar.DAY_OF_MONTH));
							 	a.setNbTests(nbTestsWeek);
							 	a.setNbTestsTrue(nbTestsTrueWeek);
							 	
							 	da.getAnalytics().add(a);
//							 	System.out.println("{x:new Date("+begin.get(Calendar.YEAR)+","+begin.get(Calendar.MONTH)+","+begin.get(Calendar.DAY_OF_MONTH)+"), y:"+nbTestsWeek+", z:"+nbTestsTrueWeek+"},");
						 }
//						 System.out.println("var nbTestsTot = "+nbTestsTot+";");
//						 System.out.println("var nbTestsTrueTot = "+nbTestsTrueTot+";");
						 da.setNbTotalTests(nbTestsTot);
						 da.setNbTotalTestsTrue(nbTestsTrueTot);
						
						 datasetsAnalytics.add(da);
						 
						 begin.set(Calendar.YEAR,tempYear);
						begin.set(Calendar.MONTH,tempMonth);
						begin.set(Calendar.DAY_OF_MONTH,tempDay);
				 }
					
				 
				return datasetsAnalytics;
				 		
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
	 }
	 
	 
	 
	
	 
	 /*
	  * liste des 10 meilleurs endpoints >1 semaine
	  */
	 private void Top10(List<DatasetAnalytics> datasetsAnalytics){
		 List<DatasetAnalytics> ordered = new ArrayList<DatasetAnalytics>(datasetsAnalytics.size());
		 for(int i=0; i<datasetsAnalytics.size();i++){
			 DatasetAnalytics dBest =  getBestRatio(datasetsAnalytics,ordered);
			 if(dBest!=null)ordered.add(dBest);
			 else break;
		 }
		 NumberFormat f = NumberFormat.getPercentInstance();  
	        f.setMinimumFractionDigits(2);  
	        f.setMaximumFractionDigits(2);
	        System.out.println("DatasetURI\tDatasetId\tDatasetTitle\tdatasetEndpoint\tavailabilityPercentage\tnumberOfTests");
	     StringBuffer sb = new StringBuffer();
	        
		 for(int i=0; i<ordered.size();i++){
			 DatasetAnalytics da = ordered.get(i);
			 System.out.println(da.getDatasetURI()+"\t"+
					 da.getDatasetId()+"\t"+
					 da.getDatasetTitle()+"\t"+
					 da.getEndpointURI()+"\t"+
					 f.format(da.getRatio())+"\t"+
					 da.getNbTotalTests());
			 
			 if(i<20){
				 sb.append("<tr");
				 if(i%2==0)sb.append(" class=\"row_odd\"");
				 sb.append("><td><img src=\"../images/star.png\"></td><td><a href=\"../details/"+da.getDatasetId()+".html\">"+
						 da.getDatasetTitle()+"</a></td><td>"+f.format(da.getRatio())+"</td><td>"+
						 da.getNbTotalTests()+"</td><td><a target=\"_blank\" href=\"../feeds/"+da.getDatasetId()+".xml\">"+
						 "<img width=\"18\" height=\"18\" class=\"rssIMG\" title=\"Subscribe to this endpoint status changements\" " +
						 "src=\"../images/rss.gif\"></a><a target=\"_blank\" href=\""+da.getEndpointURI()+"\">" +
						 "<img class=\"endpointIMG\" title=\"Link to this endpoint URL\" src=\"../images/endpoint.png\"></a>" +
						 "<a target=\"_blank\" href=\""+da.getDatasetURI()+"\"><img class=\"endpointIMG\" " +
						 "title=\"Link to CKAN endpoint information page\" src=\"../images/ckan.png\"></a></td></tr>\n");
			 }
		 }
		 System.out.println(sb.toString());
	 }
	 
	 private DatasetAnalytics getBestRatio(List<DatasetAnalytics> datasetsAnalytics, List<DatasetAnalytics> ordered){
		 DatasetAnalytics dBest = null;
		 for(int i=0; i<datasetsAnalytics.size();i++){
			 DatasetAnalytics da = datasetsAnalytics.get(i);
			 if(dBest==null){
				 if(!ordered.contains(da))dBest=da;
			 }
			 else{
				 if(da.getRatio()>dBest.getRatio() && !ordered.contains(da))dBest=da;
				 else{
					 if(da.getRatio()==dBest.getRatio() && !ordered.contains(da) && da.getNbTotalTests()>dBest.getNbTotalTests())dBest=da;
				 }
			 }
		 }
		 return dBest;
	 }
	 
	 private void CSVAvailPerEndpointPerMonth(List<DatasetAnalytics> datasetsAnalytics){
		 if(datasetsAnalytics.size()>0){
			 StringBuilder output= new StringBuilder();
			 NumberFormat f = NumberFormat.getNumberInstance();  
		        f.setMinimumFractionDigits(4);  
		        f.setMaximumFractionDigits(4);
		        
			 for (int i = 0; i < datasetsAnalytics.size(); i++) {
				 DatasetAnalytics d =  datasetsAnalytics.get(i);
				 output.append("\n"+d.getDatasetURI());
				 
				 for(int j=0; j<d.getAnalytics().size();j++){
					 AnalyticsPerWeek ana = d.getAnalytics().get(j);
					 String date = d.getAnalytics().get(j).getDate();
					 String value = "?";
					 if(ana.getNbTests()>0)value=f.format(((Double.parseDouble(""+ana.getNbTestsTrue())/Double.parseDouble(""+ana.getNbTests())))).replace(",",".");
					 output.append(","+date+","+value);
				 } 
				 
			 }
			 System.out.println(output.toString());
		 }
		
	 }
	 
	 
	 private void trueFalsePerWeek(List<DatasetAnalytics> datasetsAnalytics){
		 if(datasetsAnalytics.size()>0){
			 DatasetAnalytics d =  datasetsAnalytics.get(0);
			 String firstArray= "";
			 String secondArray= "";
			 for(int j=0; j<d.getAnalytics().size();j++){
				 String date = d.getAnalytics().get(j).getDate();
				 int cptTotal = 0;
				 int cpttrue = 0;
				 for(int i=0; i<datasetsAnalytics.size();i++){
					 DatasetAnalytics da =  datasetsAnalytics.get(i);
					 AnalyticsPerWeek apw = da.getAnalyticsWithDate(date);
					 cptTotal+=apw.getNbTests();
					 cpttrue+=apw.getNbTestsTrue();
				 }
				 String[] dateSplit = date.split("_");
				 NumberFormat f = NumberFormat.getNumberInstance();  
			        f.setMinimumFractionDigits(2);  
			        f.setMaximumFractionDigits(2);
			        firstArray+="\t{x:new Date("+dateSplit[0]+","+dateSplit[1]+","+dateSplit[2]+"), " +
							"y:"+f.format(((Double.parseDouble(""+cpttrue)/Double.parseDouble(""+cptTotal)))*100).replace(",",".")+"},\n";
			        secondArray+="\t{x:new Date("+dateSplit[0]+","+dateSplit[1]+","+dateSplit[2]+"), " +
							"y:"+f.format(100-(((Double.parseDouble(""+cpttrue)/Double.parseDouble(""+cptTotal)))*100)).replace(",",".")+"},\n";
				
			 }
			 System.out.println("var data = ["+secondArray+"\t];");
		 }
	 }
	 
	 private void DeadEndpointsLastMonth(List<DatasetAnalytics> datasetsAnalytics){
		 StringBuffer sb = new StringBuffer();
		 int cpt=0;
		 for(int i=0; i<datasetsAnalytics.size();i++){
			 DatasetAnalytics da = datasetsAnalytics.get(i);
			 
			 if(da.getAnalytics().size()>4){
				 AnalyticsPerWeek a1 = da.getAnalytics().get(da.getAnalytics().size()-1);
				 AnalyticsPerWeek a2 = da.getAnalytics().get(da.getAnalytics().size()-2);
				 AnalyticsPerWeek a3 = da.getAnalytics().get(da.getAnalytics().size()-3);
				 AnalyticsPerWeek a4 = da.getAnalytics().get(da.getAnalytics().size()-4);
			 	if(a1.getNbTests()+a2.getNbTests()+a3.getNbTests()+a4.getNbTests()>0
			 			&& a1.getNbTestsTrue()+a2.getNbTestsTrue()+a3.getNbTestsTrue()+a4.getNbTestsTrue()==0){
			 		 sb.append("<tr");
					 if(cpt%2==0)sb.append(" class=\"row_odd\"");
					 sb.append("><td><img src=\"../images/warning.png\"></td><td><a href=\"../details/"+da.getDatasetId()+".html\">"+
							 da.getDatasetTitle()+"</a></td><td><a target=\"_blank\" href=\"../feeds/"+da.getDatasetId()+".xml\">"+
							 "<img width=\"18\" height=\"18\" class=\"rssIMG\" title=\"Subscribe to this endpoint status changements\" " +
							 "src=\"../images/rss.gif\"></a><a target=\"_blank\" href=\""+da.getEndpointURI()+"\">" +
							 "<img class=\"endpointIMG\" title=\"Link to this endpoint URL\" src=\"../images/endpoint.png\"></a>" +
							 "<a target=\"_blank\" href=\""+da.getDatasetURI()+"\"><img class=\"endpointIMG\" " +
							 "title=\"Link to CKAN endpoint information page\" src=\"../images/ckan.png\"></a></td></tr>\n");
					 cpt++;
			 	}
			 }
		 }
		 
		 System.out.println(sb.toString());
	 }
}

