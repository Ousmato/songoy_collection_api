package songoycollection.article.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ArticleRequestDto {
    private Long categoryId;
    private Long articleTypeId;
    private List<MultipartFile> files;
}
