package com.ziyiou.netshare.operation.delete;

import com.ziyiou.netshare.operation.delete.domain.DeleteFile;

public abstract class Deleter {
    public abstract void delete(DeleteFile deleteFile);
}