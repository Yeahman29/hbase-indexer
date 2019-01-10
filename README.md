HBase Indexer
=============

HBase Indexer allows you to easily and quickly index HBase rows into Solr.
Usage documentation can be found on the hbase-indexer Wiki -
http://github.com/NGDATA/hbase-indexer/wiki.

this fork make hbase-indexer usable in HDP 3.1.0.0
- HDFS 3.1.1
- HBASE 2.0.2
- SOLR 7.4

## Subprojects

### HBase MapReduce

The subprojet hbase-indexer-mr subproject has been deleted due to the end of the project solr-map-reduce in solr 7.4.
So, it is no longer possible to use the Batch Mode in hbase-indexer.


### HBase SEP

A standalone library for asynchronously processing HBase mutation events
by hooking into HBase replication, see [the SEP readme](hbase-sep/README.md).

### HBase SEP & replication monitoring

A standalone utility to monitor HBase replication progress,
see [the SEP-tools readme](hbase-sep/hbase-sep-tools/README.md).


## Building

You can build the full hbase-indexer project as follows:

    mvn clean package -DskipTests -Pdist
