package ws.furrify.storage.dto.tag.category.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ws.furrify.core.entity.request.BaseCreateEntityRequest;
import ws.furrify.storage.domain.tag.category.TagCategory;
import ws.furrify.storage.dto.tag.category.TagCategoryDTO;

@Data
public class CreateTagCategoryRequest implements BaseCreateEntityRequest<TagCategory, TagCategoryDTO> {
    @NotBlank
    String name;

    @Pattern(regexp = "^#(?:[0-9a-fA-F]{3}){1,2}$")
    @NotBlank
    @Size(max = 7)
    String hexColor;
}
