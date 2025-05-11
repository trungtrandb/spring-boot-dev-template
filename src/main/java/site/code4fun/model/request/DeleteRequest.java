package site.code4fun.model.request;

import lombok.Data;

import java.util.List;

@Data
public class DeleteRequest<T> {
    private List<T> id;
}
