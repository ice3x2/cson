package com.snoworca.cson.serializer;

import java.lang.reflect.Constructor;
import java.util.List;

interface SchemaArrayValue extends SchemaValue {

     Types getValueType();

     TypeElement getObjectValueTypeElement();

     List<CollectionItems> getCollectionItems();





     static boolean equalsCollectionTypes(List<CollectionItems> a, List<CollectionItems> b) {
            if(a.size() != b.size()) {
                return false;
            }
            for(int i = 0; i < a.size(); i++) {
                Constructor<?> aConstructor = a.get(i).collectionConstructor;
                Constructor<?> bConstructor = b.get(i).collectionConstructor;

                if(!aConstructor.equals(bConstructor)) {
                    return false;
                }
            }
            return true;
     }



}
