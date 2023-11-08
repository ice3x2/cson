package com.snoworca.cson.serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class SchemaElementNode implements SchemaNode {
    private SchemaNode parent;
    private List<SchemaField> parentSchemaFieldList = new ArrayList<>();

    /**
     * 브런치 노드. 참조된 필드가 없는 노드.
     */
    protected boolean isBranchNode = true;


    protected boolean isBranchNode() {
        return isBranchNode;
    }

    @SuppressWarnings("unchecked")
    protected <T extends SchemaElementNode> T setBranchNode(boolean branchNode) {
        isBranchNode = branchNode;
        this.onBranchNode(branchNode);
        return (T) this;
    }


    public SchemaElementNode() {}

    public SchemaNode getParent() {
        return parent;
    }


    protected abstract void onBranchNode(boolean branchNode);


    public SchemaElementNode setParent(SchemaNode parent) {
        this.parent = parent;
        return this;
    }

    protected List<SchemaField> getParentSchemaFieldList() {
        return parentSchemaFieldList;
    }

    protected void setParentSchemaFieldList(List<SchemaField> parentSchemaFieldList) {
        this.parentSchemaFieldList = parentSchemaFieldList;
    }

    public SchemaElementNode addParentFieldRack(SchemaField parentFieldRack) {
        if(this.parentSchemaFieldList.contains(parentFieldRack)) {
            return this;
        }
        this.parentSchemaFieldList.add(parentFieldRack);
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
