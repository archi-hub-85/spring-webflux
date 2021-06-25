package ru.akh.spring_webflux.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;

public class BookContent {

    @Id
    @Min(1)
    private long id;

    @NotBlank
    private String fileName;

    @NotBlank
    private String mimeType;

    @NotNull
    @Size(min = 1)
    private byte[] content;

    private long size;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "BookContent [id=" + id + ", fileName=" + fileName + ", mimeType=" + mimeType + ", size=" + size + "]";
    }

}
