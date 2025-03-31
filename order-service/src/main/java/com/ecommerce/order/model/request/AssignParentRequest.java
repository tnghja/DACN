package com.ecommerce.order.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignParentRequest {
    private Long parentId; // ID của danh mục cha
    private List<Long> categoryIds; // Danh sách ID của các danh mục con
}
