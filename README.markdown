![Iron Cushion logo](http://mgp.github.com/assets/images/iron-cushion.png)

Iron Cushion is a benchmark and load testing tool for [CouchDB](http://couchdb.apache.org/), developed by [adhoclabs](http://adhoclabs.co). It proceeds in two steps: First, documents are bulk inserted using CouchDB's [Bulk Document API](http://wiki.apache.org/couchdb/HTTP_Bulk_Document_API). Second, documents are individually created, read, updated, and deleted with random ordering of operations using CouchDB's [Document API](http://wiki.apache.org/couchdb/HTTP_Document_API). Below we refer to the former as the "bulk insert step," and the latter as the "CRUD operations step." Statistics for both steps are recorded separately and displayed afterward.

It is written in [Java](http://www.java.com) for version 5.0 and higher, depends only on the [Netty library](http://netty.io), and is released under the [MIT license](http://www.opensource.org/licenses/mit-license.html).

## Command Line Flags

* `database_address`: The address of the database, of the form `http://[hostname]:[port]` or `http://[ipaddress]:[port]`.
* `database_name`: The name of the database.
* `num_connections`: The number of concurrent connections to establish to the database.
* `json_document_schema_file`: A file containing JSON describing the schema of documents created during the benchmark.
* `xml_document_schema_file`: An file containing XML describing the schema of documents created during the benchmark.
* `seed`: An optional integer flag that specifies the seed to use for the random number generator.

Either `json_document_schema_file` or `xml_document_schema_file` must be provided. For details on the contents of these files, see "Document Generation" below.

### Bulk Insert Flags

The following flags control the bulk insert step:

* `num_documents_per_bulk_insert`: The number of documents in each bulk insert operation.
* `num_bulk_insert_operations`: The number of bulk insert operations performed by each connection.

For example, if `num_connections` is `50`, `num_documents_per_bulk_insert` is `1000`, and `num_bulk_insert_operations` is `20`, then after the bulk insert step there will be 50 x 1,000 x 20 = 1,000,000 documents in the database.

### CRUD Flags

The following flags control the CRUD operations step:

* `num_crud_operations`: The number of CRUD operations performed by each connection.
* `create_weight`: Weight defining the number of create operations relative to other operations.
* `read_weight`: Weight defining the number of read operations relative to other operations.
* `update_weight`: Weight defining the number of update operations relative to other operations.
* `delete_weight`: Weight defining the number of delete operations relative to other operations.

For example, if `create_weight` is `2`, `read_weight` is `3`, `update_weight` is `2`, and `delete_weight` is `1`, then 2/8 of all CRUD operations will be create operations, 3/8 of all CRUD operations will be read operations, 2/8 of all CRUD operations will be update operations, and 1/8 of all CRUD operations will be delete operations. If `num_crud_operations` is `10000`, this equals 2,500 create operations, 3,750 read operations, 2,500 update operations, and 1,250 delete operations per connection.

Every update and delete operation requires the `_rev` value of a document. Such a value comes from either reading the document from the database earlier, or from creating the document earlier and recording the returned value. Therefore the sum `create_weight + read_weight` must be greater than or equal to both `update_weight` and `delete_weight`. If not, the flags fail validation. Furthermore, if `delete_weight` is large enough such that the number of documents to be deleted exceeds the sum of number of documents bulk inserted and the number of documents created from CRUD operations, the flags fail validation. To remedy this, bulk insert more documents, increase `create_weight`, or decrease `delete_weight`.

## Document Generation

Note that while CouchDB is schemaless, Iron Cushion requires a schema to serve as a template for generated documents that are inserted during the bulk insert step, or inserted or updated during the CRUD operations step. This allows the user to easily control their level of complexity. A schema can be defined either using JSON or XML, but you will likely find the former easier.

Subject to the quality of the pseudo-random number generator, generated values adhere to the following rules:

* Boolean values have the same likelihood of being `true` or `false`.
* Integer values follow a uniform distribution.
* Floating point values follow a uniform distribution between `0.0` and `1.0`.
* Strings are between 1 and 5 words in length, where adjacent words are separated by a space. Words are chosen from a dictionary of 32,768 words. Each word is between 3 and 15 characters in length and follows the alphabet `[A-Za-z0-9?!]`. The chosen number of words in a string, the length of each word, and each character in each word all follow uniform distributions.

### JSON

The `json_document_schema_file` command line flag specifies a file containing JSON that defines a schema for documents in the database.

A new document is generated from the schema by the following rules:

* Any boolean value read is replaced by a randomly generated boolean value.
* Any integer value read is replaced by a randomly generated integer value.
* Any floating point value read is replaced by a randomly generated floating point value.
* Any string value read is replaced by a randomly generated string.

All other properties of the JSON, such as array lengths, value names in objects, `null` values, and nested types are preserved. The advantage of using a JSON schema is that **any document stored in CouchDB is a valid schema for Iron Cushion**. Iron Cushion will automatically remove the special `_id` and `_rev` values from the JSON schema provided.

An example can be found in `iron-cushion/iron-cushion/data/example_schema.json`. Its schema is equivalent to the one in `iron-cushion/iron-cushion/data/example_schema.xml`.

### XML

The `xml_document_schema_file` command line flag specifies an file containing XML that defines a schema for documents in the database. In the future, the XML file may allow adding attributes to these tags to specify properties like minimum and maximum values for generated integers, etc.

There are seven principal tags, and the outer-most tag must be `<object>`:

* The `<object>` tag translates to a JSON object containing name-value pairs. The `<object>` tag encloses 0 or more `<entry>` tags to define name-value pairs. Each `<entry>` tag contains a `<name>` and `<value>` tag. The name of the value, which must be a string, is enclosed by the `<name>` tag. The type of the value is enclosed by the `<value>` tag.
* The `<array>` tag translates to an JSON array. The `<array>` tag encloses 0 or more `<element>` tags to define its elements. The type of each element is enclosed by its `<element>` tag.
* The `<boolean />` tag translates to a generated boolean value.
* The `<integer />` tag translates to an generated integer value.
* The `<float />` tag translates to a generated floating point value.
* The `<string />` tag translates to a generated string. 
* The `<null />` tag translates to a `null` value.

An example can be found in `iron-cushion/iron-cushion/data/example_schema.xml`. Its schema is equivalent to the one in `iron-cushion/iron-cushion/data/example_schema.json`.

### Document Updates

To update to a document during the CRUD operations step, Iron Cushion regenerates and replaces a randomly chosen value from the document's top level object. The updated document is sent to CouchDB using a `PUT` request.

### Example

The following schema, found in file `iron-cushion/iron-cushion/data/example_schema.json`:

```json
{
    "array1": [
        [
            0.0, 
            0.0
        ], 
        {}, 
        true,
        null
    ], 
    "boolean1": true, 
    "integer1": 0, 
    "obj1": {
        "array2": [], 
        "obj2": {
            "boolean2": true
        }
    }, 
    "string1": ""
}
```

Can generate the following document:

```json
{
  "array1": [
    [
      0.8474894, 
      0.30425853
    ], 
    {}, 
    false,
    null
  ], 
  "boolean1": true, 
  "integer1": 1929847379, 
  "obj1": {
    "array2": [], 
    "obj2": {
      "boolean2": false
    }
  }, 
  "string1": "928lR8eM7DcBSgR 598A8VxzeFE2 uKTF FqiMEmxdLJmDni"
}
```

## Interpreting Results

The following flags specify using 100 connections, collectively bulk inserting 2,000,000 documents, followed by performing 20,000 create operations, 20,000 read operations, 30,000 update operations, and 30,000 delete operations.

```
--num_connections=100
--num_documents_per_bulk_insert=1000
--num_bulk_insert_operations=20
--num_crud_operations=1000
--create_weight=2
--read_weight=2
--update_weight=3
--delete_weight=3
```

Running the benchmark program with these flags on my 1.83 GHz Intel Core Duo MacBook, CouchDB on my Intel Core 2 2.83GHz quad-core desktop, and across my 100Mbit home LAN, I get the following results:

```
BULK INSERT BENCHMARK RESULTS:
  timeTaken=276.385 secs
  totalBytesSent=374,240,177 bytes
  totalBytesReceived=138,823,936 bytes
  localProcessing={min=1.013 secs, max=2.598 secs, median=1.705 secs, sd=0.307 secs}
  sendData={min=9.694 secs, max=29.616 secs, median=17.892 secs, sd=4.522 secs}
  remoteProcessing={min=178.823 secs, max=239.883 secs, median=224.736 secs, sd=11.307 secs}
  receiveData={min=6.484 secs, max=26.548 secs, median=15.457 secs, sd=4.913 secs}
  remoteProcessingRate=9,013.480 docs/sec
  localInsertRate=7,807.968 docs/sec
```

* `timeTaken` is how long it took for the slowest connection to complete all bulk inserts.
* `totalBytesSent` is the number of bytes of JSON sent to CouchDB, and therefore does not include bytes from HTTP headers.
* `totalBytesReceived` is the number of bytes of JSON received from CouchDB, and therefore does not include bytes from HTTP headers.
* `localProcessing` is how much time each connection spent preparing and encoding sent JSON and decoding received JSON.
* `sendData` is how much time each connection spent sending data to CouchDB.
* `remoteProcessing` is how much time each connection spent waiting for the beginning of responses from CouchDB after sending bulk insert messages.
* `receiveData` is how much time each connection spent receiving data from CouchDB.
* `remoteProcessingRate` is the rate at which CouchDB adds documents in bulk, i.e. the total number of documents bulk inserted divided by the sum of all `remoteProcessing` times.
* `localInsertRate` is the rate at which the benchmark added documents in bulk, i.e. the total number of documents bulk inserted divided by the sum of all `sendData`, `remoteProcessing`, and `receiveData` times.

```
CRUD BENCHMARK RESULTS:
  timeTaken=92.240 secs
  totalBytesSent=10,644,292 bytes
  totalBytesReceived=10,705,118 bytes
  localProcessing={min=0.000 secs, max=0.055 secs, median=0.016 secs, sd=0.011 secs}
  sendData={min=0.000 secs, max=0.023 secs, median=0.001 secs, sd=0.003 secs}
  remoteCreateProcessing={min=21.603 secs, max=24.592 secs, median=22.840 secs, sd=0.504 secs}
  remoteReadProcessing={min=2.101 secs, max=2.812 secs, median=2.516 secs, sd=0.146 secs}
  remoteUpdateProcessing={min=31.514 secs, max=34.182 secs, median=32.920 secs, sd=0.548 secs}
  remoteDeleteProcessing={min=32.071 secs, max=35.141 secs, median=33.548 secs, sd=0.546 secs}
  receiveDataStatistics={min=0.000 secs, max=0.000 secs, median=0.000 secs, sd=0.000 secs}
  remoteCreateProcessingRate=875.059 docs/sec
  remoteReadProcessingRate=7,961.536 docs/sec
  remoteUpdateProcessingRate=894.642 docs/sec
  remoteDeleteProcessingRate=894.642 docs/sec
```

* `timeTaken` is how long it took for the slowest connection to complete all CRUD operations.
* `totalBytesSent` is the number of bytes of JSON sent to CouchDB, and therefore does not include bytes from HTTP headers.
* `totalBytesReceived` is the number of bytes of JSON received from CouchDB, and therefore does not include bytes from HTTP headers.
* `localProcessing` is how much time each connection spent preparing and encoding sent JSON and decoding received JSON.
* `sendData` is how much time each connection spent sending data to CouchDB.
* `remoteCreateProcessing` is how much time each connection spent waiting for the beginning of responses from CouchDB after sending messages for create operations.
* `remoteReadProcessing` is how much time each connection spent waiting for the beginning of responses from CouchDB after sending messages for read operations.
* `remoteUpdateProcessing` is how much time each connection spent waiting for the beginning of responses from CouchDB after sending messages for update operations.
* `remoteDeleteProcessing` is how much time each connection spent waiting for the beginning of responses from CouchDB after sending messages for delete operations.
* `remoteCreateProcessingRate` is the rate at which CouchDB creates documents, i.e. the total number of documents created divided by the sum of all `remoteCreateProcessing` times.
* `remoteReadProcessingRate` is the rate at which CouchDB reads documents, i.e. the total number of documents read divided by the sum of all `remoteReadProcessing` times.
* `remoteUpdateProcessingRate` is the rate at which CouchDB updates documents, i.e. the total number of documents updated divided by the sum of all `remoteUpdateProcessing` times.
* `remoteDeleteProcessingRate` is the rate at which CouchDB deletes documents, i.e. the total number of documents deleted divided by the sum of all `remoteDeleteProcessing` times.

