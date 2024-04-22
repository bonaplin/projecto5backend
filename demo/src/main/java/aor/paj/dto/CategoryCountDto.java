package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;

public class CategoryCountDto {

    private String category;
    private Long count;

    public CategoryCountDto() {
    }

    public CategoryCountDto(String category, Long count) {
        this.category = category;
        this.count = count;
    }

    public String getCategory() {
        return category;
    }

    public Long getCount() {
        return count;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
