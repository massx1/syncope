package org.apache.syncope.client.console.panels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.client.console.wicket.markup.html.form.AjaxDateFieldPanel;
import org.apache.syncope.client.console.wicket.markup.html.form.AjaxDropDownChoicePanel;
import org.apache.syncope.client.console.wicket.markup.html.form.AjaxTextFieldPanel;
import org.apache.syncope.client.console.wicket.markup.html.form.FieldPanel;
import org.apache.syncope.client.console.wicket.markup.html.form.MultiFieldPanel;
import org.apache.syncope.client.console.wicket.markup.html.form.SpinnerFieldPanel;
import org.apache.syncope.common.lib.SyncopeConstants;
import org.apache.syncope.common.lib.to.AttrTO;
import org.apache.syncope.common.lib.to.PlainSchemaTO;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

public class ParametersCreateWizardAttrStep extends WizardStep {

    private static final long serialVersionUID = -7843275202297616553L;

    private final PlainSchemaTO plainSchemaTO;

    public ParametersCreateWizardAttrStep(final ParametersCreateWizardPanel.ParametersForm modelObject) {
        this.plainSchemaTO = modelObject.getPlainSchemaTO();

        System.out.println(">>>>>>>>>>> " + plainSchemaTO.getType());
        
        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        add(content);

        final AjaxTextFieldPanel schema = new AjaxTextFieldPanel(
                "schema", getString("schema"), new PropertyModel<String>(modelObject.getAttrTO(), "schema"));
        schema.setEnabled(true);
        schema.setVisible(true);
        content.add(schema);

        
        
        final Panel panel = getFieldPanel("panel", modelObject.getAttrTO());
        panel.setEnabled(true);
        panel.setVisible(true);
        content.add(panel);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Panel getFieldPanel(final String id, final AttrTO attrTO) {

        final String valueHeaderName = getString("values");

        final FieldPanel panel;
        switch (plainSchemaTO.getType()) {
            case Date:
                panel = new AjaxDateFieldPanel(
                        id, valueHeaderName, new Model<Date>(), plainSchemaTO.getConversionPattern());
                break;
            case Enum:
                panel = new AjaxDropDownChoicePanel<>(id, valueHeaderName, new Model<String>(), false);
                ((AjaxDropDownChoicePanel<String>) panel).setChoices(getEnumeratedValues(plainSchemaTO));

                if (!attrTO.getValues().isEmpty()) {
                    ((AjaxDropDownChoicePanel) panel).setChoiceRenderer(new IChoiceRenderer<String>() {

                        private static final long serialVersionUID = -3724971416312135885L;

                        final List<String> list = attrTO.getValues();

                        @Override
                        public String getDisplayValue(final String value) {
                            return value;
                        }

                        @Override
                        public String getIdValue(final String value, final int i) {
                            return value;
                        }

                        @Override
                        public String getObject(
                                final String id, final IModel<? extends List<? extends String>> choices) {
                            return id;
                        }
                    });
                }
                break;

            case Long:
                panel = new SpinnerFieldPanel<>(id, valueHeaderName, Long.class, new Model<Long>());
                break;

            case Double:
                panel = new SpinnerFieldPanel<>(id, valueHeaderName, Double.class, new Model<Double>());
                break;

            default:
                panel = new AjaxTextFieldPanel(id, valueHeaderName, new Model<String>(), false);
        }
        if (plainSchemaTO.isMultivalue()) {
            return new MultiFieldPanel.Builder<>(
                    new PropertyModel<List<String>>(attrTO, "values")).build(id, valueHeaderName, panel);
        } else {
            panel.setNewModel(attrTO.getValues());
        }
        return panel;
    }

    private List<String> getEnumeratedValues(final PlainSchemaTO schemaTO) {
        final List<String> res = new ArrayList<>();

        final String[] values = StringUtils.isBlank(schemaTO.getEnumerationValues())
                ? new String[0]
                : schemaTO.getEnumerationValues().split(SyncopeConstants.ENUM_VALUES_SEPARATOR);

        for (String value : values) {
            res.add(value.trim());
        }

        return res;
    }
}
