/*
 *  Copyright (C) 2011 Axel Morgner, structr <structr@structr.org>
 * 
 *  This file is part of structr <http://structr.org>.
 * 
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.ui.page.admin;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.click.Control;
import org.apache.click.control.Checkbox;
import org.apache.click.control.Field;
import org.apache.click.control.FieldSet;
import org.apache.click.control.Form;
import org.apache.click.control.HiddenField;
import org.apache.click.control.Submit;
import org.apache.click.control.TextField;
import org.apache.click.extras.control.DateField;
import org.apache.click.extras.control.DoubleField;
import org.apache.click.extras.control.IntegerField;
import org.apache.click.extras.control.LongField;
import org.apache.click.extras.control.NumberField;
import org.apache.click.util.ContainerUtils;
import org.structr.core.Command;
import org.structr.core.Services;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.ArbitraryNode;
import org.structr.core.entity.NodeType;
import org.structr.core.node.StructrTransaction;
import org.structr.core.node.TransactionCommand;

/**
 *
 * @author amorgner
 */
public class EditArbitraryNode extends DefaultEdit {

    private final static String prefix = "_";
    protected Form dynamicPropertiesForm = new Form("dynamicPropertiesForm");
    private Set<String> keys;
    private NodeType typeNode;

    public EditArbitraryNode() {

        super();

    }

    @Override
    public void onInit() {

        super.onInit();


        ArbitraryNode arbitraryNode = null;

        if (node != null) {

            externalViewUrl = "/view/" + node.getId();
            localViewUrl = getContext().getRequest().getContextPath().concat(
                    "/view".concat(
                    node.getNodePath().replace("&", "%26")));

            if (node instanceof ArbitraryNode) {
                arbitraryNode = (ArbitraryNode) node;
            }

        }

        if (arbitraryNode == null) {
            return;
        }

        typeNode = arbitraryNode.getTypeNode();
        if (typeNode == null) {
            return;
        }

        Map<String, Class> signature = typeNode.getSignature();

        keys = signature.keySet();



        FieldSet dynamicProperties = new FieldSet("Dynamic Properties");
        for (String key : keys) {

            if (key.startsWith(prefix)) {

                dynamicProperties.add(getControlForClass(key.substring(prefix.length()), typeNode.getStringProperty(key)));
            }

        }

        dynamicPropertiesForm.add(dynamicProperties);
        dynamicPropertiesForm.copyTo(node);

        dynamicPropertiesForm.setActionURL(dynamicPropertiesForm.getActionURL().concat("#properties-tab"));
        if (editPropertiesAllowed) {
            dynamicProperties.add(new Submit("saveDynamicProperties", " Save Dynamic Properties ", this, "onSaveDynamicProperties"));
//            editPropertiesForm.add(new Submit("savePropertiesAndReturn", " Save and Return ", this, "onSaveAndReturn"));
            dynamicProperties.add(new Submit("cancel", " Cancel ", this, "onCancel"));
        }

        editPropertiesPanel.add(dynamicPropertiesForm);
        addControl(dynamicPropertiesForm);


    }

    @Override
    public void onRender() {

        super.onRender();

        if (node != null && typeNode != null) {

            List<Field> fieldList = ContainerUtils.getInputFields(dynamicPropertiesForm);

            for (Field f : fieldList) {

                String fieldName = f.getName();

                if (!f.isHidden() && keys.contains(prefix + fieldName)) {

                    String javaClassName = typeNode.getStringProperty(prefix + fieldName);
                    if (javaClassName != null) {

                        if (javaClassName.equals("java.util.Date")) {
                            f.setValueObject(node.getDateProperty(fieldName));
                        } else {
                            f.setValueObject(node.getProperty(fieldName));
                        }

                    }

                }

                dynamicPropertiesForm.add(new HiddenField(NODE_ID_KEY, nodeId != null ? nodeId : ""));
                dynamicPropertiesForm.add(new HiddenField(RENDER_MODE_KEY, renderMode != null ? renderMode : ""));
                dynamicPropertiesForm.add(new HiddenField(RETURN_URL_KEY, returnUrl != null ? returnUrl : ""));

            }

        }

    }

    private Control getControlForClass(final String name, final String javaClassName) {

        if (javaClassName.equals(String.class.getCanonicalName())) {

            return new TextField(name);

        } else if (javaClassName.equals(Double.class.getCanonicalName())) {

            return new DoubleField(name);

        } else if (javaClassName.equals(Integer.class.getCanonicalName())) {

            return new IntegerField(name);

        } else if (javaClassName.equals(Long.class.getCanonicalName())) {

            if (Date.class.getCanonicalName().equals(javaClassName)) {

                return new DateField(name);

            } else {

                return new LongField(name);

            }

        } else if (javaClassName.equals(Float.class.getCanonicalName())) {

            return new NumberField(name);

        } else if (javaClassName.equals(Boolean.class.getCanonicalName())) {

            return new Checkbox(name);

        } else if (javaClassName.equals(Date.class.getCanonicalName())) {

            return new DateField(name);

        }

        // nothing found
        return null;

    }

    /**
     * Save form data and stay in edit mode
     *
     * @return
     */
    public boolean onSaveDynamicProperties() {

        if (dynamicPropertiesForm.isValid()) {

            saveDynamicNodeProperties();
            okMsg = "Dynamic node parameter successfully saved.";

            return redirect();

        } else {
            return true;
        }
    }

    /**
     * Save dynamic node properties
     */
    protected void saveDynamicNodeProperties() {

        final Command transactionCommand = Services.command(TransactionCommand.class);
        transactionCommand.execute(new StructrTransaction() {

            @Override
            public Object execute() throws Throwable {
                AbstractNode s = getNodeByIdOrPath(getNodeId());

                if (dynamicPropertiesForm.isValid()) {

                    List<Field> fieldList = ContainerUtils.getInputFields(dynamicPropertiesForm);

                    for (Field f : fieldList) {
                        
                        String fieldName = f.getName();
                        
                        if (!f.isHidden() && keys.contains(prefix + fieldName)) {
                            node.setProperty(fieldName, f.getValueObject());
                        }

                    }
                    transactionCommand.setExitCode(Command.exitCode.SUCCESS);
                    okMsg = "Dynamic properties saved successfully";

                } else {

                    transactionCommand.setExitCode(Command.exitCode.FAILURE);
                    errorMsg = "Dynamic properties form is invalid";
                    transactionCommand.setErrorMessage(errorMsg);

                }
                return (null);
            }
        });
    }
}