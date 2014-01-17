/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.web.page.admin.users.component;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismReference;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.AjaxButton;
import com.evolveum.midpoint.web.component.AjaxSubmitButton;
import com.evolveum.midpoint.web.component.data.ObjectDataProvider;
import com.evolveum.midpoint.web.component.data.TablePanel;
import com.evolveum.midpoint.web.component.data.column.LinkColumn;
import com.evolveum.midpoint.web.page.PageBase;
import com.evolveum.midpoint.web.page.admin.users.dto.OrgTableDto;
import com.evolveum.midpoint.web.util.WebModelUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.OrgType;
import org.apache.commons.lang.Validate;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lazyman
 */
public class OrgUnitBrowser extends ModalWindow {

    public static enum Operation {MOVE, ADD, REMOVE}

    private static final Trace LOGGER = TraceManager.getTrace(OrgUnitBrowser.class);

    private static final String DOT_CLASS = OrgUnitBrowser.class.getName() + ".";
    private static final String OPERATION_LOAD_PARENT_ORG_REFS = DOT_CLASS + "loadParentOrgRefs";

    private static final String ID_SEARCH_TEXT = "searchText";
    private static final String ID_SEARCH = "search";
    private static final String ID_TABLE = "table";
    private static final String ID_MAIN_FORM = "mainForm";
    private static final String ID_CANCEL = "cancel";
    private static final String ID_CREATE_ROOT = "createRoot";

    private boolean initialized;
    private Operation operation;

    /**
     * Objects which were selected on page, not in this dialog. It's used for example to filter org. units
     * when removing from hierarchy. In that case we don't want to show list of all org. units, just org.
     * units which are parents to selected.
     */
    private List<OrgTableDto> selected;

    private IModel<String> searchModel = new Model<String>();

    public OrgUnitBrowser(String id) {
        super(id);

        setTitle(createStringResource("OrgUnitBrowser.title"));
        setCssClassName(ModalWindow.CSS_CLASS_GRAY);
        setCookieName(OrgUnitBrowser.class.getSimpleName() + ((int) (Math.random() * 100)));
        setInitialWidth(900);
        setInitialHeight(530);
        setWidthUnit("px");

        WebMarkupContainer content = new WebMarkupContainer(getContentId());
        setContent(content);
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        Validate.notNull(operation);
        this.operation = operation;
    }

    private PageBase getPageBase() {
        return (PageBase) getPage();
    }

    public StringResourceModel createStringResource(String resourceKey, Object... objects) {
        return new StringResourceModel(resourceKey, this, null, resourceKey, objects);
    }

    public void setSelectedObjects(List<OrgTableDto> selected) {
        this.selected = selected;
    }

    public List<OrgTableDto> getSelected() {
        return selected;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();

        if (initialized) {
            return;
        }

        initLayout((WebMarkupContainer) get(getContentId()));
        initialized = true;
    }

    private void initLayout(WebMarkupContainer container) {
        Form form = new Form(ID_MAIN_FORM);
        container.add(form);

        TextField searchText = new TextField(ID_SEARCH_TEXT, searchModel);
        form.add(searchText);

        AjaxSubmitButton search = new AjaxSubmitButton(ID_SEARCH,
                createStringResource("OrgUnitBrowser.search")) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                searchPerformed(target);
            }
        };
        form.add(search);

        ObjectDataProvider<OrgTableDto, OrgType> provider = new ObjectDataProvider<OrgTableDto, OrgType>(this, OrgType.class) {

            @Override
            public OrgTableDto createDataObjectWrapper(PrismObject<OrgType> obj) {
                return OrgTableDto.createDto(obj);
            }

            @Override
            public ObjectQuery getQuery() {
                return createQueryFromSelected();
            }
        };
        List<IColumn<OrgTableDto, String>> columns = initColumns();
        TablePanel table = new TablePanel(ID_TABLE, provider, columns);
        container.add(table);

        AjaxButton cancel = new AjaxButton(ID_CANCEL,
                createStringResource("OrgUnitBrowser.cancel")) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                cancelPerformed(target);
            }
        };
        container.add(cancel);

        AjaxButton createRoot = new AjaxButton(ID_CREATE_ROOT,
                createStringResource("OrgUnitBrowser.createRoot")) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                createRootPerformed(target);
            }
        };
        container.add(createRoot);
    }

    private ObjectQuery createQueryFromSelected() {
        if (selected == null) {
            return null;
        }

        OperationResult result = new OperationResult(OPERATION_LOAD_PARENT_ORG_REFS);
        List<String> oids = new ArrayList<String>();
        try {
            for (OrgTableDto dto : selected) {
                PrismObject object = WebModelUtils.loadObject(dto.getType(), dto.getOid(),
                        WebModelUtils.createOptionsForParentOrgRefs(), result, getPageBase());
                PrismReference parentRef = object.findReference(OrgType.F_PARENT_ORG_REF);
                if (parentRef != null) {
                    for (PrismReferenceValue value : parentRef.getValues()) {
                        oids.add(value.getOid());
                    }
                }
            }
        } catch (Exception ex) {
            LoggingUtils.logException(LOGGER, "Couldn't load parent org. refs for selected objects", ex);
            result.recordFatalError("Couldn't load parent org. refs for selected objects.", ex);
        } finally {
            result.computeStatusIfUnknown();
        }

        //todo here we must create query which select object which have oid from oids list [lazyman]
        //todo create IN(oids) filter in schema, objectfilter and repo implementation
        return null;
    }

    private List<IColumn<OrgTableDto, String>> initColumns() {
        List<IColumn<OrgTableDto, String>> columns = new ArrayList<IColumn<OrgTableDto, String>>();

        columns.add(new LinkColumn<OrgTableDto>(createStringResource("ObjectType.name"), "name") {

            @Override
            public void onClick(AjaxRequestTarget target, IModel<OrgTableDto> rowModel) {
                rowSelected(target, rowModel, operation);
            }
        });
        columns.add(new PropertyColumn<OrgTableDto, String>(createStringResource("OrgType.displayName"),
                OrgTableDto.F_DISPLAY_NAME));
        columns.add(new PropertyColumn<OrgTableDto, String>(createStringResource("OrgType.identifier"),
                OrgTableDto.F_IDENTIFIER));

        return columns;
    }

    protected void createRootPerformed(AjaxRequestTarget target) {

    }

    protected void rowSelected(AjaxRequestTarget target, IModel<OrgTableDto> row, Operation operation) {

    }

    private void searchPerformed(AjaxRequestTarget target) {

    }

    private void cancelPerformed(AjaxRequestTarget target) {
        close(target);
    }
}