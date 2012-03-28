# CouchDB Benchmark

CouchDB Benchmark is a benchmark and load testing tool for CouchDB, written in Java and using [Netty](http://netty.io) for high performance.

The benchmark proceeds in two steps: First, documents are bulk inserted. Second, documents are individually created, read, updated, and deleted. The times for both steps are recorded separately and displayed afterward.

## Command Line Flags

* `database_address`: The address of the database, of the form `http://hostname:port` or `http://ipaddress:port`.
* `database_name`: The name of the database.
* `num_connections`: The number of concurrent connections to establish to the database.
* `document_schema_file`: An XML file describing the schema of documents created during the benchmark. For more details, see "Document Generation" below.

Note that while CouchDB is schemaless, the `document_schema_file` allows the user to specify the level of complexity for generated documents.

### Bulk Insert Flags

* `num_documents_per_bulk_insert`: The number of documents in each bulk insert operation.
* `num_bulk_insert_operations`: The number of bulk insert operations performed by each connection.

For example, if `num_connections` is `50`, `num_documents_per_bulk_insert` is `2000`, and `num_bulk_insert_operations` is `10`, then after the bulk insert phase there will be 50 x 2,000 x 10 = 1,000,000 documents in the database.

### CRUD Flags:

* `num_crud_operations`: The number of CRUD operations performed by each connection.
* `create_weight`: Weight defining the number of create operations relative to other operations.
* `read_weight`: Weight defining the number of read operations relative to other operations.
* `update_weight`: Weight defining the number of update operations relative to other operations.
* `delete_weight`: Weight defining the number of delete operations relative to other operations.

For example, if `create_weight` is `2`, `read_weight` is `3`, `update_weight` is `2`, and `delete_weight` is `1`, then 2/8 of all CRUD operations will be create operations, 3/8 of all CRUD operations will be read operations, 2/8 of all CRUD operations will be update operations, and 1/8 of all CRUD operations will be delete operations. If `num_crud_operations` is `100000`, this equals 25,000 create operations, 37,500 read operations, 25,000 update operations, and 12,500 delete operations per connection.

Note that if `delete_weight` is larger than `create_weight`, documents from the bulk insertion step may be deleted.

## Document Generation



**Dictionary**

```
<dict>
  <key>
  </key>
  <value>
  </value>
</dict>
```

**Array**

```
<array>
  <element>
  </element>
</array>
```

**Integer**

```
<integer>
</integer>
```

**Float**

```
<float>
</float>
```

**Boolean**

```
<boolean>
</boolean>
```

