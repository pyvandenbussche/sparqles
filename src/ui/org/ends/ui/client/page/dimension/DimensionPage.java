package org.ends.ui.client.page.dimension;

import org.ends.ui.client.ICom;
import org.ends.ui.client.page.PageBuilder;

public abstract class DimensionPage extends PageBuilder {
	

	public DimensionPage(ICom com, String dimensionName, String dimensionDescriptionHTML){
		super(com, dimensionName, dimensionDescriptionHTML);		
	}
}
