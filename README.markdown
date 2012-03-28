# CouchDB Benchmark

CouchDB Benchmark is a benchmark and load testing tool for CouchDB, written in Java and using [Netty](http://netty.io) for high performance.

The benchmark proceeds in two steps: First, documents are bulk inserted. Second, documents are individually created, read, updated, and deleted. The times for both steps are recorded separately and displayed afterward.

## Command Line Flags

* `database_address`: The address of the database, of the form `http://hostname:port` or `http://ipaddress:port`.
* `database_name`: The name of the database.
* `num_connections`: The number of concurrent connections to establish to the database.
* `document_schema_file`: An XML file describing the schema of documents created during the benchmark.

Note that while CouchDB is schemaless, the `document_schema_file` allows the user to specify the level of complexity for generated documents. For more details, see "Document Generation" below.

### Bulk Insert Flags

* `num_documents_per_bulk_insert`: The number of documents in each bulk insert operation.
* `num_bulk_insert_operations`: The number of bulk insert operations performed by each connection.

For example, if `num_connections` is `50`, `num_documents_per_bulk_insert` is `2000`, and `num_bulk_insert_operations` is `10`, then after the bulk insert phase there will be 50 x 2,000 x 10 = 1,000,000 documents in the database.

### CRUD Flags

* `num_crud_operations`: The number of CRUD operations performed by each connection.
* `create_weight`: Weight defining the number of create operations relative to other operations.
* `read_weight`: Weight defining the number of read operations relative to other operations.
* `update_weight`: Weight defining the number of update operations relative to other operations.
* `delete_weight`: Weight defining the number of delete operations relative to other operations.

For example, if `create_weight` is `2`, `read_weight` is `3`, `update_weight` is `2`, and `delete_weight` is `1`, then 2/8 of all CRUD operations will be create operations, 3/8 of all CRUD operations will be read operations, 2/8 of all CRUD operations will be update operations, and 1/8 of all CRUD operations will be delete operations. If `num_crud_operations` is `100000`, this equals 25,000 create operations, 37,500 read operations, 25,000 update operations, and 12,500 delete operations per connection.

Note that if `delete_weight` is larger than `create_weight`, documents from the bulk insertion step may be deleted.

## Document Generation

The `document_schema_file` command line flag specifies an XML file that defines a schema for all documents inserted into the database. XML was chosen so that future versions of XXX can add attributes to these tags (e.g. minimum and maximum values for `<integer />`).

There are six principal tags, each of which are described below: `<object>`, `<array>`, `<string />`, `<integer />`, `<float />`, and `<boolean />`. The outer-most tag must be `<object>`.

### Tags

**Object**

The `<object>` tag translates to a JSON object containing name-value pairs. The `<object>` tag encloses 0 or more `<entry>` tags to define name-value pairs. The name of the value, which must be a string, is enclosed by the `<name>` tag. The type of the value is enclosed by the `<value>` tag.

```xml
<object>
  <!-- first name-value pair -->
  <entry>
    <name>
      ...
    </name>
    <value>
      ...
    </value>
  </entry>
  <!-- second name-value pair -->
  <entry>
    <name>
      ...
    </name>
    <value>
      ...
    </value>
  </entry>
  <!-- remaining name-value pairs follow -->
  ...
</object>
```

**Array**

The `<array>` tag translates to an array in JSON. The `<array>` tag encloses 0 or more `<element>` tags to define its elements. The type of each element is enclosed by its `<element>` tag.


```xml
<array>
  <!-- first element -->
  <element>
    ...
  </element>
  <!-- second element -->
  <element>
    ...
  </element>
  <!-- remaining elements follow -->
  ...
</array>
```

**String**

The `<string />` tag translates to a string in JSON. The string is between 1 and 5 words in length, where each word is chosen from a dictionary of 32,768 words, each generated from the alphabet `[A-Za-z0-9?!]`.

**Integer**

The `<integer />` tag translates to an integer value in JSON.

**Float**

The `<float />` tag translates to a floating point value in JSON.

**Boolean**

The `<boolean />` tag translates to a boolean value in JSON.

### Document Updates

To update to a document, XXX chooses a random `<entry>` from the top level `<object>` of the original document, generates a new `<value>` for it, and sends the updated document in a `PUT` request.

### Example

The following schema, found in file `xxx/data/example-schema`:

```xml
<object>
  <!-- "obj1": (object value) -->
  <entry>
    <name>obj1</name>
    <value>
      <object>
        <!-- "array2": [] -->
        <entry>
          <name>array2</name>
          <value>
            <array>
            </array>
          </value>
        </entry>

        <!-- "obj2": (object value) -->
        <entry>
          <name>obj2</name>
          <value>
            <object>
              <!-- "boolean2": (boolean value) -->
              <entry>
                <name>boolean2</name>
                <value>
                  <boolean />
                </value>
              </entry>
            </object>
          </value>
        </entry>
      </object>
    </value>
  </entry>

  <!-- "array1": (array value) -->
  <entry>
    <name>array1</name>
    <value>
      <array>
        <!-- first element: [(float value), (float value)] -->
        <element>
          <array>
            <element>
              <float />
            </element>

            <element>
              <float />
            </element>
          </array>
        </element>
        
        <!-- second element: {} -->
        <element>
          <object>
          </object>
        </element>

        <!-- third element: (boolean value) -->
        <element>
          <boolean />
        </element>
      </array>
    </value>
  </entry>

  <!-- "string1": (string value) -->
  <entry>
    <name>string1</name>
    <value>
      <string />
    </value>
  </entry>

  <!-- "integer1": (integer value) -->
  <entry>
    <name>integer1</name>
    <value>
      <integer />
    </value>
  </entry>

  <!-- "boolean1": (boolean value) -->
  <entry>
    <name>boolean1</name>
    <value>
      <boolean />
    </value>
  </entry>
</object>

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
    true
  ], 
  "boolean1": true, 
  "obj1": {
    "array2": [], 
    "obj2": {
      "boolean2": false
    }
  }, 
  "integer1": 1929847379, 
  "string1": "928lR8eM7DcBSgR 598A8VxzeFE2 uKTF FqiMEmxdLJmDni"
}

```

