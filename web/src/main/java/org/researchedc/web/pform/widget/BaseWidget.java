package org.researchedc.web.pform.widget;

import javax.sql.DataSource;

import org.researchedc.bean.core.ItemDataType;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.bean.submit.ItemGroupBean;
import org.researchedc.web.pform.dto.Bind;
import org.researchedc.web.pform.dto.UserControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseWidget implements Widget {

    protected final Logger log = LoggerFactory.getLogger(BaseWidget.class);
	private DataSource ds = null;

	@Override
	public abstract UserControl getUserControl();
	
	@Override
	public abstract Bind getBinding();

	protected String getDataType(ItemBean item)
	{
		String type = ItemDataType.get(item.getItemDataTypeId()).getName();
		
		switch(type)
		{
		case "st": return "string";
		case "int": return "int";
		case "date": return "date";
		case "real": return "float";
		case "bl": return "boolean";
		//TODO: "BN","ED","TEL",FILE"
		case "pdate":
		default: 
			log.debug("Unsupported item data type encountered.  Returning null.");
			System.out.println("Unsupported item data type encountered: " + type + ".  Returning null.");
			return null;
		}
	}
}
