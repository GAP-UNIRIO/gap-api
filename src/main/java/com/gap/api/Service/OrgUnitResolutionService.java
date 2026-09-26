package com.gap.api.Service;

import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.OrgUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrgUnitResolutionService {

    public OrgUnit resolveUnitForOrder(OrgUnit.OrgUnitType targetType, Order order) {
        Course course = order.getUser().getCourse();
        return switch (targetType){
            case COORDENACAO -> course.getCoordenacao();
            case SECRETARIA ->  course.getSecretaria();
            case DIRECAO ->   course.getDirecao();
            case REITORIA -> course.getReitoria();
            case ESCOLA -> course.getEscola();

            default ->  throw new IllegalArgumentException("Invalid target type");
        };
    }

}
