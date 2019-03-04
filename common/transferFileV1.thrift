namespace java thrift.services

struct FileInfo {
	1: string fileName,
	2: i32 stt,
	3: string ip,
	4: i64 length,
	5: binary chunk,
}

service FileUpload {
	bool uploadFile(1: bool success, 2: i64 recordSize)
}
