/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.ui.component;

import java.time.LocalTime;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.shared.Registration;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

/**
 * A custom field for displaying a time - renders an hour combo box and a minute
 * combo box
 *
 */
public class TimeField extends CustomField<LocalTime> {

    private static final long serialVersionUID = -676425827861766118L;

    private static final int HOURS = 24;

    private static final int MINUTES = 60;

    private AttributeModel attributeModel;

    private final ComboBox<Integer> hourSelect;

    private final ComboBox<Integer> minuteSelect;

    /**
     * Constructor
     *
     * @param textField      the text field that this component wraps around
     * @param attributeModel the attribute model used to construct the compoent
     * @param editable       whether to display the field in editable mode
     */
    public TimeField(AttributeModel attributeModel) {
        this.attributeModel = attributeModel;
        hourSelect = new ComboBox<>();
        hourSelect.setSizeFull();
        hourSelect.setItemCaptionGenerator(t -> StringUtils.leftPad(t.toString(), 2, '0'));
        minuteSelect = new ComboBox<>();
        minuteSelect.setSizeFull();
        minuteSelect.setItemCaptionGenerator(t -> StringUtils.leftPad(t.toString(), 2, '0'));
    }

    @Override
    protected Component initContent() {
        ResponsiveLayout main = new ResponsiveLayout();

        ResponsiveRow row = ResponsiveUtil.createRowWithSpacing();
        main.addComponent(row);

        setCaption(attributeModel.getDisplayName(VaadinUtils.getLocale()));
        hourSelect.setItems(IntStream.range(0, HOURS).boxed());
        minuteSelect.setItems(IntStream.range(0, MINUTES).boxed());

        row.addColumn().withDisplayRules(DynamoConstants.HALF_COLUMNS, DynamoConstants.HALF_COLUMNS, DynamoConstants.HALF_COLUMNS,
                DynamoConstants.HALF_COLUMNS).withComponent(hourSelect);
        row.addColumn().withDisplayRules(DynamoConstants.HALF_COLUMNS, DynamoConstants.HALF_COLUMNS, DynamoConstants.HALF_COLUMNS,
                DynamoConstants.HALF_COLUMNS).withComponent(minuteSelect);

        return main;
    }

    @Override
    protected void doSetValue(LocalTime value) {
        if (value == null) {
            hourSelect.clear();
            minuteSelect.setValue(0);
        } else {
            hourSelect.setValue(value.getHour());
            minuteSelect.setValue(value.getMinute());
        }
    }

    @Override
    public LocalTime getValue() {
        if (hourSelect.getValue() == null || minuteSelect.getValue() == null) {
            return null;
        }
        int hour = hourSelect.getValue();
        int minute = minuteSelect.getValue();
        return LocalTime.of(hour, minute);
    }

    @Override
    public Registration addValueChangeListener(final ValueChangeListener<LocalTime> listener) {
        hourSelect.addValueChangeListener(valueChangeEvent -> listener.valueChange(new ValueChangeEvent<>(this, this, null, false)));
        return minuteSelect
                .addValueChangeListener(valueChangeEvent -> listener.valueChange(new ValueChangeEvent<>(this, this, null, false)));
    }

    public ComboBox<Integer> getHourSelect() {
        return hourSelect;
    }

    public ComboBox<Integer> getMinuteSelect() {
        return minuteSelect;
    }

}
