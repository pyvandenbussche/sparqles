package org.ends.ui.client.page.dimension;

import org.ends.ui.client.ICom;

public class PerformancePage extends DimensionPage {

	public PerformancePage(ICom com){
		super(com,"Performance","To assess the reliability of SPARQL Endpoints, we test hourly (by default) " +
				"the endpoints uptime. In order to accommodate patchy SPARQL compliance, we try two queries to " +
				"test availability for each endpoint: <span class='dimensionDescriptionCode'>ASK WHERE{ ?s ?p ?o . }</span>" +
				" <span class='dimensionDescriptionCode'>SELECT ?s WHERE{ ?s ?p ?o . } LIMIT 1</span> " +
				"If the ASK query fails (e.g., is not supported) we try the SELECT query.");
	}
	
	
}
