/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table;

import java.beans.PropertyDescriptor;
import org.apache.syncope.common.lib.to.BulkActionResult;
import org.apache.syncope.common.lib.to.BulkActionResult.Status;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

public class ActionResultColumn<T, S> extends AbstractColumn<T, S> {

    private static final long serialVersionUID = 7955560320949560716L;

    private static final Logger LOG = LoggerFactory.getLogger(ActionResultColumn.class);

    private final BulkActionResult results;

    private final String keyFieldName;

    public ActionResultColumn(final BulkActionResult results, final String keyFieldName) {
        super(new Model<String>());
        this.results = results;
        this.keyFieldName = keyFieldName;
    }

    @Override
    public String getCssClass() {
        return "bulkResultColumn";
    }

    @Override
    public Component getHeader(final String componentId) {
        return new Label(componentId, new ResourceModel("bulkActionResultLabel", "Result"));
    }

    @Override
    public void populateItem(final Item<ICellPopulator<T>> item, final String componentId, final IModel<T> rowModel) {
        try {
            PropertyDescriptor propDesc =
                    BeanUtils.getPropertyDescriptor(rowModel.getObject().getClass(), keyFieldName);
            Object id = propDesc.getReadMethod().invoke(rowModel.getObject(), new Object[0]);
            Status status = id == null ? null : results.getResults().get(id.toString());
            item.add(new Label(componentId, status == null ? Status.SUCCESS : status.toString()));
        } catch (Exception e) {
            LOG.error("Errore retrieving target id value", e);
        }
    }
}
