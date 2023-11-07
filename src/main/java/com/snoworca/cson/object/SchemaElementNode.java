package com.snoworca.cson.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class SchemaElementNode implements SchemaNode {
    private SchemaNode parent;
    private List<SchemaField> parentFieldRackList = new ArrayList<>();

    protected boolean isBranchNode = true;

    protected boolean isBranchNode() {
        return isBranchNode;
    }

    protected void setBranchNode(boolean branchNode) {
        isBranchNode = branchNode;
    }
    public SchemaElementNode() {}

    public SchemaNode getParent() {
        return parent;
    }





    public SchemaElementNode setParent(SchemaNode parent) {
        this.parent = parent;
        return this;
    }

    protected List<SchemaField> getParentFieldRackList() {
        return parentFieldRackList;
    }

    protected void setParentFieldRackList(List<SchemaField> parentFieldRackList) {
        isBranchNode = parentFieldRackList.isEmpty();
        this.parentFieldRackList = parentFieldRackList;
    }

    public SchemaElementNode addParentFieldRack(SchemaField parentFieldRack) {
        if(this.parentFieldRackList.contains(parentFieldRack)) {
            return this;
        }
        isBranchNode = false;
        this.parentFieldRackList.add(parentFieldRack);
        return this;
    }

    public SchemaElementNode addParentFieldRackAll(Collection<SchemaField> parentFieldRackCollection) {
        for(SchemaField parentFieldRack : parentFieldRackCollection) {
            addParentFieldRack(parentFieldRack);
        }
        return this;
    }

    public abstract void merge(SchemaElementNode schemaElementNode);


}
