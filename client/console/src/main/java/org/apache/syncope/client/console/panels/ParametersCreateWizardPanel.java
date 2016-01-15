package org.apache.syncope.client.console.panels;

import java.io.Serializable;
import org.apache.syncope.client.console.wizards.AjaxWizardBuilder;
import org.apache.syncope.common.lib.to.AttrTO;
import org.apache.syncope.common.lib.to.PlainSchemaTO;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.wicket.PageReference;
import org.apache.wicket.extensions.wizard.WizardModel;

public class ParametersCreateWizardPanel extends AjaxWizardBuilder<ParametersCreateWizardPanel.ParametersForm> {

    private static final long serialVersionUID = -2868592590785581481L;

    public ParametersCreateWizardPanel(final String id, final ParametersForm defaultItem, PageReference pageRef) {
        super(id, defaultItem, pageRef);
    }

    @Override
    protected WizardModel buildModelSteps(final ParametersForm modelObject, final WizardModel wizardModel) {
        wizardModel.add(new ParametersCreateWizardSchemaStep(ParametersForm.class.cast(modelObject)));
        wizardModel.add(new ParametersCreateWizardAttrStep(ParametersForm.class.cast(modelObject)));
        return wizardModel;
    }

    @Override
    protected void onCancelInternal(final ParametersForm modelObject) {

    }

    @Override
    protected Serializable onApplyInternal(final ParametersForm modelObject) {
        System.out.println("> > > " + modelObject.getPlainSchemaTO().getType());
        System.out.println("> > > " + modelObject.getPlainSchemaTO().isMultivalue());
        System.out.println("> > > " + modelObject.getAttrTO().getSchema());
        System.out.println("> > > " + modelObject.getAttrTO().getValues());
//        final PlainSchemaTO plainSchemaTO = modelObject.getPlainSchemaTO();
//        final AttrTO attrTO = modelObject.getAttrTO();
        return null;
    }

    public static class ParametersForm implements Serializable {

        private static final long serialVersionUID = 412294016850871853L;

        private final PlainSchemaTO plainSchemaTO;

        private final AttrTO attrTO;

        public ParametersForm() {
            plainSchemaTO = new PlainSchemaTO();
            attrTO = new AttrTO();
        }

        public PlainSchemaTO getPlainSchemaTO() {
            return plainSchemaTO;
        }

        public AttrTO getAttrTO() {
            return attrTO;
        }

    }
}
