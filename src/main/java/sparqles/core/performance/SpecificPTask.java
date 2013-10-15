package sparqles.core.performance;

import java.util.ArrayList;
import java.util.List;

import sparqles.core.Endpoint;

public enum  SpecificPTask {
/*
	ask-o.txt
	ask-p.txt
	ask-po.txt
	ask-s.txt
	ask-so.txt
	ask-sp.txt
	ask-spo.txt
	count-class.txt
	count-pred.txt
	join-oo.txt
	join-so.txt
	join-ss.txt
	limit1.txt
	limit100001.txt
	limit12500.txt
	limit25000.txt
	limit3125.txt
	limit5.txt
	limit50000.txt
	limit6250.txt
	
	
	*/
    ASKO ("ask-o.txt"),
    ASKP ("ask-p.txt"),
    ASKPO ("ask-po.txt"),
    ASKS ("ask-s.txt"),
    ASKSO ("ask-so.txt"),
    ASKSP ("ask-sp.txt"),
    ASKSPO ("ask-spo.txt"),
    COUNTCLASS ("count-class.txt"),
    COUNTPRED ("count-pred.txt"),
    JOINOO ("join-oo.txt"),
    JOINSO ("join-so.txt"),
    JOINSS ("join-ss.txt"),
    LIMIT1 ("limit1.txt"),
    LIMIT100K1 ("limit100001.txt"),
    LIMIT12500 ("limit12500.txt"),
    LIMIT25K ("limit25000.txt"),
    LIMIT3125 ("limit3125.txt"),
    LIMIT5 ("limit5.txt"),
    LIMIT50K ("limit50000.txt"),
    LIMIT6250 ("limit6250.txt");
	
   
    private String query;

	private SpecificPTask(String query){
        this.query = query;
    }
   
    public String toString(){
        return query;
        
    }
   
    public PRun get(Endpoint ep){
        return new PRun(ep, query);
    }
   
    public static List<PRun> allTasks(Endpoint ep){
    	List<PRun> res = new ArrayList<PRun>();
    	
    	for (SpecificPTask action : values()){
    		res.add(action.get(ep));
    	}
    	return res;
    }
}