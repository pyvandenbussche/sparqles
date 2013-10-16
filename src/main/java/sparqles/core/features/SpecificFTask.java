package sparqles.core.features;

import java.util.ArrayList;
import java.util.List;

import sparqles.core.Endpoint;
import sparqles.core.EndpointResult;

public enum  SpecificFTask {
/*
*/
SPARQL1_ASK 			("sparql10/ASK[.].rq"),
SPARQL1_CON 			("sparql10/CON[.].rq"),
SPARQL1_CONJOIN 		("sparql10/CON[JOIN].rq"),
SPARQL1_CONOPT 			("sparql10/CON[OPT].rq"),
SPARQL1_SELDISTINCT 	("sparql10/SEL-DISTINCT[.].rq"),
SPARQL1_SELREDUCED 		("sparql10/SEL-REDUCED[.].rq"),
SPARQL1_SEL 			("sparql10/SEL[.].rq"),
SPARQL1_SELBNODE 		("sparql10/SEL[BNODE].rq"),
SPARQL1_SELEMPTY 		("sparql10/SEL[EMPTY].rq"),
SPARQL1_SELFILBOUND 	("sparql10/SEL[FIL(!BOUND)].rq"),
SPARQL1_SELFILBLANK 	("sparql10/SEL[FIL(BLANK)].rq"),
SPARQL1_SELFILBOOL 		("sparql10/SEL[FIL(BOOL)].rq"),
SPARQL1_SELFILIRI 		("sparql10/SEL[FIL(IRI)].rq.rq"),
SPARQL1_SELFILNUM 		("sparql10/SEL[FIL(NUM)].rq"),
SPARQL1_SELFILREGEXI 	("sparql10/SEL[FIL(REGEX-i)].rq"),
SPARQL1_SELFILREGEX 	("sparql10/SEL[FIL(REGEX)].rq"),
SPARQL1_SELFILSTR 		("sparql10/SEL[FIL(STR)].rq"),
SPARQL1_SELFROM 		("sparql10/SEL[FROM].rq"),
SPARQL1_SELGRAPHJOIN 	("sparql10/SEL[GRAPH;JOIN].rq"),
SPARQL1_SELGRAPHUNION 	("sparql10/SEL[GRAPH;UNION].rq"),
SPARQL1_SELGRAPH 		("sparql10/SEL[GRAPH].rq"),
SPARQL1_SELJOIN 		("sparql10/SEL[JOIN].rq"),
SPARQL1_SELOPT 			("sparql10/SEL[OPT].rq"),
SPARQL1_SELUNION 		("sparql10/SEL[UNION].rq"),
SPARQL1_SELORDERBYASC 		("sparql10/SEL[.]*ORDERBY-ASC.rq"),
SPARQL1_SELORDERBYDESC 		("sparql10/SEL[.]*ORDERBY-DESC.rq"),
SPARQL1_SELORDERBY		("sparql10/SEL[.]*ORDERBY.rq"),
SPARQL1_SELORDERBYOFFSET		("sparql10/SEL[.]*ORDERBY*OFFSET.rq"),



SPARQL11_ASKFILNIN   	("sparql11/ASK[FIL(!IN)].rq"),
SPARQL11_CON   			("sparql11/CON-[.].rq"),
SPARQL11_SELAVG   		("sparql11/SEL[AVG].rq"),
SPARQL11_SELBIND   		("sparql11/SEL[BIND].rq"),
SPARQL11_SELFILNEXISTS  ("sparql11/SEL[FIL(!EXISTS)].rq"),
SPARQL11_SELFILABS   	("sparql11/SEL[FIL(ABS)].rq"),
SPARQL11_SELFILCONTAINS ("sparql11/SEL[FIL(CONTAINS)].rq"),
SPARQL11_SELFILEXISTS   ("sparql11/SEL[FIL(EXISTS)].rq"),
SPARQL11_SELFILSTART   	("sparql11/SEL[FIL(START)].rq"),
SPARQL11_SELMAX   		("sparql11/SEL[MAX].rq"),
SPARQL11_SELMIN   		("sparql11/SEL[MIN].rq"),
SPARQL11_SELMINUS   	("sparql11/SEL[MINUS].rq"),
SPARQL11_SELPATH   		("sparql11/SEL[PATHS].rq"),
SPARQL11_SELSERVICE   	("sparql11/SEL[SERVICE].rq"),
SPARQL11_SELSUBQGRAPH   ("sparql11/SEL[SUBQ;GRAPH].rq"),
SPARQL11_SELSUBQ   		("sparql11/SEL[SUBQ].rq"),
SPARQL11_SELSUM   		("sparql11/SEL[SUM].rq"),
SPARQL11_SELVALUES   	("sparql11/SEL[VALUES].rq"),
SPARQL11_SELAVGGROUPBY   	("sparql11/SEL[AVG]*GROUPBY.rq"),
SPARQL11_SELCOUNTGROUPBY   	("sparql11/SEL[COUNT]*GROUPBY.rq");




   
    private String query;

	private SpecificFTask(String query){
        this.query = query;
    }
   
    public String toString(){
        return query;
        
    }
   
    public FRun get(Endpoint ep){
        return new FRun(ep, query);
    }
   
    public static List<FRun> allTasks(Endpoint ep){
    	List<FRun> res = new ArrayList<FRun>();
    	
    	for (SpecificFTask action : values()){
    		res.add(action.get(ep));
    	}
    	return res;
    }

	public FRun get(EndpointResult epr) {
		return new FRun(epr.getEndpoint(), query, epr.getStart());
	}
}