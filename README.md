# cson
  - API usage is similar to JSON-JAVA (https://github.com/stleary/JSON-java) 
  - Data structures that can be represented in JSON can be serialized(deserialized) in binary format. The binary structure can have a smaller data size than the json string type, and has a performance advantage in parsing.
  - JSON string type conversion is supported through the toString() method  Code to parse json data in String type uses 'JSONTokener.java' of JSON-JAVA.


## Usage (gradle)
```groovy
dependencies {
    implementation group: 'com.snoworca', name: 'cson', version: '0.0.1'
}
```

  
# 