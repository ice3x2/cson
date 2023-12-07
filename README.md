# cson
  - JAVA 1.6 or higher environment is supported. 
  - API usage is similar to JSON-JAVA (https://github.com/stleary/JSON-java) 
  - Data structures that can be represented in JSON can be serialized(or deserialized) in binary format. The binary structure can have a smaller data size than the json string type, and has a performance advantage in parsing.
  - Supports [JSON5](https://json5.org/) format. Comments are parsed alongside the data..
  - Provides object serialization and deserialization using Java annotations. In addition to this, Collection and Map can also be used for JSON type serialization and deserialization.

## Usage (gradle)
```groovy
dependencies {
    implementation group: 'com.snoworca', name: 'cson', version: '0.0.1'
}
```


## Basic usage (JSON)
* Pure JSON is the easiest to use. This usage method is the same as [JAVA-JSON](https://github.com/stleary/JSON-java).
   ```java
    CSONObject obj = new CSONObject();
    // You can change the format options. 
    // default is StringFormatOption.jsonPure()
    // CSONObject obj = new CSONObject(StringFormatOption.jsonPure());
    
    obj.put("name", "John");
    obj.put("age", 25);
    CSONArray friends = new CSONArray();
    friends.put("Nancy");
    friends.put("Mary");
    friends.put("Tom", "Jerry");
    obj.put("friends", friends);
    String json = obj.toString();
    // {"name":"John","age":25,"friends":["Nancy","Mary","Tom","Jerry"]}
  
    // Parsing
    CSONObject parsed = new CSONObject(json);
    String name = parsed.getString("name");
    // If "age" does not exist or cannot be converted to String, it returns "unknown".
    String age = parsed.optString("age", "unknown");
    ```
* If you want to print the json string pretty, do as follows.
   ```java
    JSONOptions jsonOptions = StringFormatOption.json();
    jsonOptions.setPretty(true);
    // Only arrays can be displayed on one line.
    jsonOptions.setUnprettyArray(true);
    String prettyJSONString = csonObjectPure.toString(jsonOptions);
    //{
    //  "name": "John",
    //  "age": 25,
    //  "friends": [ "Nancy", "Mary", "Tom", "Jerry" ]
    //}
    ```
   
## JSON5 usage
  * JSON5 is a superset of JSON that allows comments, trailing commas, and more. key names can be unquoted if they’re valid identifiers. Single and multi-line strings are allowed, as well as escaped newlines, tabs, and Unicode characters in strings and names.
    ```java
    CSONObject obj = new CSONObject(StringFormatOption.json5());
  
    // You can change the default options. (It will be applied to all CSONObject and CONSArray)
    // CSONObject.setDefaultJSONOptions(StringFormatOption.json5());
    // Even if you change the default options, you can specify the options when creating the object.
  
    obj.put("name", "John");
    obj.put("age", 25);
    CSONArray friends = new CSONArray();
    friends.put("Nancy");
    friends.put("Mary");
    friends.put("Tom", "Jerry");
    obj.put("friends", friends);
  
    // You can add comments before and after the key, or before and after the value.
    obj.setCommentForKey("friends", "Lists only people's names.");
    obj.setCommentAfterValue("friends", "A total of 4 friends");
  
    obj.setCommentThis("This is a comment for this object.");
    obj.setCommentAfterThis("This is a comment after this object.");
  
    String yourInfo = obj.toString();
    System.out.println(yourInfo);
    //  //This is a comment for this object.
    //  {
    //      name:'John',
    //      age:25,
    //      //Lists only people's names.
    //      friends:['Nancy','Mary','Tom','Jerry']/* A total of 4 friends */
    //  }
    //  //This is a comment after this object.
    ```       
## Binary conversion
  * The binary format is a format that can be converted to a byte array. It is a format that can be used for object serialization and deserialization.
    ```java
    CSONObject obj = new CSONObject();
    obj.put("name", "John");
    obj.put("age", 25);
    //...
    byte[] bytes = obj.toBytes();
    CSONObject parsed = new CSONObject(bytes, 0, bytes.length);
    ```
## CSON Path
 * CSONPath is a way to access specific values in a CSON object. This is similar to XPath for XML. Or equivalent to Javascript syntax.
   ```java
    String json5 = "{user: { name: 'John',  age: 25,  friends: [ 'Nancy', 'Mary', 'Tom', 'Jerry' ], addr: { city: 'seoul', zipCode: '06164'  } }}";
    CSONObject user = new CSONObject(json5, JSONOptions.json5());
    String firstFriend = user.getCsonPath().optString("user.friends[0]");
    String city = user.getCsonPath().optString("user.addr.city");
    System.out.println("firstFriend: "  + firstFriend);
    System.out.println("city: "  + city);
    // firstFriend: Nancy
    // city: seoul
    
    user.getCsonPath().put("user.friends[4]", "Suji");
    user.getCsonPath().put("user.addr.city", "Incheon");
    
    System.out.println(user);
    // {"user":{"name":"John","age":25,"friends":["Nancy","Mary","Tom","Jerry","Suji"],"addr":{"city":"Incheon","zipCode":"06164"}}}
   ```
## Object serialization/deserialization
  * You can use the annotation to serialize and deserialize the object. 
  ```java
    @CS
  ```
  * However, there are some conditions.
    1. Should have a default constructor 'if possible'. It doesn’t matter if it’s private.
    2. Collection and Map cannot use RAW type. You must use generics.
    3. You can nest a Collection within a Collection. However, Map cannot be placed inside Collection. If you want to put a map inside a Collection, put a class that wraps Map.
    4. The Key of Map must be of 'String' type. Collection and MAP cannot be used as values. If you need to put a Collection in a Map, create a class that wraps the Collection.
    5. Beware of circular references. For example, if classes A and B both declare fields of each other's types, you could end up in an infinite loop!!
    6. @CSONValueSetter can return anything. But it must have one parameter.
    7. @CSONValueGetter must have no parameters. But it must have a return value.
    8. Object serialization/deserialization also includes values from parent classes. Use with caution.




