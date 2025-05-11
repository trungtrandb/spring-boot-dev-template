package site.code4fun.model.mapper;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.constant.DayOfWeek;
import site.code4fun.model.ShiftEntity;
import site.code4fun.model.dto.ShiftDTO;

import java.util.ArrayList;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface ShiftMapper extends BaseMapper<ShiftEntity, ShiftDTO>{
    
    default List<DayOfWeek> map(List<ShiftDTO.DayOfWeekRec> value){
        List<DayOfWeek> result = new ArrayList<>();
        if (value != null) {
            for (ShiftDTO.DayOfWeekRec day : value) {
                result.add(DayOfWeek.valueOf(day.value()));
            }
        }
        return result;
    }

    default List<ShiftDTO.DayOfWeekRec> toDayOfWeekRec(List<DayOfWeek> value){
        List<ShiftDTO.DayOfWeekRec> result = new ArrayList<>();
        if (value != null) {
            for (DayOfWeek day : value) {
                ShiftDTO.DayOfWeekRec dayRec = new ShiftDTO.DayOfWeekRec(StringUtils.capitalize(day.toString().toLowerCase()), day.toString());
                result.add(dayRec);
            }
        }
        return result;
    }
}