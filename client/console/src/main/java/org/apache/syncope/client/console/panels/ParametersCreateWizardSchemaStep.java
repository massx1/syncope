package org.apache.syncope.client.console.panels;

import java.util.Arrays;
import org.apache.syncope.client.console.wicket.markup.html.form.AjaxCheckBoxPanel;
import org.apache.syncope.client.console.wicket.markup.html.form.AjaxDropDownChoicePanel;
import org.apache.syncope.common.lib.types.AttrSchemaType;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.PropertyModel;

public class ParametersCreateWizardSchemaStep extends WizardStep {

    private static final long serialVersionUID = -7843275202297616553L;

    public ParametersCreateWizardSchemaStep(final ParametersCreateWizardPanel.ParametersForm modelObject) {
        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        add(content);

        final AjaxDropDownChoicePanel<AttrSchemaType> type = new AjaxDropDownChoicePanel<>(
                "type", getString("type"), new PropertyModel<AttrSchemaType>(modelObject.getPlainSchemaTO(), "type"));
        type.setChoices(Arrays.asList(AttrSchemaType.values()));
        type.setEnabled(true);
        type.setVisible(true);
        content.add(type);
        
        final AjaxCheckBoxPanel multiValue = new AjaxCheckBoxPanel("panel", getString("multivalue"),
                new PropertyModel<Boolean>(modelObject.getPlainSchemaTO(), "multivalue"), false);
        multiValue.setEnabled(true);
        multiValue.setVisible(true);
        content.add(multiValue);
    }
}
