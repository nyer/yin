package org.yinwang.yin.ast;

import org.yinwang.yin.Constants;
import org.yinwang.yin.Scope;
import org.yinwang.yin._;
import org.yinwang.yin.value.RecordType;
import org.yinwang.yin.value.Value;

import java.util.List;
import java.util.Map;


public class RecordDef extends Node {
    public Name name;
    public List<Name> parents;
    public Scope propertyForm;
    public Scope properties;


    public RecordDef(Name name, List<Name> parents, Scope propertyForm,
                     String file, int start, int end, int line, int col)
    {
        super(file, start, end, line, col);
        this.name = name;
        this.parents = parents;
        this.propertyForm = propertyForm;
    }


    public Value interp(Scope s) {
        Scope properties = Declare.evalProperties(propertyForm, s);

        if (parents != null) {
            for (Node p : parents) {
                Value pv = p.interp(s);
                if (pv instanceof RecordType) {
                    Scope parentProps = ((RecordType) pv).properties;

                    // check for duplicated keys
                    for (String key : parentProps.keySet()) {
                        Value existing = properties.lookupLocal(key);
                        if (existing != null) {
                            _.abort(p, "conflicting field " + key +
                                    " inherited from parent: " + p + ", value: " + pv);
                            return null;
                        }
                    }

                    // add all properties or all fields in parent
                    properties.putAll(parentProps);
                } else {
                    _.abort(p, "parent is not a record");
                    return null;
                }
            }
        }

        return new RecordType(name.id, this, properties);
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.TUPLE_BEGIN);
        sb.append(Constants.RECORD_KEYWORD).append(" ");
        sb.append(name);

        if (parents != null) {
            sb.append(" (" + Node.printList(parents) + ")");
        }

        for (String field : propertyForm.keySet()) {
            Map<String, Object> props = propertyForm.lookupAllProps(field);
            for (Map.Entry<String, Object> e : props.entrySet()) {
                sb.append(" :" + e.getKey() + " " + e.getValue());
            }
        }

        sb.append(Constants.TUPLE_END);
        return sb.toString();
    }
}
