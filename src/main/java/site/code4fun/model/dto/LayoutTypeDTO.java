package site.code4fun.model.dto;

import lombok.Data;
import site.code4fun.constant.LayoutType;
import site.code4fun.constant.ProductCardLayout;
import site.code4fun.constant.Status;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class LayoutTypeDTO implements Serializable {

	private Long id;
	private String name;
	private String slug;
	private String language;
	private String icon;
	private Status status;
	boolean home;
	private LayoutType layoutType;
	private ProductCardLayout productCard;
	private List<AttachmentDTO> promotionSliders = new ArrayList<>();
	private List<BannerDTO> banners = new ArrayList<>();
}
