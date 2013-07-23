package org.ends.ui.client.page.dimension;

import org.ends.ui.client.ICom;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;

public abstract class DimensionPage extends SimplePanel {
	
	protected ICom com=null;

	public DimensionPage(ICom com, String dimensionName, String dimensionDescriptionHTML){
		this.com=com;
		
		//update the description
		this.com.getDescriptionPanel().add(new HTML("<span class='dimensionName'>"+dimensionName+" - </span><span>"+dimensionDescriptionHTML+"</span>"));
	}
}
