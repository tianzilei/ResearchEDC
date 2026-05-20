package org.researchedc.web.pform.widget;

import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.bean.submit.ItemGroupBean;
import org.researchedc.web.pform.dto.Bind;
import org.researchedc.web.pform.dto.Hint;
import org.researchedc.web.pform.dto.Input;
import org.researchedc.web.pform.dto.Label;
import org.researchedc.web.pform.dto.UserControl;

public class SubHeaderWidget extends BaseWidget {
	private ItemBean item = null;
	private ItemFormMetadataBean itemMetaData = null;
	private ItemGroupBean itemGroup = null;
	private CRFVersionBean version = null;
	private String appearance = null;
	private String expression;

	public SubHeaderWidget(CRFVersionBean version, ItemBean item, ItemFormMetadataBean itemMetaData, ItemGroupBean itemGroup,
			String appearance, String expression) {
		this.item = item;
		this.itemMetaData = itemMetaData;
		this.itemGroup = itemGroup;
		this.version = version;
		this.appearance = appearance;
		this.expression = expression;
	}

	@Override
	public UserControl getUserControl() {
		Input input = new Input();
		Label label = new Label();
		label.setLabel(itemMetaData.getSubHeader());
		input.setLabel(label);
		input.setRef("/" + version.getOid() + "/" + itemGroup.getOid() + "/" + item.getOid() + ".SUBHEADER");
		return input;
	}

	@Override
	public Bind getBinding() {

		Bind binding = new Bind();
		binding.setNodeSet("/" + version.getOid() + "/" + itemGroup.getOid() + "/" + item.getOid() + ".SUBHEADER");
		binding.setType("string");
		binding.setReadOnly("true()");
		String relevant = null;
		relevant = expression;
		if (relevant != null)
			binding.setRelevant(relevant);

		return binding;
	}

}
