package site.code4fun.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.model.CampaignEntity;
import site.code4fun.model.dto.CampaignDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface CampaignMapper extends BaseMapper<CampaignEntity, CampaignDTO>{

}