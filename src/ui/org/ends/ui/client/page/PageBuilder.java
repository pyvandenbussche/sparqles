package org.ends.ui.client.page;

import org.ends.ui.client.ICom;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;

public abstract class PageBuilder extends SimplePanel {
	
	protected ICom com=null;

	public PageBuilder(ICom com, String mainTitle, String descriptionHTML){
		this.com=com;
		
		//update the description
		this.com.getDescriptionPanel().add(new HTML("<span class='dimensionName'>"+mainTitle+" - </span><span>"+descriptionHTML+"</span>"));
	}
}
