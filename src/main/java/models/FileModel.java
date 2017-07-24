package models;

import models.components.FileUtils;

import java.nio.file.Path;

/**
 * Created by Lenovo on 23.07.2017.
 */
public class FileModel {
    private String name;
    private Path path;
    private String content;
    private String author;

    public FileModel(String name, Path path) {
        this.name = name;
        this.path = path;

        content = FileUtils.readAllFile(path);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileModel fileModel = (FileModel) o;

        if (name != null ? !name.equals(fileModel.name) : fileModel.name != null) return false;
        return path != null ? path.equals(fileModel.path) : fileModel.path == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
