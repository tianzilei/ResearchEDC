package org.researchedc.web.pform.widget;

import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.bean.submit.ItemGroupBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.web.pform.dto.Bind;
import org.researchedc.web.pform.dto.Hint;
import org.researchedc.web.pform.dto.Input;
import org.researchedc.web.pform.dto.Label;
import org.researchedc.web.pform.dto.UserControl;
import org.springframework.beans.factory.annotation.Autowired;

public class SectionWidget extends BaseWidget {
	private SectionBean section = null;
	private CRFVersionBean version = null;
	private String expression = null;

	public SectionWidget(SectionBean section, CRFVersionBean version, String expression) {
		this.section = section;
		this.version = version;
		this.expression = expression;
	}

	@Override
	public Bind getBinding() {
		Bind binding = new Bind();
		String relevant = expression;
		if (relevant != null)
			binding.setRelevant(relevant);

		binding.setNodeSet("/" + version.getOid() + "/" + "SECTION_"+ section.getLabel().replaceAll("\\W", "_"));
		return binding;
	}

	@Override
	public UserControl getUserControl() {
		// TODO Auto-generated method stub
		return null;
	}

}
