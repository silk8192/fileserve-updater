# fileserve-updater

The fileserve-updater is a file transmission server and client bundle. It can be used for simple file transfers over the internet or adjusted to provide for an update protocol which can request for files on-demand. It used a priority based system for replying to file requests. 

A folder of files, also known as the cache, must be provided to the server which indexes all files and stores an *update table*. This is then transferred to the client which may verify its own contents and request for new files based on the differences. The client can also provide a priority level for the file, so that it may arrive faster before other sent requests. The server responds with compressed chunks which are stitched together and stored by the client.

## Data formats:
#### Update Table
The Update Table is stored into the index file at the root of the cache directory. It holds basic information about each file that is being handled by the server. It is the binary representation of all File Reference objects.
#### File Reference object:
| data type | value                                                                              |
|-----------|------------------------------------------------------------------------------------|
| int       | file id                                                                            |
| int       | total block length calculated the lengths of each data value, inclusive of itself. |
| long      | file size                                                                          |
| long      | crc checksum of file                                                               |
| byte[]    | the binary representation of the file name                                         |
#### Request:
The Request object is what is sent to the server.

| data type | value                                                                       |
|-----------|-----------------------------------------------------------------------------|
| int       | file id                                                                     |
| byte      | priority, ranging from 0 to 2, 0 signifying lowest and 2 signifying highest |
#### Response:
The Response object is what is sent to the client as a reply to the Request. It is one of many responses which are split into "chunks" and sent to the client.

| data type | value                                                           |
|-----------|-----------------------------------------------------------------|
| int       | file id                                                         |
| short     | chunk id                                                        |
| int       | final chunk id which signifies that the final chunk has arrived |
| byte[]    | the chunk data of gzip-compressed 512 bytes                     |
